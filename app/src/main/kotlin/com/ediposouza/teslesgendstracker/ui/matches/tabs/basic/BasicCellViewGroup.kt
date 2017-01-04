package com.ediposouza.teslesgendstracker.ui.matches.tabs.basic

import android.content.Context
import android.graphics.Typeface
import android.util.AttributeSet
import android.view.View
import android.widget.FrameLayout
import android.widget.TextView
import com.ediposouza.teslesgendstracker.R
import miguelbcr.ui.tableFixHeadesWrapper.TableFixHeaderAdapter


/**
 * Created by miguel on 09/02/2016.
 */
class BasicCellViewGroup : FrameLayout, TableFixHeaderAdapter.FirstHeaderBinder<String>, TableFixHeaderAdapter.HeaderBinder<String>, TableFixHeaderAdapter.FirstBodyBinder<List<String>>, TableFixHeaderAdapter.BodyBinder<List<String>>, TableFixHeaderAdapter.SectionBinder<List<String>> {

    private var ctx: Context? = null
    var textView: TextView? = null
    var vg_root: View? = null

    constructor(ctx: Context) : super(ctx) {
        this.ctx = ctx
    }

    constructor(ctx: Context, attrs: AttributeSet) : super(ctx, attrs) {
        this.ctx = ctx
    }

    init {
        View.inflate(context, R.layout.itemcell_result, this)
        textView = rootView.findViewById(R.id.cell_result) as TextView
        vg_root = rootView.findViewById(R.id.cell_bg)
    }

    override fun bindFirstHeader(headerName: String) {
        textView?.text = headerName
        textView?.setTypeface(null, Typeface.BOLD)
    }

    override fun bindHeader(headerName: String, column: Int) {
        textView?.text = headerName
        textView?.setTypeface(null, Typeface.BOLD)
    }

    override fun bindFirstBody(items: List<String>, row: Int) {
        textView?.text = items[0]
        textView?.setTypeface(null, Typeface.NORMAL)
    }

    override fun bindBody(items: List<String>, row: Int, column: Int) {
        textView?.text = items[column + 1]
        textView?.setTypeface(null, Typeface.NORMAL)
    }

    override fun bindSection(item: List<String>, row: Int, column: Int) {
        textView?.text = if (column == 0) "Section" else ""
    }
}
