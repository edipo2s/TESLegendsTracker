package com.ediposouza.teslesgendstracker.ui.cards.tabs

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

}