package com.ediposouza.teslesgendstracker.ui.cards.tabs

import android.view.Menu
import android.view.MenuInflater
import com.ediposouza.teslesgendstracker.R
import com.ediposouza.teslesgendstracker.interactor.PublicInteractor

/**
 * Created by EdipoSouza on 10/30/16.
 */
class CardsTokensFragment : CardsAllFragment() {

    override fun showCards() {
        PublicInteractor.getTokens(setFilter, currentAttr) { tokens ->
            cardsLoaded = tokens.filter { it.isToken() }
            cardsAdapter.showCards(filteredCards())
            scrollToTop()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
        super.onCreateOptionsMenu(menu, inflater)
        menu?.findItem(R.id.menu_only_favorite)?.isVisible = false
    }

}