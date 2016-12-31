/*
 * Beauty Date - http://www.beautydate.com
 * Created by ediposouza on 1/22/2016
 * Copyright (c) 2016 Beauty Date. All rights reserved.
 */

package com.ediposouza.teslesgendstracker.ui.widget

import android.content.Context
import android.support.design.widget.CoordinatorLayout
import android.support.v4.view.NestedScrollingParent
import android.util.AttributeSet
import android.view.View

/**
 * Created by ediposouza on 1/22/16.
 */
class NestedCoordinatorLayout(context: Context, attrs: AttributeSet?, defStyleAttr: Int) :
        CoordinatorLayout(context, attrs, defStyleAttr), NestedScrollingParent {

    constructor(context: Context) : this(context, null, 0)

    constructor(context: Context, attrs: AttributeSet) : this(context, attrs, 0)

    override fun onStartNestedScroll(child: View, target: View, nestedScrollAxes: Int): Boolean {
        parentCoordinatorLayout.onStartNestedScroll(child, target, nestedScrollAxes)
        return super.onStartNestedScroll(child, target, nestedScrollAxes)
    }

    private val parentCoordinatorLayout: CoordinatorLayout
        get() = parent as CoordinatorLayout

    override fun onNestedScrollAccepted(child: View, target: View, nestedScrollAxes: Int) {
        parentCoordinatorLayout.onNestedScrollAccepted(child, target, nestedScrollAxes)
        super.onNestedScrollAccepted(child, target, nestedScrollAxes)
    }

    override fun onStopNestedScroll(target: View) {
        parentCoordinatorLayout.onStopNestedScroll(target)
        super.onStopNestedScroll(target)
    }

    override fun onNestedScroll(target: View, dxConsumed: Int, dyConsumed: Int, dxUnconsumed: Int, dyUnconsumed: Int) {
        parentCoordinatorLayout.onNestedScroll(target, dxConsumed, dyConsumed, dxUnconsumed, dyUnconsumed)
        super.onNestedScroll(target, dxConsumed, dyConsumed, dxUnconsumed, dyUnconsumed)
    }

    override fun onNestedPreScroll(target: View, dx: Int, dy: Int, consumed: IntArray) {
        parentCoordinatorLayout.onNestedPreScroll(target, dx, dy, consumed)
        super.onNestedPreScroll(target, dx, dy, consumed)
    }

    override fun onNestedFling(target: View, velocityX: Float, velocityY: Float, consumed: Boolean): Boolean {
        parentCoordinatorLayout.onNestedFling(target, velocityX, velocityY, consumed)
        return super.onNestedFling(target, velocityX, velocityY, consumed)
    }

    override fun onNestedPreFling(target: View, velocityX: Float, velocityY: Float): Boolean {
        parentCoordinatorLayout.onNestedPreFling(target, velocityX, velocityY)
        return super.onNestedPreFling(target, velocityX, velocityY)
    }

}
