package com.ediposouza.teslesgendstracker.ui.matches.tabs

import android.content.Context
import android.os.Bundle
import android.support.v4.app.ActivityOptionsCompat
import android.support.v4.content.ContextCompat
import android.support.v4.util.Pair
import android.view.*
import android.widget.CompoundButton
import android.widget.FrameLayout
import com.ediposouza.teslesgendstracker.R
import com.ediposouza.teslesgendstracker.data.*
import com.ediposouza.teslesgendstracker.interactor.PrivateInteractor
import com.ediposouza.teslesgendstracker.ui.base.BaseFragment
import com.ediposouza.teslesgendstracker.ui.matches.CmdFilterMode
import com.ediposouza.teslesgendstracker.ui.matches.CmdFilterSeason
import com.ediposouza.teslesgendstracker.ui.matches.CmdUpdateMatches
import com.ediposouza.teslesgendstracker.ui.matches.MatchesStatisticsClassActivity
import com.ediposouza.teslesgendstracker.util.MetricAction
import com.ediposouza.teslesgendstracker.util.MetricsManager
import com.ediposouza.teslesgendstracker.util.inflate
import kotlinx.android.synthetic.main.fragment_matches_statistics.*
import kotlinx.android.synthetic.main.itemcell_class.view.*
import kotlinx.android.synthetic.main.itemcell_text.view.*
import miguelbcr.ui.tableFixHeadesWrapper.TableFixHeaderAdapter
import org.greenrobot.eventbus.Subscribe
import org.jetbrains.anko.childrenSequence
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread

/**
 * Created by EdipoSouza on 1/3/17.
 */
class MatchesStatisticsFragment : BaseFragment() {

    private val HEADER_FIRST by lazy { getString(R.string.match_vs) }
    private val attr1TransitionName: String by lazy { getString(R.string.deck_attr1_transition_name) }
    private val attr2TransitionName: String by lazy { getString(R.string.deck_attr2_transition_name) }

    private var currentMatchMode = MatchMode.RANKED
    private var currentSeason: Season? = null
    private var selectedClass: DeckClass? = null
    private var showPercent: CompoundButton? = null

    var statisticsTableAdapter: StatisticsTableAdapter? = null
    var results: Map<DeckClass, MutableList<Match>> = mutableMapOf()

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return container?.inflate(R.layout.fragment_matches_statistics)
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setHasOptionsMenu(true)
        statisticsTableAdapter = StatisticsTableAdapter(context).apply {
            setFirstHeader(HEADER_FIRST)
            val classTotal: DeckClass? = null
            header = DeckClass.values().asList().plus(classTotal)
            setFirstBody(DeckClass.values().map { listOf(BodyItem(cls = it)) }.plus(listOf(listOf(BodyItem()))))
            loadingStatisticsData(this)
            setSection(listOf())
            setClickListenerFirstBody { rowItems, view, row, col -> selectRow(row) }
            setClickListenerBody { rowItems, view, row, col -> selectRow(row) }
        }
        matches_statistics_table.adapter = statisticsTableAdapter
        getMatches()
    }

    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
        val menuPercent = menu?.findItem(R.id.menu_percent)
        menuPercent?.isVisible = true
        showPercent = menuPercent?.actionView as CompoundButton
        showPercent?.setOnCheckedChangeListener { button, checked ->
            updateStatisticsData()
            MetricsManager.trackAction(MetricAction.ACTION_MATCH_STATISTICS_WIN_RATE(checked))
        }
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onResume() {
        super.onResume()
        selectRow(-1)
    }

    private fun selectRow(row: Int) {
        selectedClass = if (row >= 0 && row < DeckClass.values().size) DeckClass.values()[row] else null
        updateStatisticsData()
        statisticsTableAdapter?.setFirstBody(DeckClass.values().map { listOf(BodyItem(null, it, it == selectedClass)) }
                .plus(listOf(listOf(BodyItem()))))
        if (selectedClass != null) {
            val classView = matches_statistics_table.childrenSequence()
                    .filter {
                        it.getTag(com.inqbarna.tablefixheaders.R.id.tag_row) == row &&
                                it.getTag(com.inqbarna.tablefixheaders.R.id.tag_type_view) == 2
                    }.first()
            startActivity(MatchesStatisticsClassActivity.newIntent(context, currentMatchMode, currentSeason,
                    selectedClass!!), ActivityOptionsCompat.makeSceneTransitionAnimation(activity,
                    Pair(classView.cell_class_attr1 as View, attr1TransitionName),
                    Pair(classView.cell_class_attr2 as View, attr2TransitionName)).toBundle())
            MetricsManager.trackAction(MetricAction.ACTION_MATCH_STATISTICS_CLASS(selectedClass!!))
        }
    }

    private fun getMatches() {
        loadingStatisticsData()
        PrivateInteractor.getUserMatches(currentSeason) {
            it.filter { it.mode == currentMatchMode }.groupBy { it.player.cls }.forEach {
                results[it.key]?.addAll(it.value)
            }
            updateStatisticsData()
        }
    }

    private fun loadingStatisticsData(tableAdapter: StatisticsTableAdapter? = statisticsTableAdapter) {
        results = DeckClass.values().map { it to mutableListOf<Match>() }.toMap()
        tableAdapter?.body = mutableListOf<List<BodyItem>>().apply {
            DeckClass.values().forEach { myCls ->
                add(mutableListOf<BodyItem>().apply {
                    DeckClass.values().forEach { opponentCls ->
                        add(BodyItem())
                    }
                    add(BodyItem())
                })
            }
            add(mutableListOf<BodyItem>().apply {
                DeckClass.values().forEach {
                    add(BodyItem())
                }
                add(BodyItem())
            })
        }
    }

    private fun updateStatisticsData() {
        doAsync {
            val data = mutableListOf<List<BodyItem>>().apply {
                DeckClass.values().forEach { myCls ->
                    add(mutableListOf<BodyItem>().apply {
                        val resByMyCls = results[myCls]!!
                        DeckClass.values().forEach { opponentCls ->
                            val matchesVsOpponent = resByMyCls.filter { it.opponent.cls == opponentCls }
                            add(getResultBodyItem(matchesVsOpponent, myCls == selectedClass))
                        }
                        add(getResultBodyItem(resByMyCls, myCls == selectedClass))
                    })
                }
                val allMatches = results.flatMap { it.value }
                add(mutableListOf<BodyItem>().apply {
                    DeckClass.values().forEach {
                        val resByOpponent = allMatches.groupBy { it.opponent.cls }[it] ?: listOf()
                        add(getResultBodyItem(resByOpponent, false))
                    }
                    add(getResultBodyItem(allMatches, false))
                })
            }
            uiThread {
                statisticsTableAdapter?.body = data
            }
        }
    }

    private fun getResultBodyItem(matches: List<Match>, cellSelected: Boolean): BodyItem {
        val result = matches.groupBy { it.win }
        val wins = result[true]?.size ?: 0
        val losses = result[false]?.size ?: 0
        val resultText = if (!(showPercent?.isChecked ?: false)) "$wins/$losses" else
            getString(R.string.match_statistics_percent, calcWinRate(wins.toFloat(), losses.toFloat()))
        return BodyItem(resultText, selected = cellSelected)
    }

    private fun calcWinRate(wins: Float, losses: Float): Float {
        val total = (wins + losses)
        return if (total == 0f) -1f else 100 / total * wins
    }

    @Subscribe
    @Suppress("unused")
    fun onCmdFilterMode(cmdFilterMode: CmdFilterMode) {
        currentMatchMode = cmdFilterMode.mode
        if (isFragmentSelected) {
            getMatches()
        }
    }

    @Subscribe
    @Suppress("unused")
    fun onCmdFilterSeason(cmdFilterSeason: CmdFilterSeason) {
        currentSeason = cmdFilterSeason.season
        if (isFragmentSelected) {
            getMatches()
        }
    }

    @Subscribe
    @Suppress("unused")
    fun onCmdUpdateMatches(cmdUpdateMatches: CmdUpdateMatches) {
        if (isFragmentSelected) {
            getMatches()
        }
    }

    class BodyItem(val result: String? = null, val cls: DeckClass? = null, val selected: Boolean = false)

    class StatisticsTableAdapter(val context: Context) : TableFixHeaderAdapter<String, CellTextCenter,
            DeckClass, CellClass, List<BodyItem>, CellClass, CellTextCenter, CellTextCenter>(context) {

        override fun inflateFirstHeader() = CellTextCenter(context)

        override fun inflateHeader() = CellClass(context)

        override fun inflateFirstBody() = CellClass(context)

        override fun inflateBody() = CellTextCenter(context)

        override fun inflateSection() = CellTextCenter(context)

        override fun getHeaderHeight() = context.resources.getDimensionPixelSize(R.dimen.match_statistics_cell_height)

        override fun getHeaderWidths(): List<Int> {
            val headerWidth = context.resources.getDimensionPixelSize(R.dimen.match_statistics_header_width)
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
            TableFixHeaderAdapter.HeaderBinder<DeckClass>,
            TableFixHeaderAdapter.FirstBodyBinder<List<BodyItem>> {

        init {
            LayoutInflater.from(context).inflate(R.layout.itemcell_class, this, true)
        }

        override fun bindHeader(cls: DeckClass?, col: Int) {
            bindClass(cls, false)
        }

        override fun bindFirstBody(bodyItems: List<BodyItem>, row: Int) {
            val bodyItem = bodyItems[0]
            bindClass(bodyItem.cls, bodyItem.selected)
        }

        private fun bindClass(cls: DeckClass?, selected: Boolean) {
            with(rootView) {
                val attr1Visibility = if (cls != null) View.VISIBLE else View.GONE
                val attr2Visibility = if (cls?.attr2 != CardAttribute.NEUTRAL) attr1Visibility else View.GONE
                cell_class_attr1.visibility = attr1Visibility
                cell_class_attr2.visibility = attr2Visibility
                cell_class_attr1.setImageResource(cls?.attr1?.imageRes ?: 0)
                cell_class_attr2.setImageResource(cls?.attr2?.imageRes ?: 0)
                cell_total.visibility = if (cls == null) View.VISIBLE else View.GONE
                val cellColor = if (selected) R.color.colorAccent else android.R.color.transparent
                setBackgroundColor(ContextCompat.getColor(context, cellColor))
            }
        }

    }

    class CellTextCenter(context: Context) : FrameLayout(context),
            TableFixHeaderAdapter.FirstHeaderBinder<String>,
            TableFixHeaderAdapter.BodyBinder<List<BodyItem>>,
            TableFixHeaderAdapter.SectionBinder<List<BodyItem>> {

        init {
            LayoutInflater.from(context).inflate(R.layout.itemcell_text, this, true)
        }

        override fun bindFirstHeader(result: String) {
            bindResult(result, false)
        }

        override fun bindBody(bodyItems: List<BodyItem>, row: Int, col: Int) {
            val bodyItem = bodyItems[col]
            bindResult(bodyItem.result, bodyItem.selected)
        }

        private fun bindResult(result: String?, selected: Boolean) {
            with(rootView) {
                cell_text.text = if (result == "0/0" || result?.contains("-") ?: false) "-" else result
                cell_text.visibility = if (result == null) View.GONE else View.VISIBLE
                cell_progress.visibility = if (result == null) View.VISIBLE else View.GONE
                val cellColor = if (selected) R.color.colorAccent else android.R.color.transparent
                setBackgroundColor(ContextCompat.getColor(context, cellColor))
            }
        }

        override fun bindSection(bodyItems: List<BodyItem>, row: Int, col: Int) {
        }

    }

}