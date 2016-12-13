package com.ediposouza.teslesgendstracker.ui.widget.filter

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.view.animation.AnimationUtils
import android.widget.LinearLayout
import com.ediposouza.teslesgendstracker.R
import com.ediposouza.teslesgendstracker.data.Attribute
import kotlinx.android.synthetic.main.widget_attributes_filter.view.*

/**
 * Created by EdipoSouza on 11/2/16.
 */
class FilterAttr(ctx: Context?, attrs: AttributeSet?, defStyleAttr: Int) :
        LinearLayout(ctx, attrs, defStyleAttr) {

    var deckMode: Boolean = false
        set(value) {
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
    var lockAttr1: Attribute? = null
    var lockAttr2: Attribute? = null

    init {
        inflate(context, R.layout.widget_attributes_filter, this)
        val a = context.obtainStyledAttributes(attrs, R.styleable.FilterAttr)
        deckMode = a.getBoolean(R.styleable.FilterAttr_deckMode, false)
        a.recycle()
        if (!isInEditMode) {
            rootView.attr_filter_strength?.setOnClickListener { filterClick?.invoke(Attribute.STRENGTH) }
            rootView.attr_filter_intelligence?.setOnClickListener { filterClick?.invoke(Attribute.INTELLIGENCE) }
            rootView.attr_filter_willpower?.setOnClickListener { filterClick?.invoke(Attribute.WILLPOWER) }
            rootView.attr_filter_agility?.setOnClickListener { filterClick?.invoke(Attribute.AGILITY) }
            rootView.attr_filter_endurance?.setOnClickListener { filterClick?.invoke(Attribute.ENDURANCE) }
            rootView.attr_filter_dual?.setOnClickListener { filterClick?.invoke(Attribute.DUAL) }
            rootView.attr_filter_neutral?.setOnClickListener { filterClick?.invoke(Attribute.NEUTRAL) }
        }
    }

    constructor(ctx: Context?) : this(ctx, null, 0) {
    }

    constructor(ctx: Context?, attrs: AttributeSet) : this(ctx, attrs, 0) {
    }

    fun selectAttr(attr: Attribute, only: Boolean) {
        updateVisibility(rootView.attr_filter_strength_indicator, attr == Attribute.STRENGTH, only)
        updateVisibility(rootView.attr_filter_intelligence_indicator, attr == Attribute.INTELLIGENCE, only)
        updateVisibility(rootView.attr_filter_willpower_indicator, attr == Attribute.WILLPOWER, only)
        updateVisibility(rootView.attr_filter_agility_indicator, attr == Attribute.AGILITY, only)
        updateVisibility(rootView.attr_filter_endurance_indicator, attr == Attribute.ENDURANCE, only)
        updateVisibility(rootView.attr_filter_dual_indicator, attr == Attribute.DUAL, only)
        updateVisibility(rootView.attr_filter_neutral_indicator, attr == Attribute.NEUTRAL, only)
    }

    private fun updateVisibility(v: View, show: Boolean, only: Boolean) {
        v.visibility = if (show) View.VISIBLE else if (only) View.INVISIBLE else v.visibility
    }

    fun unselectAttr(attr: Attribute) {
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

    fun isAttrSelected(attr: Attribute): Boolean {
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

    fun lockAttrs(clsAttr1: Attribute, clsAttr2: Attribute) {
        lockAttr(clsAttr1)
        lockAttr(clsAttr2)
        if (lockAttr1 != null && lockAttr2 != null && lockAttr1 != lockAttr2) {
            startAnimLock()
        }
    }

    fun lockAttr(clsAttr: Attribute) {
        if (lockAttr1 == null) {
            lockAttr1 = clsAttr
            return
        }
        if (lockAttr2 == null) {
            lockAttr2 = clsAttr
            return
        }
    }

    fun unlockAttr(attr: Attribute) {
        if (lockAttr1 == attr) {
            lockAttr1 = null
        }
        if (lockAttr2 == attr) {
            lockAttr2 = null
        }
        if (lockAttr1 == null || lockAttr2 == null) {
            startAnimUnlock()
        }
    }

    private fun startAnimLock() {
        val scaleDownAnimation = AnimationUtils.loadAnimation(context, R.anim.fab_scale_down)
        rootView.attr_filter_strength?.startAnimation(scaleDownAnimation)
        rootView.attr_filter_intelligence?.startAnimation(scaleDownAnimation)
        rootView.attr_filter_willpower?.startAnimation(scaleDownAnimation)
        rootView.attr_filter_agility?.startAnimation(scaleDownAnimation)
        rootView.attr_filter_endurance?.startAnimation(scaleDownAnimation)
        val scaleUpAnimation = AnimationUtils.loadAnimation(context, R.anim.fab_scale_down)
        rootView.attr_filter_strength?.setImageResource(lockAttr1?.imageRes ?: R.drawable.attr_strength)
        rootView.attr_filter_intelligence?.setImageResource(lockAttr2?.imageRes ?: R.drawable.attr_intelligence)
        rootView.attr_filter_strength?.startAnimation(scaleUpAnimation)
        rootView.attr_filter_intelligence?.startAnimation(scaleUpAnimation)
        rootView.attr_filter_strength?.setOnClickListener { filterClick?.invoke(lockAttr1 ?: Attribute.STRENGTH) }
        rootView.attr_filter_intelligence?.setOnClickListener { filterClick?.invoke(lockAttr2 ?: Attribute.INTELLIGENCE) }
    }

    private fun startAnimUnlock() {
        val scaleDownAnimation = AnimationUtils.loadAnimation(context, R.anim.fab_scale_down)
        rootView.attr_filter_strength?.startAnimation(scaleDownAnimation)
        rootView.attr_filter_intelligence?.startAnimation(scaleDownAnimation)
        rootView.attr_filter_strength?.setImageResource(R.drawable.attr_strength)
        rootView.attr_filter_intelligence?.setImageResource(R.drawable.attr_intelligence)
        val scaleUpAnimation = AnimationUtils.loadAnimation(context, R.anim.fab_scale_down)
        rootView.attr_filter_strength?.startAnimation(scaleUpAnimation)
        rootView.attr_filter_intelligence?.startAnimation(scaleUpAnimation)
        rootView.attr_filter_willpower?.startAnimation(scaleUpAnimation)
        rootView.attr_filter_agility?.startAnimation(scaleUpAnimation)
        rootView.attr_filter_endurance?.startAnimation(scaleUpAnimation)
        rootView.attr_filter_strength?.setOnClickListener { filterClick?.invoke(Attribute.STRENGTH) }
        rootView.attr_filter_intelligence?.setOnClickListener { filterClick?.invoke(Attribute.INTELLIGENCE) }
    }
}