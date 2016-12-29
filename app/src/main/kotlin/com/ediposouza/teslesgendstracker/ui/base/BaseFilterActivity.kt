package com.ediposouza.teslesgendstracker.ui.base

import android.animation.Animator
import android.animation.ValueAnimator
import android.support.design.widget.CoordinatorLayout
import android.text.format.DateUtils
import com.ediposouza.teslesgendstracker.R
import com.ediposouza.teslesgendstracker.ui.widget.filter.FilterMagika
import com.ediposouza.teslesgendstracker.ui.widget.filter.FilterRarity
import org.greenrobot.eventbus.Subscribe
import org.jetbrains.anko.find

/**
 * Created by EdipoSouza on 12/29/16.
 */

open class BaseFilterActivity : BaseActivity() {

    val UPDATE_FILTERS_POSITION_DURATION = DateUtils.SECOND_IN_MILLIS
    val UPDATE_FILTERS_VISIBILITY_DURATION = UPDATE_FILTERS_POSITION_DURATION / 2

    var filterGreatMargin = false

    val fab_filter_magika by lazy { find<FilterMagika>(R.id.filter_magika) }
    val fab_filter_rarity by lazy { find<FilterRarity>(R.id.filter_rarity) }

    fun updateRarityMagikaFiltersVisibility(show: Boolean) {
        val margin = if (filterGreatMargin) R.dimen.filter_great_margin_bottom else R.dimen.large_margin
        val filterMagikaLP = fab_filter_magika.layoutParams as CoordinatorLayout.LayoutParams
        val filterRarityLP = fab_filter_rarity.layoutParams as CoordinatorLayout.LayoutParams
        val showBottomMargin = resources.getDimensionPixelSize(margin)
        val hideBottomMargin = -resources.getDimensionPixelSize(R.dimen.filter_hide_height)
        if (show && filterMagikaLP.bottomMargin == showBottomMargin ||
                !show && filterMagikaLP.bottomMargin == hideBottomMargin) {
            return
        }
        val animFrom = if (show) hideBottomMargin else showBottomMargin
        val animTo = if (show) showBottomMargin else hideBottomMargin
        with(ValueAnimator.ofInt(animFrom, animTo)) {
            duration = UPDATE_FILTERS_VISIBILITY_DURATION
            addUpdateListener {
                filterRarityLP.bottomMargin = it.animatedValue as Int
                filterMagikaLP.bottomMargin = it.animatedValue as Int
                fab_filter_magika.layoutParams = filterMagikaLP
                fab_filter_rarity.layoutParams = filterRarityLP
            }
            addListener(object : Animator.AnimatorListener {
                override fun onAnimationRepeat(p0: Animator?) {
                }

                override fun onAnimationEnd(p0: Animator?) {
                }

                override fun onAnimationCancel(p0: Animator?) {
                }

                override fun onAnimationStart(p0: Animator?) {
                }

            })
            start()
        }
    }

    @Subscribe
    fun onCmdUpdateRarityMagikaFiltersPosition(update: CmdUpdateRarityMagikaFiltersPosition) {
        filterGreatMargin = update.high
        val filterMagikaLP = fab_filter_magika.layoutParams as CoordinatorLayout.LayoutParams
        val filterRarityLP = fab_filter_rarity.layoutParams as CoordinatorLayout.LayoutParams
        val endMargin = if (filterGreatMargin) R.dimen.filter_great_margin_bottom else R.dimen.large_margin
        with(ValueAnimator.ofInt(filterMagikaLP.bottomMargin, resources.getDimensionPixelSize(endMargin))) {
            duration = UPDATE_FILTERS_POSITION_DURATION
            addUpdateListener {
                filterRarityLP.bottomMargin = it.animatedValue as Int
                filterMagikaLP.bottomMargin = it.animatedValue as Int
                fab_filter_magika.layoutParams = filterMagikaLP
                fab_filter_rarity.layoutParams = filterRarityLP
            }
            start()
        }
    }

}
