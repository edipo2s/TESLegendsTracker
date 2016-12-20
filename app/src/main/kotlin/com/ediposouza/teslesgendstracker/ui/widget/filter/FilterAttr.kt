package com.ediposouza.teslesgendstracker.ui.widget.filter

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import com.ediposouza.teslesgendstracker.R
import com.ediposouza.teslesgendstracker.data.Attribute
import kotlinx.android.synthetic.main.widget_attributes_filter.view.*

/**
 * Created by EdipoSouza on 11/2/16.
 */
open class FilterAttr(ctx: Context?, attrs: AttributeSet?, defStyleAttr: Int) :
        LinearLayout(ctx, attrs, defStyleAttr) {

    var deckMode: Boolean = false
        set(value) {
            field = value
            if (!value) {
                selectAttr(Attribute.STRENGTH, true)
            } else {
                rootView.attr_filter_dual.visibility = if (value) View.GONE else View.VISIBLE
                rootView.attr_filter_dual_indicator.visibility = if (value) View.GONE else View.VISIBLE
                rootView.attr_filter_neutral.visibility = if (value) View.GONE else View.VISIBLE
                rootView.attr_filter_neutral_indicator.visibility = if (value) View.GONE else View.VISIBLE
            }
        }

    var filterClick: ((Attribute) -> Unit)? = null
    var lastAttrSelected: Attribute = Attribute.STRENGTH

    init {
        inflate(context, R.layout.widget_attributes_filter, rootView as ViewGroup)
        val a = context.obtainStyledAttributes(attrs, R.styleable.FilterAttr)
        deckMode = a.getBoolean(R.styleable.FilterAttr_deckMode, false)
        a.recycle()
        if (!isInEditMode) {
            rootView.attr_filter_strength?.setOnClickListener { attrClick(Attribute.STRENGTH, false) }
            rootView.attr_filter_intelligence?.setOnClickListener { attrClick(Attribute.INTELLIGENCE, false) }
            rootView.attr_filter_willpower?.setOnClickListener { attrClick(Attribute.WILLPOWER, true) }
            rootView.attr_filter_agility?.setOnClickListener { attrClick(Attribute.AGILITY, true) }
            rootView.attr_filter_endurance?.setOnClickListener { attrClick(Attribute.ENDURANCE, true) }
            rootView.attr_filter_dual?.setOnClickListener { attrClick(Attribute.DUAL, false) }
            rootView.attr_filter_neutral?.setOnClickListener { attrClick(Attribute.NEUTRAL, false) }
        }
    }

    constructor(ctx: Context?) : this(ctx, null, 0) {
    }

    constructor(ctx: Context?, attrs: AttributeSet) : this(ctx, attrs, 0) {
    }

    open protected fun attrClick(attr: Attribute, lockable: Boolean) {
        filterClick?.invoke(attr)
    }

    open fun selectAttr(attr: Attribute, only: Boolean) {
        lastAttrSelected = attr
        updateVisibility(rootView.attr_filter_strength_indicator, attr == Attribute.STRENGTH, only)
        updateVisibility(rootView.attr_filter_intelligence_indicator, attr == Attribute.INTELLIGENCE, only)
        updateVisibility(rootView.attr_filter_willpower_indicator, attr == Attribute.WILLPOWER, only)
        updateVisibility(rootView.attr_filter_agility_indicator, attr == Attribute.AGILITY, only)
        updateVisibility(rootView.attr_filter_endurance_indicator, attr == Attribute.ENDURANCE, only)
        updateVisibility(rootView.attr_filter_dual_indicator, attr == Attribute.DUAL, only)
        updateVisibility(rootView.attr_filter_neutral_indicator, attr == Attribute.NEUTRAL, only)
    }

    open fun unSelectAttr(attr: Attribute) {
        when (attr) {
            Attribute.STRENGTH -> rootView.attr_filter_strength_indicator.visibility = View.INVISIBLE
            Attribute.INTELLIGENCE -> rootView.attr_filter_intelligence_indicator.visibility = View.INVISIBLE
            Attribute.WILLPOWER -> rootView.attr_filter_willpower_indicator.visibility = View.INVISIBLE
            Attribute.AGILITY -> rootView.attr_filter_agility_indicator.visibility = View.INVISIBLE
            Attribute.ENDURANCE -> rootView.attr_filter_endurance_indicator.visibility = View.INVISIBLE
            Attribute.DUAL -> rootView.attr_filter_dual_indicator.visibility = View.INVISIBLE
            Attribute.NEUTRAL -> rootView.attr_filter_neutral_indicator.visibility = View.INVISIBLE
        }
    }

    open fun isAttrSelected(attr: Attribute): Boolean {
        return when (attr) {
            Attribute.STRENGTH -> rootView.attr_filter_strength_indicator.visibility == View.VISIBLE
            Attribute.INTELLIGENCE -> rootView.attr_filter_intelligence_indicator.visibility == View.VISIBLE
            Attribute.WILLPOWER -> rootView.attr_filter_willpower_indicator.visibility == View.VISIBLE
            Attribute.AGILITY -> rootView.attr_filter_agility_indicator.visibility == View.VISIBLE
            Attribute.ENDURANCE -> rootView.attr_filter_endurance_indicator.visibility == View.VISIBLE
            Attribute.DUAL -> rootView.attr_filter_dual_indicator.visibility == View.VISIBLE
            Attribute.NEUTRAL -> rootView.attr_filter_neutral_indicator.visibility == View.VISIBLE
            else -> false
        }
    }

    fun getSelectedAttrs(): List<Attribute> {
        val attrs = Attribute.values().filter { isAttrSelected(it) }
        return if (deckMode) attrs.plus(Attribute.NEUTRAL) else attrs
    }

    protected fun updateVisibility(v: View, show: Boolean, only: Boolean) {
        v.visibility = if (show) View.VISIBLE else if (only) View.INVISIBLE else v.visibility
    }

}