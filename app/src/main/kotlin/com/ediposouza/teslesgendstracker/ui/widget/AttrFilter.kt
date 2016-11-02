package com.ediposouza.teslesgendstracker.ui.widget

import android.content.Context
import android.util.AttributeSet
import android.widget.LinearLayout
import com.ediposouza.teslesgendstracker.R
import com.ediposouza.teslesgendstracker.data.Attribute
import kotlinx.android.synthetic.main.widget_attributes.view.*

/**
 * Created by EdipoSouza on 11/2/16.
 */
class AttrFilter : LinearLayout {

    var filterClick: ((Attribute) -> Unit)? = null

    constructor(ctx: Context?) : super(ctx) {
        init()
    }

    constructor(ctx: Context?, attrs: AttributeSet) : super(ctx, attrs) {
        init()
    }

    constructor(ctx: Context?, attrs: AttributeSet, defStyleAttr: Int) : super(ctx, attrs, defStyleAttr) {
        init()
    }

    fun init() {
        inflate(context, R.layout.widget_attributes, this)
        if (isInEditMode) {
            return
        }
        rootView.attr_filter_strength?.setOnClickListener { filterClick?.invoke(Attribute.STRENGTH) }
        rootView.attr_filter_intelligence?.setOnClickListener { filterClick?.invoke(Attribute.INTELLIGENCE) }
        rootView.attr_filter_willpower?.setOnClickListener { filterClick?.invoke(Attribute.WILLPOWER) }
        rootView.attr_filter_agility?.setOnClickListener { filterClick?.invoke(Attribute.AGILITY) }
        rootView.attr_filter_endurance?.setOnClickListener { filterClick?.invoke(Attribute.ENDURANCE) }
        rootView.attr_filter_dual?.setOnClickListener { filterClick?.invoke(Attribute.DUAL) }
        rootView.attr_filter_neutral?.setOnClickListener { filterClick?.invoke(Attribute.NEUTRAL) }
    }

}