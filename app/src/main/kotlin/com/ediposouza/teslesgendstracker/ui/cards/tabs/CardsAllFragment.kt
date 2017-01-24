package com.ediposouza.teslesgendstracker.ui.cards.tabs

import android.os.Bundle
import android.support.annotation.LayoutRes
import android.support.v4.app.ActivityCompat
import android.support.v4.app.ActivityOptionsCompat
import android.support.v7.util.DiffUtil
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.*
import com.ediposouza.teslesgendstracker.App
import com.ediposouza.teslesgendstracker.R
import com.ediposouza.teslesgendstracker.data.*
import com.ediposouza.teslesgendstracker.interactor.PrivateInteractor
import com.ediposouza.teslesgendstracker.interactor.PublicInteractor
import com.ediposouza.teslesgendstracker.ui.base.*
import com.ediposouza.teslesgendstracker.ui.cards.*
import com.ediposouza.teslesgendstracker.ui.util.GridSpacingItemDecoration
import com.ediposouza.teslesgendstracker.ui.util.SimpleDiffCallback
import com.ediposouza.teslesgendstracker.util.MetricAction
import com.ediposouza.teslesgendstracker.util.MetricsManager
import com.ediposouza.teslesgendstracker.util.inflate
import jp.wasabeef.recyclerview.animators.ScaleInAnimator
import kotlinx.android.synthetic.main.fragment_cards_list.*
import kotlinx.android.synthetic.main.include_login_button.*
import kotlinx.android.synthetic.main.itemlist_card.view.*
import org.greenrobot.eventbus.Subscribe
import org.jetbrains.anko.itemsSequence
import java.util.*

/**
 * Created by EdipoSouza on 10/30/16.
 */
open class CardsAllFragment : BaseFragment() {

    protected val KEY_CURRENT_ATTR = "currentClassKey"

    open val ADS_EACH_ITEMS = 21 //after 7 lines
    open val CARDS_PER_ROW = 3

    var currentAttr: Attribute = Attribute.STRENGTH
    var cardsLoaded: List<Card> = ArrayList()
    var magikaFilter: Int = -1
    var setFilter: CardSet? = null
    var classFilter: Class? = null
    var rarityFilter: CardRarity? = null
    var searchFilter: String? = null
    var menuSets: SubMenu? = null

    val publicInteractor: PublicInteractor by lazy { PublicInteractor() }
    val privateInteractor: PrivateInteractor by lazy { PrivateInteractor() }
    val transitionName: String by lazy { getString(R.string.card_transition_name) }
    val gridLayoutManager by lazy { cards_recycler_view.layoutManager as GridLayoutManager }

    protected var shouldScrollToTop: Boolean = false

    open protected val isCardsCollection: Boolean = false

    open val cardsAdapter by lazy {
        CardsAllAdapter(ADS_EACH_ITEMS, gridLayoutManager, R.layout.itemlist_card_ads,
                { view, card -> showCardExpanded(card, view) }) {
            view: View, card: Card ->
            showCardExpanded(card, view)
            true
        }
    }

    open val itemDecoration by lazy {
        GridSpacingItemDecoration(CARDS_PER_ROW,
                resources.getDimensionPixelSize(R.dimen.card_margin), true)
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return container?.inflate(R.layout.fragment_cards_list)
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setHasOptionsMenu(true)
        configRecycleView()
    }

    override fun onSaveInstanceState(outState: Bundle?) {
        outState?.apply {
            putInt(KEY_CURRENT_ATTR, currentAttr.ordinal)
        }
        super.onSaveInstanceState(outState)
    }

    override fun onViewStateRestored(savedInstanceState: Bundle?) {
        super.onViewStateRestored(savedInstanceState)
        cardsAdapter.onRestoreState(cards_recycler_view.layoutManager as GridLayoutManager)
        currentAttr = Attribute.values()[savedInstanceState?.getInt(KEY_CURRENT_ATTR) ?: 0]
    }

    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
        super.onCreateOptionsMenu(menu, inflater)
        menuSets = menu?.findItem(R.id.menu_sets)?.subMenu
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item?.itemId) {
            R.id.menu_sets_all -> filterSet(item, null)
            R.id.menu_sets_core -> filterSet(item, CardSet.CORE)
            R.id.menu_sets_madhouse -> filterSet(item, CardSet.MADHOUSE)
        }
        return super.onOptionsItemSelected(item)
    }

    private fun filterSet(menuItem: MenuItem?, set: CardSet?) {
        menuSets?.itemsSequence()?.forEach {
            it.setIcon(if (it.itemId == menuItem?.itemId) R.drawable.ic_checked else 0)
        }
        eventBus.post(CmdFilterSet(set))
    }

    open fun configRecycleView() {
        cards_recycler_view.layoutManager = object : GridLayoutManager(context, CARDS_PER_ROW) {
            override fun supportsPredictiveItemAnimations(): Boolean = false
        }
        cards_recycler_view.itemAnimator = ScaleInAnimator()
        cards_recycler_view.adapter = cardsAdapter
        cards_recycler_view.addItemDecoration(itemDecoration)
        cards_refresh_layout.setOnRefreshListener {
            cards_refresh_layout.isRefreshing = false
            loadCardsByAttr(currentAttr)
        }
    }

    fun configLoggedViews() {
        signin_button.setOnClickListener { showLogin() }
        signin_button.visibility = if (App.hasUserLogged()) View.INVISIBLE else View.VISIBLE
        cards_recycler_view.visibility = if (App.hasUserLogged()) View.VISIBLE else View.INVISIBLE
    }

    @Subscribe
    fun onCmdUpdateRarityMagikaFiltersVisibility(update: CmdUpdateVisibility) {
        if (isFragmentSelected) {
            (activity as BaseFilterActivity).updateRarityMagikaFiltersVisibility(update.show)
        }
    }

    @Subscribe
    @Suppress("UNUSED_PARAMETER")
    fun onCmdLoginSuccess(cmdLoginSuccess: CmdLoginSuccess) {
        configLoggedViews()
        loadCardsByAttr(currentAttr)
    }

    @Subscribe
    fun onCmdShowCardsByAttr(showCardsByAttr: CmdShowCardsByAttr) {
        loadCardsByAttr(showCardsByAttr.attr)
        if (isFragmentSelected) {
            MetricsManager.trackAction(MetricAction.ACTION_CARD_FILTER_ATTR(showCardsByAttr.attr))
        }
    }

    @Subscribe
    fun onCmdFilterSet(filterSet: CmdFilterSet) {
        setFilter = filterSet.set
        loadCardsByAttr(currentAttr)
    }

    @Subscribe
    fun onCmdFilterClass(filterClass: CmdFilterClass) {
        classFilter = filterClass.cls
        showCards()
        if (isFragmentSelected) {
            MetricsManager.trackAction(MetricAction.ACTION_CARD_FILTER_SET(setFilter))
        }
    }

    @Subscribe
    fun onCmdFilterSearch(filterSearch: CmdFilterSearch) {
        searchFilter = filterSearch.search
        showCards()
    }

    @Subscribe
    fun onCmdFilterRarity(filterRarity: CmdFilterRarity) {
        rarityFilter = filterRarity.rarity
        showCards()
        if (isFragmentSelected) {
            MetricsManager.trackAction(MetricAction.ACTION_CARD_FILTER_RARITY(rarityFilter))
        }
    }

    @Subscribe
    fun onCmdFilterMagika(filterMagika: CmdFilterMagika) {
        magikaFilter = filterMagika.magika
        showCards()
        if (isFragmentSelected) {
            MetricsManager.trackAction(MetricAction.ACTION_CARD_FILTER_MAGIKA(magikaFilter))
        }
    }

    private fun loadCardsByAttr(attribute: Attribute) {
        currentAttr = attribute
        publicInteractor.getCards(setFilter, attribute) {
            cardsLoaded = it
            showCards()
        }
    }

    open fun showCards() {
        cardsAdapter.showCards(filteredCards())
        eventBus.post(CmdUpdateVisibility(true))
        scrollToTop()
    }

    protected fun scrollToTop() {
        if (shouldScrollToTop) {
            cards_recycler_view?.scrollToPosition(0)
        } else {
            shouldScrollToTop = true
        }
    }

    fun updateCardsList(selectedAttr: Attribute = currentAttr) {
        currentAttr = selectedAttr
        shouldScrollToTop = false
        if (cards_recycler_view != null) {
            loadCardsByAttr(currentAttr)
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
                .filter {
                    when {
                        classFilter != null && currentAttr == Attribute.DUAL ->
                            if (classFilter?.attr2 == Attribute.NEUTRAL)
                                it.dualAttr1 == classFilter?.attr1 || it.dualAttr2 == classFilter?.attr1
                            else
                                (it.dualAttr1 == classFilter?.attr1 && it.dualAttr2 == classFilter?.attr2) ||
                                        (it.dualAttr1 == classFilter?.attr2 && it.dualAttr2 == classFilter?.attr1)
                        else -> it.attr is Attribute
                    }
                }
    }

    open fun showCardExpanded(card: Card, view: View) {
        ActivityCompat.startActivity(activity, CardActivity.newIntent(context, card),
                ActivityOptionsCompat.makeSceneTransitionAnimation(activity, view, transitionName).toBundle())
    }

    open class CardsAllAdapter(adsEachItems: Int, layoutManager: GridLayoutManager,
                               @LayoutRes adsLayout: Int, val itemClick: (View, Card) -> Unit,
                               val itemLongClick: (View, Card) -> Boolean) : BaseAdsAdapter(adsEachItems, adsLayout, layoutManager) {

        var items: List<Card> = ArrayList()

        override fun onCreateDefaultViewHolder(parent: ViewGroup): RecyclerView.ViewHolder {
            return CardsAllViewHolder(parent.inflate(R.layout.itemlist_card), itemClick, itemLongClick)
        }

        override fun onBindDefaultViewHolder(holder: RecyclerView.ViewHolder?, position: Int) {
            (holder as CardsAllViewHolder).bind(items[position])
        }

        override fun getDefaultItemCount(): Int = items.size

        fun showCards(cards: List<Card>) {
            val oldItems = items
            items = cards
            if (items.isEmpty() || items.minus(oldItems).isEmpty()) {
                notifyDataSetChanged()
                return
            }
            DiffUtil.calculateDiff(SimpleDiffCallback(items, oldItems) { oldItem, newItem ->
                oldItem.shortName == newItem.shortName
            }).dispatchUpdatesTo(this)
        }

    }

    open class CardsAllViewHolder(val view: View, val itemClick: (View, Card) -> Unit,
                                  val itemLongClick: (View, Card) -> Boolean) : RecyclerView.ViewHolder(view) {

        init {
            val cardLayoutParams = itemView.card_all_image.layoutParams
            cardLayoutParams.height = itemView.context.resources.getDimensionPixelSize(R.dimen.card_height)
            itemView.card_all_image.layoutParams = cardLayoutParams
        }

        fun bind(card: Card) {
            with(itemView) {
                setOnClickListener { itemClick(card_all_image, card) }
                setOnLongClickListener { itemLongClick(card_all_image, card) }
                card_all_image.setImageBitmap(card.imageBitmap(context))
            }
        }

    }

}
