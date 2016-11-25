package com.ediposouza.teslesgendstracker.ui.widget

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import com.ediposouza.teslesgendstracker.R
import com.ediposouza.teslesgendstracker.data.CardSlot
import com.ediposouza.teslesgendstracker.data.Deck
import com.ediposouza.teslesgendstracker.inflate
import com.ediposouza.teslesgendstracker.interactor.PublicInteractor
import kotlinx.android.synthetic.main.itemlist_decklist_slot.view.*
import kotlinx.android.synthetic.main.widget_decklist.view.*

/**
 * Created by EdipoSouza on 11/2/16.
 */
class DeckList(ctx: Context?, attrs: AttributeSet?, defStyleAttr: Int) :
        LinearLayout(ctx, attrs, defStyleAttr) {

    init {
        inflate(context, R.layout.widget_decklist, this)
        decklist_recycle_view.adapter = DeckListAdapter()
        decklist_recycle_view.setHasFixedSize(true)
        if (!isInEditMode) {
        }
    }

    constructor(ctx: Context?) : this(ctx, null, 0) {
    }

    constructor(ctx: Context?, attrs: AttributeSet) : this(ctx, attrs, 0) {
    }

    fun showDeck(deck: Deck) {
        PublicInteractor().getDeckCards(deck) {
            (decklist_recycle_view.adapter as DeckListAdapter).showDeck(it)
        }
    }

}

class DeckListAdapter() : RecyclerView.Adapter<DeckListViewHolder>() {

    private val items = arrayListOf<CardSlot>()

    override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): DeckListViewHolder {
        return DeckListViewHolder(parent?.inflate(R.layout.itemlist_decklist_slot))
    }

    override fun onBindViewHolder(holder: DeckListViewHolder?, position: Int) {
        holder?.bind(items[position])
    }

    override fun getItemCount(): Int = items.size

    fun showDeck(cards: List<CardSlot>) {
        items.clear()
        items.addAll(cards)
        notifyDataSetChanged()
    }

}

class DeckListViewHolder(view: View?) : RecyclerView.ViewHolder(view) {

    fun bind(slot: CardSlot) {
        itemView.deckslot_card_image.setImageBitmap(slot.card.imageBitmap(itemView.context))
        itemView.deckslot_card_rarity.setImageResource(slot.card.rarity.imageRes)
        itemView.deckslot_card_magika.setImageResource(when (slot.card.cost) {
            0 -> R.drawable.ic_magika_0
            1 -> R.drawable.ic_magika_1
            2 -> R.drawable.ic_magika_2
            3 -> R.drawable.ic_magika_3
            4 -> R.drawable.ic_magika_4
            5 -> R.drawable.ic_magika_5
            6 -> R.drawable.ic_magika_6
            else -> R.drawable.ic_magika_7plus
        })
        itemView.deckslot_card_qtd.text = slot.qtd.toString()
        itemView.deckslot_card_qtd.visibility = if (slot.qtd > 1) View.VISIBLE else View.INVISIBLE
        itemView.deckslot_card_qtd_layout.visibility = if (slot.qtd > 1) View.VISIBLE else View.INVISIBLE
    }

}