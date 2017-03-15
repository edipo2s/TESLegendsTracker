package com.ediposouza.teslesgendstracker.ui.spoiler

import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.support.v4.view.MenuItemCompat
import android.support.v7.widget.SearchView
import android.text.format.DateUtils
import android.view.*
import android.view.inputmethod.InputMethodManager
import com.ediposouza.teslesgendstracker.R
import com.ediposouza.teslesgendstracker.ui.base.BaseFilterActivity
import com.ediposouza.teslesgendstracker.ui.base.BaseFragment
import com.ediposouza.teslesgendstracker.ui.base.CmdShowCardsByAttr
import com.ediposouza.teslesgendstracker.ui.cards.*
import com.ediposouza.teslesgendstracker.util.MetricScreen
import com.ediposouza.teslesgendstracker.util.MetricsManager
import com.ediposouza.teslesgendstracker.util.inflate
import kotlinx.android.synthetic.main.activity_dash.*
import kotlinx.android.synthetic.main.fragment_spoiler.*
import org.greenrobot.eventbus.Subscribe

/**
 * Created by EdipoSouza on 10/30/16.
 */
class SpoilerFragment : BaseFragment(), SearchView.OnQueryTextListener {

    private var query: String? = null
    private var searchView: SearchView? = null
    private val handler = Handler()
    private val trackSearch = Runnable { MetricsManager.trackSearch(query ?: "") }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return container?.inflate(R.layout.fragment_spoiler)
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setHasOptionsMenu(true)
        activity.dash_navigation_view.setCheckedItem(R.id.menu_spoiler)
        spoiler_filter_attr.filterClick = {
            eventBus.post(CmdShowCardsByAttr(it))
            spoiler_filter_attr.selectAttr(it, true)
        }
        spoiler_filter_rarity.filterClick = { eventBus.post(CmdFilterRarity(it)) }
        spoiler_filter_magika.filterClick = { eventBus.post(CmdFilterMagika(it)) }
        Handler().postDelayed({
            eventBus.post(CmdFilterSet(null))
        }, DateUtils.SECOND_IN_MILLIS)
        MetricsManager.trackScreen(MetricScreen.SCREEN_SPOILER())
        fragmentManager.beginTransaction()
                .replace(R.id.spoiler_cards_container, SpoilerCardsFragment())
                .commit()
    }

    override fun onResume() {
        super.onResume()
        (activity as BaseFilterActivity).updateRarityMagikaFiltersVisibility(true)
    }

    override fun onPause() {
        super.onPause()
        (activity as BaseFilterActivity).updateRarityMagikaFiltersVisibility(false)
    }

    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
        menu?.clear()
        inflater?.inflate(R.menu.menu_search, menu)
        menu?.findItem(R.id.menu_import)?.isVisible = false
        searchView = MenuItemCompat.getActionView(menu?.findItem(R.id.menu_search)) as? SearchView
        searchView?.apply {
            queryHint = getString(R.string.cards_search_hint)
            setOnQueryTextListener(this@SpoilerFragment)
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

}
