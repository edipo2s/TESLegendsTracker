package com.ediposouza.teslesgendstracker.ui.cards

import android.graphics.BitmapFactory
import android.os.Bundle
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.ediposouza.teslesgendstracker.R
import com.ediposouza.teslesgendstracker.data.Attribute
import com.ediposouza.teslesgendstracker.data.Card
import com.ediposouza.teslesgendstracker.interactor.CardInteractor
import com.ediposouza.teslesgendstracker.ui.utils.GridSpacingItemDecoration
import kotlinx.android.synthetic.main.fragment_cards_all.*
import kotlinx.android.synthetic.main.itemlist_card.view.*
import org.jetbrains.anko.toast
import java.util.*

/**
 * Created by EdipoSouza on 10/30/16.
 */
class CardsAllFragment : BaseFragment() {

    val cards: ArrayList<Card> = ArrayList()

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater?.inflate(R.layout.fragment_cards_all, container, false)
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        cards_recycler_view.adapter = CardsAllAdapter(cards, {
            context.toast(it.name)
        })
        cards_recycler_view.setHasFixedSize(true)
        cards_recycler_view.addItemDecoration(GridSpacingItemDecoration(3,
                resources.getDimensionPixelSize(R.dimen.card_margin), true, false))
        cards_attr_filter.filterClick = { loadCardsByAttr(it) }
        loadCardsByAttr(Attribute.STRENGTH)
    }

    private fun loadCardsByAttr(attr: Attribute) {
        CardInteractor().getCards(attr, {
            cards.clear()
            cards.addAll(it)
            cards_recycler_view.adapter.notifyDataSetChanged()
            cards_recycler_view.scrollToPosition(0)
        })
    }

}

class CardsAllAdapter(val cards: List<Card>, val itemClick: (Card) -> Unit) : RecyclerView.Adapter<CardsAllViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): CardsAllViewHolder {
        return CardsAllViewHolder(LayoutInflater.from(parent?.context)
                .inflate(R.layout.itemlist_card, parent, false), itemClick)
    }

    override fun onBindViewHolder(holder: CardsAllViewHolder?, position: Int) {
        holder?.bind(cards.get(position))
    }

    override fun getItemCount(): Int = cards.size

}

class CardsAllViewHolder(val view: View, val itemClick: (Card) -> Unit) : RecyclerView.ViewHolder(view) {

    fun bind(card: Card) {
        itemView.setOnClickListener { itemClick(card) }
        val cardImage = BitmapFactory.decodeStream(itemView.resources.assets.open(card.imagePath()))
        itemView.card_imageview.setImageBitmap(cardImage)
    }

}