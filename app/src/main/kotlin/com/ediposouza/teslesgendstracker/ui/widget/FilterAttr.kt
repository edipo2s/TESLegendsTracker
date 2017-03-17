package com.ediposouza.teslesgendstracker.ui.widget

import android.content.Context
import android.os.Parcel
import android.os.Parcelable
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import com.ediposouza.teslesgendstracker.R
import com.ediposouza.teslesgendstracker.data.CardAttribute
import kotlinx.android.synthetic.main.widget_attributes_filter.view.*

/**
 * Created by EdipoSouza on 11/2/16.
 */
open class FilterAttr(ctx: Context?, attrs: AttributeSet?, defStyleAttr: Int) :
        LinearLayout(ctx, attrs, defStyleAttr) {

    var filterClick: ((CardAttribute) -> Unit)? = null
    var lastAttrSelected: CardAttribute = CardAttribute.STRENGTH

    var deckMode: Boolean = false
        set(value) {
            field = value
            if (!value) {
                selectAttr(lastAttrSelected, true)
            } else {
                if (onlyBasicAttributes) {
                    rootView.attr_filter_dual.visibility = View.GONE.takeIf { value } ?: View.VISIBLE
                    rootView.attr_filter_dual_indicator.visibility = View.GONE.takeIf { value } ?: View.VISIBLE
                    rootView.attr_filter_neutral.visibility = View.GONE.takeIf { value } ?: View.VISIBLE
                    rootView.attr_filter_neutral_indicator.visibility = View.GONE.takeIf { value } ?: View.VISIBLE
                }
            }
        }

    var onlyBasicAttributes: Boolean = false

    init {
        inflate(context, R.layout.widget_attributes_filter, rootView as ViewGroup)
        val a = context.obtainStyledAttributes(attrs, R.styleable.FilterAttr)
        onlyBasicAttributes = a.getBoolean(R.styleable.FilterAttr_onlyBasicAttributes, false)
        deckMode = a.getBoolean(R.styleable.FilterAttr_selectMode, false)
        a.recycle()
        if (!isInEditMode) {
            rootView.attr_filter_strength?.setOnClickListener { attrClick(CardAttribute.STRENGTH, false) }
            rootView.attr_filter_intelligence?.setOnClickListener { attrClick(CardAttribute.INTELLIGENCE, false) }
            rootView.attr_filter_willpower?.setOnClickListener { attrClick(CardAttribute.WILLPOWER, true) }
            rootView.attr_filter_agility?.setOnClickListener { attrClick(CardAttribute.AGILITY, true) }
            rootView.attr_filter_endurance?.setOnClickListener { attrClick(CardAttribute.ENDURANCE, true) }
            rootView.attr_filter_dual?.setOnClickListener { attrClick(CardAttribute.DUAL, false) }
            rootView.attr_filter_neutral?.setOnClickListener { attrClick(CardAttribute.NEUTRAL, false) }
        }
    }

    constructor(ctx: Context?) : this(ctx, null, 0)

    constructor(ctx: Context?, attrs: AttributeSet) : this(ctx, attrs, 0)

    open protected fun attrClick(attr: CardAttribute, lockable: Boolean) {
        filterClick?.invoke(attr)
    }

    override fun onSaveInstanceState(): Parcelable {
        val superState = super.onSaveInstanceState()
        return SavedState(superState, lastAttrSelected)
    }

    override fun onRestoreInstanceState(state: Parcelable?) {
        if (state is SavedState) {
            if (!deckMode) {
                selectAttr(state.attrSelected, true)
            }
            super.onRestoreInstanceState(state.superState)
        } else {
            super.onRestoreInstanceState(state)
        }
    }

    open fun selectAttr(attr: CardAttribute, only: Boolean) {
        lastAttrSelected = attr
        updateVisibility(rootView.attr_filter_strength_indicator, attr == CardAttribute.STRENGTH, only)
        updateVisibility(rootView.attr_filter_intelligence_indicator, attr == CardAttribute.INTELLIGENCE, only)
        updateVisibility(rootView.attr_filter_willpower_indicator, attr == CardAttribute.WILLPOWER, only)
        updateVisibility(rootView.attr_filter_agility_indicator, attr == CardAttribute.AGILITY, only)
        updateVisibility(rootView.attr_filter_endurance_indicator, attr == CardAttribute.ENDURANCE, only)
        updateVisibility(rootView.attr_filter_dual_indicator, attr == CardAttribute.DUAL, only)
        updateVisibility(rootView.attr_filter_neutral_indicator, attr == CardAttribute.NEUTRAL, only)
    }

    open fun unSelectAttr(attr: CardAttribute) {
        when (attr) {
            CardAttribute.STRENGTH -> rootView.attr_filter_strength_indicator.visibility = View.INVISIBLE
            CardAttribute.INTELLIGENCE -> rootView.attr_filter_intelligence_indicator.visibility = View.INVISIBLE
            CardAttribute.WILLPOWER -> rootView.attr_filter_willpower_indicator.visibility = View.INVISIBLE
            CardAttribute.AGILITY -> rootView.attr_filter_agility_indicator.visibility = View.INVISIBLE
            CardAttribute.ENDURANCE -> rootView.attr_filter_endurance_indicator.visibility = View.INVISIBLE
            CardAttribute.DUAL -> rootView.attr_filter_dual_indicator.visibility = View.INVISIBLE
            CardAttribute.NEUTRAL -> rootView.attr_filter_neutral_indicator.visibility = View.INVISIBLE
        }
    }

    open fun isAttrSelected(attr: CardAttribute): Boolean {
        return when (attr) {
            CardAttribute.STRENGTH -> rootView.attr_filter_strength_indicator.visibility == View.VISIBLE
            CardAttribute.INTELLIGENCE -> rootView.attr_filter_intelligence_indicator.visibility == View.VISIBLE
            CardAttribute.WILLPOWER -> rootView.attr_filter_willpower_indicator.visibility == View.VISIBLE
            CardAttribute.AGILITY -> rootView.attr_filter_agility_indicator.visibility == View.VISIBLE
            CardAttribute.ENDURANCE -> rootView.attr_filter_endurance_indicator.visibility == View.VISIBLE
            CardAttribute.DUAL -> rootView.attr_filter_dual_indicator.visibility == View.VISIBLE
            CardAttribute.NEUTRAL -> rootView.attr_filter_neutral_indicator.visibility == View.VISIBLE
            else -> false
        }
    }

    fun getSelectedAttrs(): List<CardAttribute> {
        val attrs = CardAttribute.values().filter { isAttrSelected(it) }
        return if (deckMode && onlyBasicAttributes) attrs.plus(CardAttribute.NEUTRAL) else attrs
    }

    protected fun updateVisibility(v: View, show: Boolean, only: Boolean) {
        v.visibility = View.VISIBLE.takeIf { show } ?: View.INVISIBLE.takeIf { only } ?: v.visibility
    }

    class SavedState : BaseSavedState {

        var attrSelected: CardAttribute = CardAttribute.STRENGTH

        constructor(source: Parcel?) : super(source) {
            this.attrSelected = CardAttribute.values()[source?.readInt() ?: 0]
        }

        constructor(source: Parcelable, attrSelected: CardAttribute) : super(source) {
            this.attrSelected = attrSelected
        }

        companion object {
            @Suppress("unused")
            @JvmField val CREATOR: Parcelable.Creator<SavedState> = object : Parcelable.Creator<SavedState> {
                override fun createFromParcel(source: Parcel): SavedState = SavedState(source)
                override fun newArray(size: Int): Array<SavedState?> = arrayOfNulls(size)
            }
        }

        override fun describeContents() = 0

        override fun writeToParcel(dest: Parcel?, flags: Int) {
            super.writeToParcel(dest, flags)
            dest?.writeInt(attrSelected.ordinal)
        }
    }

}