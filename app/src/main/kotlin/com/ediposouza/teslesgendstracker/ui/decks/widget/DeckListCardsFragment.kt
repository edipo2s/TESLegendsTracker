package com.ediposouza.teslesgendstracker.ui.decks

import android.support.v7.widget.RecyclerView
import android.view.View
import android.view.ViewGroup
import com.ediposouza.teslesgendstracker.R
import com.ediposouza.teslesgendstracker.data.Card
import com.ediposouza.teslesgendstracker.data.CardMissing
import com.ediposouza.teslesgendstracker.data.CardSlot
import com.ediposouza.teslesgendstracker.ui.cards.tabs.CardsCollectionFragment
import com.ediposouza.teslesgendstracker.ui.util.GridSpacingItemDecoration
import com.ediposouza.teslesgendstracker.util.inflate
import com.ediposouza.teslesgendstracker.util.loadFromCard
import kotlinx.android.synthetic.main.fragment_cards_list.*
import kotlinx.android.synthetic.main.itemlist_card_deck_list.view.*

class DeckListCardsFragment : CardsCollectionFragment() {

    companion object {

        val EXTRA_DECK_CARDS = "deckExtra"
        val EXTRA_MISSING_CARDS = "missingExtra"

    }

    override val ADS_EACH_ITEMS = 20 //after 10 lines
    override val CARDS_PER_ROW = 3

    override var enableMenu = false
    override val itemDecoration by lazy {
        GridSpacingItemDecoration(CARDS_PER_ROW,
                resources.getDimensionPixelSize(R.dimen.deck_new_card_margin), false)
    }

    override val cardsCollectionAdapter by lazy {
        val missingCards = arguments?.getParcelableArrayList<CardMissing>(EXTRA_MISSING_CARDS) ?: listOf<CardMissing>()
        CardsDeckListAdapter(missingCards, { _ -> }, { view: View, card: Card ->
            showCardExpanded(card, view)
            true
        })
    }

    override fun configRecycleView() {
        super.configRecycleView()
        cards_recycler_view.setPadding(0, 0, 0, 0)
        cards_refresh_layout.isEnabled = false
        isFragmentSelected = true
        arguments?.getParcelableArrayList<CardSlot>(EXTRA_DECK_CARDS)?.apply {
            cardsCollectionAdapter.showCards(this)
        }
    }

    override fun configLoggedViews() {
    }

    fun getListView(): View = cards_recycler_view

    class CardsDeckListAdapter(val missingCards: List<CardMissing>,
                               itemClick: (CardSlot) -> Unit,
                               itemLongClick: (View, Card) -> Boolean) : CardsCollectionAdapter(itemClick, itemLongClick) {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
            return CardsDeckListViewHolder(parent.inflate(R.layout.itemlist_card_deck_list), itemLongClick)
        }

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            val cardSlot = items[position]
            val cardMissing = missingCards.find { it.shortName == cardSlot.card.shortName }
            (holder as CardsDeckListViewHolder).bind(cardSlot, cardMissing?.qtd ?: 0)
        }

    }

    class CardsDeckListViewHolder(view: View?, val itemLongClick: (View, Card) -> Boolean) :
            RecyclerView.ViewHolder(view) {

        fun bind(cardSlot: CardSlot, missingQtd: Int) {
            with(itemView) {
                card_decklist_image.apply {
                    loadFromCard(cardSlot.card)
                    layoutParams = layoutParams.apply { height = context.resources.getDimensionPixelSize(R.dimen.card_height_micro) }
                }
                setOnClickListener { itemLongClick(this, cardSlot.card) }
                setOnLongClickListener { itemLongClick(this, cardSlot.card) }
                card_decklist_qtd.setText("${cardSlot.qtd}")
                card_decklist_qtd_missing.apply {
                    text = "-$missingQtd"
                    visibility = View.VISIBLE.takeIf { missingQtd > 0 } ?: View.INVISIBLE
                }
            }
        }

    }

}