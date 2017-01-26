package com.ediposouza.teslesgendstracker.ui.util

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.content.Context
import android.support.annotation.ColorRes
import android.support.v4.content.ContextCompat
import android.support.v4.view.animation.FastOutLinearInInterpolator
import android.view.View
import android.view.ViewAnimationUtils
import com.ediposouza.teslesgendstracker.R

/**
 * Created by ediposouza on 25/01/17.
 */
class GUIUtils {

    companion object {

        fun animateRevealShow(ctx: Context, view: View, startRadius: Int, @ColorRes color: Int,
                              x: Int, y: Int, listener: () -> Unit) {
            val finalRadius = Math.hypot(view.width.toDouble(), view.height.toDouble()).toFloat()
            ViewAnimationUtils.createCircularReveal(view, x, y, startRadius.toFloat(), finalRadius).apply {
                duration = ctx.resources.getInteger(R.integer.anim_slide_duration).toLong()
                startDelay = 80;
                interpolator = FastOutLinearInInterpolator()
                addListener(object : AnimatorListenerAdapter() {
                    override fun onAnimationStart(animation: Animator?) {
                        view.setBackgroundColor(ContextCompat.getColor(ctx, color))
                    }

                    override fun onAnimationEnd(animation: Animator?) {
                        view.visibility = View.VISIBLE
                        listener()
                    }

                })
                start()
            }
        }

        fun animateRevealHide(ctx: Context, view: View, @ColorRes color: Int,
                              finalRadius: Int, listener: () -> Unit) {
            val cx = (view.left + view.right) / 2
            val cy = (view.top + view.bottom) / 2
            val initialRadius = view.width

            val anim = ViewAnimationUtils.createCircularReveal(view, cx, cy, initialRadius.toFloat(), finalRadius.toFloat())
            anim.addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationStart(animation: Animator) {
                    super.onAnimationStart(animation)
                    view.setBackgroundColor(ctx.resources.getColor(color))
                }

                override fun onAnimationEnd(animation: Animator) {
                    super.onAnimationEnd(animation)
                    listener()
                    view.visibility = View.INVISIBLE
                }
            })
            anim.duration = ctx.resources.getInteger(R.integer.anim_slide_duration).toLong()
            anim.start()
        }

    }

}