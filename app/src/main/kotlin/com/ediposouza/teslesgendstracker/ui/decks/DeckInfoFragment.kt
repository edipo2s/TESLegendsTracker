package com.ediposouza.teslesgendstracker.ui.decks

import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.support.v4.app.ActivityOptionsCompat
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.text.format.DateUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import com.ediposouza.teslesgendstracker.App
import com.ediposouza.teslesgendstracker.R
import com.ediposouza.teslesgendstracker.TIME_PATTERN
import com.ediposouza.teslesgendstracker.data.*
import com.ediposouza.teslesgendstracker.interactor.PrivateInteractor
import com.ediposouza.teslesgendstracker.interactor.PublicInteractor
import com.ediposouza.teslesgendstracker.ui.base.BaseFragment
import com.ediposouza.teslesgendstracker.ui.cards.CardActivity
import com.ediposouza.teslesgendstracker.util.inflate
import kotlinx.android.synthetic.main.fragment_deck_info.*
import kotlinx.android.synthetic.main.itemlist_deck_update.view.*
import org.greenrobot.eventbus.Subscribe
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.runOnUiThread
import org.threeten.bp.format.DateTimeFormatter
import timber.log.Timber
import java.text.NumberFormat

/**
 * Created by ediposouza on 20/02/17.
 */
class DeckInfoFragment : BaseFragment() {

    companion object {

        val EXTRA_DECK = "deckExtra"
        val EXTRA_OWNED = "ownedExtra"

    }

    private val deck by lazy { arguments.getParcelable<Deck>(EXTRA_DECK) }
    private val deckOwned by lazy { arguments.getBoolean(EXTRA_OWNED, false) }
    private val numberInstance: NumberFormat by lazy { NumberFormat.getNumberInstance() }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return container?.inflate(R.layout.fragment_deck_info)
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        loadDeckInfo()
    }

    fun updateLikes(like: Boolean) {
        val deckLikes = Integer.parseInt(deck_details_likes.text.toString())
        deck_details_likes.text = numberInstance.format(deckLikes + if (like) 1 else -1)
    }

    private fun loadDeckInfo() {
        if (deckOwned) {
            deck_details_likes.visibility = View.GONE
            deck_details_views.visibility = View.GONE
        }
        deck_details_cardlist.showDeck(deck, false)
        deck_details_type.text = deck.type.name.toLowerCase().capitalize()
        deck_details_views.text = numberInstance.format(deck.views)
        deck_details_likes.text = numberInstance.format(deck.likes.size)
        deck_details_soul_cost.text = numberInstance.format(deck.cost)
        deck_details_create_at.text = deck.createdAt.toLocalDate().toString()
        val updateDate = deck.updatedAt.toLocalDate()
        val updateTime = deck.updatedAt.toLocalTime().format(DateTimeFormatter.ofPattern(TIME_PATTERN))
        deck_details_update_at.text = getString(R.string.deck_details_last_update_format, updateDate, updateTime)
        configDeckUpdates()
        loadDeckRemoteInfo()
    }

    private fun loadDeckRemoteInfo() {
        doAsync {
            if (App.hasUserLogged()) {
                calculateMissingSoul(deck)
            }
            if (!deckOwned) {
                PublicInteractor.incDeckView(deck) {
                    deck_details_views?.text = it.toString()
                }
            }
            PublicInteractor.getPatches {
                val patch = it.find { it.uuidDate == deck.patch }
                context?.runOnUiThread {
                    deck_details_patch?.text = patch?.desc ?: ""
                }
            }
        }
    }

    private fun calculateMissingSoul(deck: Deck) {
        deck_details_soul_missing?.apply {
            context.runOnUiThread {
                visibility = View.INVISIBLE
                deck_details_soul_missing_loading?.visibility = View.VISIBLE
            }
            PrivateInteractor.getDeckMissingCards(deck, { deck_details_soul_missing_loading?.visibility = View.VISIBLE }) {
                deck_details_soul_missing_loading?.visibility = View.GONE
                val missingSoul = it.map { it.qtd * it.rarity.soulCost }.sum()
                Timber.d("Missing %d", missingSoul)
                text = NumberFormat.getNumberInstance().format(missingSoul)
                visibility = View.VISIBLE
                deck_details_cardlist?.showMissingCards(it)
            }
        }
    }

    private fun configDeckUpdates() {
        deck_details_updates_label.visibility = View.VISIBLE.takeIf { deck.updates.isNotEmpty() } ?: View.GONE
        if (deck.updates.isNotEmpty()) {
            with(deck_details_updates) {
                adapter = DeckUpdateAdapter(deck.updates.reversed(), deck.cls)
                layoutManager = LinearLayoutManager(context)
                setHasFixedSize(true)
                postDelayed({ deck_details_scroll.smoothScrollTo(0, 0) }, DateUtils.SECOND_IN_MILLIS)
            }
        }
    }

    @Subscribe
    fun onCmdChangeDeckViewMode(cmdChangeDeckViewMode: CmdChangeDeckViewMode) {
        deck_details_cardlist.setCardViewMode(fragmentManager, cmdChangeDeckViewMode.compactMode)
        deck_details_cardlist.layoutParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT,
                2.5f.takeIf { cmdChangeDeckViewMode.compactMode } ?: 1f)
    }

    class DeckUpdateAdapter(val items: List<DeckUpdate>, val cls: DeckClass) : RecyclerView.Adapter<DeckUpdateViewHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): DeckUpdateViewHolder {
            return DeckUpdateViewHolder(parent?.inflate(R.layout.itemlist_deck_update))
        }

        override fun onBindViewHolder(holder: DeckUpdateViewHolder?, position: Int) {
            holder?.bind(items[position], cls)
        }

        override fun getItemCount(): Int = items.size

    }

    class DeckUpdateViewHolder(view: View?) : RecyclerView.ViewHolder(view) {

        fun bind(deckUpdate: DeckUpdate, cls: DeckClass) {
            with(itemView) {
                val updateDate = deckUpdate.date.toLocalDate()
                val updateTime = deckUpdate.date.toLocalTime().format(DateTimeFormatter.ofPattern(TIME_PATTERN))
                deck_update_title.text = context.getString(R.string.deck_details_last_update_format, updateDate, updateTime)
                PublicInteractor.getCards(null, cls.attr1, cls.attr2, CardAttribute.DUAL, CardAttribute.NEUTRAL) { cards ->
                    configUpdateCardsChanges(cards, deckUpdate)
                }
            }
        }

        private fun DeckUpdateViewHolder.configUpdateCardsChanges(cards: List<Card>, deckUpdate: DeckUpdate) {
            with(itemView.deck_update_changes) {
                val onItemClick = { view: View, card: Card -> showExpandedCard(context, card, view) }
                adapter = com.ediposouza.teslesgendstracker.ui.decks.widget.DeckList.DeckListAdapter({ }, onItemClick, { _, _ -> true }).apply {
                    updateMode = true
                    showDeck(deckUpdate.changes.map {
                        val cardQtd = it
                        com.ediposouza.teslesgendstracker.data.CardSlot(cards.find { it.shortName == cardQtd.key }!!, it.value)
                    })
                }
                layoutManager = android.support.v7.widget.LinearLayoutManager(context)
                setHasFixedSize(true)
            }
        }

        private fun showExpandedCard(context: Context, card: Card, view: View) {
            val transitionName = context.getString(R.string.card_transition_name)
            ActivityCompat.startActivity(context, CardActivity.newIntent(context, card),
                    ActivityOptionsCompat.makeSceneTransitionAnimation(context as Activity, view, transitionName).toBundle())
        }

    }

}