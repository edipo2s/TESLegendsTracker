package com.ediposouza.teslesgendstracker.ui.decks

import android.content.Context
import android.os.Bundle
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentStatePagerAdapter
import android.support.v4.view.ViewPager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.ediposouza.teslesgendstracker.R
import com.ediposouza.teslesgendstracker.inflate
import com.ediposouza.teslesgendstracker.ui.cards.BaseFragment
import com.ediposouza.teslesgendstracker.ui.decks.tabs.DecksPublicFragment
import com.ediposouza.teslesgendstracker.ui.widget.CmdShowCardsByAttr
import kotlinx.android.synthetic.main.activity_dash.*
import kotlinx.android.synthetic.main.fragment_cards.*
import kotlinx.android.synthetic.main.fragment_decks.*

/**
 * Created by EdipoSouza on 11/12/16.
 */
class DecksFragment : BaseFragment() {

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return container?.inflate(R.layout.fragment_decks)
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        decks_view_pager.adapter = DecksPageAdapter(context, fragmentManager)
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
            }

        })
        activity.dash_tab_layout.setupWithViewPager(cards_view_pager)
        decks_attr_filter.filterClick = { eventBus.post(CmdShowCardsByAttr(it)) }
    }

}

class DecksPageAdapter(ctx: Context, fm: FragmentManager) : FragmentStatePagerAdapter(fm) {

    var titles: Array<String>
    val decksPublicFragment by lazy { DecksPublicFragment() }
    val decksMyFragment by lazy { DecksPublicFragment() }
    val decksSavedFragment by lazy { DecksPublicFragment() }

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