package com.bachir.mymemory.models

enum class BoardSize (val numOfPieces : Int) {

    EASY(8),
    MEDIUM(18),
    HARD(28);
    fun getWidth():Int {
         return when (this) {
             EASY -> 2
             MEDIUM -> 3
             HARD -> 4
         }
    }
    fun getHeight():Int{
       return numOfPieces/getWidth()
    }

    fun getNumOfPairs():Int{
        return numOfPieces/2
    }
}