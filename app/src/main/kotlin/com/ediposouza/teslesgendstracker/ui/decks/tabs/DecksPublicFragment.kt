package com.ediposouza.teslesgendstracker.ui.decks.tabs

import android.os.Bundle
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
import com.ediposouza.teslesgendstracker.data.Class
import com.ediposouza.teslesgendstracker.data.Deck
import com.ediposouza.teslesgendstracker.interactor.FirebaseParsers
import com.ediposouza.teslesgendstracker.interactor.PrivateInteractor
import com.ediposouza.teslesgendstracker.interactor.PublicInteractor
import com.ediposouza.teslesgendstracker.ui.DeckActivity
import com.ediposouza.teslesgendstracker.ui.base.*
import com.ediposouza.teslesgendstracker.ui.util.firebase.OnLinearLayoutItemScrolled
import com.ediposouza.teslesgendstracker.util.inflate
import com.google.firebase.auth.FirebaseAuth
import jp.wasabeef.recyclerview.animators.SlideInLeftAnimator
import kotlinx.android.synthetic.main.fragment_decks_list.*
import kotlinx.android.synthetic.main.include_login_button.*
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

    protected var currentClasses = Class.values()
    protected val publicInteractor = PublicInteractor()
    protected val privateInteractor = PrivateInteractor()

    private val nameTransitionName: String by lazy { getString(R.string.deck_name_transition_name) }
    private val coverTransitionName: String by lazy { getString(R.string.deck_cover_transition_name) }
    private val attr1TransitionName: String by lazy { getString(R.string.deck_attr1_transition_name) }
    private val attr2TransitionName: String by lazy { getString(R.string.deck_attr2_transition_name) }

    open protected val isDeckPrivate: Boolean = false

    open protected val dataRef = {
        publicInteractor.getPublicDecksRef()
    }

    open protected val dataFilter: (FirebaseParsers.DeckParser) -> Boolean = {
        currentClasses.map { it.ordinal }.contains(it.cls)
    }

    val itemClick = { view: View, deck: Deck ->
        PrivateInteractor().getFavoriteDecks(deck.cls) {
            val favorite = it?.filter { it.id == deck.id }?.isNotEmpty() ?: false
            val userId = FirebaseAuth.getInstance().currentUser?.uid
            val like = deck.likes.contains(userId)
            startActivity(DeckActivity.newIntent(context, deck, favorite, like, deck.owner == userId),
                    ActivityOptionsCompat.makeSceneTransitionAnimation(activity,
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

    open protected val decksAdapter: BaseAdsFirebaseAdapter<*, DecksAllViewHolder> by lazy {
        object : BaseAdsFirebaseAdapter<FirebaseParsers.DeckParser, DecksAllViewHolder>(
                FirebaseParsers.DeckParser::class.java, dataRef, DECK_PAGE_SIZE, ADS_EACH_ITEMS,
                R.layout.itemlist_deck_ads, false, dataFilter) {

            override fun onCreateDefaultViewHolder(parent: ViewGroup): DecksAllViewHolder {
                return DecksAllViewHolder(parent.inflate(R.layout.itemlist_deck), itemClick, itemLongClick)
            }

            override fun onBindContentHolder(itemKey: String, model: FirebaseParsers.DeckParser, viewHolder: DecksAllViewHolder) {
                Timber.d(model.toString())
                viewHolder.bind(model.toDeck(itemKey, isDeckPrivate), privateInteractor)
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
                decksAdapter.more()
            })
        }
        decks_refresh_layout.setOnRefreshListener {
            decksAdapter.reset()
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

    open fun showDecks() {
        decksAdapter.reset()
    }

    class DecksAllViewHolder(val view: View, val itemClick: (View, Deck) -> Unit,
                             val itemLongClick: (View, Deck) -> Boolean) : RecyclerView.ViewHolder(view) {

        constructor(view: View) : this(view, { view, deck -> }, { view, deck -> true })

        fun bind(itemKey: String, publicInteractor: PublicInteractor, privateInteractor: PrivateInteractor) {
            itemView.deck_loading.visibility = View.VISIBLE
            itemView.deck_cover.visibility = View.GONE
            itemView.deck_info.visibility = View.GONE
            publicInteractor.getPublicDeck(itemKey) {
                bind(it, privateInteractor)
            }
        }

        fun bind(deck: Deck, privateInteractor: PrivateInteractor) {
            with(itemView) {
                deck_loading.visibility = View.GONE
                deck_cover.visibility = View.VISIBLE
                deck_info.visibility = View.VISIBLE
                setOnClickListener { itemClick(itemView, deck) }
                setOnLongClickListener { itemLongClick(itemView, deck) }
                deck_cover.setImageResource(deck.cls.imageRes)
                deck_private.layoutParams.width = if (deck.private) ViewGroup.LayoutParams.WRAP_CONTENT else 0
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
                deck_comments.visibility = if (deck.private) View.INVISIBLE else View.VISIBLE
                deck_likes.text = numberInstance.format(deck.likes.size)
                deck_likes.visibility = if (deck.private) View.INVISIBLE else View.VISIBLE
                deck_views.text = numberInstance.format(deck.views)
                deck_views.visibility = if (deck.private) View.INVISIBLE else View.VISIBLE
                calculateMissingSoul(deck, privateInteractor)
            }
        }

        fun calculateMissingSoul(deck: Deck, privateInteractor: PrivateInteractor) {
            with(itemView.deck_soul_missing) {
                visibility = View.INVISIBLE
                itemView.deck_soul_missing_loading.visibility = View.VISIBLE
                privateInteractor.getMissingCards(deck, { itemView.deck_soul_missing_loading.visibility = View.VISIBLE }) {
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