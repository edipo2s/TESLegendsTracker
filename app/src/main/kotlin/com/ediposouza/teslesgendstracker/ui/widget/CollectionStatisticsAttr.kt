package com.ediposouza.teslesgendstracker.ui.widget

import android.content.Context
import android.util.AttributeSet
import android.widget.LinearLayout
import com.ediposouza.teslesgendstracker.R
import com.ediposouza.teslesgendstracker.data.Attribute
import com.ediposouza.teslesgendstracker.data.CardRarity
import kotlinx.android.synthetic.main.widget_collection_statistics_attr.view.*

/**
 * Created by EdipoSouza on 11/2/16.
 */
class CollectionStatisticsAttr(ctx: Context?, attrs: AttributeSet?, defStyleAttr: Int) :
        LinearLayout(ctx, attrs, defStyleAttr) {

    private var attribute = Attribute.STRENGTH
    private val cards = hashMapOf(CardRarity.COMMON to Pair(0, 0), CardRarity.RARE to Pair(0, 0),
            CardRarity.EPIC to Pair(0, 0), CardRarity.LEGENDARY to Pair(0, 0))

    var soulMissing: Int = 0
    var owned: Int = 0
    var total: Int = 0

    init {
        inflate(context, R.layout.widget_collection_statistics_attr, this)
        val a = context.obtainStyledAttributes(attrs, R.styleable.CollectionStatisticsAttr)
        attribute = Attribute.values()[a.getInteger(R.styleable.CollectionStatisticsAttr_attribute, 0)]
        a.recycle()
        attr_statistics_attr.setImageResource(attribute.imageRes)
        if (!isInEditMode) {
        }
    }

    constructor(ctx: Context?) : this(ctx, null, 0)

    constructor(ctx: Context?, attrs: AttributeSet) : this(ctx, attrs, 0)

    fun setCommon(owned: Int, total: Int) {
        attr_statistics_common.text = context.getString(R.string.statistics_rarity, owned, total)
        cards[CardRarity.COMMON] = Pair(owned, total)
        updateTotal()
    }

    fun setRare(owned: Int, total: Int) {
        attr_statistics_rare.text = context.getString(R.string.statistics_rarity, owned, total)
        cards[CardRarity.RARE] = Pair(owned, total)
        updateTotal()
    }

    fun setEpic(owned: Int, total: Int) {
        attr_statistics_epic.text = context.getString(R.string.statistics_rarity, owned, total)
        cards[CardRarity.EPIC] = Pair(owned, total)
        updateTotal()
    }

    fun setLegendary(owned: Int, total: Int) {
        attr_statistics_legendary.text = context.getString(R.string.statistics_rarity, owned, total)
        cards[CardRarity.LEGENDARY] = Pair(owned, total)
        updateTotal()
    }

    private fun updateTotal() {
        owned = cards.map { it.value.first }.sum()
        total = cards.map { it.value.second }.sum()
        soulMissing = cards.map { (it.value.second - it.value.first) * it.key.soulCost }.sum()
        attr_statistics_total.text = context.getString(R.string.statistics_total, owned, total)
    }

}