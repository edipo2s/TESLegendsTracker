package com.ediposouza.teslesgendstracker.ui.decks.tabs

import android.os.Build
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.support.v7.app.AppCompatActivity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import com.ediposouza.teslesgendstracker.R
import com.ediposouza.teslesgendstracker.data.*
import com.ediposouza.teslesgendstracker.interactor.PublicInteractor
import com.ediposouza.teslesgendstracker.ui.base.BaseFragment
import com.ediposouza.teslesgendstracker.ui.cards.CmdFilterAttr
import com.ediposouza.teslesgendstracker.ui.cards.CmdFilterRarity
import com.ediposouza.teslesgendstracker.ui.matches.NewMatchesActivity
import com.ediposouza.teslesgendstracker.util.MetricAction
import com.ediposouza.teslesgendstracker.util.MetricScreen
import com.ediposouza.teslesgendstracker.util.MetricsManager
import com.ediposouza.teslesgendstracker.util.inflate
import kotlinx.android.synthetic.main.fragment_arena_draft.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.jetbrains.anko.toast
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

    private val selectedClass by lazy {
        DeckClass.values()[arguments?.getInt(EXTRA_SELECTED_CLASS) ?: 0]
    }

    private val cardListOnClick: (Card) -> Unit = { card ->
        when (arena_draft_cardlist.getCards().sumBy { it.qtd }) {
            in 0..28 -> chooseCard(card)
            29 -> {
                chooseCard(card)
                showTrackMatches()
            }
            else -> showTrackMatches()
        }
    }

    private fun chooseCard(card: Card) {
        arena_draft_cardlist.addCard(card)
        resetChoice()
        MetricsManager.trackAction(MetricAction.ACTION_ARENA_PICK(card))
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return container?.inflate(R.layout.fragment_arena_draft)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        with(activity as AppCompatActivity) {
            setSupportActionBar(toolbar)
            supportActionBar?.title = ""
            supportActionBar?.setDisplayHomeAsUpEnabled(true)
        }
        if (savedInstanceState == null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            val layoutParams = toolbar.layoutParams as FrameLayout.LayoutParams
            layoutParams.topMargin = resources.getDimensionPixelSize(R.dimen.status_bar_height)
            toolbar.layoutParams = layoutParams
        }
        arena_draft_class_cover.setImageResource(selectedClass.imageRes)
        arena_draft_toolbar_title.text = getString(R.string.new_arena_title, selectedClass.name.toLowerCase().capitalize())
        arena_draft_cardlist.arenaMode = true
        with(arena_draft_rarity) {
            expand()
            showSelectedRarity = true
            filterClick = { rarity ->
                EventBus.getDefault().post(CmdFilterRarity(rarity))
            }
        }
        PublicInteractor.getCards(null) {
            val cards = it.filter {
                it.attr == CardAttribute.NEUTRAL
                        || (it.attr == selectedClass.attr1 || it.attr == selectedClass.attr2)
                        || (listOf(it.dualAttr1, it.dualAttr2, it.dualAttr3)
                        .containsAll(listOf(selectedClass.attr1, selectedClass.attr2)))
            }
            arena_draft_cards1.config(activity, selectedClass, cards, cardListOnClick, arena_draft_cardlist)
            arena_draft_cards2.config(activity, selectedClass, cards, cardListOnClick, arena_draft_cardlist)
            arena_draft_cards3.config(activity, selectedClass, cards, cardListOnClick, arena_draft_cardlist)
        }
        MetricsManager.trackScreen(MetricScreen.SCREEN_NEW_ARENA_DRAFT())
        context?.toast(R.string.new_arena_warning)
    }

    @Subscribe
    @Suppress("unused")
    fun onCmdFilterAttr(filterAttr: CmdFilterAttr) {
        arena_draft_cards1.currentAttr = filterAttr.attr
        arena_draft_cards2.currentAttr = filterAttr.attr
        arena_draft_cards3.currentAttr = filterAttr.attr
    }

    @Subscribe
    @Suppress("unused")
    fun onCmdFilterRarity(filterRarity: CmdFilterRarity) {
        arena_draft_cards1.currentRarity = filterRarity.rarity
        arena_draft_cards2.currentRarity = filterRarity.rarity
        arena_draft_cards3.currentRarity = filterRarity.rarity
        if (filterRarity.rarity == null) {
            resetChoice()
        }
    }

    private fun resetChoice() {
        arena_draft_cards1.reset()
        arena_draft_cards2.reset()
        arena_draft_cards3.reset()
        arena_draft_rarity.expand()
    }

    private fun showTrackMatches() {
        val draftCards = arena_draft_cardlist.getCards().map { it.card.shortName to it.qtd }.toMap()
        val deck = Deck("", "", "", false, DeckType.OTHER, selectedClass, 0, LocalDateTime.now(),
                LocalDateTime.now(), "", listOf(), 0, draftCards, listOf(), listOf())
        activity?.let {
            startActivity(NewMatchesActivity.newIntent(it, null, selectedClass, DeckType.OTHER, MatchMode.ARENA, deck))
            ActivityCompat.finishAfterTransition(it)
            MetricsManager.trackAction(MetricAction.ACTION_NEW_MATCH_START_WITH(deck, true))
        }
    }

}