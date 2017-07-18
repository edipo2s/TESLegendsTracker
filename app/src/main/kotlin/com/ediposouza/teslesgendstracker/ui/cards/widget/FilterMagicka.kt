package com.ediposouza.teslesgendstracker.ui.cards.widget

import android.content.Context
import android.util.AttributeSet
import android.widget.LinearLayout
import com.ediposouza.teslesgendstracker.R
import kotlinx.android.synthetic.main.widget_magicka_filter.view.*

/**
 * Created by EdipoSouza on 11/2/16.
 */
class FilterMagicka(ctx: Context?, attrs: AttributeSet?, defStyleAttr: Int) :
        LinearLayout(ctx, attrs, defStyleAttr) {

    var filterClick: ((Int) -> Unit)? = null
    var collapseOnClick: Boolean = true

    init {
        inflate(context, R.layout.widget_magicka_filter, this)
        if (!isInEditMode) {
            with(rootView) {
                magicka_filter_0?.setOnClickListener { magickaClick(0) }
                magicka_filter_1?.setOnClickListener { magickaClick(1) }
                magicka_filter_2?.setOnClickListener { magickaClick(2) }
                magicka_filter_3?.setOnClickListener { magickaClick(3) }
                magicka_filter_4?.setOnClickListener { magickaClick(4) }
                magicka_filter_5?.setOnClickListener { magickaClick(5) }
                magicka_filter_6?.setOnClickListener { magickaClick(6) }
                magicka_filter_7plus?.setOnClickListener { magickaClick(7) }
            }
            magicka_filter.setOnMenuButtonClickListener {
                when (magicka_filter.isOpened) {
                    true -> magickaMenuOpenedClick()
                    false -> magickaMenuClosedClick()
                }
            }
        }
    }

    constructor(ctx: Context?) : this(ctx, null, 0)

    constructor(ctx: Context?, attrs: AttributeSet) : this(ctx, attrs, 0)

    fun open() {
        rootView.magicka_filter.open(true)
    }

    fun close() {
        magicka_filter.close(true)
    }

    private fun magickaMenuOpenedClick() {
        if (collapseOnClick) {
            close()
        } else {
            magickaClick(-1)
        }
    }

    private fun magickaMenuClosedClick() {
        if (magicka_filter.tag == true)
            magickaClick(-1)
        else
            open()
    }

    private fun magickaClick(magicka: Int) {
        filterClick?.invoke(magicka)
        val icon = R.drawable.ic_magicka.takeIf { magicka == -1 } ?: R.drawable.ic_magicka_clear
        rootView.magicka_filter.apply {
            tag = magicka != -1
            if (collapseOnClick) {
                close()
            }
            menuIconView.setImageResource(icon)
        }
    }

}