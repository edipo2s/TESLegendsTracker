package com.ediposouza.teslesgendstracker.ui.decks.new

import android.support.v7.widget.GridLayoutManager
import android.view.View
import com.ediposouza.teslesgendstracker.R
import com.ediposouza.teslesgendstracker.data.Card
import com.ediposouza.teslesgendstracker.ui.cards.tabs.CardsAllFragment
import com.ediposouza.teslesgendstracker.ui.decks.CmdAddCard
import com.ediposouza.teslesgendstracker.ui.util.GridSpacingItemDecoration
import kotlinx.android.synthetic.main.fragment_cards_list.*
import org.greenrobot.eventbus.EventBus

class NewDeckCardsListFragment : CardsAllFragment() {

    override val ADS_EACH_ITEMS = 20 //after 10 lines
    override val CARDS_PER_ROW = 2

    val onItemClick = { view: View, card: Card ->
        EventBus.getDefault().post(CmdAddCard(card))
    }

    override val itemDecoration by lazy {
        GridSpacingItemDecoration(CARDS_PER_ROW,
                resources.getDimensionPixelSize(R.dimen.deck_new_card_margin), false)
    }

    override val cardsAdapter by lazy {
        val gridLayoutManager = cards_recycler_view.layoutManager as GridLayoutManager
        CardsAllAdapter(ADS_EACH_ITEMS, gridLayoutManager, R.layout.itemlist_new_deck_card_ads,
                R.dimen.card_height, onItemClick) {
            view: View, card: Card ->
            showCardExpanded(card, view)
            true
        }
    }

    override fun configRecycleView() {
        super.configRecycleView()
        isFragmentSelected = true
        cards_recycler_view.setPadding(0, 0, 0, 0)
    }

}