package com.ediposouza.teslesgendstracker.ui.cards.tabs

import android.app.Activity
import android.content.Intent
import android.support.v4.app.ActivityOptionsCompat
import android.view.View
import com.ediposouza.teslesgendstracker.data.Card
import com.ediposouza.teslesgendstracker.ui.CardActivity
import kotlinx.android.synthetic.main.fragment_cards_list.*

/**
 * Created by EdipoSouza on 10/30/16.
 */
class CardsFavoritesFragment : CardsAllFragment() {

    val EXPAND_CODE = 123

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == EXPAND_CODE && resultCode == Activity.RESULT_OK) {
            updateCardsList()
        }
    }

    override fun configRecycleView() {
        super.configRecycleView()
        configLoggedViews()
    }

    override fun showCards() {
        privateInteractor.getFavoriteCards(currentAttr) {
            userFavorites = it
            cardsAdapter.showCards(filteredCards().filter { userFavorites.contains(it.shortName) })
            cards_recycler_view?.scrollToPosition(0)
        }
    }

    override fun showCardExpanded(card: Card, view: View) {
        startActivityForResult(CardActivity.newIntent(context, card, true), EXPAND_CODE,
                ActivityOptionsCompat.makeSceneTransitionAnimation(activity, view, transitionName).toBundle())
    }

}