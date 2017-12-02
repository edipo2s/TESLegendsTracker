package com.ediposouza.teslesgendstracker.ui.spoiler

import android.support.v4.app.ActivityCompat
import android.support.v4.app.ActivityOptionsCompat
import android.view.View
import com.ediposouza.teslesgendstracker.data.Card
import com.ediposouza.teslesgendstracker.data.CardAttribute
import com.ediposouza.teslesgendstracker.interactor.PublicInteractor
import com.ediposouza.teslesgendstracker.ui.base.CmdUpdateTitle
import com.ediposouza.teslesgendstracker.ui.cards.CardActivity
import com.ediposouza.teslesgendstracker.ui.cards.CmdFilterAttrs
import com.ediposouza.teslesgendstracker.ui.cards.tabs.CardsAllFragment
import org.greenrobot.eventbus.Subscribe

class SpoilerCardsFragment : CardsAllFragment() {

    protected var currentAttributes = CardAttribute.values().toList()

    override fun configRecycleView() {
        super.configRecycleView()
        isFragmentSelected = true
        PublicInteractor.getSpoilerTitle {
            eventBus.post(CmdUpdateTitle(title = it))
        }
    }

    override fun loadCardsByAttr(attribute: CardAttribute) {
        PublicInteractor.getSpoilerCards() {
            cardsLoaded = it
            showCards()
        }
    }

    override fun filteredCards(): List<Card> {
        return super.filteredCards().filter {
            currentAttributes.contains(it.attr)
        }.sortedBy { it.name }.sortedBy { it.cost }
    }

    override fun showCardExpanded(card: Card, view: View) {
        activity?.let {
            ActivityCompat.startActivity(it, CardActivity.newIntent(it, card, fromSpoiler = true),
                    ActivityOptionsCompat.makeSceneTransitionAnimation(it, view, transitionName).toBundle())
        }
    }

    @Subscribe
    @Suppress("unused")
    fun onFilterAttrs(cmdFilterAttrs: CmdFilterAttrs) {
        currentAttributes = cmdFilterAttrs.attrs
        showCards()
    }

}