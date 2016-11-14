package com.ediposouza.teslesgendstracker.ui.widget

import android.content.Context
import android.util.AttributeSet
import android.widget.LinearLayout
import com.ediposouza.teslesgendstracker.R
import com.ediposouza.teslesgendstracker.data.Attribute
import kotlinx.android.synthetic.main.widget_collection_statistics_attr.view.*

/**
 * Created by EdipoSouza on 11/2/16.
 */
class CollectionStatisticsAttr(ctx: Context?, attrs: AttributeSet?, defStyleAttr: Int) :
        LinearLayout(ctx, attrs, defStyleAttr) {

    init {
        inflate(context, R.layout.widget_collection_statistics_attr, this)
        val a = context.obtainStyledAttributes(R.styleable.CollectionStatisticsAttr)
        val attribute = Attribute.values()[a.getInt(R.styleable.CollectionStatisticsAttr_attr, 0)]
        a.recycle()
        attr_statistics_attr.setImageResource(when (attribute) {
            Attribute.STRENGTH -> R.drawable.attr_strength
            Attribute.INTELLIGENCE -> R.drawable.attr_intelligence
            Attribute.WILLPOWER -> R.drawable.attr_willpower
            Attribute.AGILITY -> R.drawable.attr_agility
            Attribute.ENDURANCE -> R.drawable.attr_endurance
            Attribute.DUAL -> R.drawable.attr_dual
            Attribute.NEUTRAL -> R.drawable.attr_neutral
            else -> R.drawable.attr_strength
        })
        if (!isInEditMode) {
        }
    }

    constructor(ctx: Context?) : this(ctx, null, 0) {
    }

    constructor(ctx: Context?, attrs: AttributeSet) : this(ctx, attrs, 0) {
    }

    fun setCommon(owner: Int, total: Int) {
        attr_statistics_common.text = context.getString(R.string.statistics_rarity, owner, total)
    }

    fun setRare(owner: Int, total: Int) {
        attr_statistics_rare.text = context.getString(R.string.statistics_rarity, owner, total)
    }

    fun setEpic(owner: Int, total: Int) {
        attr_statistics_epic.text = context.getString(R.string.statistics_rarity, owner, total)
    }

    fun setLegendary(owner: Int, total: Int) {
        attr_statistics_legendary.text = context.getString(R.string.statistics_total, owner, total)
    }

}