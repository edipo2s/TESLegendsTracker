package com.ediposouza.teslesgendstracker.ui.cards

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.support.design.widget.BottomSheetBehavior
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentStatePagerAdapter
import android.support.v4.view.MenuItemCompat
import android.support.v4.view.ViewPager
import android.support.v7.widget.SearchView
import android.text.format.DateUtils
import android.view.*
import android.view.inputmethod.InputMethodManager
import com.ediposouza.teslesgendstracker.R
import com.ediposouza.teslesgendstracker.ui.base.*
import com.ediposouza.teslesgendstracker.ui.cards.tabs.CardsAllFragment
import com.ediposouza.teslesgendstracker.ui.cards.tabs.CardsCollectionFragment
import com.ediposouza.teslesgendstracker.ui.cards.tabs.CardsFavoritesFragment
import com.ediposouza.teslesgendstracker.ui.cards.tabs.CardsTokensFragment
import com.ediposouza.teslesgendstracker.ui.cards.widget.CollectionStatistics
import com.ediposouza.teslesgendstracker.util.*
import kotlinx.android.synthetic.main.activity_dash.*
import kotlinx.android.synthetic.main.fragment_cards.*
import kotlinx.android.synthetic.main.include_new_update.*
import org.greenrobot.eventbus.Subscribe
import timber.log.Timber

/**
 * Created by EdipoSouza on 10/30/16.
 */
class CardsFragment : BaseFragment(), SearchView.OnQueryTextListener {

    private val KEY_PAGE_VIEW_POSITION = "pageViewPositionKey"

    private var query: String? = null
    private var searchView: SearchView? = null
    private val handler = Handler()
    private val trackSearch = Runnable { MetricsManager.trackSearch(query ?: "") }

    private val statisticsSheetBehavior: BottomSheetBehavior<CollectionStatistics>
        get() = BottomSheetBehavior.from(cards_collection_statistics)

    val pageChange = object : ViewPager.SimpleOnPageChangeListener() {
        override fun onPageSelected(position: Int) {
            updateActivityTitle(position)
            val selectedAttr = cards_filter_attr.getSelectedAttrs().first()
            (cards_view_pager.adapter as CardsPageAdapter).getItem(position).updateCardsList(selectedAttr)
            if (position == 1) {
                statisticsSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
                cards_collection_statistics.updateStatistics()
            } else {
                statisticsSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN
            }
            eventBus.post(CmdUpdateRarityMagikaFiltersPosition(position == 1))
            MetricsManager.trackScreen(when (position) {
                0 -> MetricScreen.SCREEN_CARDS_ALL()
                1 -> MetricScreen.SCREEN_CARDS_COLLECTION()
                2 -> MetricScreen.SCREEN_CARDS_FAVORED()
                else -> MetricScreen.SCREEN_CARDS_TOKENS()
            })
        }

    }

    private fun updateActivityTitle(position: Int) {
        val title = when (position) {
            1 -> R.string.title_tab_cards_collection
            2 -> R.string.title_tab_cards_favorites
            3 -> R.string.title_tab_cards_tokens
            else -> R.string.title_tab_cards_all
        }
        eventBus.post(CmdUpdateTitle(title))
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return container?.inflate(R.layout.fragment_cards)
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setHasOptionsMenu(true)
        activity.dash_navigation_view.setCheckedItem(R.id.menu_cards)
        cards_collection_statistics.setOnClickListener {
            statisticsSheetBehavior.toggleExpanded()
        }
        statisticsSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN
        cards_view_pager.adapter = CardsPageAdapter(context, childFragmentManager)
        cards_view_pager.addOnPageChangeListener(pageChange)
        cards_filter_attr.filterClick = {
            eventBus.post(CmdShowCardsByAttr(it))
            cards_filter_attr.selectAttr(it, true)
        }
        cards_filter_rarity.filterClick = { eventBus.post(CmdFilterRarity(it)) }
        cards_filter_magika.filterClick = { eventBus.post(CmdFilterMagika(it)) }
        Handler().postDelayed({
            eventBus.post(CmdFilterSet(null))
        }, DateUtils.SECOND_IN_MILLIS)
        MetricsManager.trackScreen(MetricScreen.SCREEN_CARDS_ALL())
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        view?.post { updateActivityTitle(cards_view_pager?.currentItem ?: 0) }
        cards_tab_layout.setupWithViewPager(cards_view_pager)
    }

    override fun onSaveInstanceState(outState: Bundle?) {
        outState?.apply { putInt(KEY_PAGE_VIEW_POSITION, cards_view_pager?.currentItem ?: 0) }
        super.onSaveInstanceState(outState)
    }

    override fun onViewStateRestored(savedInstanceState: Bundle?) {
        super.onViewStateRestored(savedInstanceState)
        savedInstanceState?.apply {
            cards_view_pager.currentItem = getInt(KEY_PAGE_VIEW_POSITION)
        }
    }

    override fun onResume() {
        super.onResume()
        cards_app_bar_layout.setExpanded(true, true)
        (activity as BaseFilterActivity).updateRarityMagikaFiltersVisibility(true)
        context.checkLastVersion {
            Timber.d("New version $it found!")
            new_update_layout?.visibility = View.VISIBLE
            new_update_later?.rippleDuration = 200
            new_update_later?.setOnRippleCompleteListener {
                new_update_layout?.visibility = View.GONE
                MetricsManager.trackAction(MetricAction.ACTION_NEW_VERSION_UPDATE_LATER())
            }
            new_update_now?.rippleDuration = 200
            new_update_now?.setOnRippleCompleteListener {
                startActivity(Intent(Intent.ACTION_VIEW)
                        .setData(Uri.parse(getString(R.string.playstore_url_format, context.packageName))))
                MetricsManager.trackAction(MetricAction.ACTION_NEW_VERSION_UPDATE_NOW())
            }
            MetricsManager.trackAction(MetricAction.ACTION_NEW_VERSION_DETECTED())
        }
    }

    override fun onPause() {
        super.onPause()
        (activity as BaseFilterActivity).updateRarityMagikaFiltersVisibility(false)
    }

    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
        menu?.clear()
        inflater?.inflate(R.menu.menu_search, menu)
        inflater?.inflate(R.menu.menu_import, menu)
        inflater?.inflate(R.menu.menu_sets, menu)
        menu?.findItem(R.id.menu_import)?.isVisible = false
        searchView = MenuItemCompat.getActionView(menu?.findItem(R.id.menu_search)) as? SearchView
        searchView?.apply {
            queryHint = getString(R.string.cards_search_hint)
            setOnQueryTextListener(this@CardsFragment)
        }
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onQueryTextChange(newText: String?): Boolean {
        query = newText
        eventBus.post(CmdFilterSearch(newText))
        handler.removeCallbacks(trackSearch)
        if (query?.isNotEmpty() ?: false) {
            handler.postDelayed(trackSearch, DateUtils.SECOND_IN_MILLIS * 2)
        }
        return true
    }

    override fun onQueryTextSubmit(query: String?): Boolean {
        eventBus.post(CmdFilterSearch(query))
        val imm = activity.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(activity.currentFocus!!.windowToken, 0)
        return true
    }

    @Subscribe
    @Suppress("unused", "UNUSED_PARAMETER")
    fun onCmdInputSearch(cmdInputSearch: CmdInputSearch) {
        searchView?.isIconified = false
        searchView?.setQuery(cmdInputSearch.search, true)
    }

    class CardsPageAdapter(ctx: Context, fm: FragmentManager) : FragmentStatePagerAdapter(fm) {

        var titles: Array<String> = ctx.resources.getStringArray(R.array.cards_tabs)
        val cardsAllFragment by lazy { CardsAllFragment() }
        val cardsCollectionFragment by lazy { CardsCollectionFragment() }
        val cardsTokensFragment by lazy { CardsTokensFragment() }
        val cardsFavoritesFragment by lazy { CardsFavoritesFragment() }

        override fun getItem(position: Int): CardsAllFragment {
            return when (position) {
                1 -> cardsCollectionFragment.apply { isEditStarted = false }
                2 -> cardsFavoritesFragment
                3 -> cardsTokensFragment
                else -> cardsAllFragment
            }
        }

        override fun getCount(): Int {
            return titles.size
        }

        override fun getPageTitle(position: Int): CharSequence {
            return titles[position]
        }

    }

}