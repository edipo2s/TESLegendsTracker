package com.ediposouza.teslesgendstracker.ui.decks

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.support.v4.app.ActivityOptionsCompat
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentStatePagerAdapter
import android.support.v4.view.MenuItemCompat
import android.support.v4.view.ViewPager
import android.support.v7.widget.SearchView
import android.text.format.DateUtils
import android.view.*
import android.view.inputmethod.InputMethodManager
import com.ediposouza.teslesgendstracker.R
import com.ediposouza.teslesgendstracker.data.DeckClass
import com.ediposouza.teslesgendstracker.ui.base.*
import com.ediposouza.teslesgendstracker.ui.cards.CmdFilterSearch
import com.ediposouza.teslesgendstracker.ui.decks.tabs.DecksFavoritedFragment
import com.ediposouza.teslesgendstracker.ui.decks.tabs.DecksOwnerFragment
import com.ediposouza.teslesgendstracker.ui.decks.tabs.DecksPublicFragment
import com.ediposouza.teslesgendstracker.util.MetricScreen
import com.ediposouza.teslesgendstracker.util.MetricsManager
import com.ediposouza.teslesgendstracker.util.inflate
import kotlinx.android.synthetic.main.activity_dash.*
import kotlinx.android.synthetic.main.fragment_decks.*
import org.greenrobot.eventbus.Subscribe
import org.jetbrains.anko.intentFor

/**
 * Created by EdipoSouza on 11/12/16.
 */
class DecksFragment : BaseFragment(), SearchView.OnQueryTextListener {

    private val KEY_FAB_NEW_DECK = "newDeckKey"
    private val KEY_PAGE_VIEW_POSITION = "pageViewPositionKey"
    private val RC_NEW_DECK = 125

    private val adapter by lazy { DecksPageAdapter(context, fragmentManager) }

    private val pageChange = object : ViewPager.SimpleOnPageChangeListener() {

        override fun onPageSelected(position: Int) {
            updateActivityTitle(position)
            MetricsManager.trackScreen(when (position) {
                0 -> MetricScreen.SCREEN_DECKS_PUBLIC()
                1 -> MetricScreen.SCREEN_DECKS_OWNED()
                else -> MetricScreen.SCREEN_DECKS_FAVORED()
            })
        }

    }

    private fun updateActivityTitle(position: Int) {
        val title = CmdUpdateTitle(when (position) {
            1 -> R.string.title_tab_decks_owned
            2 -> R.string.title_tab_decks_favorites
            else -> R.string.title_tab_decks_public
        })
        eventBus.post(title)
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return container?.inflate(R.layout.fragment_decks)
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setHasOptionsMenu(true)
        decks_view_pager.adapter = adapter
        activity.dash_navigation_view.setCheckedItem(R.id.menu_decks)
        decks_view_pager.addOnPageChangeListener(pageChange)
        decks_attr_filter.filterClick = {
            if (decks_attr_filter.isAttrSelected(it)) {
                decks_attr_filter.unSelectAttr(it)
            } else {
                decks_attr_filter.selectAttr(it, false)
            }
            eventBus.post(CmdShowDecksByClasses(DeckClass.getClasses(decks_attr_filter.getSelectedAttrs())))
        }
        decks_fab_add.setOnClickListener {
            val anim = ActivityOptionsCompat.makeCustomAnimation(context, R.anim.slide_up, R.anim.slide_down)
            startActivityForResult(context.intentFor<NewDeckActivity>(), RC_NEW_DECK, anim.toBundle())
        }
        MetricsManager.trackScreen(MetricScreen.SCREEN_DECKS_PUBLIC())
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        view?.post { updateActivityTitle(decks_view_pager?.currentItem ?: 0) }
        decks_tab_layout.setupWithViewPager(decks_view_pager)
    }

    override fun onSaveInstanceState(outState: Bundle?) {
        outState?.apply {
            putInt(KEY_PAGE_VIEW_POSITION, decks_view_pager?.currentItem ?: 0)
            putBoolean(KEY_FAB_NEW_DECK, decks_fab_add?.isShown ?: false)
        }
        super.onSaveInstanceState(outState)
    }

    override fun onViewStateRestored(savedInstanceState: Bundle?) {
        super.onViewStateRestored(savedInstanceState)
        savedInstanceState?.apply {
            decks_view_pager.currentItem = getInt(KEY_PAGE_VIEW_POSITION)
        }
    }

    override fun onResume() {
        super.onResume()
        decks_app_bar_layout.setExpanded(true, true)
    }

    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
        menu?.clear()
        inflater?.inflate(R.menu.menu_search, menu)
        val actionView = MenuItemCompat.getActionView(menu?.findItem(R.id.menu_search))
        if (actionView is SearchView) {
            with(actionView) {
                queryHint = getString(R.string.decks_search_hint)
                setOnQueryTextListener(this@DecksFragment)
            }
        }
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == RC_NEW_DECK && resultCode == Activity.RESULT_OK) {
            val privateExtra = data?.getBooleanExtra(NewDeckActivity.DECK_PRIVATE_EXTRA, false) ?: false
            decks_view_pager.currentItem = if (privateExtra) 1 else 0
            Handler().postDelayed({ eventBus.post(CmdUpdateDeckAndShowDeck()) }, DateUtils.SECOND_IN_MILLIS / 2)
        }
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

    @Subscribe
    @Suppress("unused")
    fun onCmdUpdateVisibility(update: CmdUpdateVisibility) {
        if (update.show) {
            decks_fab_add.show()
        } else {
            decks_fab_add.hide()
        }
    }

    class DecksPageAdapter(ctx: Context, fm: FragmentManager) : FragmentStatePagerAdapter(fm) {

        var titles: Array<String> = ctx.resources.getStringArray(R.array.decks_tabs)
        val decksPublicFragment by lazy { DecksPublicFragment() }
        val decksMyFragment by lazy { DecksOwnerFragment() }
        val decksSavedFragment by lazy { DecksFavoritedFragment() }

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

}
