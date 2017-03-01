package com.ediposouza.teslesgendstracker.ui.decks.tabs

import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.support.v4.app.ActivityOptionsCompat
import android.support.v4.util.Pair
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RelativeLayout
import com.ediposouza.teslesgendstracker.App
import com.ediposouza.teslesgendstracker.R
import com.ediposouza.teslesgendstracker.data.Deck
import com.ediposouza.teslesgendstracker.data.DeckClass
import com.ediposouza.teslesgendstracker.interactor.FirebaseParsers
import com.ediposouza.teslesgendstracker.interactor.PrivateInteractor
import com.ediposouza.teslesgendstracker.interactor.PublicInteractor
import com.ediposouza.teslesgendstracker.ui.base.*
import com.ediposouza.teslesgendstracker.ui.cards.CmdFilterSearch
import com.ediposouza.teslesgendstracker.ui.decks.DeckActivity
import com.ediposouza.teslesgendstracker.ui.util.firebase.OnLinearLayoutItemScrolled
import com.ediposouza.teslesgendstracker.util.inflate
import com.google.firebase.auth.FirebaseAuth
import jp.wasabeef.recyclerview.animators.SlideInLeftAnimator
import kotlinx.android.synthetic.main.fragment_decks_list.*
import kotlinx.android.synthetic.main.itemlist_deck.view.*
import org.greenrobot.eventbus.Subscribe
import timber.log.Timber
import java.text.NumberFormat

/**
 * Created by EdipoSouza on 11/18/16.
 */
open class DecksPublicFragment : BaseFragment() {

    val ADS_EACH_ITEMS = 10 //after 10 lines
    val DECK_PAGE_SIZE = 8

    protected var searchFilter: String? = null
    protected var currentClasses = DeckClass.values()

    private val nameTransitionName: String by lazy { getString(R.string.deck_name_transition_name) }
    private val coverTransitionName: String by lazy { getString(R.string.deck_cover_transition_name) }
    private val attr1TransitionName: String by lazy { getString(R.string.deck_attr1_transition_name) }
    private val attr2TransitionName: String by lazy { getString(R.string.deck_attr2_transition_name) }

    open protected val isDeckPrivate: Boolean = false

    open protected val dataRef = {
        PublicInteractor.getPublicDecksRef()
    }

    private val dataFilter: (FirebaseParsers.DeckParser) -> Boolean = {
        currentClasses.map { it.ordinal }.contains(it.cls) &&
                it.name.toLowerCase().trim().contains(searchFilter ?: "")
    }

    val itemClick = { view: View, deck: Deck ->
        PrivateInteractor.getUserFavoriteDecks(deck.cls) {
            val favorite = it?.filter { it.uuid == deck.uuid }?.isNotEmpty() ?: false
            val userId = FirebaseAuth.getInstance().currentUser?.uid
            val like = deck.likes.contains(userId)
            val deckIntent = DeckActivity.newIntent(context, deck, favorite, like, deck.owner == userId)
            ActivityCompat.startActivity(activity, deckIntent, ActivityOptionsCompat
                    .makeSceneTransitionAnimation(activity,
                            Pair(view.deck_name as View, nameTransitionName),
                            Pair(view.deck_cover as View, coverTransitionName),
                            Pair(view.deck_attr1 as View, attr1TransitionName),
                            Pair(view.deck_attr2 as View, attr2TransitionName)).toBundle())
        }
    }

    val itemLongClick = {
        _: View, _: Deck ->
        true
    }

    open protected val decksAdapter: BaseAdsFirebaseAdapter<*, DecksAllViewHolder> by lazy {
        object : BaseAdsFirebaseAdapter<FirebaseParsers.DeckParser, DecksAllViewHolder>(
                FirebaseParsers.DeckParser::class.java, dataRef, DECK_PAGE_SIZE, ADS_EACH_ITEMS,
                R.layout.itemlist_deck_ads, false, dataFilter) {

            override fun onCreateDefaultViewHolder(parent: ViewGroup): DecksAllViewHolder {
                return DecksAllViewHolder(parent.inflate(R.layout.itemlist_deck), itemClick, itemLongClick)
            }

            override fun onBindContentHolder(itemKey: String, model: FirebaseParsers.DeckParser, viewHolder: DecksAllViewHolder) {
                viewHolder.bind(model.toDeck(itemKey, isDeckPrivate))
            }

            override fun onSyncEnd() {
                decks_refresh_layout?.isRefreshing = false
            }

        }
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return container?.inflate(R.layout.fragment_decks_list)
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        with(decks_recycler_view) {
            adapter = decksAdapter
            itemAnimator = SlideInLeftAnimator()
            layoutManager = object : LinearLayoutManager(context) {
                override fun supportsPredictiveItemAnimations(): Boolean = false
            }
            addOnScrollListener(OnLinearLayoutItemScrolled(decksAdapter.getContentCount() - 3) {
                view?.post { decksAdapter.more() }
            })
        }
        decks_refresh_layout.setOnRefreshListener {
            decksAdapter.reset()
        }
    }

    override fun configLoggedViews() {
        super.configLoggedViews()
        decks_recycler_view.visibility = View.VISIBLE.takeIf { App.hasUserLogged() } ?: View.INVISIBLE
    }

    @Subscribe
    @Suppress("unused", "UNUSED_PARAMETER")
    fun onCmdLoginSuccess(cmdLoginSuccess: CmdLoginSuccess) {
        configLoggedViews()
        showDecks()
    }

    @Subscribe
    @Suppress("unused", "UNUSED_PARAMETER")
    fun onCmdUpdateDeckAndShowDeck(cmdUpdateDeckAndShowDeck: CmdUpdateDeckAndShowDeck) {
        showDecks()
    }

    @Subscribe
    @Suppress("unused")
    fun onCmdFilterSearch(filterSearch: CmdFilterSearch) {
        searchFilter = filterSearch.search?.toLowerCase()?.trim()
        decksAdapter.reset()
    }

    @Subscribe
    @Suppress("unused")
    fun onCmdShowDecksByClasses(cmdShowDecksByClasses: CmdShowDecksByClasses) {
        currentClasses = cmdShowDecksByClasses.classes?.toTypedArray() ?: DeckClass.values()
        showDecks()
        if (currentClasses.isEmpty()) {
            decksAdapter.notifyDataSetChanged()
        }
    }

    open fun showDecks() {
        decksAdapter.reset()
        decksAdapter.notifyDataSetChanged()
        eventBus.post(CmdUpdateVisibility(true))
    }

    class DecksAllViewHolder(view: View, val itemClick: (View, Deck) -> Unit,
                             val itemLongClick: (View, Deck) -> Boolean) : RecyclerView.ViewHolder(view) {

        fun bind(itemKey: String) {
            itemView.deck_loading.visibility = View.VISIBLE
            itemView.deck_cover.visibility = View.GONE
            itemView.deck_info.visibility = View.GONE
            PublicInteractor.getPublicDeck(itemKey) {
                bind(it)
            }
        }

        fun bind(deck: Deck) {
            with(itemView) {
                deck_loading.visibility = View.GONE
                deck_cover.visibility = View.VISIBLE
                deck_info.visibility = View.VISIBLE
                setOnClickListener { itemClick(itemView, deck) }
                setOnLongClickListener { itemLongClick(itemView, deck) }
                deck_cover.setImageResource(deck.cls.imageRes)
                deck_private.layoutParams.width = ViewGroup.LayoutParams.WRAP_CONTENT.takeIf { deck.private } ?: 0
                deck_name.text = deck.name
                deck_attr1.setImageResource(deck.cls.attr1.imageRes)
                deck_attr2.setImageResource(deck.cls.attr2.imageRes)
                deck_type.text = deck.type.name.toLowerCase().capitalize()
                deck_date.text = deck.updatedAt.toLocalDate().toString()
                (deck_date.layoutParams as RelativeLayout.LayoutParams).apply {
                    if (deck.private) {
                        addRule(RelativeLayout.ALIGN_PARENT_END)
                        removeRule(RelativeLayout.END_OF)
                    } else {
                        addRule(RelativeLayout.END_OF, R.id.deck_center)
                        removeRule(RelativeLayout.ALIGN_PARENT_END)
                    }
                    deck_date.layoutParams = this
                }
                deck_date.setCompoundDrawablesWithIntrinsicBounds(if (deck.updates.isEmpty())
                    R.drawable.ic_create_at else R.drawable.ic_updated_at, 0, 0, 0)
                val numberInstance = NumberFormat.getNumberInstance()
                deck_soul_cost.text = numberInstance.format(deck.cost)
                deck_comments.text = numberInstance.format(deck.comments.size)
                deck_comments.visibility = View.INVISIBLE.takeIf { deck.private } ?: View.VISIBLE
                deck_likes.text = numberInstance.format(deck.likes.size)
                deck_likes.visibility = View.INVISIBLE.takeIf { deck.private } ?: View.VISIBLE
                deck_views.text = numberInstance.format(deck.views)
                deck_views.visibility = View.INVISIBLE.takeIf { deck.private } ?: View.VISIBLE
                if (App.hasUserLogged()) {
                    calculateMissingSoul(deck)
                }
            }
        }

        fun calculateMissingSoul(deck: Deck) {
            with(itemView.deck_soul_missing) {
                visibility = View.INVISIBLE
                itemView.deck_soul_missing_loading.visibility = View.VISIBLE
                PrivateInteractor.getDeckMissingCards(deck, { itemView.deck_soul_missing_loading.visibility = View.VISIBLE }) {
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