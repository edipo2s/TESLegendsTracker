package com.ediposouza.teslesgendstracker.ui.decks

import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.View
import android.view.ViewGroup
import com.ediposouza.teslesgendstracker.R
import com.ediposouza.teslesgendstracker.data.Card
import com.ediposouza.teslesgendstracker.data.CardSlot
import com.ediposouza.teslesgendstracker.ui.cards.tabs.CardsCollectionFragment
import com.ediposouza.teslesgendstracker.ui.util.GridSpacingItemDecoration
import com.ediposouza.teslesgendstracker.util.inflate
import kotlinx.android.synthetic.main.fragment_cards_list.*
import kotlinx.android.synthetic.main.itemlist_card_deck_list.view.*

class DeckListCardsFragment : CardsCollectionFragment() {

    companion object {

        val EXTRA_DECK_CARDS = "deckExtra"

    }

    override val ADS_EACH_ITEMS = 20 //after 10 lines
    override val CARDS_PER_ROW = 3

    override var enableMenu = false
    override val itemDecoration by lazy {
        GridSpacingItemDecoration(CARDS_PER_ROW,
                resources.getDimensionPixelSize(R.dimen.deck_new_card_margin), false)
    }

    override val cardsCollectionAdapter by lazy {
        CardsDeckListAdapter(ADS_EACH_ITEMS, gridLayoutManager, { _ -> }, {
            view: View, card: Card ->
            showCardExpanded(card, view)
            true
        })
    }

    override fun configRecycleView() {
        super.configRecycleView()
        cards_recycler_view.setPadding(0, 0, 0, 0)
        cards_refresh_layout.isEnabled = false
        isFragmentSelected = true
        arguments.getParcelableArrayList<CardSlot>(EXTRA_DECK_CARDS)?.apply {
            cardsCollectionAdapter.showCards(this)
        }
    }

    fun getListView(): View = cards_recycler_view

    class CardsDeckListAdapter(adsEachItems: Int, layoutManager: GridLayoutManager, itemClick: (CardSlot) -> Unit,
                               itemLongClick: (View, Card) -> Boolean) : CardsCollectionAdapter(adsEachItems,
            layoutManager, R.layout.itemlist_new_deck_card_ads, itemClick, itemLongClick) {

        override fun onCreateDefaultViewHolder(parent: ViewGroup): RecyclerView.ViewHolder {
            return CardsDeckListViewHolder(parent.inflate(R.layout.itemlist_card_deck_list), itemLongClick)
        }

        override fun onBindDefaultViewHolder(holder: RecyclerView.ViewHolder?, position: Int) {
            (holder as CardsDeckListViewHolder).bind(items[position])
        }

    }

    class CardsDeckListViewHolder(view: View, val itemLongClick: (View, Card) -> Boolean) :
            RecyclerView.ViewHolder(view) {

        fun bind(cardSlot: CardSlot) {
            with(itemView.card_decklist_image) {
                setImageBitmap(cardSlot.card.imageBitmap(itemView.context))
                layoutParams = layoutParams.apply { height = itemView.context.resources.getDimensionPixelSize(R.dimen.card_height_min) }
                itemView.setOnClickListener { itemLongClick(this, cardSlot.card) }
                itemView.setOnLongClickListener { itemLongClick(this, cardSlot.card) }
            }
            itemView.card_decklist_qtd.setText("${cardSlot.qtd}")
        }

    }

}