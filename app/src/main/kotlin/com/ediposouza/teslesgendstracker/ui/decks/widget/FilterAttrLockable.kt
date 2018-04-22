package com.ediposouza.teslesgendstracker.ui.decks.widget

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.view.animation.AnimationUtils
import com.ediposouza.teslesgendstracker.R
import com.ediposouza.teslesgendstracker.data.CardAttribute
import com.ediposouza.teslesgendstracker.ui.widget.FilterAttr
import kotlinx.android.synthetic.main.widget_attributes_filter.view.*

/**
 * Created by EdipoSouza on 11/2/16.
 */
class FilterAttrLockable(ctx: Context?, attrs: AttributeSet?, defStyleAttr: Int) :
        FilterAttr(ctx, attrs, defStyleAttr) {

    var lockAttr1: CardAttribute? = null
    var lockAttr2: CardAttribute? = null
    var lockAttr3: CardAttribute? = null

    var onAttrLock: ((CardAttribute, CardAttribute, CardAttribute) -> Unit)? = null
    var onAttrUnlock: (() -> Unit)? = null

    constructor(ctx: Context?) : this(ctx, null, 0)

    constructor(ctx: Context?, attrs: AttributeSet) : this(ctx, attrs, 0)

    override fun attrClick(attr: CardAttribute, lockable: Boolean) {
        if (isLocked() && attr.isBasic && lockable) {
            return
        }
        filterClick?.invoke(attr)
    }

    override fun selectAttr(attr: CardAttribute, only: Boolean) {
        val strengthVisibility = (isLocked() && attr == lockAttr1) || isAttrEquals(attr, CardAttribute.STRENGTH)
        val intelligenceVisibility = (isLocked() && attr == lockAttr2) || isAttrEquals(attr, CardAttribute.INTELLIGENCE)
        val willpowerVisibility = (isLocked() && attr == lockAttr3) || isAttrEquals(attr, CardAttribute.WILLPOWER)
        updateVisibility(rootView.attr_filter_strength_indicator, strengthVisibility, only)
        updateVisibility(rootView.attr_filter_intelligence_indicator, intelligenceVisibility, only)
        updateVisibility(rootView.attr_filter_willpower_indicator, willpowerVisibility, only)
        updateVisibility(rootView.attr_filter_agility_indicator, isAttrEquals(attr, CardAttribute.AGILITY), only)
        updateVisibility(rootView.attr_filter_endurance_indicator, isAttrEquals(attr, CardAttribute.ENDURANCE), only)
        updateVisibility(rootView.attr_filter_dual_indicator, isAttrEquals(attr, CardAttribute.DUAL), only)
        updateVisibility(rootView.attr_filter_neutral_indicator, isAttrEquals(attr, CardAttribute.NEUTRAL), only)
    }

    private fun isAttrEquals(attr: CardAttribute, attrPos: CardAttribute): Boolean {
        if (isLocked()) {
            return attr == attrPos && attr != lockAttr1 && attr != lockAttr2
        } else {
            return attr == attrPos
        }
    }

    override fun unSelectAttr(attr: CardAttribute) {
        when (attr) {
            lockAttr1 -> rootView.attr_filter_strength_indicator.visibility = View.INVISIBLE
            lockAttr2 -> rootView.attr_filter_intelligence_indicator.visibility = View.INVISIBLE
            lockAttr3 -> rootView.attr_filter_willpower_indicator.visibility = View.INVISIBLE
            else -> super.unSelectAttr(attr)
        }
    }

    override fun isAttrSelected(attr: CardAttribute): Boolean {
        return if (isLocked()) when (attr) {
            lockAttr1 -> rootView.attr_filter_strength_indicator.visibility == View.VISIBLE
            lockAttr2 -> rootView.attr_filter_intelligence_indicator.visibility == View.VISIBLE
            lockAttr3 -> rootView.attr_filter_willpower_indicator.visibility == View.VISIBLE
            else -> super.isAttrSelected(attr)
        }
        else super.isAttrSelected(attr)
    }

    private fun isLocked() = lockAttr1 != null && lockAttr2 != null && lockAttr1 != lockAttr2 && lockAttr2 != lockAttr3

    fun isTrialLocked() = lockAttr1 != null && lockAttr2 != null && lockAttr3 != null &&
            lockAttr1 != lockAttr2 && lockAttr2 != lockAttr3

    fun lockAttr(attr: CardAttribute) {
        if (lockAttr1 == null && attr != lockAttr2 && attr.isBasic) {
            lockAttr1 = attr
            return
        }
        if (lockAttr2 == null && attr != lockAttr1 && attr.isBasic) {
            lockAttr2 = attr
            return
        }
        if (lockAttr3 == null && attr != lockAttr1 && attr != lockAttr2 && attr.isBasic) {
            lockAttr3 = attr
            return
        }
    }

    fun lockAttrs(dualAttr1: CardAttribute, dualAttr2: CardAttribute, dualAttr3: CardAttribute,
                  reselectBasicAttr: Boolean = true) {
        if (isLocked() && dualAttr3 == CardAttribute.NEUTRAL) {
            return
        }
        lockAttr(dualAttr1)
        lockAttr(dualAttr2)
        lockAttr(dualAttr3)
        if (isLocked()) {
            startAnimLock()
            if (reselectBasicAttr && lastAttrSelected.isBasic) {
                selectAttr(lastAttrSelected, true)
            }
            onAttrLock?.invoke(lockAttr1!!, lockAttr2!!, lockAttr3 ?: CardAttribute.NEUTRAL)
        }
    }

    fun unlockAttr(attr: CardAttribute) {
        if (lockAttr1 == attr || lockAttr2 == attr || lockAttr3 == attr) {
            if (lockAttr1 == attr) {
                lockAttr1 = lockAttr2
                lockAttr2 = lockAttr3
                lockAttr3 = null
            }
            if (lockAttr2 == attr) {
                lockAttr2 = lockAttr3
                lockAttr3 = null
            }
            if (lockAttr3 == attr) {
                lockAttr3 = null
            }
            startAnimUnlock()
            if (lastAttrSelected.isBasic) {
                selectAttr(lastAttrSelected, true)
            }
            if (lockAttr1 != null && lockAttr1 != CardAttribute.NEUTRAL
                    && lockAttr2 != null && lockAttr2 != CardAttribute.NEUTRAL) {
                startAnimLock()
                onAttrLock?.invoke(lockAttr1!!, lockAttr2!!, CardAttribute.NEUTRAL)
            } else {
                onAttrUnlock?.invoke()
            }
        }
    }

    private fun startAnimLock() {
        if (lockAttr2!!.ordinal > lockAttr3?.ordinal ?: CardAttribute.NEUTRAL.ordinal) {
            val lockAttrTmp = lockAttr2
            lockAttr2 = lockAttr3
            lockAttr3 = lockAttrTmp
        }
        if (lockAttr1!!.ordinal > lockAttr2!!.ordinal) {
            val lockAttrTmp = lockAttr1
            lockAttr1 = lockAttr2
            lockAttr2 = lockAttrTmp
        }
        val scaleDownAnimation = AnimationUtils.loadAnimation(context, R.anim.fab_scale_down)
        scaleDownAnimation.fillAfter = true
        rootView.attr_filter_strength?.startAnimation(scaleDownAnimation)
        rootView.attr_filter_intelligence?.startAnimation(scaleDownAnimation)
        rootView.attr_filter_willpower?.startAnimation(scaleDownAnimation)
        rootView.attr_filter_agility?.startAnimation(scaleDownAnimation)
        rootView.attr_filter_endurance?.startAnimation(scaleDownAnimation)
        val scaleUpAnimation = AnimationUtils.loadAnimation(context, R.anim.fab_scale_up)
        scaleUpAnimation.fillAfter = true
        rootView.attr_filter_strength?.setImageResource(lockAttr1?.imageRes ?: R.drawable.attr_strength)
        rootView.attr_filter_intelligence?.setImageResource(lockAttr2?.imageRes ?: R.drawable.attr_intelligence)
        rootView.attr_filter_strength?.startAnimation(scaleUpAnimation)
        rootView.attr_filter_intelligence?.startAnimation(scaleUpAnimation)
        rootView.attr_filter_strength?.setOnClickListener { attrClick(lockAttr1 ?: CardAttribute.STRENGTH, false) }
        rootView.attr_filter_intelligence?.setOnClickListener { attrClick(lockAttr2 ?: CardAttribute.INTELLIGENCE, false) }
        if (lockAttr3 != null) {
            rootView.attr_filter_willpower?.setImageResource(lockAttr3?.imageRes
                    ?: R.drawable.attr_willpower)
            rootView.attr_filter_willpower?.startAnimation(scaleUpAnimation)
            rootView.attr_filter_willpower?.setOnClickListener {
                attrClick(lockAttr3 ?: CardAttribute.WILLPOWER, false)
            }
        }
    }

    private fun startAnimUnlock() {
        val scaleDownAnimation = AnimationUtils.loadAnimation(context, R.anim.fab_scale_down)
        scaleDownAnimation.fillAfter = true
        rootView.attr_filter_strength?.startAnimation(scaleDownAnimation)
        rootView.attr_filter_intelligence?.startAnimation(scaleDownAnimation)
        rootView.attr_filter_strength?.setImageResource(R.drawable.attr_strength)
        rootView.attr_filter_intelligence?.setImageResource(R.drawable.attr_intelligence)
        rootView.attr_filter_willpower?.setImageResource(R.drawable.attr_willpower)
        val scaleUpAnimation = AnimationUtils.loadAnimation(context, R.anim.fab_scale_up)
        scaleUpAnimation.fillAfter = true
        rootView.attr_filter_strength?.startAnimation(scaleUpAnimation)
        rootView.attr_filter_intelligence?.startAnimation(scaleUpAnimation)
        rootView.attr_filter_willpower?.startAnimation(scaleUpAnimation)
        rootView.attr_filter_agility?.startAnimation(scaleUpAnimation)
        rootView.attr_filter_endurance?.startAnimation(scaleUpAnimation)
        rootView.attr_filter_strength?.setOnClickListener { attrClick(CardAttribute.STRENGTH, false) }
        rootView.attr_filter_intelligence?.setOnClickListener { attrClick(CardAttribute.INTELLIGENCE, false) }
        rootView.attr_filter_willpower?.setOnClickListener { attrClick(CardAttribute.WILLPOWER, false) }
    }

}