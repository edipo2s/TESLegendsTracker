package com.ediposouza.teslesgendstracker.ui.articles

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
import com.ediposouza.teslesgendstracker.ui.articles.tabs.ArticlesNewsFragment
import com.ediposouza.teslesgendstracker.ui.articles.tabs.ArticlesWorldFragment
import com.ediposouza.teslesgendstracker.ui.base.BaseFragment
import com.ediposouza.teslesgendstracker.ui.base.CmdUpdateTitle
import com.ediposouza.teslesgendstracker.util.MetricScreen
import com.ediposouza.teslesgendstracker.util.MetricsManager
import com.ediposouza.teslesgendstracker.util.inflate
import kotlinx.android.synthetic.main.activity_dash.*
import kotlinx.android.synthetic.main.fragment_articles.*

/**
 * Created by EdipoSouza on 1/3/17.
 */
class ArticlesFragment : BaseFragment() {

    private val KEY_PAGE_VIEW_POSITION = "pageViewPositionKey"

    val pageChange = object : ViewPager.SimpleOnPageChangeListener() {
        override fun onPageSelected(position: Int) {
            updateActivityTitle(position)
            MetricsManager.trackScreen(when (position) {
                0 -> MetricScreen.SCREEN_ARTICLES_NEWS()
                else -> MetricScreen.SCREEN_ARTICLES_WORLD()
            })
        }

    }

    private fun updateActivityTitle(position: Int) {
        val title = when (position) {
            0 -> R.string.title_tab_articles_news
            else -> R.string.title_tab_articles_world
        }
        eventBus.post(CmdUpdateTitle(title))
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return container?.inflate(R.layout.fragment_articles)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setHasOptionsMenu(true)
        activity?.dash_navigation_view?.setCheckedItem(R.id.menu_articles)
        articles_view_pager.adapter = ArticlesPageAdapter(context, childFragmentManager)
        articles_view_pager.addOnPageChangeListener(pageChange)
        MetricsManager.trackScreen(MetricScreen.SCREEN_ARTICLES_NEWS())
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        eventBus.post(CmdUpdateTitle(R.string.title_tab_articles_news))
        articles_tab_layout.setupWithViewPager(articles_view_pager)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.apply { putInt(KEY_PAGE_VIEW_POSITION, articles_view_pager?.currentItem ?: 0) }
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
        articles_app_bar_layout.setExpanded(true, true)
    }

    class ArticlesPageAdapter(ctx: Context?, fm: FragmentManager) : FragmentStatePagerAdapter(fm) {

        var titles: Array<String> = ctx?.resources?.getStringArray(R.array.articles_tabs) ?: arrayOf()
        val articlesNewsFragment by lazy { ArticlesNewsFragment() }
        val articlesWorldFragment by lazy { ArticlesWorldFragment() }

        override fun getItem(position: Int): Fragment {
            return when (position) {
                0 -> articlesNewsFragment
                else -> articlesWorldFragment
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