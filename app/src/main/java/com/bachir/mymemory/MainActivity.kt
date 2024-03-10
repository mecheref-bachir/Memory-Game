package com.bachir.mymemory

import android.animation.ArgbEvaluator
import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.widget.Button
import android.widget.ImageView
import android.widget.PopupMenu
import android.widget.RadioGroup
import android.widget.TextView
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bachir.mymemory.MemoryBoardAdapter.CardClickListener
import com.bachir.mymemory.models.BoardSize
import com.bachir.mymemory.models.MemoryGame

class MainActivity : ComponentActivity() {

    private lateinit var rvBoard: RecyclerView
    private lateinit var tvNumMoves: TextView
    private lateinit var tvNumPairs: TextView
    private lateinit var memoryGame: MemoryGame
    private lateinit var adapter: MemoryBoardAdapter
    private var boardSize: BoardSize = BoardSize.MEDIUM


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {

            setContentView(R.layout.activity_main)
            resetTheGame()
            val btnRefresh: ImageView = findViewById(R.id.btnRefresh)
            val btnMenu: ImageView = findViewById(R.id.menu)

            btnRefresh.setOnClickListener {
                showAlertDialogForGameReset("Quit your current game?")
            }
            btnMenu.setOnClickListener { view ->
                val popupMenu = PopupMenu(this, view)
                popupMenu.inflate(R.menu.menu_main)

                popupMenu.setOnMenuItemClickListener { item: MenuItem ->
                    when (item.itemId) {
                        R.id.gameLevel -> {
                            showRadioDialogForGameLevel()
                            true
                        }
                        R.id.customGame -> {
                            showCustomGameCreationDialog()
                            true
                        }
                        else -> false
                    }
                }
                popupMenu.show()
            }
        }
    }

    private fun showCustomGameCreationDialog() {
        var desiredBoardSize :Int = 8;
        val dialog = Dialog(this)
        dialog.setContentView(R.layout.custom_radio_layout)
        val radioGroup = dialog.findViewById<RadioGroup>(R.id.radioGroup)
        val btnOK = dialog.findViewById<Button>(R.id.btnOk)
        val btnCancel = dialog.findViewById<Button>(R.id.btnCancel)
        radioGroup.check(R.id.easy)
        
        
        btnCancel.setOnClickListener {
            dialog.dismiss()
        }
        
        btnOK.setOnClickListener {
            val intent = Intent(this,CreateActivity::class.java)
            intent.putExtra("SIZE",desiredBoardSize)
            startActivity(intent)
        }

        radioGroup.setOnCheckedChangeListener { _, checkedId ->
            when (checkedId) {
                R.id.easy -> {
                 desiredBoardSize = BoardSize.EASY.numOfPieces
                }
                R.id.medium -> {
                    desiredBoardSize = BoardSize.MEDIUM.numOfPieces
                }
                R.id.hard -> {
                    desiredBoardSize = BoardSize.HARD.numOfPieces
                }
            }
        }

        dialog.show()

    }


    private fun showRadioDialogForGameLevel() {
        val dialog = Dialog(this)
        val currentboardSize = boardSize
        dialog.setContentView(R.layout.custom_radio_layout)
        val radioGroup = dialog.findViewById<RadioGroup>(R.id.radioGroup)
        val btnOK = dialog.findViewById<Button>(R.id.btnOk)
        val btnCancel = dialog.findViewById<Button>(R.id.btnCancel)

        when (boardSize) {
            BoardSize.EASY -> radioGroup.check(R.id.easy)
            BoardSize.MEDIUM -> radioGroup.check(R.id.medium)
            BoardSize.HARD -> radioGroup.check(R.id.hard)
        }

         btnCancel.setOnClickListener {
             boardSize= currentboardSize
             dialog.dismiss()

         }
         btnOK.setOnClickListener {
             resetTheGame()
             dialog.dismiss()

         }
        radioGroup.setOnCheckedChangeListener { _, checkedId ->
            when (checkedId) {
                R.id.easy -> {
                    boardSize = BoardSize.EASY

                }

                R.id.medium -> {
                    boardSize = BoardSize.MEDIUM

                }

                R.id.hard -> {
                    boardSize = BoardSize.HARD

                }
            }
        }

        dialog.show()
    }

    private fun showAlertDialogForGameReset(title: String) {
        AlertDialog.Builder(this)
            .setTitle(title)
            .setNegativeButton("Cancel", null)
            .setPositiveButton("OK") { _, _ ->
                resetTheGame()
            }.show()
    }


    @SuppressLint("NotifyDataSetChanged")
    private fun updateGameWithFlip(position: Int) {
        if (memoryGame.memoryCards[position].isFaceUp) return

        memoryGame.memoryCards[position].isFaceUp = !memoryGame.memoryCards[position].isFaceUp
        adapter.notifyDataSetChanged()

        val flipedCards = mutableListOf<Int>()

        for (card in memoryGame.memoryCards) {
            if (card.isFaceUp && !card.isMatched) flipedCards.add(card.identifier);
        }


        if (flipedCards.size == 1) return

        if (flipedCards.size == 2 && flipedCards[0] == flipedCards[1]) {
            for (card in memoryGame.memoryCards) {
                if (card.identifier == flipedCards[0] || card.identifier == flipedCards[1]) card.isMatched =
                    true


            }


            return
        }

        if (flipedCards.size == 3) {
            for (card in memoryGame.memoryCards) {
                if (!card.isMatched)

                    card.isFaceUp = false

            }
            memoryGame.memoryCards[position].isFaceUp = true
            adapter.notifyDataSetChanged()
        }


    }

    private fun resetTheGame() {


        rvBoard = findViewById(R.id.rvBoard)
        tvNumMoves = findViewById(R.id.tvNumMoves)
        tvNumPairs = findViewById(R.id.tvNumPairs)
        memoryGame = MemoryGame(boardSize)
        tvNumMoves.setText("Moves: 0")
        tvNumPairs.setText("Pais: 0 / ${boardSize.getNumOfPairs()}")


        adapter =
            MemoryBoardAdapter(this, boardSize, memoryGame.memoryCards, object : CardClickListener {
                override fun onCardClickListener(context: Context, position: Int) {
                    memoryGame.updateGameWithFlip(this@MainActivity, position, adapter)
                    tvNumMoves.setText("Moves: ${memoryGame.getNumberOfMoves()}")
                    tvNumPairs.setText("Pais: ${memoryGame.getNumberOfPairs()} / ${boardSize.getNumOfPairs()}")

                    var color = ArgbEvaluator().evaluate(
                        memoryGame.getNumberOfPairs().toFloat() / boardSize.getNumOfPairs(),
                        ContextCompat.getColor(this@MainActivity, R.color.none),
                        ContextCompat.getColor(this@MainActivity, R.color.full)
                    ) as Int

                    tvNumPairs.setTextColor(color)

                }

            })




        rvBoard.adapter = adapter
        rvBoard.layoutManager = GridLayoutManager(this, boardSize.getWidth())
        rvBoard.setHasFixedSize(true)
    }
}


