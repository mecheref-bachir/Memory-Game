package com.bachir.mymemory

import android.content.Context
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import kotlin.math.min

class ImagePickerAdapter(
    private val context: Context,
    private val chosenImagesUris: MutableList<Uri>,
    private val boardSize: Int,
    private val imageClickedListener:ImageClickedListener
) : RecyclerView.Adapter<ImagePickerAdapter.ViewHolder>() {

    interface ImageClickedListener{
        fun onPlaceHolderClicked()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.card_image, parent, false)
        val cardWidth = parent.width / getwidth(boardSize)-20
        val cardHeight = parent.height / getwidth(boardSize)-20
        val cardSide = min(cardHeight, cardWidth)
        val params = view.findViewById<ImageView>(R.id.customImage).layoutParams as ViewGroup.MarginLayoutParams
        params.width = cardSide
        params.height = cardSide
        params.setMargins(10, 10, 10, 10)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
         return this.boardSize/2
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
       if(position<chosenImagesUris.size){
           holder.bind(chosenImagesUris[position])
       }else
           holder.bind()
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val ivCustomImage = itemView.findViewById<ImageView>(R.id.customImage)
        fun bind(uri: Uri) {
                 ivCustomImage.setImageURI(uri)
                 ivCustomImage.setOnClickListener(null)
        }
        fun bind() {
            ivCustomImage.setOnClickListener{
                 imageClickedListener.onPlaceHolderClicked()
            }
        }

    }

    private fun getwidth(numOfPieces: Int): Int {
        return when (numOfPieces) {
            8-> 2
            18 -> 3
            else -> 3
        }
    }
}
