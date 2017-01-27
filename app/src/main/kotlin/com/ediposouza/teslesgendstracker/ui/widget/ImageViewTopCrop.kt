package com.ediposouza.teslesgendstracker.ui.widget

import android.content.Context
import android.util.AttributeSet
import android.widget.ImageView
import com.ediposouza.teslesgendstracker.R

/**
 * Created by EdipoSouza on 1/21/17.
 */
class ImageViewTopCrop(context: Context, attrs: AttributeSet?, defStyleAttr: Int) :
        ImageView(context, attrs, defStyleAttr) {

    var imageTopMargin = 0f

    init {
        val a = context.obtainStyledAttributes(attrs, R.styleable.ImageViewTopCrop)
        imageTopMargin = a.getFloat(R.styleable.ImageViewTopCrop_imageTopMargin, 0f)
        a.recycle()
        scaleType = ScaleType.MATRIX
    }

    constructor(context: Context) : this(context, null, 0)

    constructor(context: Context, attrs: AttributeSet) : this(context, attrs, 0)

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)
        recomputeImgMatrix()
    }

    override fun setFrame(l: Int, t: Int, r: Int, b: Int): Boolean {
        recomputeImgMatrix()
        return super.setFrame(l, t, r, b)
    }

    private fun recomputeImgMatrix() {
        val drawable = drawable ?: return
        val matrix = imageMatrix

        val scale: Float
        val viewWidth = width - paddingLeft - paddingRight
        val viewHeight = height - paddingTop - paddingBottom
        val drawableWidth = drawable.intrinsicWidth
        val drawableHeight = drawable.intrinsicHeight

        if (drawableWidth * viewHeight > drawableHeight * viewWidth) {
            scale = viewHeight.toFloat() / drawableHeight.toFloat()
        } else {
            scale = viewWidth.toFloat() / drawableWidth.toFloat()
        }

        matrix.setScale(scale, scale)
        if (imageTopMargin > 0) {
            matrix.postTranslate(0f, drawableHeight * -1 * imageTopMargin)
        }

        imageMatrix = matrix
    }

}