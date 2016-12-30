package com.ediposouza.teslesgendstracker.ui.util

import android.content.Context
import android.support.design.widget.CoordinatorLayout
import android.support.v4.view.ViewCompat
import android.util.AttributeSet
import android.view.View
import com.ediposouza.teslesgendstracker.ui.base.CmdUpdateVisibility
import org.greenrobot.eventbus.EventBus
import timber.log.Timber


/**
 * Created by EdipoSouza on 11/12/16.
 */
class AutoHideBehaviour(context: Context?, attrs: AttributeSet?) :
        CoordinatorLayout.Behavior<View>(context, attrs) {

    val eventBus: EventBus by lazy { EventBus.getDefault() }

    constructor() : this(null, null)

    override fun onStartNestedScroll(coordinatorLayout: CoordinatorLayout, child: View,
                                     directTargetChild: View, target: View, nestedScrollAxes: Int): Boolean {
        // Ensure we react to vertical scrolling
        return nestedScrollAxes == ViewCompat.SCROLL_AXIS_VERTICAL ||
                super.onStartNestedScroll(coordinatorLayout, child, directTargetChild, target, nestedScrollAxes)
    }

    override fun onNestedScroll(coordinatorLayout: CoordinatorLayout, child: View,
                                target: View, dxConsumed: Int, dyConsumed: Int, dxUnconsumed: Int, dyUnconsumed: Int) {
        super.onNestedScroll(coordinatorLayout, child, target, dxConsumed, dyConsumed, dxUnconsumed, dyUnconsumed)
        if (dyConsumed > 0) {
            // User scrolled down and the FAB is currently visible -> hide the FAB
            Timber.d("Scroll Down")
            eventBus.post(CmdUpdateVisibility(false))
        } else if (dyConsumed < 0) {
            // User scrolled up and the FAB is currently not visible -> show the FAB
            Timber.d("Scroll Up")
            eventBus.post(CmdUpdateVisibility(true))
        }
    }

}