package com.ediposouza.teslesgendstracker.ui.articles

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.support.customtabs.CustomTabsIntent
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentStatePagerAdapter
import android.support.v4.content.ContextCompat
import android.support.v4.view.ViewPager
import android.view.*
import com.ediposouza.teslesgendstracker.R
import com.ediposouza.teslesgendstracker.ui.base.BaseFragment
import com.ediposouza.teslesgendstracker.ui.base.CmdUpdateTitle
import com.ediposouza.teslesgendstracker.ui.wabbatrack.WabbaTrackFeatureFragment
import com.ediposouza.teslesgendstracker.util.MetricAction
import com.ediposouza.teslesgendstracker.util.MetricScreen
import com.ediposouza.teslesgendstracker.util.MetricsManager
import com.ediposouza.teslesgendstracker.util.inflate
import kotlinx.android.synthetic.main.activity_dash.*
import kotlinx.android.synthetic.main.fragment_articles.*
import kotlinx.android.synthetic.main.fragment_wabbatrack.*

/**
 * Created by EdipoSouza on 1/3/17.
 */
class WabbaTrackFragment : BaseFragment() {

    private val KEY_PAGE_VIEW_POSITION = "pageViewPositionKey"

    val pageChange = object : ViewPager.SimpleOnPageChangeListener() {
        override fun onPageSelected(position: Int) {
            updateActivityTitle(position)
            MetricsManager.trackScreen(when (position) {
                1 -> MetricScreen.SCREEN_WABBATARCK_ARENATIER()
                2 -> MetricScreen.SCREEN_WABBATARCK_AUTOBUILD()
                3 -> MetricScreen.SCREEN_WABBATARCK_DECKTRACKER()
                4 -> MetricScreen.SCREEN_WABBATARCK_MATCHES()
                else -> MetricScreen.SCREEN_WABBATARCK_ABOUT()
            })
        }

    }

    private fun updateActivityTitle(position: Int) {
        val title = when (position) {
            1 -> R.string.title_tab_wabbatrack_arenatier
            2 -> R.string.title_tab_wabbatrack_autobuild
            3 -> R.string.title_tab_wabbatrack_decktracker
            4 -> R.string.title_tab_wabbatrack_matches
            else -> R.string.title_tab_wabbatrack_about
        }
        eventBus.post(CmdUpdateTitle(title))
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return container?.inflate(R.layout.fragment_wabbatrack)
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setHasOptionsMenu(true)
        activity.dash_navigation_view.setCheckedItem(R.id.menu_wabbatrack)
        wabbatrack_view_pager.adapter = ArticlesPageAdapter(context, childFragmentManager)
        wabbatrack_view_pager.addOnPageChangeListener(pageChange)
        MetricsManager.trackScreen(MetricScreen.SCREEN_ARTICLES_NEWS())
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        eventBus.post(CmdUpdateTitle(R.string.title_tab_wabbatrack_decktracker))
        wabbatrack_tab_layout.setupWithViewPager(wabbatrack_view_pager)
    }

    override fun onSaveInstanceState(outState: Bundle?) {
        outState?.apply { putInt(KEY_PAGE_VIEW_POSITION, wabbatrack_view_pager?.currentItem ?: 0) }
        super.onSaveInstanceState(outState)
    }

    override fun onViewStateRestored(savedInstanceState: Bundle?) {
        super.onViewStateRestored(savedInstanceState)
        savedInstanceState?.apply {
            articles_view_pager.currentItem = getInt(KEY_PAGE_VIEW_POSITION)
        }
    }

    override fun onResume() {
        super.onResume()
        wabbatrack_app_bar_layout.setExpanded(true, true)
    }

    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
        inflater?.inflate(R.menu.menu_wabbatrack, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item?.itemId) {
            R.id.menu_video -> {
                val updateUri = Uri.parse(getString(R.string.wabbatrack_video_url))
                startActivity(Intent(Intent.ACTION_VIEW).setData(updateUri))
                MetricsManager.trackAction(MetricAction.ACTION_WABBATRACK_VIDEO())
                return true
            }
            R.id.menu_download -> {
                CustomTabsIntent.Builder()
                        .setToolbarColor(ContextCompat.getColor(context, R.color.colorPrimary))
                        .setStartAnimations(context, R.anim.slide_in_right, R.anim.slide_out_left)
                        .setExitAnimations(context, R.anim.slide_in_left, R.anim.slide_out_right)
                        .build()
                        .launchUrl(context, Uri.parse(getString(R.string.wabbatrack_url)))
                MetricsManager.trackAction(MetricAction.ACTION_WABBATRACK_WEBSITE())
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    class ArticlesPageAdapter(ctx: Context, fm: FragmentManager) : FragmentStatePagerAdapter(fm) {

        var titles: Array<String> = ctx.resources.getStringArray(R.array.wabbatrack_tabs)

        val aboutFragment by lazy {
            WabbaTrackFeatureFragment(R.drawable.wabbatrack_about, R.string.wabbatrack_about_desc)
        }
        val arenaTierFragment by lazy {
            WabbaTrackFeatureFragment(R.drawable.wabbatrack_arenatier, R.string.wabbatrack_arenatier_desc)
        }
        val autoBuildFragment by lazy {
            WabbaTrackFeatureFragment(R.drawable.wabbatrack_autobuild, R.string.wabbatrack_autobuild_desc)
        }
        val deckTrackerFragment by lazy {
            WabbaTrackFeatureFragment(R.drawable.wabbatrack_decktracker, R.string.wabbatrack_decktracker_desc)
        }
        val matchesFragment by lazy {
            WabbaTrackFeatureFragment(R.drawable.wabbatrack_matches, R.string.wabbatrack_matches_desc)
        }

        override fun getItem(position: Int): Fragment {
            return when (position) {
                1 -> arenaTierFragment
                2 -> autoBuildFragment
                3 -> deckTrackerFragment
                4 -> matchesFragment
                else -> aboutFragment
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