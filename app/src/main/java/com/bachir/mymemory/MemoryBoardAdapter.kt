package com.bachir.mymemory

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.bachir.mymemory.models.BoardSize
import com.bachir.mymemory.models.MemoryCard
import kotlin.math.min

class MemoryBoardAdapter(
    private val context: Context,
    private val boardSize: BoardSize,
    private val cards: List<MemoryCard>,
    private val cardClickListener :CardClickListener
) : RecyclerView.Adapter<MemoryBoardAdapter.ViewHolder>() {


companion object{
    private const val MARGIN =10
    private const val TAG ="MemoryBoardAdapter"
}
interface CardClickListener{
    fun onCardClickListener(context:Context,position :Int)
}


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view =LayoutInflater.from(context).inflate(R.layout.memory_card,parent,false)
        val cardWidth = parent.width/boardSize.getWidth()-(2* MARGIN)
        val cardHeight =parent.height/ boardSize.getHeight()-(2* MARGIN)
        val sideCard = min(cardHeight,cardWidth)

        val params =view.findViewById<CardView>(R.id.cardView).layoutParams as ViewGroup.MarginLayoutParams
        params.height= sideCard
        params.width = sideCard
        params.setMargins(MARGIN, MARGIN, MARGIN, MARGIN)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
      return boardSize.numOfPieces
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
     holder.bind(position)
    }


    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

       private val imageButton =  itemView.findViewById<ImageButton>(R.id.imageButton)

        fun bind(position: Int) {
               imageButton.setImageResource(if(cards[position].isFaceUp) cards[position].identifier else R.drawable.ic_launcher_background)
               imageButton.setOnClickListener{
               Log.i(TAG, "position $position clicked ")
               cardClickListener.onCardClickListener(context,position)
           }
        }


    }



}
