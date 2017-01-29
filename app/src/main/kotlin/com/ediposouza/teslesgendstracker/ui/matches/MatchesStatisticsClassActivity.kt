package com.ediposouza.teslesgendstracker.ui.matches

import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.support.design.widget.CollapsingToolbarLayout
import android.support.v4.app.ActivityCompat
import android.view.*
import android.widget.CompoundButton
import android.widget.FrameLayout
import android.widget.RelativeLayout
import com.ediposouza.teslesgendstracker.R
import com.ediposouza.teslesgendstracker.data.*
import com.ediposouza.teslesgendstracker.interactor.PrivateInteractor
import com.ediposouza.teslesgendstracker.interactor.PublicInteractor
import com.ediposouza.teslesgendstracker.ui.base.BaseActivity
import com.ediposouza.teslesgendstracker.util.MetricAction
import com.ediposouza.teslesgendstracker.util.MetricScreen
import com.ediposouza.teslesgendstracker.util.MetricsManager
import com.ediposouza.teslesgendstracker.util.load
import kotlinx.android.synthetic.main.activity_matches_statistics_class.*
import kotlinx.android.synthetic.main.itemcell_class.view.*
import kotlinx.android.synthetic.main.itemcell_text.view.*
import miguelbcr.ui.tableFixHeadesWrapper.TableFixHeaderAdapter
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.intentFor
import org.jetbrains.anko.itemsSequence
import org.jetbrains.anko.uiThread
import java.util.*

/**
 * Created by EdipoSouza on 1/3/17.
 */
class MatchesStatisticsClassActivity : BaseActivity() {

    companion object {

        private val EXTRA_CLASS = "classExtra"
        private val EXTRA_SEASON_ID = "SeasonIdExtra"
        private val EXTRA_MATCH_MODE = "MatchModeExtra"

        fun newIntent(context: Context, mode: MatchMode, season: Season?, cls: DeckClass): Intent {
            return context.intentFor<MatchesStatisticsClassActivity>(
                    EXTRA_MATCH_MODE to mode.ordinal,
                    EXTRA_SEASON_ID to (season?.id ?: 0),
                    EXTRA_CLASS to cls.ordinal)
        }

    }

    private val HEADER_FIRST by lazy { getString(R.string.match_vs) }

    private val playerClass by lazy { DeckClass.values()[intent.getIntExtra(EXTRA_CLASS, 0)] }
    private val matchMode by lazy { MatchMode.values()[intent.getIntExtra(EXTRA_MATCH_MODE, 0)] }
    private var seasons = listOf<Season>()
    private var currentSeason: Season? = null
    private var menuSeasons: SubMenu? = null
    private var showPercent: CompoundButton? = null

    var statisticsClassTableAdapter: StatisticsTableAdapter? = null
    var results: HashMap<MatchDeck, ArrayList<Match>> = HashMap()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_matches_statistics_class)
        val statusBarHeight = resources.getDimensionPixelSize(R.dimen.status_bar_height)
        if (Build.VERSION.SDK_INT == Build.VERSION_CODES.KITKAT) {
            val coverLP = statistics_class_cover.layoutParams as RelativeLayout.LayoutParams
            coverLP.height = coverLP.height - statusBarHeight
            statistics_class_cover.layoutParams = coverLP
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            val layoutParams = toolbar.layoutParams as CollapsingToolbarLayout.LayoutParams
            layoutParams.topMargin = statusBarHeight
            toolbar.layoutParams = layoutParams
        }
    }

    @Suppress("unchecked_cast")
    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        statisticsClassTableAdapter = StatisticsTableAdapter(this).apply {
            setFirstHeader(HEADER_FIRST)
            val classTotal: DeckClass? = null
            header = DeckClass.values().asList().plus(classTotal)
            setFirstBody(DeckClass.values().map { listOf(BodyItem(cls = it)) })
            loadingStatisticsData(this)
            setSection(listOf())
        }
        matches_statistics_class_table.adapter = statisticsClassTableAdapter
        with(playerClass) {
            statistics_class_name.text = getString(R.string.matches_class_title, name.toLowerCase().capitalize())
            statistics_class_cover.setImageResource(imageRes)
            statistics_class_attr1.setImageResource(attr1.imageRes)
            statistics_class_attr2.setImageResource(attr2.imageRes)
        }
        matches_statistics_class_ads_view.load()
        MetricsManager.trackScreen(MetricScreen.SCREEN_MATCHES_STATISTICS_CLASS())
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_percent, menu)
        menuInflater?.inflate(R.menu.menu_season, menu)
        getSeasons(menu?.findItem(R.id.menu_season))
        val menuPercent = menu?.findItem(R.id.menu_percent)
        showPercent = menuPercent?.actionView as CompoundButton
        showPercent?.setOnCheckedChangeListener { button, checked ->
            updateStatisticsData()
            MetricsManager.trackAction(MetricAction.ACTION_MATCH_STATISTICS_CLASS_WIN_RATE(checked))
        }
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item?.itemId) {
            android.R.id.home -> {
                ActivityCompat.finishAfterTransition(this)
                return true
            }
            R.id.menu_season_all -> filterSeason(null)
            else -> seasons.find { it.id == item?.itemId }?.apply { filterSeason(this) }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun filterSeason(season: Season?) {
        currentSeason = season
        val seasonId = season?.id ?: R.id.menu_season_all
        menuSeasons?.itemsSequence()?.forEach {
            it.setIcon(if (it.itemId == seasonId) R.drawable.ic_checked else 0)
        }
        getMatches()
        MetricsManager.trackAction(MetricAction.ACTION_MATCH_STATISTICS_CLASS_FILTER_SEASON(season))
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
                currentSeason = seasons.find { it.id == intent.getIntExtra(EXTRA_SEASON_ID, 0) }
                filterSeason(currentSeason)
            }
        }
    }

    private fun getMatches() {
        loadingStatisticsData()
        PrivateInteractor.getUserMatches(currentSeason) {
            results.clear()
            val classMatches = it.filter { it.player.cls == playerClass }
            classMatches.filter { it.mode == matchMode }.groupBy { it.player }.forEach {
                results.put(it.key, it.value as ArrayList)
            }
            updateStatisticsData()
        }
    }

    private fun loadingStatisticsData(tableAdapter: StatisticsTableAdapter? = statisticsClassTableAdapter) {
        tableAdapter?.setFirstBody(DeckClass.values().map { listOf(BodyItem(cls = it)) })
        tableAdapter?.body = mutableListOf<List<BodyItem>>().apply {
            DeckClass.values().forEach { myCls ->
                add(mutableListOf<BodyItem>().apply {
                    DeckClass.values().forEach { opponentCls ->
                        add(BodyItem())
                    }
                    add(BodyItem())
                })
            }
        }
    }

    private fun updateStatisticsData() {
        doAsync {
            val data = mutableListOf<List<BodyItem>>().apply {
                results.forEach { result ->
                    add(mutableListOf<BodyItem>().apply {
                        DeckClass.values().forEach { opponentCls ->
                            val matchesVsOpponent = result.value.filter { it.opponent.cls == opponentCls }
                            add(getResultBodyItem(matchesVsOpponent))
                        }
                        add(getResultBodyItem(result.value))
                    })
                }
            }
            uiThread {
                statisticsClassTableAdapter?.setFirstBody(results.map { listOf(BodyItem(it.key.name)) })
                statisticsClassTableAdapter?.body = data
                val allMatches = results.flatMap { it.value }
                val wins = allMatches.filter { it.win }.size
                val losses = allMatches.filter { !it.win }.size
                val winRate = calcWinRate(wins.toFloat(), losses.toFloat())
                matches_statistics_games.text = allMatches.size.toString()
                matches_statistics_wins.text = wins.toString()
                matches_statistics_losses.text = losses.toString()
                matches_statistics_win_rate.text = getString(R.string.match_statistics_percent,
                        if (winRate < 0) 0f else winRate)
            }
        }
    }

    private fun getResultBodyItem(matches: List<Match>): BodyItem {
        val result = matches.groupBy { it.win }
        val wins = result[true]?.size ?: 0
        val losses = result[false]?.size ?: 0
        return BodyItem(if (!(showPercent?.isChecked ?: false)) "$wins/$losses" else
            getString(R.string.match_statistics_percent, calcWinRate(wins.toFloat(), losses.toFloat())))
    }

    private fun calcWinRate(wins: Float, losses: Float): Float {
        val total = (wins + losses)
        return if (total == 0f) -1f else 100 / total * wins
    }

    class BodyItem(val result: String? = null, val cls: DeckClass? = null)

    class StatisticsTableAdapter(val context: Context) : TableFixHeaderAdapter<String, CellTextCenter,
            DeckClass, CellClass, List<BodyItem>, CellTextCenter, CellTextCenter, CellTextCenter>(context) {

        override fun inflateFirstHeader() = CellTextCenter(context)

        override fun inflateHeader() = CellClass(context)

        override fun inflateFirstBody() = CellTextCenter(context)

        override fun inflateBody() = CellTextCenter(context)

        override fun inflateSection() = CellTextCenter(context)

        override fun getHeaderHeight() = context.resources.getDimensionPixelSize(R.dimen.match_statistics_cell_height)

        override fun getHeaderWidths(): List<Int> {
            val headerWidth = context.resources.getDimensionPixelSize(R.dimen.match_statistics_class_header_width)
            val cellWidth = context.resources.getDimensionPixelSize(R.dimen.match_statistics_cell_width)
            val colWidths = mutableListOf(headerWidth)
            DeckClass.values().forEach { colWidths.add(cellWidth) }
            colWidths.add(headerWidth)
            return colWidths
        }

        override fun getBodyHeight() = context.resources.getDimensionPixelSize(R.dimen.match_statistics_cell_height)

        override fun isSection(items: List<List<BodyItem>>?, row: Int): Boolean = false

        override fun getSectionHeight() = 0

    }

    class CellClass(context: Context) : FrameLayout(context),
            TableFixHeaderAdapter.HeaderBinder<DeckClass> {

        init {
            LayoutInflater.from(context).inflate(R.layout.itemcell_class, this, true)
        }

        override fun bindHeader(cls: DeckClass?, col: Int) {
            bindClass(cls)
        }

        private fun bindClass(cls: DeckClass?) {
            with(rootView) {
                val attr1Visibility = if (cls != null) View.VISIBLE else View.GONE
                val attr2Visibility = if (cls?.attr2 != CardAttribute.NEUTRAL) attr1Visibility else View.GONE
                cell_class_attr1.visibility = attr1Visibility
                cell_class_attr2.visibility = attr2Visibility
                cell_class_attr1.setImageResource(cls?.attr1?.imageRes ?: 0)
                cell_class_attr2.setImageResource(cls?.attr2?.imageRes ?: 0)
                cell_total.visibility = if (cls == null) View.VISIBLE else View.GONE
            }
        }

    }

    class CellTextCenter(context: Context) : FrameLayout(context),
            TableFixHeaderAdapter.FirstHeaderBinder<String>,
            TableFixHeaderAdapter.FirstBodyBinder<List<BodyItem>>,
            TableFixHeaderAdapter.BodyBinder<List<BodyItem>>,
            TableFixHeaderAdapter.SectionBinder<List<BodyItem>> {

        init {
            LayoutInflater.from(context).inflate(R.layout.itemcell_text, this, true)
        }

        override fun bindFirstHeader(result: String) {
            bindResult(result)
        }

        override fun bindFirstBody(bodyItems: List<BodyItem>, row: Int) {
            bindResult(bodyItems[0].result)
        }

        override fun bindBody(bodyItems: List<BodyItem>, row: Int, col: Int) {
            bindResult(bodyItems[col].result)
        }

        private fun bindResult(result: String?) {
            with(rootView) {
                cell_text.text = if (result == "0/0" || result?.contains("-") ?: false) "-" else result
                cell_text.visibility = if (result == null) View.GONE else View.VISIBLE
                cell_progress.visibility = if (result == null) View.VISIBLE else View.GONE
            }
        }

        override fun bindSection(bodyItems: List<BodyItem>, row: Int, col: Int) {
        }

    }

}