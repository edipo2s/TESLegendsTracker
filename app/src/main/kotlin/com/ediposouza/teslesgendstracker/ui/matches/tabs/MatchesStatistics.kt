package com.ediposouza.teslesgendstracker.ui.matches.tabs

import android.content.Context
import android.os.Bundle
import android.view.*
import android.widget.FrameLayout
import android.widget.Switch
import com.ediposouza.teslesgendstracker.R
import com.ediposouza.teslesgendstracker.data.Attribute
import com.ediposouza.teslesgendstracker.data.Class
import com.ediposouza.teslesgendstracker.data.Match
import com.ediposouza.teslesgendstracker.interactor.PrivateInteractor
import com.ediposouza.teslesgendstracker.ui.base.BaseFragment
import com.ediposouza.teslesgendstracker.util.inflate
import kotlinx.android.synthetic.main.fragment_matches_statistics.*
import kotlinx.android.synthetic.main.itemcell_class.view.*
import kotlinx.android.synthetic.main.itemcell_text.view.*
import miguelbcr.ui.tableFixHeadesWrapper.TableFixHeaderAdapter
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread
import java.util.*

/**
 * Created by EdipoSouza on 1/3/17.
 */
class MatchesStatistics : BaseFragment() {

    private val HEADER_FIRST = "Vs"

    private var showPercent: Switch? = null

    var statisticsTableAdapter: StatisticsTableAdapter? = null
    val results = HashMap(Class.values().map { it to mutableListOf<Match>() }.toMap())

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return container?.inflate(R.layout.fragment_matches_statistics)
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setHasOptionsMenu(true)
        statisticsTableAdapter = StatisticsTableAdapter(context).apply {
            setFirstHeader(HEADER_FIRST)
            val classTotal: Class? = null
            header = Class.values().asList().plus(classTotal)
            setFirstBody(Class.values().map { listOf(BodyItem(it)) }.plus(listOf(listOf(BodyItem()))))
            body = mutableListOf<List<BodyItem>>().apply {
                Class.values().forEach { myCls ->
                    add(mutableListOf<BodyItem>().apply {
                        Class.values().forEach { opponentCls ->
                            add(BodyItem())
                        }
                        add(BodyItem())
                    })
                }
                add(mutableListOf<BodyItem>().apply {
                    Class.values().forEach {
                        add(BodyItem())
                    }
                    add(BodyItem())
                })
            }
            setSection(listOf())
        }
        matches_statistics_table.adapter = statisticsTableAdapter
        getMatches()
    }

    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
        menu?.clear()
        inflater?.inflate(R.menu.menu_percent, menu)
        inflater?.inflate(R.menu.menu_season, menu)
        showPercent = menu?.findItem(R.id.menu_percent)?.actionView as Switch
        showPercent?.setOnCheckedChangeListener { button, checked -> updateStatisticsData() }
        super.onCreateOptionsMenu(menu, inflater)
    }

    private fun getMatches() {
        PrivateInteractor().getUserMatches {
            it.groupBy { it.player.cls }.forEach {
                results[it.key]?.addAll(it.value)
                updateStatisticsData()
            }
        }
    }

    private fun updateStatisticsData() {
        doAsync {
            val data = mutableListOf<List<BodyItem>>().apply {
                Class.values().forEach { myCls ->
                    add(mutableListOf<BodyItem>().apply {
                        val resByMyCls = results[myCls]!!
                        Class.values().forEach { opponentCls ->
                            val matchesVsOpponent = resByMyCls.filter { it.opponent.cls == opponentCls }
                            add(getResultBodyItem(matchesVsOpponent))
                        }
                        add(getResultBodyItem(resByMyCls))
                    })
                }
                val allMatches = results.flatMap { it.value }
                add(mutableListOf<BodyItem>().apply {
                    Class.values().forEach {
                        val resByOpponent = allMatches.groupBy { it.opponent.cls }[it] ?: listOf()
                        add(getResultBodyItem(resByOpponent))
                    }
                    add(getResultBodyItem(allMatches))
                })
            }
            uiThread {
                statisticsTableAdapter?.body = data
            }
        }
    }

    private fun getResultBodyItem(matches: List<Match>): BodyItem {
        val result = matches.groupBy { it.win }
        val wins = result[true]?.size ?: 0
        val losses = result[false]?.size ?: 0
        return BodyItem(result = if (!(showPercent?.isChecked ?: false)) "$wins/$losses" else
            getString(R.string.match_statistics_percent, calcWinRate(wins.toFloat(), losses.toFloat())))
    }

    private fun calcWinRate(losses: Float, wins: Float) = losses / (wins + losses) * 100

    class BodyItem(val cls: Class? = null, val result: String? = null)

    class StatisticsTableAdapter(val context: Context) : TableFixHeaderAdapter<String, CellTextCenter,
            Class, CellClass, List<BodyItem>, CellClass, CellTextCenter, CellTextCenter>(context) {

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
            Class.values().forEach { colWidths.add(cellWidth) }
            colWidths.add(headerWidth)
            return colWidths
        }

        override fun getBodyHeight() = context.resources.getDimensionPixelSize(R.dimen.match_statistics_cell_height)

        override fun isSection(items: List<List<BodyItem>>?, row: Int): Boolean = false

        override fun getSectionHeight() = 0

    }

    class CellClass(context: Context) : FrameLayout(context),
            TableFixHeaderAdapter.HeaderBinder<Class>,
            TableFixHeaderAdapter.FirstBodyBinder<List<BodyItem>> {

        init {
            LayoutInflater.from(context).inflate(R.layout.itemcell_class, this, true)
        }

        override fun bindHeader(cls: Class?, col: Int) {
            bindClass(cls)
        }

        override fun bindFirstBody(bodyItems: List<BodyItem>, row: Int) {
            bindClass(bodyItems[0].cls)
        }

        private fun bindClass(cls: Class?) {
            with(rootView) {
                val attr1Visibility = if (cls != null) View.VISIBLE else View.GONE
                val attr2Visibility = if (cls?.attr2 != Attribute.NEUTRAL) attr1Visibility else View.GONE
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
            TableFixHeaderAdapter.BodyBinder<List<BodyItem>>,
            TableFixHeaderAdapter.SectionBinder<List<BodyItem>> {

        init {
            LayoutInflater.from(context).inflate(R.layout.itemcell_text, this, true)
        }

        override fun bindFirstHeader(result: String) {
            bindResult(result)
        }

        override fun bindBody(bodyItems: List<BodyItem>, row: Int, col: Int) {
            bindResult(bodyItems[col].result)
        }

        private fun bindResult(result: String?) {
            with(rootView) {
                cell_text.text = result
                cell_text.visibility = if (result == null) View.GONE else View.VISIBLE
                cell_progress.visibility = if (result == null) View.VISIBLE else View.GONE
            }
        }

        override fun bindSection(bodyItems: List<BodyItem>, row: Int, col: Int) {
        }

    }

}