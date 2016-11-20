package com.ediposouza.teslesgendstracker.ui.widget.filter

import android.animation.Animator
import android.animation.ValueAnimator
import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.LinearLayout
import com.ediposouza.teslesgendstracker.R
import com.ediposouza.teslesgendstracker.data.CardRarity
import kotlinx.android.synthetic.main.widget_rarity_filter.view.*

/**
 * Created by EdipoSouza on 11/2/16.
 */
class FilterRarity(ctx: Context?, attrs: AttributeSet?, defStyleAttr: Int) :
        LinearLayout(ctx, attrs, defStyleAttr) {

    val ANIM_DURATION: Long = 200
    val rarityBGMinWidth by lazy { resources.getDimensionPixelSize(R.dimen.size_medium) }
    val rarityBGMaxWidth by lazy { resources.getDimensionPixelSize(R.dimen.rarity_bg_max_width) }

    var filterClick: ((CardRarity?) -> Unit)? = null

    init {
        inflate(context, R.layout.widget_rarity_filter, this)
        if (!isInEditMode) {
            with(rootView) {
                rarity_filter_common.setOnClickListener { rarityClick(CardRarity.COMMON) }
                rarity_filter_rare.setOnClickListener { rarityClick(CardRarity.RARE) }
                rarity_filter_epic.setOnClickListener { rarityClick(CardRarity.EPIC) }
                rarity_filter_legendary.setOnClickListener { rarityClick(CardRarity.LEGENDARY) }
                rarity_filter.setOnClickListener {
                    when (rootView.rarity_filter_common.visibility) {
                        View.VISIBLE -> collapse()
                        View.GONE ->
                            if (rarity_filter.tag == true)
                                rarityClick(null)
                            else
                                expand()
                    }
                }
            }
        }
    }

    constructor(ctx: Context?) : this(ctx, null, 0) {
    }

    constructor(ctx: Context?, attrs: AttributeSet) : this(ctx, attrs, 0) {
    }

    private fun expand() {
        with(rootView) {
            with(ValueAnimator.ofInt(rarityBGMinWidth, rarityBGMaxWidth)) {
                duration = ANIM_DURATION
                addUpdateListener {
                    rarity_filter_bg.layoutParams.width = it.animatedValue as Int
                }
                addListener(object : Animator.AnimatorListener {
                    override fun onAnimationStart(p0: Animator?) {
                    }

                    override fun onAnimationRepeat(p0: Animator?) {
                    }

                    override fun onAnimationEnd(p0: Animator?) {
                        rarity_filter_common?.visibility = View.VISIBLE
                        rarity_filter_rare?.visibility = View.VISIBLE
                        rarity_filter_epic?.visibility = View.VISIBLE
                        rarity_filter_legendary?.visibility = View.VISIBLE
                    }

                    override fun onAnimationCancel(p0: Animator?) {
                    }
                })
                start()
            }
        }
    }

    private fun collapse() {
        with(rootView) {
            rarity_filter_common?.visibility = View.GONE
            rarity_filter_rare?.visibility = View.GONE
            rarity_filter_epic?.visibility = View.GONE
            rarity_filter_legendary?.visibility = View.GONE
            with(ValueAnimator.ofInt(rarityBGMinWidth, rarityBGMaxWidth)) {
                reverse()
                duration = ANIM_DURATION
                addUpdateListener {
                    rarity_filter_bg.layoutParams.width = it.animatedValue as Int
                }
                addListener(object : Animator.AnimatorListener {
                    override fun onAnimationStart(p0: Animator?) {
                    }

                    override fun onAnimationRepeat(p0: Animator?) {
                    }

                    override fun onAnimationEnd(p0: Animator?) {
                        rarity_filter_bg.layoutParams.width = rarityBGMinWidth
                    }

                    override fun onAnimationCancel(p0: Animator?) {
                    }
                })
                start()
            }
        }
    }

    private fun rarityClick(rarity: CardRarity?) {
        filterClick?.invoke(rarity)
        collapse()
        val icon = if (rarity == null) R.drawable.ic_rarity else R.drawable.ic_rarity_clear
        rootView.rarity_filter.apply {
            tag = rarity != null
            setImageResource(icon)
        }
    }

}