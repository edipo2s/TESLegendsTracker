package com.ediposouza.teslesgendstracker.ui.decks.new

import android.os.Bundle
import android.support.v7.widget.GridLayoutManager
import android.view.View
import com.ediposouza.teslesgendstracker.R
import com.ediposouza.teslesgendstracker.data.Card
import com.ediposouza.teslesgendstracker.ui.cards.tabs.CardsAllAdapter
import com.ediposouza.teslesgendstracker.ui.cards.tabs.CardsAllFragment
import com.ediposouza.teslesgendstracker.ui.decks.CmdAddCard
import com.ediposouza.teslesgendstracker.ui.utils.GridSpacingItemDecoration
import kotlinx.android.synthetic.main.fragment_cards_list.*
import org.greenrobot.eventbus.EventBus

class NewDeckCardsListFragment() : CardsAllFragment() {

    val onItemClick = { view: View, card: Card ->
        EventBus.getDefault().post(CmdAddCard(card))
    }

    override val cardsAdapter by lazy {
        val gridLayoutManager = cards_recycler_view.layoutManager as GridLayoutManager
        CardsAllAdapter(ADS_EACH_ITEMS, gridLayoutManager, R.dimen.deck_new_card_height, onItemClick) {
            view: View, card: Card ->
            showCardExpanded(card, view)
            true
        }
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        cards_recycler_view.addItemDecoration(GridSpacingItemDecoration(CARDS_PER_ROW,
                resources.getDimensionPixelSize(R.dimen.deck_new_card_margin), true))
    }

}