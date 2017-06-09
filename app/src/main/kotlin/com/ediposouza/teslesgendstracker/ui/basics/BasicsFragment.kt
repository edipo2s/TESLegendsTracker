package com.ediposouza.teslesgendstracker.ui.basics

import android.content.Context
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentStatePagerAdapter
import android.support.v4.view.ViewPager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.ediposouza.teslesgendstracker.R
import com.ediposouza.teslesgendstracker.ui.base.BaseFragment
import com.ediposouza.teslesgendstracker.ui.base.CmdUpdateTitle
import com.ediposouza.teslesgendstracker.util.MetricScreen
import com.ediposouza.teslesgendstracker.util.MetricsManager
import com.ediposouza.teslesgendstracker.util.inflate
import kotlinx.android.synthetic.main.activity_dash.*
import kotlinx.android.synthetic.main.fragment_basics.*

/**
 * Created by EdipoSouza on 1/3/17.
 */
class BasicsFragment : BaseFragment() {

    private val KEY_PAGE_VIEW_POSITION = "pageViewPositionKey"

    val pageChange = object : ViewPager.SimpleOnPageChangeListener() {
        override fun onPageSelected(position: Int) {
            updateActivityTitle(position)
            MetricsManager.trackScreen(when (position) {
                1 -> MetricScreen.SCREEN_BASICS_RACES()
                2 -> MetricScreen.SCREEN_BASICS_RANKED()
                else -> MetricScreen.SCREEN_BASICS_LEVELUP()
            })
        }

    }

    private fun updateActivityTitle(position: Int) {
        val title = when (position) {
            1 -> R.string.title_tab_basics_races
            2 -> R.string.title_tab_basics_ranked
            else -> R.string.title_tab_basics_levelup
        }
        eventBus.post(CmdUpdateTitle(title))
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return container?.inflate(R.layout.fragment_basics)
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setHasOptionsMenu(true)
        activity.dash_navigation_view.setCheckedItem(R.id.menu_wabbatrack)
        basics_view_pager.adapter = BasicsPageAdapter(context, childFragmentManager)
        basics_view_pager.addOnPageChangeListener(pageChange)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        eventBus.post(CmdUpdateTitle(R.string.title_tab_basics_levelup))
        basics_tab_layout.setupWithViewPager(basics_view_pager)
    }

    override fun onSaveInstanceState(outState: Bundle?) {
        outState?.apply { putInt(KEY_PAGE_VIEW_POSITION, basics_view_pager?.currentItem ?: 0) }
        super.onSaveInstanceState(outState)
    }

    override fun onViewStateRestored(savedInstanceState: Bundle?) {
        super.onViewStateRestored(savedInstanceState)
        savedInstanceState?.apply {
            basics_view_pager.currentItem = getInt(KEY_PAGE_VIEW_POSITION)
        }
    }

    override fun onResume() {
        super.onResume()
        basics_app_bar_layout.setExpanded(true, true)
    }

    class BasicsPageAdapter(ctx: Context, fm: FragmentManager) : FragmentStatePagerAdapter(fm) {

        var titles: Array<String> = ctx.resources.getStringArray(R.array.basics_tabs)

        val basicsLevelUpFragment by lazy { BasicsLevelUpFragment() }
        val basicsRacesFragment by lazy { BasicsRacesFragment() }
        val basicsRankedFragment by lazy { BasicsRankedFragment() }

        override fun getItem(position: Int): Fragment {
            return when (position) {
                1 -> basicsRacesFragment
                2 -> basicsRankedFragment
                else -> basicsLevelUpFragment
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