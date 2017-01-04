package com.ediposouza.teslesgendstracker.ui.matches.tabs.basic

import android.content.Context
import android.support.design.widget.Snackbar
import android.support.v4.content.ContextCompat
import com.ediposouza.teslesgendstracker.R
import com.inqbarna.tablefixheaders.adapters.BaseTableAdapter
import miguelbcr.ui.tableFixHeadesWrapper.TableFixHeaderAdapter
import java.util.*

/**
 * Created by miguel on 12/02/2016.
 */
class BasicTableFixHeader(private val context: Context) {

    val instance: BaseTableAdapter
        get() {
            val adapter = BasicTableFixHeaderAdapter(context)
            val body = body

            adapter.setFirstHeader("FH")
            adapter.header = header
            adapter.setFirstBody(body)
            adapter.body = body
            adapter.setSection(body)

            setListeners(adapter)

            return adapter
        }

    private fun setListeners(adapter: BasicTableFixHeaderAdapter) {
        val clickListenerHeader = TableFixHeaderAdapter.ClickListener<String, BasicCellViewGroup> { s, viewGroup, row, column ->
            viewGroup.vg_root?.setBackgroundColor(ContextCompat.getColor(context, R.color.colorAccent))
            Snackbar.make(viewGroup, "Click on " + viewGroup.textView?.text + " (" + row + "," + column + ")", Snackbar.LENGTH_SHORT).show()
        }
        val clickListenerBody = TableFixHeaderAdapter.ClickListener<List<String>, BasicCellViewGroup> { array, viewGroup, row, column ->
            viewGroup.vg_root?.setBackgroundColor(ContextCompat.getColor(context, R.color.colorYellow))
            Snackbar.make(viewGroup, "Click on " + viewGroup.textView?.text + " (" + row + "," + column + ")", Snackbar.LENGTH_SHORT).show()
        }

        val longClickListenerHeader = TableFixHeaderAdapter.LongClickListener<String, BasicCellViewGroup> { s, viewGroup, row, column ->
            viewGroup.vg_root?.setBackgroundColor(ContextCompat.getColor(context, R.color.colorPrimary))
            Snackbar.make(viewGroup, "LongClick on " + viewGroup.textView?.text + " (" + row + "," + column + ")", Snackbar.LENGTH_SHORT).show()
        }

        val longClickListenerBody = TableFixHeaderAdapter.LongClickListener<List<String>, BasicCellViewGroup> { array, viewGroup, row, column ->
            viewGroup.vg_root?.setBackgroundColor(ContextCompat.getColor(context, R.color.colorBlue))
            Snackbar.make(viewGroup, "LongClick on " + viewGroup.textView?.text + " (" + row + "," + column + ")", Snackbar.LENGTH_SHORT).show()
        }

        adapter.setClickListenerFirstHeader(clickListenerHeader)
        adapter.setLongClickListenerFirstHeader(longClickListenerHeader)
        adapter.setClickListenerHeader(clickListenerHeader)
        adapter.setLongClickListenerHeader(longClickListenerHeader)
        adapter.setClickListenerFirstBody(clickListenerBody)
        adapter.setLongClickListenerFirstBody(longClickListenerBody)
        adapter.setClickListenerBody(clickListenerBody)
        adapter.setLongClickListenerBody(longClickListenerBody)
        adapter.setClickListenerSection(clickListenerBody)
        adapter.setLongClickListenerSection(longClickListenerBody)
    }

    private val header: List<String>
        get() {
            val header = ArrayList<String>()

            for (i in 0..19)
                header.add("H " + (i + 1))

            return header
        }

    private val body: List<List<String>>
        get() {
            val rows = ArrayList<List<String>>()

            for (row in 1..100) {
                val cols = ArrayList<String>()

                for (col in 0..29) {
                    val type = if (col == 0) "FB" else "B"
                    cols.add("$type ($row, $col)")
                }

                rows.add(cols)
            }

            return rows
        }
}
