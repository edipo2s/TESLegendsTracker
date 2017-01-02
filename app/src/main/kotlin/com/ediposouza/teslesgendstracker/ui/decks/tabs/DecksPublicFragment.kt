package com.ediposouza.teslesgendstracker.ui.decks.tabs

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.support.annotation.LayoutRes
import android.support.v4.app.ActivityOptionsCompat
import android.support.v4.util.Pair
import android.support.v7.util.DiffUtil
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.ediposouza.teslesgendstracker.App
import com.ediposouza.teslesgendstracker.R
import com.ediposouza.teslesgendstracker.data.Class
import com.ediposouza.teslesgendstracker.data.Deck
import com.ediposouza.teslesgendstracker.interactor.PrivateInteractor
import com.ediposouza.teslesgendstracker.interactor.PublicInteractor
import com.ediposouza.teslesgendstracker.ui.DeckActivity
import com.ediposouza.teslesgendstracker.ui.base.*
import com.ediposouza.teslesgendstracker.ui.util.SimpleDiffCallback
import com.ediposouza.teslesgendstracker.util.inflate
import com.google.firebase.auth.FirebaseAuth
import jp.wasabeef.recyclerview.animators.SlideInLeftAnimator
import kotlinx.android.synthetic.main.fragment_decks_list.*
import kotlinx.android.synthetic.main.include_login_button.*
import kotlinx.android.synthetic.main.itemlist_deck.view.*
import org.greenrobot.eventbus.Subscribe
import timber.log.Timber
import java.text.NumberFormat
import java.util.*

/**
 * Created by EdipoSouza on 11/18/16.
 */
open class DecksPublicFragment : BaseFragment() {

    val ADS_EACH_ITEMS = 15 //after 15 lines
    val DECK_PAGE_SIZE = 7
    val RC_DECK = 123

    protected val publicInteractor = PublicInteractor()
    protected var currentClasses: Array<Class> = Class.values()

    val nameTransitionName: String by lazy { getString(R.string.deck_name_transition_name) }
    val coverTransitionName: String by lazy { getString(R.string.deck_cover_transition_name) }
    val attr1TransitionName: String by lazy { getString(R.string.deck_attr1_transition_name) }
    val attr2TransitionName: String by lazy { getString(R.string.deck_attr2_transition_name) }

    open protected val isDeckOwned: Boolean = false

    val itemClick = { view: View, deck: Deck ->
        PrivateInteractor().getFavoriteDecks(deck.cls) {
            val favorite = it?.filter { it.id == deck.id }?.isNotEmpty() ?: false
            val like = deck.likes.contains(FirebaseAuth.getInstance().currentUser?.uid)
            startActivityForResult(DeckActivity.newIntent(context, deck, favorite, like, isDeckOwned),
                    RC_DECK, ActivityOptionsCompat.makeSceneTransitionAnimation(activity,
                    Pair(view.deck_name as View, nameTransitionName),
                    Pair(view.deck_cover as View, coverTransitionName),
                    Pair(view.deck_attr1 as View, attr1TransitionName),
                    Pair(view.deck_attr2 as View, attr2TransitionName)).toBundle())
        }
    }
    val itemLongClick = {
        view: View, deck: Deck ->
        true
    }
    open protected val decksAdapter = DecksAllAdapter(ADS_EACH_ITEMS, R.layout.itemlist_deck_ads,
            itemClick, itemLongClick)

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return container?.inflate(R.layout.fragment_decks_list)
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        decks_recycler_view.adapter = decksAdapter
        decks_recycler_view.itemAnimator = SlideInLeftAnimator()
        decks_recycler_view.layoutManager = object : LinearLayoutManager(context) {
            override fun supportsPredictiveItemAnimations(): Boolean = false
        }
        decks_refresh_layout.setOnRefreshListener {
            decks_refresh_layout.isRefreshing = false
            showDecks()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == RC_DECK && resultCode == Activity.RESULT_OK) {
            showDecks()
        }
    }

    fun configLoggedViews() {
        signin_button.setOnClickListener { showLogin() }
        signin_button.visibility = if (App.hasUserLogged()) View.INVISIBLE else View.VISIBLE
        decks_recycler_view.visibility = if (App.hasUserLogged()) View.VISIBLE else View.INVISIBLE
    }

    @Subscribe
    fun onCmdLoginSuccess(cmdLoginSuccess: CmdLoginSuccess) {
        configLoggedViews()
        showDecks()
    }

    @Subscribe
    fun onCmdUpdateDeckAndShowDeck(cmdUpdateDeckAndShowDeck: CmdUpdateDeckAndShowDeck) {
        showDecks()
    }

    @Subscribe
    fun onCmdShowDecksByClasses(cmdShowDecksByClasses: CmdShowDecksByClasses) {
        currentClasses = cmdShowDecksByClasses.classes.toTypedArray()
        showDecks()
        if (currentClasses.isEmpty()) {
            decksAdapter.notifyDataSetChanged()
        }
    }

    fun showDecks() {
        Timber.d("Classes: %s", currentClasses.toSet())
        decksAdapter.clearItems()
        for (i in currentClasses.indices) {
            getDecks(currentClasses[i], i == currentClasses.size - 1)
        }
    }

    open fun getDecks(cls: Class?, last: Boolean) {
        publicInteractor.getPublicDecks(cls, {
            it.forEach { Timber.d("Public: %s", it.toString()) }
            decksAdapter.showDecks(it.sortedByDescending(Deck::updatedAt), last)
        })
    }

    class DecksAllAdapter(adsEachItems: Int, @LayoutRes adsLayout: Int, val itemClick: (View, Deck) -> Unit,
                          val itemLongClick: (View, Deck) -> Boolean) : BaseAdsAdapter(adsEachItems, adsLayout) {

        val privateInteractor = PrivateInteractor()

        var items: List<Deck> = listOf()
        var newItems: ArrayList<Deck> = ArrayList()

        override fun onCreateDefaultViewHolder(parent: ViewGroup): RecyclerView.ViewHolder {
            return DecksAllViewHolder(parent.inflate(R.layout.itemlist_deck), itemClick, itemLongClick)
        }

        override fun onBindDefaultViewHolder(holder: RecyclerView.ViewHolder?, position: Int) {
            val deck = items[position]
            (holder as DecksAllViewHolder).bind(deck, privateInteractor)
        }

        override fun getDefaultItemCount(): Int = items.size

        fun clearItems() {
            newItems.clear()
        }

        fun showDecks(decks: List<Deck>, last: Boolean) {
            newItems.addAll(decks)
            if (!last) {
                return
            }
            Collections.sort(newItems, { d1, d2 -> d2.updatedAt.compareTo(d1.updatedAt) })
            val oldItems = items
            items = newItems
            if (items.isEmpty() || items.minus(oldItems).isEmpty()) {
                notifyDataSetChanged()
                return
            }
            DiffUtil.calculateDiff(SimpleDiffCallback(items, oldItems) { oldItem, newItem ->
                oldItem.id == newItem.id
            }).dispatchUpdatesTo(this)
        }

    }

    class DecksAllViewHolder(val view: View, val itemClick: (View, Deck) -> Unit,
                             val itemLongClick: (View, Deck) -> Boolean) : RecyclerView.ViewHolder(view) {

        fun bind(deck: Deck, privateInteractor: PrivateInteractor) {
            itemView.setOnClickListener { itemClick(itemView, deck) }
            itemView.setOnLongClickListener { itemLongClick(itemView, deck) }
            itemView.deck_cover.setImageResource(deck.cls.imageRes)
            itemView.deck_private.layoutParams.width = if (deck.private) ViewGroup.LayoutParams.WRAP_CONTENT else 0
            itemView.deck_name.text = deck.name
            itemView.deck_attr1.setImageResource(deck.cls.attr1.imageRes)
            itemView.deck_attr2.setImageResource(deck.cls.attr2.imageRes)
            itemView.deck_type.text = deck.type.name.toLowerCase().capitalize()
            itemView.deck_date.setCompoundDrawablesWithIntrinsicBounds(if (deck.updates.isEmpty())
                R.drawable.ic_create_at else R.drawable.ic_updated_at, 0, 0, 0)
            itemView.deck_date.text = deck.updatedAt.toLocalDate().toString()
            val numberInstance = NumberFormat.getNumberInstance()
            itemView.deck_soul_cost.text = numberInstance.format(deck.cost)
            itemView.deck_comments.text = numberInstance.format(deck.comments.size)
            itemView.deck_likes.text = numberInstance.format(deck.likes.size)
            itemView.deck_views.text = numberInstance.format(deck.views)
            calculateMissingSoul(deck, privateInteractor)
        }

        fun calculateMissingSoul(deck: Deck, interactor: PrivateInteractor) {
            with(itemView.deck_soul_missing) {
                visibility = View.INVISIBLE
                itemView.deck_soul_missing_loading.visibility = View.VISIBLE
                interactor.getMissingCards(deck, { itemView.deck_soul_missing_loading.visibility = View.VISIBLE }) {
                    itemView.deck_soul_missing_loading.visibility = View.GONE
                    val missingSoul = it.map { it.qtd * it.rarity.soulCost }.sum()
                    Timber.d("Missing %d", missingSoul)
                    text = NumberFormat.getNumberInstance().format(missingSoul)
                    visibility = View.VISIBLE
                }
            }
        }

    }

}