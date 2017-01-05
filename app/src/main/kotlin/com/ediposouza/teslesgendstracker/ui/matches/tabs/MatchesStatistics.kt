package com.ediposouza.teslesgendstracker.ui.matches.tabs

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import com.ediposouza.teslesgendstracker.R
import com.ediposouza.teslesgendstracker.data.Attribute
import com.ediposouza.teslesgendstracker.data.Class
import com.ediposouza.teslesgendstracker.data.Match
import com.ediposouza.teslesgendstracker.ui.base.BaseFragment
import com.ediposouza.teslesgendstracker.util.TestUtils
import com.ediposouza.teslesgendstracker.util.inflate
import kotlinx.android.synthetic.main.fragment_matches_statistics.*
import kotlinx.android.synthetic.main.itemcell_class.view.*
import kotlinx.android.synthetic.main.itemcell_text.view.*
import miguelbcr.ui.tableFixHeadesWrapper.TableFixHeaderAdapter
import java.util.*

/**
 * Created by EdipoSouza on 1/3/17.
 */
class MatchesStatistics : BaseFragment() {

    val results = HashMap(Class.values().map { it to mutableListOf<Match>() }.toMap())

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return container?.inflate(R.layout.fragment_matches_statistics)
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        matches_statistics_table.adapter = StatisticsTableAdapter(context).apply {
            setFirstHeader("Vs")
            val classTotal: Class? = null
            header = Class.values().asList().plus(classTotal)
            setFirstBody(Class.values().map { listOf(BodyItem(it)) }.plus(listOf(listOf(BodyItem()))))
            updateStatisticsData(this)
            setSection(listOf())
        }
        getMatches()
    }

    private fun getMatches() {
        val matches = TestUtils.getTestMatches()
        matches.groupBy { it.player.cls }.forEach {
            results[it.key]?.addAll(it.value)
            updateStatisticsData(matches_statistics_table.adapter as StatisticsTableAdapter)
        }
    }

    private fun updateStatisticsData(statisticsTableAdapter: StatisticsTableAdapter) {
        statisticsTableAdapter.body = mutableListOf<List<BodyItem>>().apply {
            Class.values().forEach { myCls ->
                add(mutableListOf<BodyItem>().apply {
                    val resByMyCls = results[myCls]!!
                    Class.values().forEach { opponentCls ->
                        val matchesVsOpponent = resByMyCls.filter { it.opponent.cls == opponentCls }
                        add(getWinLossBodyItem(matchesVsOpponent))
                    }
                    add(getWinLossBodyItem(resByMyCls))
                })
            }
            val allMatches = results.flatMap { it.value }
            add(mutableListOf<BodyItem>().apply {
                Class.values().forEach {
                    val resByOpponent = allMatches.groupBy { it.opponent.cls }[it] ?: listOf()
                    add(getWinLossBodyItem(resByOpponent))
                }
                add(getWinLossBodyItem(allMatches))
            })
        }
    }

    private fun getWinLossBodyItem(matches: List<Match>): BodyItem {
        val result = matches.groupBy { it.win }
        return BodyItem(result = "${result[true]?.size ?: 0}/${result[false]?.size ?: 0}")
    }

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
            rootView.cell_text.text = result
        }

        override fun bindBody(bodyItems: List<BodyItem>, row: Int, col: Int) {
            rootView.cell_text.text = bodyItems[col].result
        }

        override fun bindSection(bodyItems: List<BodyItem>, row: Int, col: Int) {
        }

    }

}