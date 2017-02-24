package com.ediposouza.teslesgendstracker.ui.decks

import android.support.v4.content.ContextCompat
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.View
import android.view.ViewGroup
import com.ediposouza.teslesgendstracker.R
import com.ediposouza.teslesgendstracker.data.Card
import com.ediposouza.teslesgendstracker.data.CardAttribute
import com.ediposouza.teslesgendstracker.data.CardSlot
import com.ediposouza.teslesgendstracker.data.Deck
import com.ediposouza.teslesgendstracker.interactor.PublicInteractor
import com.ediposouza.teslesgendstracker.ui.cards.tabs.CardsAllFragment
import com.ediposouza.teslesgendstracker.ui.util.GridSpacingItemDecoration
import com.ediposouza.teslesgendstracker.util.inflate
import kotlinx.android.synthetic.main.fragment_cards_list.*
import kotlinx.android.synthetic.main.itemlist_card.view.*
import org.greenrobot.eventbus.Subscribe

class NewDeckCardsListFragment : CardsAllFragment() {

    companion object {

        val EXTRA_DECK = "deckExtra"

    }

    override val ADS_EACH_ITEMS = 20 //after 10 lines
    override val CARDS_PER_ROW = 2

    private val onItemClick = { _: View, card: Card ->
        eventBus.post(CmdAddCard(card))
    }

    override val itemDecoration by lazy {
        GridSpacingItemDecoration(CARDS_PER_ROW,
                resources.getDimensionPixelSize(R.dimen.deck_new_card_margin), false)
    }

    override val cardsAdapter by lazy {
        cardsNewDeckAdapter(ADS_EACH_ITEMS, gridLayoutManager, onItemClick, {
            view: View, card: Card ->
            showCardExpanded(card, view)
            true
        })
    }

    override fun configRecycleView() {
        super.configRecycleView()
        cards_recycler_view.setPadding(0, 0, 0, 0)
        isFragmentSelected = true
        arguments.getParcelable<Deck>(EXTRA_DECK)?.apply {
            PublicInteractor.getCards(null, cls.attr1, cls.attr2, CardAttribute.DUAL, CardAttribute.NEUTRAL) {
                loadDeckCards(it)
            }
        }
    }

    private fun Deck.loadDeckCards(it: List<Card>) {
        cards.forEach { cardShortName, qtd ->
            val card = it.find { it.shortName == cardShortName }
            if (card != null) {
                cardsAdapter.updateCardSlot(CardSlot(card, qtd))
            }
        }
    }

    @Subscribe
    @Suppress("unused")
    fun onCmdUpdateCardSlot(cmdUpdateCardSlot: CmdUpdateCardSlot) {
        cardsAdapter.updateCardSlot(cmdUpdateCardSlot.cardSlot)
    }

    class cardsNewDeckAdapter(adsEachItems: Int, layoutManager: GridLayoutManager, itemClick: (View, Card) -> Unit,
                              itemLongClick: (View, Card) -> Boolean) : CardsAllAdapter(adsEachItems,
            layoutManager, R.layout.itemlist_new_deck_card_ads, itemClick, itemLongClick) {

        var deckCardSlots = arrayListOf<CardSlot>()

        override fun onCreateDefaultViewHolder(parent: ViewGroup): RecyclerView.ViewHolder {
            return CardsNewDeckViewHolder(parent.inflate(R.layout.itemlist_card), itemClick, itemLongClick)
        }

        override fun onBindDefaultViewHolder(holder: RecyclerView.ViewHolder?, position: Int) {
            val card = items[position]
            val deckQtd = deckCardSlots.find { it.card.shortName == card.shortName }?.qtd ?: 0
            (holder as CardsNewDeckViewHolder).bind(CardSlot(card, deckQtd))
        }

        fun updateCardSlot(cardSlot: CardSlot) {
            deckCardSlots.removeIf { it.card.shortName == cardSlot.card.shortName }
            deckCardSlots.add(cardSlot)
            notifyDataSetChanged()
        }
    }

    class CardsNewDeckViewHolder(view: View, itemClick: (View, Card) -> Unit, itemLongClick: (View, Card) -> Boolean) :
            CardsAllViewHolder(view, itemClick, itemLongClick) {

        fun bind(cardSlot: CardSlot) {
            itemView.setOnLongClickListener { itemLongClick(itemView.card_all_image, cardSlot.card) }
            itemView.card_all_image.setImageBitmap(cardSlot.card.imageBitmap(itemView.context))
            val isCardUnique = cardSlot.card.unique
            if (isCardUnique && cardSlot.qtd == 1 || !isCardUnique && cardSlot.qtd == 3) {
                val color = ContextCompat.getColor(itemView.context, R.color.card_zero_qtd)
                itemView.card_all_image.setColorFilter(color)
                itemView.setOnClickListener { }
            } else {
                itemView.setOnClickListener { itemClick(itemView.card_all_image, cardSlot.card) }
                itemView.card_all_image.clearColorFilter()
            }
        }

    }

}