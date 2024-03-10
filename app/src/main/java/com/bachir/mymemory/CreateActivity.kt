package com.bachir.mymemory

import android.app.Activity
import android.app.AlertDialog
import android.content.ClipData
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.text.Editable
import android.text.InputFilter
import android.text.TextWatcher
import android.util.Log
import android.view.MenuItem
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bachir.mymemory.utils.isPermissionGranted
import com.bachir.mymemory.utils.requestPermission
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import com.google.firebase.storage.storage
import java.io.ByteArrayOutputStream

class CreateActivity : AppCompatActivity() {
    private var boardSize: Int = 0
    private lateinit var rvImagePicker: RecyclerView
    private lateinit var btnSave: Button
    private lateinit var tvGameName: TextView
    private val chosenImagesUris = mutableListOf<Uri>()
    private lateinit var adapter: ImagePickerAdapter
    private val storage = Firebase.storage
    private val db = Firebase.firestore


    companion object {
        private const val TAG = "CreateActivity"
        private const val PICK_PHOTO_CODE = 111
        private const val READ_EXTERNAL_PHOTOS_CODE = 222
        private const val READ_PHOTOS_PERMISSION = android.Manifest.permission.READ_EXTERNAL_STORAGE
        private const val MIN_GAME_NAME_LENGTH = 4
        private const val MAX_GAME_NAME_LENGTH = 14

    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        boardSize = intent.getIntExtra("SIZE", -1)
        supportActionBar?.title = "choose pics (0 / ${boardSize / 2})"

        rvImagePicker = findViewById(R.id.rvImagePicker)
        btnSave = findViewById(R.id.btnSave)
        tvGameName = findViewById(R.id.tvGameName)
        rvImagePicker.layoutManager = GridLayoutManager(this, getwidth(boardSize))
        rvImagePicker.setHasFixedSize(true)
        tvGameName.filters = arrayOf(InputFilter.LengthFilter(MAX_GAME_NAME_LENGTH))
        tvGameName.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                btnSave.isEnabled = shouldEnableSaveButton()
            }

        })

        btnSave.setOnClickListener {
            saveDataToFireBase()
        }

        adapter = ImagePickerAdapter(
            this,
            chosenImagesUris,
            boardSize,
            object : ImagePickerAdapter.ImageClickedListener {

                override fun onPlaceHolderClicked() {
                    if (isPermissionGranted(this@CreateActivity, READ_PHOTOS_PERMISSION)) {
                        lunchIntentForPhotos()
                    } else {
                        requestPermission(
                            this@CreateActivity, READ_PHOTOS_PERMISSION,
                            READ_EXTERNAL_PHOTOS_CODE
                        )
                    }

                }

            })
        rvImagePicker.adapter = adapter

    }

    private fun saveDataToFireBase() {
        btnSave.isEnabled= false
        val customGameName = tvGameName.text.toString()
        db.collection("games").document(customGameName).get().addOnSuccessListener {
                document ->
            if (document != null && document.data != null) {
                AlertDialog.Builder(this)
                    .setTitle("Name taken")
                    .setMessage("A game Already exists with this name, Choose another one")
                    .setPositiveButton("OK",null)
                    .show()
                btnSave.isEnabled = true
            }else {
                var didEncounterError = false
                val uploadedImagesUrl = mutableListOf<String>()

                for ((index, photoUri) in chosenImagesUris.withIndex()) {
                    val imageByteArray = getImageByteArray(photoUri)
                    val pathFile = "images/$customGameName/${System.currentTimeMillis()}-${index}.jpg"
                    val photoReference = storage.reference.child(pathFile)
                    photoReference.putBytes(imageByteArray).continueWithTask {
                        photoReference.downloadUrl
                    }.addOnCompleteListener { downloadUrlTask ->
                        if (!downloadUrlTask.isSuccessful) {
                            Toast.makeText(this, "failed to upload image", Toast.LENGTH_SHORT).show()
                            didEncounterError = true
                            return@addOnCompleteListener

                        }
                        if (didEncounterError) {
                            return@addOnCompleteListener
                        }
                        val downloadUrl = downloadUrlTask.result.toString()
                        uploadedImagesUrl.add(downloadUrl)
                        if (uploadedImagesUrl.size == chosenImagesUris.size) {
                            handleAllImagesUploaded(customGameName, uploadedImagesUrl)
                        }
                    }
                }


            }
        }.addOnFailureListener{exception ->
            Log.e(TAG, "Encountered error while saving memory game", exception)
            btnSave.isEnabled = true
            Toast.makeText(this, "Error while saving", Toast.LENGTH_SHORT).show()
        }

    }

    private fun handleAllImagesUploaded(customGameName: String, imageUrls: MutableList<String>) {
        db.collection("games").document(customGameName)
            .set(mapOf("images" to imageUrls))
            .addOnCompleteListener { gameCreationTask ->
                if (!gameCreationTask.isSuccessful) {
                    Toast.makeText(this, "failed game creation", Toast.LENGTH_SHORT).show()
                    return@addOnCompleteListener
                }
                AlertDialog.Builder(this)
                    .setTitle("Upload complete! let's play your game")
                    .setPositiveButton("Ok") { _, _ ->
                        val resultData = Intent()
                        resultData.putExtra("EXTRA_GAME_NAME", customGameName)
                        setResult(Activity.RESULT_OK, resultData)
                        finish()

                    }.show()


            }
    }

    private fun getImageByteArray(photoUri: Uri): ByteArray {
        val originalBitmap = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            val source = ImageDecoder.createSource(contentResolver, photoUri)
            ImageDecoder.decodeBitmap(source)

        } else {
            MediaStore.Images.Media.getBitmap(contentResolver, photoUri)
        }
        val scaleBitmap = BitmapScaler.scaleToFitHeiht(originalBitmap, 250)
        val byteOutputStream = ByteArrayOutputStream()
        scaleBitmap.compress(Bitmap.CompressFormat.JPEG, 60, byteOutputStream)
        return byteOutputStream.toByteArray()
    }

    private fun lunchIntentForPhotos() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
        startActivityForResult(Intent.createChooser(intent, "choose pics"), PICK_PHOTO_CODE)
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode != PICK_PHOTO_CODE || Activity.RESULT_OK != resultCode || data == null) {
            Log.w(TAG, "did not get data , User likely denied access")
            return
        }
        val selectedUri: Uri? = data.data
        val clipData: ClipData? = data.clipData

        if (clipData != null) {
            for (i in 0 until clipData.itemCount) {
                val clipItem = clipData.getItemAt(i)
                if (chosenImagesUris.size < boardSize / 2) {
                    chosenImagesUris.add(clipItem.uri)
                }
            }
        } else if (selectedUri != null) {
            chosenImagesUris.add(selectedUri)
        }
        adapter.notifyDataSetChanged()
        supportActionBar?.title = "choose pics (${chosenImagesUris.size} / ${boardSize / 2})"
        btnSave.isEnabled = shouldEnableSaveButton()

    }

    private fun shouldEnableSaveButton(): Boolean {
        if (chosenImagesUris.size < boardSize / 2) {
            return false
        }

        return !(tvGameName.text.isBlank() || tvGameName.text.length < MIN_GAME_NAME_LENGTH)
    }

    private fun getwidth(numOfPieces: Int): Int {
        return when (numOfPieces) {
            8 -> 2
            18 -> 3
            else -> 3
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if (requestCode == READ_EXTERNAL_PHOTOS_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                lunchIntentForPhotos()
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            finish()
            return true
        } else {
            Toast.makeText(
                this,
                "To create a custom game , provide access to you photos !!",
                Toast.LENGTH_LONG
            ).show()
        }
        return super.onOptionsItemSelected(item)
    }
}