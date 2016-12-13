package com.ediposouza.teslesgendstracker.ui.cards

import android.content.Context
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
import com.ediposouza.teslesgendstracker.MetricScreen
import com.ediposouza.teslesgendstracker.R
import com.ediposouza.teslesgendstracker.data.Attribute
import com.ediposouza.teslesgendstracker.inflate
import com.ediposouza.teslesgendstracker.ui.base.BaseFragment
import com.ediposouza.teslesgendstracker.ui.base.CmdShowCardsByAttr
import com.ediposouza.teslesgendstracker.ui.base.CmdUpdateRarityMagikaFiltersVisibility
import com.ediposouza.teslesgendstracker.ui.cards.tabs.CardsAllFragment
import com.ediposouza.teslesgendstracker.ui.cards.tabs.CardsCollectionFragment
import com.ediposouza.teslesgendstracker.ui.cards.tabs.CardsFavoritesFragment
import com.ediposouza.teslesgendstracker.ui.widget.filter.CmdFilterSearch
import kotlinx.android.synthetic.main.activity_dash.*
import kotlinx.android.synthetic.main.fragment_cards.*

/**
 * Created by EdipoSouza on 10/30/16.
 */
class CardsFragment : BaseFragment(), SearchView.OnQueryTextListener {

    var query: String? = null
    val handler = Handler()
    val trackSearch = Runnable { metricsManager.trackSearch(query ?: "") }

    val pageChange = object : ViewPager.SimpleOnPageChangeListener() {
        override fun onPageSelected(position: Int) {
            val title = when (position) {
                1 -> R.string.tab_cards_collection
                2 -> R.string.tab_cards_favorites
                else -> R.string.app_name
            }
            activity.dash_toolbar_title?.setText(title)
            BottomSheetBehavior.from(activity.collection_statistics).state = BottomSheetBehavior.STATE_COLLAPSED
            metricsManager.trackScreen(when (position) {
                0 -> MetricScreen.SCREEN_CARDS_ALL()
                1 -> MetricScreen.SCREEN_CARDS_COLLECTION()
                else -> MetricScreen.SCREEN_CARDS_FAVORED()
            })
            (cards_view_pager.adapter as CardsPageAdapter).getItem(position).updateCardsList()
            if (position == 1) {
                collection_statistics.updateStatistics()
            }
        }

    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return container?.inflate(R.layout.fragment_cards)
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setHasOptionsMenu(true)
        activity.dash_navigation_view.setCheckedItem(R.id.menu_cards)
        cards_view_pager.adapter = CardsPageAdapter(context, childFragmentManager)
        cards_view_pager.addOnPageChangeListener(pageChange)
        attr_filter.filterClick = {
            eventBus.post(CmdShowCardsByAttr(it))
            attr_filter.selectAttr(it, true)
        }
        attr_filter.selectAttr(Attribute.STRENGTH, true)
        Handler().postDelayed({
            eventBus.post(CmdShowCardsByAttr(Attribute.STRENGTH))
        }, DateUtils.SECOND_IN_MILLIS)
        metricsManager.trackScreen(MetricScreen.SCREEN_CARDS_ALL())
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        activity.dash_toolbar_title.setText(R.string.app_name)
        activity.dash_tab_layout.setupWithViewPager(cards_view_pager)
    }

    override fun onResume() {
        super.onResume()
        eventBus.post(CmdUpdateRarityMagikaFiltersVisibility(true))
    }

    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
        menu?.clear()
        inflater?.inflate(R.menu.menu_search, menu)
        with(MenuItemCompat.getActionView(menu?.findItem(R.id.menu_search)) as SearchView) {
            queryHint = getString(R.string.search_hint)
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

}

class CardsPageAdapter(ctx: Context, fm: FragmentManager) : FragmentStatePagerAdapter(fm) {

    var titles: Array<String>
    val cardsCollectionFragment by lazy { CardsCollectionFragment() }
    val cardsFavoritesFragment by lazy { CardsFavoritesFragment() }
    val cardsAllFragment by lazy { CardsAllFragment() }

    init {
        titles = ctx.resources.getStringArray(R.array.cards_tabs)
    }

    override fun getItem(position: Int): CardsAllFragment {
        return when (position) {
            1 -> cardsCollectionFragment
            2 -> cardsFavoritesFragment
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