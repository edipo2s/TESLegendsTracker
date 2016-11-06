package com.ediposouza.teslesgendstracker.ui.cards

import android.content.Context
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentStatePagerAdapter
import android.support.v4.view.MenuItemCompat
import android.support.v4.view.ViewPager
import android.support.v7.widget.SearchView
import android.view.*
import android.view.inputmethod.InputMethodManager
import com.ediposouza.teslesgendstracker.R
import com.ediposouza.teslesgendstracker.ui.CmdFilterSearch
import com.ediposouza.teslesgendstracker.ui.CmdShowCardsByAttr
import kotlinx.android.synthetic.main.activity_dash.*
import kotlinx.android.synthetic.main.fragment_cards.*

/**
 * Created by EdipoSouza on 10/30/16.
 */
class CardsFragment : BaseFragment(), SearchView.OnQueryTextListener {

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater?.inflate(R.layout.fragment_cards, container, false)
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setHasOptionsMenu(true)
        activity.dash_toolbar_title.setText(R.string.app_name)
        cards_view_pager.adapter = CardsPageAdapter(context, fragmentManager)
        cards_view_pager.addOnPageChangeListener(object : ViewPager.SimpleOnPageChangeListener() {
            override fun onPageSelected(position: Int) {
                val title = when (position) {
                    1 -> R.string.tab_collection
                    2 -> R.string.tab_favorites
                    else -> R.string.app_name
                }
                activity.dash_toolbar_title.setText(title)
            }
        })
        activity.dash_tab_layout.setupWithViewPager(cards_view_pager)
        attr_filter.filterClick = { mEventBus.post(CmdShowCardsByAttr(it)) }
    }

    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
        menu?.clear()
        inflater?.inflate(R.menu.menu_search, menu)
        (MenuItemCompat.getActionView(menu?.findItem(R.id.menu_search)) as SearchView)
                .setOnQueryTextListener(this)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onQueryTextChange(newText: String?): Boolean {
        mEventBus.post(CmdFilterSearch(newText))
        return true
    }

    override fun onQueryTextSubmit(query: String?): Boolean {
        mEventBus.post(CmdFilterSearch(query))
        val imm = activity.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(activity.currentFocus!!.windowToken, 0)
        return true
    }

}

class CardsPageAdapter(ctx: Context, fm: FragmentManager) : FragmentStatePagerAdapter(fm) {

    var titles: Array<String>

    init {
        titles = ctx.resources.getStringArray(R.array.cards_tabs)
    }

    override fun getItem(position: Int): Fragment {
        return when (position) {
            1 -> CardsFavoritesFragment()
            2 -> CardsCollectionFragment()
            else -> CardsAllFragment()
        }
    }

    override fun getCount(): Int {
        return titles.size
    }

    override fun getPageTitle(position: Int): CharSequence {
        return titles.get(position)
    }

}