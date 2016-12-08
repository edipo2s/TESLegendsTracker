package com.ediposouza.teslesgendstracker.ui.cards.tabs

import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.support.v4.app.ActivityOptionsCompat
import android.support.v7.util.DiffUtil
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.StaggeredGridLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.ediposouza.teslesgendstracker.R
import com.ediposouza.teslesgendstracker.data.Attribute
import com.ediposouza.teslesgendstracker.data.Card
import com.ediposouza.teslesgendstracker.data.CardRarity
import com.ediposouza.teslesgendstracker.inflate
import com.ediposouza.teslesgendstracker.interactor.PrivateInteractor
import com.ediposouza.teslesgendstracker.interactor.PublicInteractor
import com.ediposouza.teslesgendstracker.load
import com.ediposouza.teslesgendstracker.ui.CardActivity
import com.ediposouza.teslesgendstracker.ui.base.CmdLoginSuccess
import com.ediposouza.teslesgendstracker.ui.base.CmdShowCardsByAttr
import com.ediposouza.teslesgendstracker.ui.base.CmdShowLogin
import com.ediposouza.teslesgendstracker.ui.cards.BaseFragment
import com.ediposouza.teslesgendstracker.ui.utils.GridSpacingItemDecoration
import com.ediposouza.teslesgendstracker.ui.widget.filter.CmdFilterMagika
import com.ediposouza.teslesgendstracker.ui.widget.filter.CmdFilterRarity
import com.ediposouza.teslesgendstracker.ui.widget.filter.CmdFilterSearch
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.NativeExpressAdView
import com.google.firebase.auth.FirebaseAuth
import jp.wasabeef.recyclerview.animators.ScaleInAnimator
import kotlinx.android.synthetic.main.fragment_cards_list.*
import kotlinx.android.synthetic.main.itemlist_card.view.*
import kotlinx.android.synthetic.main.itemlist_card_ads.view.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import java.util.*

/**
 * Created by EdipoSouza on 10/30/16.
 */
open class CardsAllFragment : BaseFragment() {

    var currentAttr: Attribute = Attribute.STRENGTH
    var cardsLoaded: List<Card> = ArrayList()
    var userFavorites: List<String> = ArrayList()
    var magikaFilter: Int = -1
    var rarityFilter: CardRarity? = null
    var searchFilter: String? = null

    val privateInteractor: PrivateInteractor by lazy { PrivateInteractor() }
    val transitionName: String by lazy { getString(R.string.card_transition_name) }

    open val cardsAdapter = CardsAllAdapter({ view, card -> showCardExpanded(card, view) }) {
        view: View, card: Card ->
        showCardExpanded(card, view)
        true
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return container?.inflate(R.layout.fragment_cards_list)
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        configRecycleView()
    }

    open fun configRecycleView() {
        cards_recycler_view.adapter = cardsAdapter
        cards_recycler_view.layoutManager = StaggeredGridLayoutManager(3, StaggeredGridLayoutManager.VERTICAL)
        cards_recycler_view.itemAnimator = ScaleInAnimator()
        cards_recycler_view.setHasFixedSize(true)
        cards_recycler_view.addItemDecoration(GridSpacingItemDecoration(3,
                resources.getDimensionPixelSize(R.dimen.card_margin), true, false))
        cards_refresh_layout.setOnRefreshListener {
            cards_refresh_layout.isRefreshing = false
            loadCardsByAttr(CmdShowCardsByAttr(currentAttr))
        }
    }

    fun configLoggedViews() {
        val hasLoggedUser = FirebaseAuth.getInstance().currentUser != null
        signin_button.setOnClickListener { EventBus.getDefault().post(CmdShowLogin()) }
        signin_button.visibility = if (hasLoggedUser) View.INVISIBLE else View.VISIBLE
        cards_recycler_view.visibility = if (hasLoggedUser) View.VISIBLE else View.INVISIBLE
    }

    @Subscribe
    fun onLoginSuccess(cmdLoginSuccess: CmdLoginSuccess) {
        configLoggedViews()
        loadCardsByAttr(CmdShowCardsByAttr(currentAttr))
    }

    @Subscribe
    fun onFilterSearch(filterSearch: CmdFilterSearch) {
        searchFilter = filterSearch.search
        showCards()
    }

    @Subscribe
    fun onFilterRarity(filterRarity: CmdFilterRarity) {
        rarityFilter = filterRarity.rarity
        showCards()
    }

    @Subscribe
    fun onFilterMagika(filterMagika: CmdFilterMagika) {
        magikaFilter = filterMagika.magika
        showCards()
    }

    @Subscribe
    open fun loadCardsByAttr(showCardsByAttr: CmdShowCardsByAttr) {
        currentAttr = showCardsByAttr.attr
        PublicInteractor().getCards(showCardsByAttr.attr, {
            cardsLoaded = it
            showCards()
        })
        privateInteractor.getFavoriteCards(currentAttr) {
            userFavorites = it
        }
    }

    open fun showCards() {
        cardsAdapter.showCards(filteredCards())
        cards_recycler_view.scrollToPosition(0)
    }

    fun updateCardsList() {
        if (cards_recycler_view != null) {
            loadCardsByAttr(CmdShowCardsByAttr(currentAttr))
        }
    }

    open fun filteredCards(): List<Card> {
        return cardsLoaded
                .filter {
                    when (searchFilter) {
                        null -> it is Card
                        else -> {
                            val search = searchFilter!!.toLowerCase().trim()
                            it.name.toLowerCase().contains(search) ||
                                    it.race.name.toLowerCase().contains(search) ||
                                    it.type.name.toLowerCase().contains(search) ||
                                    it.keywords.filter { it.name.toLowerCase().contains(search) }.isNotEmpty()
                        }
                    }
                }
                .filter {
                    when (rarityFilter) {
                        null -> it.rarity is CardRarity
                        else -> it.rarity == rarityFilter
                    }
                }
                .filter {
                    when {
                        magikaFilter == -1 -> it.cost is Int
                        magikaFilter < 7 -> it.cost == magikaFilter
                        else -> it.cost >= magikaFilter
                    }
                }
    }

    open fun showCardExpanded(card: Card, view: View) {
        val favorite = userFavorites.contains(card.shortName)
        ActivityCompat.startActivity(activity, CardActivity.newIntent(context, card, favorite),
                ActivityOptionsCompat.makeSceneTransitionAnimation(activity, view, transitionName).toBundle())
    }

}

class CardsAllAdapter(val itemClick: (View, Card) -> Unit,
                      val itemLongClick: (View, Card) -> Boolean) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    val ITEM_VIEW_TYPE_CARD = 0
    val ITEM_VIEW_TYPE_ADS = 1

    val ADS_EACH_ITEMS = 21 //after 7 lines

    var items: List<Card> = ArrayList()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        if (viewType == ITEM_VIEW_TYPE_CARD) {
            val cardView = updateViewSpan(parent.inflate(R.layout.itemlist_card), false)
            return CardsAllViewHolder(cardView, itemClick, itemLongClick)
        } else {
            return AdsViewHolder(updateViewSpan(parent.inflate(R.layout.itemlist_card_ads), true))
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder?, position: Int) {
        val qtdAdsBefore = getAdsQtdBefore(position)
        //        when (position) {
//            in 0..ADS_EACH_ITEMS -> 0 //0-21
//            in ADS_EACH_ITEMS + 1..2 * ADS_EACH_ITEMS + 1 -> 1  //22-43
//            in 2 * ADS_EACH_ITEMS + 2..3 * ADS_EACH_ITEMS + 2 -> 2 //44-65
//            in 3 * ADS_EACH_ITEMS + 3..4 * ADS_EACH_ITEMS + 3 -> 3 //66-87
//            else -> 0
//        }
        if (getItemViewType(position) == ITEM_VIEW_TYPE_CARD) {
            (holder as CardsAllViewHolder).bind(items[position - qtdAdsBefore])
        } else {
            (holder as AdsViewHolder).bind()
        }
    }

    override fun getItemCount(): Int = items.size + items.size.div(ADS_EACH_ITEMS)

    override fun getItemViewType(position: Int): Int {
        val qtdAdsBefore = getAdsQtdBefore(position)
        val nextEachItems = ADS_EACH_ITEMS * (qtdAdsBefore + 1) + qtdAdsBefore
        return if (position == nextEachItems) ITEM_VIEW_TYPE_ADS else ITEM_VIEW_TYPE_CARD
//        return when (position) {
//            ADS_EACH_ITEMS, //21
//            2 * ADS_EACH_ITEMS + 1, //43
//            3 * ADS_EACH_ITEMS + 2 -> ITEM_VIEW_TYPE_ADS //65
//            else -> ITEM_VIEW_TYPE_CARD
//        }
    }

    private fun getAdsQtdBefore(position: Int): Int {
        val qtdAds = position.div(ADS_EACH_ITEMS)
        return (position - qtdAds).div(ADS_EACH_ITEMS)
    }

    private fun updateViewSpan(view: View, fullSpan: Boolean): View {
        val viewLP = view.layoutParams as StaggeredGridLayoutManager.LayoutParams
        viewLP.isFullSpan = fullSpan
        view.layoutParams = viewLP
        return view
    }

    fun showCards(cards: List<Card>) {
        val oldItems = items
        items = cards
        DiffUtil.calculateDiff(object : DiffUtil.Callback() {
            override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
                return oldItems[oldItemPosition] == items[newItemPosition]
            }

            override fun getOldListSize(): Int = oldItems.size

            override fun getNewListSize(): Int = items.size

            override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
                return oldItems[oldItemPosition].shortName == items[newItemPosition].shortName
            }

        }, false).dispatchUpdatesTo(this)
    }
}

class CardsAllViewHolder(val view: View, val itemClick: (View, Card) -> Unit,
                         val itemLongClick: (View, Card) -> Boolean) : RecyclerView.ViewHolder(view) {

    fun bind(card: Card) {
        itemView.setOnClickListener { itemClick(itemView.card_all_image, card) }
        itemView.setOnLongClickListener { itemLongClick(itemView.card_all_image, card) }
        itemView.card_all_image.setImageBitmap(card.imageBitmap(itemView.context))
    }

}

class AdsViewHolder(view: View) : RecyclerView.ViewHolder(view) {

    fun bind() {
        val context = itemView.context
        itemView.ads_layout.removeAllViews()
        val adView = NativeExpressAdView(context)
        adView.adSize = AdSize(280, 80)
        adView.adUnitId = context.getString(R.string.app_ads_card_list)
        adView.load()
        itemView.ads_layout.addView(adView)
    }

}