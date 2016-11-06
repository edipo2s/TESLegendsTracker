package com.ediposouza.teslesgendstracker.ui.cards

import android.content.Context
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentStatePagerAdapter
import android.support.v4.view.ViewPager
import android.view.*
import com.ediposouza.teslesgendstracker.R
import com.ediposouza.teslesgendstracker.ui.base.command.CmdShowSnackbarMsg
import kotlinx.android.synthetic.main.activity_dash.*
import kotlinx.android.synthetic.main.fragment_cards.*

/**
 * Created by EdipoSouza on 10/30/16.
 */
class CardsFragment : BaseFragment() {

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
                val title = if (position == 0) R.string.app_name else R.string.tab_favorites
                activity.dash_toolbar_title.setText(title)
            }
        })
        activity.dash_tab_layout.setupWithViewPager(cards_view_pager)
    }

    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
        menu?.clear()
        inflater?.inflate(R.menu.menu_search, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item?.itemId) {
            R.id.menu_search -> mEventBus.post(CmdShowSnackbarMsg(CmdShowSnackbarMsg.TYPE_INFO, "Test"))
        }
        return super.onOptionsItemSelected(item)
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