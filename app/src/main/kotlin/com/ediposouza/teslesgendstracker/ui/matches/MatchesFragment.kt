package com.ediposouza.teslesgendstracker.ui.matches

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.annotation.IntegerRes
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentStatePagerAdapter
import android.support.v4.content.ContextCompat
import android.support.v4.view.ViewPager
import android.support.v7.app.AlertDialog
import android.view.*
import android.widget.AdapterView
import android.widget.ArrayAdapter
import com.ediposouza.teslesgendstracker.R
import com.ediposouza.teslesgendstracker.data.*
import com.ediposouza.teslesgendstracker.interactor.PrivateInteractor
import com.ediposouza.teslesgendstracker.interactor.PublicInteractor
import com.ediposouza.teslesgendstracker.ui.base.BaseFragment
import com.ediposouza.teslesgendstracker.ui.base.CmdUpdateTitle
import com.ediposouza.teslesgendstracker.ui.base.CmdUpdateVisibility
import com.ediposouza.teslesgendstracker.ui.matches.tabs.MatchesHistoryFragment
import com.ediposouza.teslesgendstracker.ui.matches.tabs.MatchesStatisticsFragment
import com.ediposouza.teslesgendstracker.util.MetricScreen
import com.ediposouza.teslesgendstracker.util.MetricsManager
import com.ediposouza.teslesgendstracker.util.inflate
import com.ediposouza.teslesgendstracker.util.limitHeight
import kotlinx.android.synthetic.main.activity_dash.*
import kotlinx.android.synthetic.main.dialog_new_match.view.*
import kotlinx.android.synthetic.main.fragment_matches.*
import kotlinx.android.synthetic.main.itemlist_class.view.*
import org.greenrobot.eventbus.Subscribe
import org.jetbrains.anko.itemsSequence

/**
 * Created by EdipoSouza on 1/3/17.
 */
class MatchesFragment : BaseFragment() {

    private val KEY_PAGE_VIEW_POSITION = "pageViewPositionKey"
    private val RC_NEW_MATCHES = 145

    private var seasons: List<Season> = listOf()
    private var menuSeasons: SubMenu? = null

    val pageChange = object : ViewPager.SimpleOnPageChangeListener() {
        override fun onPageSelected(position: Int) {
            updateActivityTitle(position)
            if (position == 0) {
                matches_fab_add.show()
            }
            MetricsManager.trackScreen(when (position) {
                0 -> MetricScreen.SCREEN_MATCHES_STATISTICS()
                else -> MetricScreen.SCREEN_MATCHES_HISTORY()
            })
        }

    }

    private fun updateActivityTitle(position: Int) {
        val title = when (position) {
            0 -> R.string.title_tab_matches_statistics
            else -> R.string.title_tab_matches_history
        }
        eventBus.post(CmdUpdateTitle(title))
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
        matches_nav_mode.setOnNavigationItemSelectedListener {
            when (it.itemId) {
                R.id.tab_mode_ranked -> eventBus.post(CmdFilterMode(MatchMode.RANKED))
                R.id.tab_mode_casual -> eventBus.post(CmdFilterMode(MatchMode.CASUAL))
                R.id.tab_mode_arena -> eventBus.post(CmdFilterMode(MatchMode.ARENA))
            }
            true
        }
        matches_fab_add.setOnClickListener { showNewMatchDialog() }
        MetricsManager.trackScreen(MetricScreen.SCREEN_MATCHES_STATISTICS())
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        eventBus.post(CmdUpdateTitle(R.string.title_tab_matches_statistics))
        matches_tab_layout.setupWithViewPager(matches_view_pager)
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
        matches_app_bar_layout.setExpanded(true, true)
    }

    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
        menu?.clear()
        inflater?.inflate(R.menu.menu_percent, menu)
        inflater?.inflate(R.menu.menu_season, menu)
        getSeasons(menu?.findItem(R.id.menu_season))
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item?.itemId) {
            R.id.menu_season_all -> filterSeason(null)
            else -> seasons.find { it.id == item?.itemId }?.apply { filterSeason(this) }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == RC_NEW_MATCHES && resultCode == Activity.RESULT_OK) {
            eventBus.post(CmdUpdateMatches())
        }
    }

    private fun filterSeason(season: Season?) {
        val seasonId = season?.id ?: R.id.menu_season_all
        menuSeasons?.itemsSequence()?.forEach {
            it.setIcon(if (it.itemId == seasonId) R.drawable.ic_checked else 0)
        }
        eventBus.post(CmdFilterSeason(season))
    }

    private fun getSeasons(menuSeason: MenuItem?) {
        menuSeasons = menuSeason?.subMenu
        menuSeasons?.apply {
            clear()
            add(0, R.id.menu_season_all, 0, getString(R.string.matches_seasons_all)).setIcon(R.drawable.ic_checked)
            PublicInteractor().getSeasons {
                seasons = it.reversed()
                seasons.forEach {
                    add(0, it.id, 0, it.desc)
                }
            }
        }
    }

    private fun showNewMatchDialog() {
        val dialogView = View.inflate(context, R.layout.dialog_new_match, null)
        var decks = listOf<Deck?>(null)
        val classClickListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, pos: Int, id: Long) {
                PrivateInteractor().getUserDecks(Class.values()[pos]) { myDecks ->
                    decks = (myDecks as List<Deck?>).plusElement(null).sortedBy { it?.name ?: "" }
                    dialogView.new_match_dialog_deck_spinner.adapter = ArrayAdapter<String>(context,
                            android.R.layout.simple_spinner_dropdown_item, decks.map { it?.name ?: "" })
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
            }
        }
        AlertDialog.Builder(context)
                .setView(dialogView)
                .setPositiveButton(R.string.new_match_dialog_start, { dialog, which ->
                    val deckPosition = dialogView.new_match_dialog_deck_spinner.selectedItemPosition
                    val cls = Class.values()[dialogView.new_match_dialog_class_spinner.selectedItemPosition]
                    val deck = if (deckPosition >= 0) decks[deckPosition] else null
                    val mode = MatchMode.values()[dialogView.new_match_dialog_mode_spinner.selectedItemPosition]
                    startActivityForResult(NewMatchesActivity.newIntent(context, cls, deck, mode), RC_NEW_MATCHES)
                })
                .setNegativeButton(android.R.string.cancel, { dialog, which -> })
                .create()
                .apply {
                    setOnShowListener {
                        dialogView.new_match_dialog_class_spinner.apply {
                            adapter = ClassAdapter(context)
                            onItemSelectedListener = classClickListener
                            limitHeight()
                        }
                        dialogView.new_match_dialog_deck_spinner.apply {
                            if (decks.size >= 5) {
                                limitHeight()
                            }
                        }
                        val modeTypes = MatchMode.values().map { it.name.toLowerCase().capitalize() }
                        dialogView.new_match_dialog_mode_spinner.adapter = ArrayAdapter<String>(context,
                                android.R.layout.simple_spinner_dropdown_item, modeTypes)
                    }
                    show()
                }
    }

    @Subscribe
    fun onCmdUpdateVisibility(update: CmdUpdateVisibility) {
        if (update.show) {
            matches_fab_add.show()
        } else {
            matches_fab_add.hide()
        }
    }

    class MatchesPageAdapter(ctx: Context, fm: FragmentManager) : FragmentStatePagerAdapter(fm) {

        var titles: Array<String> = ctx.resources.getStringArray(R.array.matches_tabs)
        val matchesStatisticsFragment by lazy { MatchesStatisticsFragment() }
        val matchesHistoryFragment by lazy { MatchesHistoryFragment() }

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

    class ClassAdapter(ctx: Context, @IntegerRes layout: Int = R.layout.itemlist_class,
                       @IntegerRes val textColor: Int = R.color.primary_text_dark) :
            ArrayAdapter<Class>(ctx, layout, R.id.class_name, Class.values()) {

        override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup?): View {
            return super.getDropDownView(position, convertView, parent).apply {
                with(getItem(position)) {
                    class_attr1.setImageResource(attr1.imageRes)
                    class_attr2.setImageResource(attr2.imageRes)
                    class_attr2.visibility = if (attr2 != Attribute.NEUTRAL) View.VISIBLE else View.GONE
                    class_name.text = name.toLowerCase().capitalize()
                }
            }
        }

        override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
            return super.getView(position, convertView, parent).apply {
                with(getItem(position)) {
                    class_attr1.setImageResource(attr1.imageRes)
                    class_attr2.setImageResource(attr2.imageRes)
                    class_attr2.visibility = if (attr2 != Attribute.NEUTRAL) View.VISIBLE else View.GONE
                    class_name.text = name.toLowerCase().capitalize()
                    class_name.setTextColor(ContextCompat.getColor(context, textColor))
                }
            }
        }

    }

}