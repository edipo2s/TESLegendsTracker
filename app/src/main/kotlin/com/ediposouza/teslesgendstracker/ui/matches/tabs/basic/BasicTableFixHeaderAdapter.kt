package com.ediposouza.teslesgendstracker.ui.matches.tabs.basic

import android.content.Context
import com.ediposouza.teslesgendstracker.R
import miguelbcr.ui.tableFixHeadesWrapper.TableFixHeaderAdapter
import java.util.*


/**
 * Created by miguel on 11/02/2016.
 */
class BasicTableFixHeaderAdapter(private val context: Context) : TableFixHeaderAdapter<String, BasicCellViewGroup, String, BasicCellViewGroup, List<String>, BasicCellViewGroup, BasicCellViewGroup, BasicCellViewGroup>(context) {

    override fun inflateFirstHeader(): BasicCellViewGroup {
        return BasicCellViewGroup(context)
    }

    override fun inflateHeader(): BasicCellViewGroup {
        return BasicCellViewGroup(context)
    }

    override fun inflateFirstBody(): BasicCellViewGroup {
        return BasicCellViewGroup(context)
    }

    override fun inflateBody(): BasicCellViewGroup {
        return BasicCellViewGroup(context)
    }

    override fun inflateSection(): BasicCellViewGroup {
        return BasicCellViewGroup(context)
    }

    override fun getHeaderWidths(): List<Int> {
        val headerWidths = ArrayList<Int>()

        // First header
        headerWidths.add(context.resources.getDimension(R.dimen._150dp).toInt())

        for (i in 0..19)
            headerWidths.add(context.resources.getDimension(R.dimen._100dp).toInt())

        return headerWidths
    }

    override fun getHeaderHeight(): Int {
        return context.resources.getDimension(R.dimen._40dp).toInt()
    }

    override fun getSectionHeight(): Int {
        return context.resources.getDimension(R.dimen._40dp).toInt()
    }

    override fun getBodyHeight(): Int {
        return context.resources.getDimension(R.dimen._40dp).toInt()
    }

    override fun isSection(items: List<List<String>>, row: Int): Boolean {
        return row % 10 == 0
    }
}
