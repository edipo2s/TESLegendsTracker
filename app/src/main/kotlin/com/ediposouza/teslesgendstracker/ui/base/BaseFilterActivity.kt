package com.ediposouza.teslesgendstracker.ui.base

import android.animation.Animator
import android.animation.ValueAnimator
import android.os.Bundle
import android.support.design.widget.CoordinatorLayout
import android.text.format.DateUtils
import android.view.View
import com.ediposouza.teslesgendstracker.R
import com.ediposouza.teslesgendstracker.ui.cards.widget.FilterMagicka
import com.ediposouza.teslesgendstracker.ui.cards.widget.FilterRarity
import org.greenrobot.eventbus.Subscribe

/**
 * Created by EdipoSouza on 12/29/16.
 */
open class BaseFilterActivity : BaseActivity() {

    private val KEY_FILTERS_GREAT_MARGIN = "filterGreatMarginKey"
    private val KEY_FILTERS_BOTTOM_MARGIN = "filterMagickaBottomMarginKey"
    private val UPDATE_FILTERS_POSITION_DURATION = DateUtils.SECOND_IN_MILLIS
    private val UPDATE_FILTERS_VISIBILITY_DURATION = UPDATE_FILTERS_POSITION_DURATION / 2

    protected var filterGreatMargin = false

    private var fab_filter_magicka: View? = null
        get() = findViewById(R.id.cards_filter_magicka)
    private var fab_filter_rarity: View? = null
        get() = findViewById(R.id.cards_filter_rarity)
    private val filterMagickaLP: CoordinatorLayout.LayoutParams?
        get() = fab_filter_magicka?.layoutParams as? CoordinatorLayout.LayoutParams
    private val filterRarityLP: CoordinatorLayout.LayoutParams?
        get() = fab_filter_rarity?.layoutParams as? CoordinatorLayout.LayoutParams

    override fun onSaveInstanceState(outState: Bundle?) {
        outState?.apply {
            putBoolean(KEY_FILTERS_GREAT_MARGIN, filterGreatMargin)
            putInt(KEY_FILTERS_BOTTOM_MARGIN, filterMagickaLP?.bottomMargin ?: 0)
        }
        super.onSaveInstanceState(outState)
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle?) {
        super.onRestoreInstanceState(savedInstanceState)
        savedInstanceState?.apply {
            filterGreatMargin = getBoolean(KEY_FILTERS_GREAT_MARGIN)
            updateFiltersMargins(getInt(KEY_FILTERS_BOTTOM_MARGIN))
        }
    }

    @Synchronized fun updateRarityMagickaFiltersVisibility(show: Boolean) {
        val margin = R.dimen.filter_great_margin_bottom.takeIf { filterGreatMargin } ?: R.dimen.large_margin
        val showBottomMargin = resources.getDimensionPixelSize(margin)
        val hideBottomMargin = -resources.getDimensionPixelSize(R.dimen.filter_hide_height)
        if (show && filterMagickaLP?.bottomMargin == showBottomMargin ||
                !show && filterMagickaLP?.bottomMargin == hideBottomMargin) {
            return
        }
        if (!show && fab_filter_magicka != null && fab_filter_rarity != null) {
            (fab_filter_magicka as FilterMagicka).close()
            (fab_filter_rarity as FilterRarity).collapse()
        }
        val animFrom = hideBottomMargin.takeIf { show } ?: showBottomMargin
        val animTo = showBottomMargin.takeIf { show } ?: hideBottomMargin
        with(ValueAnimator.ofInt(animFrom, animTo)) {
            duration = UPDATE_FILTERS_VISIBILITY_DURATION
            addUpdateListener {
                updateFiltersMargins(it.animatedValue as Int)
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
    @Suppress("unused")
    fun onCmdUpdateRarityMagickaFiltersPosition(update: CmdUpdateRarityMagickaFiltersPosition) {
        filterGreatMargin = update.high
        val endMargin = R.dimen.filter_great_margin_bottom.takeIf { filterGreatMargin } ?: R.dimen.large_margin
        with(ValueAnimator.ofInt(filterMagickaLP?.bottomMargin ?: 0, resources.getDimensionPixelSize(endMargin))) {
            duration = UPDATE_FILTERS_POSITION_DURATION
            addUpdateListener {
                updateFiltersMargins(it.animatedValue as Int)
            }
            start()
        }
    }

    private fun updateFiltersMargins(bottomMargin: Int) {
        filterRarityLP?.bottomMargin = bottomMargin
        filterMagickaLP?.bottomMargin = bottomMargin
        fab_filter_magicka?.layoutParams = filterMagickaLP
        fab_filter_rarity?.layoutParams = filterRarityLP
    }

}
