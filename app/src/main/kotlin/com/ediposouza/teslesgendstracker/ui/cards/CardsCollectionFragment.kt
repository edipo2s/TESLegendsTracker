package com.ediposouza.teslesgendstracker.ui.cards

import android.support.v4.content.ContextCompat
import android.support.v7.util.DiffUtil
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.ediposouza.teslesgendstracker.R
import com.ediposouza.teslesgendstracker.data.Card
import com.ediposouza.teslesgendstracker.data.Slot
import kotlinx.android.synthetic.main.fragment_cards_all.*
import kotlinx.android.synthetic.main.itemlist_card_collection.view.*
import java.util.*

/**
 * Created by EdipoSouza on 10/30/16.
 */
class CardsCollectionFragment : CardsAllFragment() {

    val cardsCollectionAdapter = CardsCollectionAdapter({ changeUserCardQtd(it) }) { view: View, card: Card ->
        showCardExpanded(card, view)
        true
    }

    override fun configRecycleView() {
        super.configRecycleView()
        cards_recycler_view.adapter = cardsCollectionAdapter
    }

    override fun showCards() {
        val cards = filteredCards()
        privateInteractor.getUserCollection(currentAttr) {
            val userCards = it
            val slots = cards.map { Slot(it, userCards[it.shortName] ?: 0L) }
            cardsCollectionAdapter.showCards(slots as ArrayList)
            cards_recycler_view.scrollToPosition(0)
        }
    }

    private fun changeUserCardQtd(slot: Slot) {
        val newQtd = slot.qtd.inc()
        val finalQtd = if (newQtd <= 3) newQtd else 0
        privateInteractor.setUserCardQtd(slot.card, finalQtd) {
            cardsCollectionAdapter.updateSlot(slot, finalQtd)
        }
    }

}

class CardsCollectionAdapter(val itemClick: (Slot) -> Unit,
                             val itemLongClick: (View, Card) -> Boolean) : RecyclerView.Adapter<CardsCollectionViewHolder>() {

    var items: ArrayList<Slot> = ArrayList()

    override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): CardsCollectionViewHolder {
        return CardsCollectionViewHolder(LayoutInflater.from(parent?.context)
                .inflate(R.layout.itemlist_card_collection, parent, false), itemClick, itemLongClick)
    }

    override fun onBindViewHolder(holder: CardsCollectionViewHolder?, position: Int) {
        holder?.bind(items[position])
    }

    override fun getItemCount(): Int = items.size

    fun showCards(slots: ArrayList<Slot>) {
        val oldItems = items
        items = slots
        DiffUtil.calculateDiff(object : DiffUtil.Callback() {
            override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
                return oldItems[oldItemPosition] == items[newItemPosition]
            }

            override fun getOldListSize(): Int = oldItems.size

            override fun getNewListSize(): Int = items.size

            override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
                return oldItems[oldItemPosition].card.shortName == items[newItemPosition].card.shortName
            }

        }, false).dispatchUpdatesTo(this)
    }

    fun updateSlot(slot: Slot, newQtd: Long) {
        val slotIndex = items.indexOf(slot)
        if (slotIndex > -1) {
            items[slotIndex] = Slot(slot.card, newQtd)
            notifyItemChanged(slotIndex)
        }
    }

}

class CardsCollectionViewHolder(val view: View, val itemClick: (Slot) -> Unit,
                                val itemLongClick: (View, Card) -> Boolean) : RecyclerView.ViewHolder(view) {

    fun bind(slot: Slot) {
        itemView.setOnClickListener { itemClick(slot) }
        itemView.setOnLongClickListener {
            itemLongClick(itemView.card_collection_image, slot.card)
        }
        itemView.card_collection_image.setImageBitmap(slot.card.imageBitmap(itemView.context))
        if (slot.qtd == 0L) {
            val color = ContextCompat.getColor(itemView.context, R.color.card_zero_qtd)
            itemView.card_collection_image.setColorFilter(color)
        } else {
            itemView.card_collection_image.clearColorFilter()
        }
        itemView.card_collection_qtd.setImageResource(when (slot.qtd) {
            0L -> R.drawable.ic_qtd_zero
            2L -> R.drawable.ic_qtd_two
            else -> R.drawable.ic_qtd_three
        })
        itemView.card_collection_qtd.visibility = if (slot.qtd == 1L) View.GONE else View.VISIBLE
    }

}