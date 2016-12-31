package com.ediposouza.teslesgendstracker.ui.widget

import android.content.Context
import android.os.Build
import android.util.AttributeSet
import android.view.MotionEvent
import android.widget.FrameLayout
import com.ediposouza.teslesgendstracker.R
import com.ediposouza.teslesgendstracker.data.Attribute
import com.ediposouza.teslesgendstracker.data.CardRarity
import com.ediposouza.teslesgendstracker.interactor.PrivateInteractor
import com.ediposouza.teslesgendstracker.interactor.PublicInteractor
import kotlinx.android.synthetic.main.widget_collection_statistics.view.*
import timber.log.Timber
import java.text.NumberFormat

/**
 * Created by EdipoSouza on 11/2/16.
 */
class CollectionStatistics(ctx: Context?, attrs: AttributeSet?, defStyleAttr: Int) :
        FrameLayout(ctx, attrs, defStyleAttr) {

    val privateInteractor by lazy { PrivateInteractor() }
    val publicInteractor by lazy { PublicInteractor() }

    init {
        inflate(context, R.layout.widget_collection_statistics, this)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            val layoutParams = collection_statistics_container.layoutParams as FrameLayout.LayoutParams
            layoutParams.bottomMargin = resources.getDimensionPixelSize(R.dimen.navigation_bar_height)
            collection_statistics_container.layoutParams = layoutParams
        }
    }

    constructor(ctx: Context?) : this(ctx, null, 0)

    constructor(ctx: Context?, attrs: AttributeSet) : this(ctx, attrs, 0)

    override fun onInterceptTouchEvent(ev: MotionEvent?): Boolean {
        val statisticPeekHeight = resources.getDimensionPixelSize(R.dimen.statistics_bottom_peek_height)
        val clickYPos = ev?.y ?: 0f
        return clickYPos < statisticPeekHeight || super.onInterceptTouchEvent(ev)
    }

    fun scrollToTop() {
        collection_statistics_container.smoothScrollTo(0, 0)
    }

    fun updateStatistics() {
        updateAttributeStatistics(Attribute.STRENGTH)
        updateAttributeStatistics(Attribute.INTELLIGENCE)
        updateAttributeStatistics(Attribute.WILLPOWER)
        updateAttributeStatistics(Attribute.AGILITY)
        updateAttributeStatistics(Attribute.ENDURANCE)
        updateAttributeStatistics(Attribute.DUAL)
        updateAttributeStatistics(Attribute.NEUTRAL)
        updateStatisticsTotal()
    }

    fun updateStatistics(attr: Attribute) {
        updateAttributeStatistics(attr)
        updateStatisticsTotal()
    }

    private fun updateStatisticsTotal() {
        with(rootView) {
            val owned = rarity_statistics_strength.owned + rarity_statistics_intelligence.owned +
                    rarity_statistics_willpower.owned + rarity_statistics_agility.owned +
                    rarity_statistics_endurance.owned + rarity_statistics_dual.owned +
                    rarity_statistics_neutral.owned
            val total = rarity_statistics_strength.total + rarity_statistics_intelligence.total +
                    rarity_statistics_willpower.total + rarity_statistics_agility.total +
                    rarity_statistics_endurance.total + rarity_statistics_dual.total +
                    rarity_statistics_neutral.total
            val soulMissing = rarity_statistics_strength.soulMissing + rarity_statistics_intelligence.soulMissing +
                    rarity_statistics_willpower.soulMissing + rarity_statistics_agility.soulMissing +
                    rarity_statistics_endurance.soulMissing + rarity_statistics_dual.soulMissing +
                    rarity_statistics_neutral.soulMissing
            val percent = if (total > 0) owned.toFloat() / total.toFloat() * 100f else 0f
            collection_statistics_total.text = context.getString(R.string.statistics_total, owned, total)
            collection_statistics_percent.text = context.getString(R.string.statistics_percent, percent)
            collection_statistics_soul.text = NumberFormat.getNumberInstance().format(soulMissing)
        }
    }

    private fun updateAttributeStatistics(attr: Attribute) {
        publicInteractor.getCardsForStatistics(null, attr) {
            val allAttrCards = it.groupBy { it.rarity }
            Timber.d(attr.name + allAttrCards.toString())
            privateInteractor.getUserCollection(null, attr) { collection: Map<String, Int> ->
                val userAttrCards = allAttrCards.map {
                    it.key to it.value.filter { collection.containsKey(it.shortName) }
                            .map { it to collection[it.shortName] }
                }.toMap()
                allAttrCards.forEach {
                    val allRarityCards = it.value
                    val userRarityCards = userAttrCards[it.key]
                    Timber.d("${attr.name} - $it: $userRarityCards")
                    val owned = userRarityCards?.map { it.second ?: 0 }?.sum() ?: 0
                    when (it.key) {
                        CardRarity.COMMON -> statisticsAttr(attr).setCommon(owned, allRarityCards.size * 3)
                        CardRarity.RARE -> statisticsAttr(attr).setRare(owned, allRarityCards.size * 3)
                        CardRarity.EPIC -> statisticsAttr(attr).setEpic(owned, allRarityCards.size * 3)
                        CardRarity.LEGENDARY -> {
                            val legendaryTotal = allRarityCards.map { if (it.unique) 1 else 3 }.sum()
                            statisticsAttr(attr).setLegendary(owned, legendaryTotal)
                        }
                        else -> {
                        }
                    }
                }
            }
        }
    }

    private fun statisticsAttr(attr: Attribute) = when (attr) {
        Attribute.STRENGTH -> rootView.rarity_statistics_strength
        Attribute.INTELLIGENCE -> rootView.rarity_statistics_intelligence
        Attribute.WILLPOWER -> rootView.rarity_statistics_willpower
        Attribute.AGILITY -> rootView.rarity_statistics_agility
        Attribute.ENDURANCE -> rootView.rarity_statistics_endurance
        Attribute.NEUTRAL -> rootView.rarity_statistics_neutral
        Attribute.DUAL -> rootView.rarity_statistics_dual
    }

}