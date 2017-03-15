package com.ediposouza.teslesgendstracker.ui.spoiler

import com.ediposouza.teslesgendstracker.data.CardAttribute
import com.ediposouza.teslesgendstracker.interactor.PublicInteractor
import com.ediposouza.teslesgendstracker.ui.base.CmdUpdateTitle
import com.ediposouza.teslesgendstracker.ui.cards.tabs.CardsAllFragment

class SpoilerCardsFragment : CardsAllFragment() {

    override fun configRecycleView() {
        super.configRecycleView()
        isFragmentSelected = true
        PublicInteractor.getSpoilerName {
            eventBus.post(CmdUpdateTitle(title = it))
        }
    }

    override fun loadCardsByAttr(attribute: CardAttribute) {
        currentAttr = attribute
        PublicInteractor.getSpoilerCards(attribute) {
            cardsLoaded = it
            showCards()
        }
    }

}