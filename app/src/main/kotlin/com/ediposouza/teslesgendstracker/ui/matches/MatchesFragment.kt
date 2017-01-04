package com.ediposouza.teslesgendstracker.ui.matches

import android.content.Context
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentStatePagerAdapter
import android.support.v4.view.ViewPager
import android.view.*
import com.ediposouza.teslesgendstracker.R
import com.ediposouza.teslesgendstracker.ui.base.BaseFragment
import com.ediposouza.teslesgendstracker.ui.base.CmdShowTabs
import com.ediposouza.teslesgendstracker.ui.matches.tabs.MatchesHistory
import com.ediposouza.teslesgendstracker.ui.matches.tabs.MatchesStatistics
import com.ediposouza.teslesgendstracker.util.MetricScreen
import com.ediposouza.teslesgendstracker.util.MetricsManager
import com.ediposouza.teslesgendstracker.util.inflate
import kotlinx.android.synthetic.main.activity_dash.*
import kotlinx.android.synthetic.main.fragment_matches.*

/**
 * Created by EdipoSouza on 1/3/17.
 */
class MatchesFragment : BaseFragment() {

    private val KEY_PAGE_VIEW_POSITION = "pageViewPositionKey"

    val pageChange = object : ViewPager.SimpleOnPageChangeListener() {
        override fun onPageSelected(position: Int) {
            updateActivityTitle(position)
//            (matches_view_pager.adapter as MatchesPageAdapter).getItem(position).updateCardsList()
            MetricsManager.trackScreen(when (position) {
                0 -> MetricScreen.SCREEN_MATCHES_STATISTICS()
                else -> MetricScreen.SCREEN_MATCHES_HISTORY()
            })
        }

    }

    private fun updateActivityTitle(position: Int) {
        activity.toolbar_title?.setText(when (position) {
            0 -> R.string.title_tab_matches_statistics
            else -> R.string.title_tab_matches_history
        })
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return container?.inflate(R.layout.fragment_matches)
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setHasOptionsMenu(true)
        activity.dash_navigation_view.setCheckedItem(R.id.menu_matches)
        matches_view_pager.adapter = MatchesPageAdapter(context, childFragmentManager)
        matches_view_pager.addOnPageChangeListener(pageChange)
        MetricsManager.trackScreen(MetricScreen.SCREEN_MATCHES_STATISTICS())
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        activity.toolbar_title.setText(R.string.title_tab_matches_statistics)
        activity.dash_tab_layout.setupWithViewPager(matches_view_pager)
    }

    override fun onSaveInstanceState(outState: Bundle?) {
        outState?.apply { putInt(KEY_PAGE_VIEW_POSITION, matches_view_pager?.currentItem ?: 0) }
        super.onSaveInstanceState(outState)
    }

    override fun onViewStateRestored(savedInstanceState: Bundle?) {
        super.onViewStateRestored(savedInstanceState)
        savedInstanceState?.apply {
            matches_view_pager.currentItem = getInt(KEY_PAGE_VIEW_POSITION)
        }
    }

    override fun onResume() {
        super.onResume()
        eventBus.post(CmdShowTabs())
    }

    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
        menu?.clear()
        inflater?.inflate(R.menu.menu_season, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    class MatchesPageAdapter(ctx: Context, fm: FragmentManager) : FragmentStatePagerAdapter(fm) {

        var titles: Array<String> = ctx.resources.getStringArray(R.array.matches_tabs)
        val matchesStatisticsFragment by lazy { MatchesStatistics() }
        val matchesHistoryFragment by lazy { MatchesHistory() }

        override fun getItem(position: Int): Fragment {
            return when (position) {
                0 -> matchesStatisticsFragment
                else -> matchesHistoryFragment
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