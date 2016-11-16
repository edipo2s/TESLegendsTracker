package com.ediposouza.teslesgendstracker.ui.widget

import android.content.Context
import android.util.AttributeSet
import android.widget.LinearLayout
import com.ediposouza.teslesgendstracker.*
import com.ediposouza.teslesgendstracker.data.Attribute
import kotlinx.android.synthetic.main.widget_collection_statistics_attr.view.*

/**
 * Created by EdipoSouza on 11/2/16.
 */
class CollectionStatisticsAttr(ctx: Context?, attrs: AttributeSet?, defStyleAttr: Int) :
        LinearLayout(ctx, attrs, defStyleAttr) {

    private var attribute = Attribute.STRENGTH
    private var commonOwned: Long = 0
    private var commonTotal: Long = 0
    private var rareOwned: Long = 0
    private var rareTotal: Long = 0
    private var epicOwned: Long = 0
    private var epicTotal: Long = 0
    private var legendaryOwned: Long = 0
    private var legendaryTotal: Long = 0

    var soulMissing: Long = 0
    var owned: Long = 0
    var total: Long = 0

    init {
        inflate(context, R.layout.widget_collection_statistics_attr, this)
        val a = context.obtainStyledAttributes(attrs, R.styleable.CollectionStatisticsAttr)
        attribute = Attribute.values()[a.getInteger(R.styleable.CollectionStatisticsAttr_attribute, 0)]
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

    fun setCommon(owned: Long, total: Long) {
        commonOwned = owned
        commonTotal = total
        attr_statistics_common.text = context.getString(R.string.statistics_rarity, owned, total)
        updateTotal()
    }

    fun setRare(owned: Long, total: Long) {
        rareOwned = owned
        rareTotal = total
        attr_statistics_rare.text = context.getString(R.string.statistics_rarity, owned, total)
        updateTotal()
    }

    fun setEpic(owned: Long, total: Long) {
        epicOwned = owned
        epicTotal = total
        attr_statistics_epic.text = context.getString(R.string.statistics_rarity, owned, total)
        updateTotal()
    }

    fun setLegendary(owned: Long, total: Long) {
        legendaryOwned = owned
        legendaryTotal = total
        attr_statistics_legendary.text = context.getString(R.string.statistics_rarity, owned, total)
        updateTotal()
    }

    private fun updateTotal() {
        owned = commonOwned + rareOwned + epicOwned + legendaryOwned
        total = commonTotal + rareTotal + epicTotal + legendaryTotal
        soulMissing = (commonTotal - commonOwned) * SOUL_COMMON + (rareTotal - rareOwned) * SOUL_RARE +
                (epicTotal - epicOwned) * SOUL_EPIC + (legendaryTotal - legendaryOwned) * SOUL_LEGENDARY
        attr_statistics_total.text = context.getString(R.string.statistics_total, owned, total)
    }

}