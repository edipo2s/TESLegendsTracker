package com.ediposouza.teslesgendstracker.ui.widget

import android.content.Context
import android.os.Build.VERSION
import android.os.Build.VERSION_CODES
import android.util.AttributeSet
import android.view.WindowInsets
import android.widget.FrameLayout

/**
 * Created by EdipoSouza on 12/28/16.
 */
class InsetFrameLayout(ctx: Context?, attrs: AttributeSet?, defStyleAttr: Int) :
        FrameLayout(ctx, attrs, defStyleAttr) {

    private val mInsets = IntArray(4)

    constructor(ctx: Context?) : this(ctx, null, 0)

    constructor(ctx: Context?, attrs: AttributeSet) : this(ctx, attrs, 0)

    override fun onApplyWindowInsets(insets: WindowInsets): WindowInsets {
        if (VERSION.SDK_INT >= VERSION_CODES.LOLLIPOP) {
            mInsets[0] = insets.systemWindowInsetLeft
            mInsets[1] = insets.systemWindowInsetTop
            mInsets[2] = insets.systemWindowInsetRight
            return super.onApplyWindowInsets(insets.replaceSystemWindowInsets(0, 0, 0, insets.systemWindowInsetBottom))
        } else {
            return insets
        }
    }
}