package com.ediposouza.teslesgendstracker.ui.matches.tabs

import android.os.Bundle
import android.support.v4.app.ActivityOptionsCompat
import android.view.*
import com.ediposouza.teslesgendstracker.R
import com.ediposouza.teslesgendstracker.data.MatchMode
import com.ediposouza.teslesgendstracker.data.Season
import com.ediposouza.teslesgendstracker.interactor.PublicInteractor
import com.ediposouza.teslesgendstracker.ui.arena.NewArenaActivity
import com.ediposouza.teslesgendstracker.util.MetricAction
import com.ediposouza.teslesgendstracker.util.MetricsManager
import com.ediposouza.teslesgendstracker.util.inflate
import kotlinx.android.synthetic.main.fragment_arena.*
import org.jetbrains.anko.intentFor
import org.jetbrains.anko.itemsSequence

/**
 * Created by EdipoSouza on 1/3/17.
 */
class ArenaFragment : MatchesHistoryFragment() {

    private var menuSeasons: SubMenu? = null
    private var seasons: List<Season> = listOf()

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        currentMatchMode = MatchMode.ARENA
        return container?.inflate(R.layout.fragment_arena)
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        arena_fab_add.setOnClickListener {
            val anim = ActivityOptionsCompat.makeCustomAnimation(context, R.anim.slide_up, R.anim.slide_down)
            startActivity(context.intentFor<NewArenaActivity>(), anim.toBundle())
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
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

    private fun getSeasons(menuSeason: MenuItem?) {
        menuSeasons = menuSeason?.subMenu
        menuSeasons?.apply {
            clear()
            add(0, R.id.menu_season_all, 0, getString(R.string.matches_seasons_all)).setIcon(R.drawable.ic_checked)
            PublicInteractor.getSeasons {
                seasons = it.reversed()
                seasons.forEach {
                    add(0, it.id, 0, "${it.date.month}/${it.date.year}")
                }
            }
        }
    }

    private fun filterSeason(season: Season?) {
        val seasonId = season?.id ?: R.id.menu_season_all
        menuSeasons?.itemsSequence()?.forEach {
            it.setIcon(if (it.itemId == seasonId) R.drawable.ic_checked else 0)
        }
        currentSeason = season
        updateMatchList()
        MetricsManager.trackAction(MetricAction.ACTION_MATCH_STATISTICS_FILTER_SEASON(season))
    }

}