package com.bachir.mymemory.models

import android.annotation.SuppressLint
import android.content.Context
import android.widget.Toast

import com.bachir.mymemory.MemoryBoardAdapter
import com.bachir.mymemory.utils.DEFAULT_ICONS

class MemoryGame(private val boardSize : BoardSize) {

    val memoryCards : List<MemoryCard>
    private var numOfPairsFound =0
    private var numOfcardFlip = 0

    init{
        val chosen = DEFAULT_ICONS.shuffled().take(boardSize.getNumOfPairs())
        val chosenImages = (chosen+chosen).shuffled()
        memoryCards =  chosenImages.map { MemoryCard(it) }
    }

    


    @SuppressLint("NotifyDataSetChanged")
    fun updateGameWithFlip(
        context : Context,
        position: Int,
        adapter: MemoryBoardAdapter
    ) {
        if(numOfPairsFound == boardSize.getNumOfPairs()){
            Toast.makeText(context, "YOU ALREADY WIN  !!", Toast.LENGTH_SHORT).show()
            return

        }


        if(memoryCards[position].isFaceUp){
            Toast.makeText(context, "wrong move !!", Toast.LENGTH_SHORT).show()
            return
        }


       memoryCards[position].isFaceUp = !memoryCards[position].isFaceUp
        adapter.notifyDataSetChanged()

        val flipedCards = mutableListOf<Int>()

        for(card in memoryCards) {
            if (card.isFaceUp && !card.isMatched)
                flipedCards.add(card.identifier);
        }


        if(flipedCards.size == 1){
            numOfcardFlip++
            return
        }


        if(flipedCards.size == 2 && flipedCards[0] == flipedCards[1]){
            numOfPairsFound ++
            numOfcardFlip++

            if(numOfPairsFound == boardSize.getNumOfPairs()){
                Toast.makeText(context, "You Won congratulations  !!", Toast.LENGTH_SHORT).show()
                return

            }
            for(card in memoryCards) {
                if (card.identifier == flipedCards[0] || card.identifier == flipedCards[1])
                    card.isMatched = true


            }
            // loack them

            return
        }else if(flipedCards.size == 2 && flipedCards[0] != flipedCards[1]){

            numOfcardFlip++
        }

        if(flipedCards.size == 3){
            numOfcardFlip++
            for(card in memoryCards) {
                if ( !card.isMatched  )

                    card.isFaceUp = false

            }
            memoryCards[position].isFaceUp = true
            adapter.notifyDataSetChanged()
        }




    }


    fun getNumberOfMoves():Int
    {
        return numOfcardFlip/2
    }


    fun getNumberOfPairs ():Int{
        return numOfPairsFound
    }


}