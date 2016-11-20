package com.ediposouza.teslesgendstracker.ui.decks

import android.content.Context
import android.os.Bundle
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentStatePagerAdapter
import android.support.v4.view.MenuItemCompat
import android.support.v4.view.ViewPager
import android.support.v7.widget.SearchView
import android.view.*
import android.view.inputmethod.InputMethodManager
import com.ediposouza.teslesgendstracker.R
import com.ediposouza.teslesgendstracker.inflate
import com.ediposouza.teslesgendstracker.ui.base.CmdShowCardsByAttr
import com.ediposouza.teslesgendstracker.ui.base.CmdUpdateRarityMagikaFiltersVisibility
import com.ediposouza.teslesgendstracker.ui.cards.BaseFragment
import com.ediposouza.teslesgendstracker.ui.decks.tabs.DecksOwnerFragment
import com.ediposouza.teslesgendstracker.ui.decks.tabs.DecksPublicFragment
import com.ediposouza.teslesgendstracker.ui.decks.tabs.DecksSavedFragment
import com.ediposouza.teslesgendstracker.ui.widget.filter.CmdFilterSearch
import kotlinx.android.synthetic.main.activity_dash.*
import kotlinx.android.synthetic.main.fragment_decks.*

/**
 * Created by EdipoSouza on 11/12/16.
 */
class DecksFragment : BaseFragment(), SearchView.OnQueryTextListener {

    val adapter by lazy { DecksPageAdapter(context, fragmentManager) }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return container?.inflate(R.layout.fragment_decks)
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setHasOptionsMenu(true)
        decks_view_pager.adapter = adapter
        decks_view_pager.addOnPageChangeListener(object : ViewPager.SimpleOnPageChangeListener() {
            override fun onPageSelected(position: Int) {
                val title = when (position) {
                    1 -> R.string.tab_decks_owned
                    2 -> R.string.tab_decks_saved
                    else -> R.string.tab_decks_public
                }
                activity.dash_toolbar_title.setText(title)
            }

            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
                (adapter.getItem(position) as DecksPublicFragment).getDecks()
            }

        })
        activity.dash_tab_layout.setupWithViewPager(decks_view_pager)
        decks_attr_filter.filterClick = { eventBus.post(CmdShowCardsByAttr(it)) }
    }

    override fun onResume() {
        super.onResume()
        eventBus.post(CmdUpdateRarityMagikaFiltersVisibility(false))
    }

    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
        menu?.clear()
        inflater?.inflate(R.menu.menu_search, menu)
        with(MenuItemCompat.getActionView(menu?.findItem(R.id.menu_search)) as SearchView) {
            queryHint = getString(R.string.search_hint)
            setOnQueryTextListener(this@DecksFragment)
        }
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onQueryTextChange(newText: String?): Boolean {
        eventBus.post(CmdFilterSearch(newText))
        return true
    }

    override fun onQueryTextSubmit(query: String?): Boolean {
        eventBus.post(CmdFilterSearch(query))
        val imm = activity.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(activity.currentFocus!!.windowToken, 0)
        return true
    }

}

class DecksPageAdapter(ctx: Context, fm: FragmentManager) : FragmentStatePagerAdapter(fm) {

    var titles: Array<String>
    val decksPublicFragment by lazy { DecksPublicFragment() }
    val decksMyFragment by lazy { DecksOwnerFragment() }
    val decksSavedFragment by lazy { DecksSavedFragment() }

    init {
        titles = ctx.resources.getStringArray(R.array.decks_tabs)
    }

    override fun getItem(position: Int): BaseFragment {
        return when (position) {
            1 -> decksMyFragment
            2 -> decksSavedFragment
            else -> decksPublicFragment
        }
    }

    override fun getCount(): Int {
        return titles.size
    }

    override fun getPageTitle(position: Int): CharSequence {
        return titles[position]
    }

}