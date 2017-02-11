package com.ediposouza.teslesgendstracker.ui.cards.widget

import android.content.Context
import android.os.Build
import android.util.AttributeSet
import android.view.MotionEvent
import android.widget.FrameLayout
import com.ediposouza.teslesgendstracker.R
import com.ediposouza.teslesgendstracker.data.CardAttribute
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

    init {
        inflate(context, R.layout.widget_collection_statistics, this)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            val layoutParams = collection_statistics_container.layoutParams as LayoutParams
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
        updateAttributeStatistics(CardAttribute.STRENGTH)
        updateAttributeStatistics(CardAttribute.INTELLIGENCE)
        updateAttributeStatistics(CardAttribute.WILLPOWER)
        updateAttributeStatistics(CardAttribute.AGILITY)
        updateAttributeStatistics(CardAttribute.ENDURANCE)
        updateAttributeStatistics(CardAttribute.DUAL)
        updateAttributeStatistics(CardAttribute.NEUTRAL)
        updateStatisticsTotal()
    }

    fun updateStatistics(attr: CardAttribute) {
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
            val percent = 0f.takeIf { total == 0 } ?: owned.toFloat() / total.toFloat() * 100f
            collection_statistics_total.text = context.getString(R.string.collection_statistics_total, owned, total)
            collection_statistics_percent.text = context.getString(R.string.collection_statistics_percent, percent)
            collection_statistics_soul.text = NumberFormat.getNumberInstance().format(soulMissing)
        }
    }

    private fun updateAttributeStatistics(attr: CardAttribute) {
        PublicInteractor.getCardsForStatistics(null, attr) {
            val allAttrCards = it.groupBy { it.rarity }
            Timber.d(attr.name + allAttrCards.toString())
            PrivateInteractor.getUserCollection(null, attr) { collection: Map<String, Int> ->
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

    private fun statisticsAttr(attr: CardAttribute) = when (attr) {
        CardAttribute.STRENGTH -> rootView.rarity_statistics_strength
        CardAttribute.INTELLIGENCE -> rootView.rarity_statistics_intelligence
        CardAttribute.WILLPOWER -> rootView.rarity_statistics_willpower
        CardAttribute.AGILITY -> rootView.rarity_statistics_agility
        CardAttribute.ENDURANCE -> rootView.rarity_statistics_endurance
        CardAttribute.NEUTRAL -> rootView.rarity_statistics_neutral
        CardAttribute.DUAL -> rootView.rarity_statistics_dual
    }

}