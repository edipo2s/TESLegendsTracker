package com.ediposouza.teslesgendstracker.ui.decks.tabs

import android.os.Build
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.support.v4.app.ActivityOptionsCompat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import com.ediposouza.teslesgendstracker.data.*
import com.ediposouza.teslesgendstracker.interactor.PublicInteractor
import com.ediposouza.teslesgendstracker.ui.base.BaseFragment
import com.ediposouza.teslesgendstracker.ui.cards.CardActivity
import com.ediposouza.teslesgendstracker.ui.matches.NewMatchesActivity
import com.ediposouza.teslesgendstracker.util.inflate
import kotlinx.android.synthetic.main.fragment_arena_draft.*
import org.threeten.bp.LocalDateTime

/**
 * Created by EdipoSouza on 11/18/16.
 */
class NewArenaDraftFragment : BaseFragment() {

    private val EXTRA_SELECTED_CLASS = "selectedClassExtra"

    companion object {

        fun newFragment(selectedCls: DeckClass): NewArenaDraftFragment {
            return NewArenaDraftFragment().apply {
                arguments = Bundle().apply {
                    putInt(EXTRA_SELECTED_CLASS, selectedCls.ordinal)
                }
            }
        }

    }

    private val selectedClass by lazy { DeckClass.values()[arguments.getInt(EXTRA_SELECTED_CLASS)] }
    private val transitionName: String by lazy { getString(R.string.card_transition_name) }

    private val cardListOnClick: (View, Card) -> Unit = { _, card ->
        when (arena_draft_cardlist.getCards().sumBy { it.qtd }) {
            in 0..28 -> arena_draft_cardlist.addCard(card)
            29 -> {
                arena_draft_cardlist.addCard(card)
                showTrackMatches()
            }
            else -> showTrackMatches()
        }
    }

    private val cardOnLongOnClick: (View, Card) -> Boolean = { view, card ->
        ActivityCompat.startActivity(activity, CardActivity.newIntent(context, card),
                ActivityOptionsCompat.makeSceneTransitionAnimation(activity, view, transitionName).toBundle())
        true
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return container?.inflate(R.layout.fragment_arena_draft)
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            val layoutParams = toolbar.layoutParams as FrameLayout.LayoutParams
            layoutParams.topMargin = resources.getDimensionPixelSize(R.dimen.status_bar_height)
            toolbar.layoutParams = layoutParams
        }
        arena_draft_class_cover.setImageResource(selectedClass.imageRes)
        arena_draft_toolbar_title.text = selectedClass.name.toLowerCase().capitalize()
        arena_draft_rarity.expand()
        arena_draft_rarity.hideMainButton()
        arena_draft_cardlist.arenaMode = true
        PublicInteractor.getCards(null) {
            arena_draft_cards1.config(it, cardListOnClick, cardOnLongOnClick)
            arena_draft_cards2.config(it, cardListOnClick, cardOnLongOnClick)
            arena_draft_cards3.config(it, cardListOnClick, cardOnLongOnClick)
        }
    }

    private fun showTrackMatches() {
        val draftCards = arena_draft_cardlist.getCards().map { it.card.shortName to it.qtd }.toMap()
        val deck = Deck("", "", "", false, DeckType.OTHER, selectedClass, 0, LocalDateTime.now(),
                LocalDateTime.now(), "", listOf(), 0, draftCards, listOf(), listOf())
        startActivity(NewMatchesActivity.newIntent(context, null, selectedClass, DeckType.OTHER, MatchMode.ARENA, deck))
        ActivityCompat.finishAfterTransition(activity)
//        MetricsManager.trackAction(MetricAction.ACTION_NEW_MATCH_START_WITH(deck))
    }

}