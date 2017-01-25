package com.ediposouza.teslesgendstracker.ui.widget

import android.content.Context
import android.util.AttributeSet
import android.view.ViewGroup
import android.widget.FrameLayout
import com.ediposouza.teslesgendstracker.R
import com.ediposouza.teslesgendstracker.data.Class
import kotlinx.android.synthetic.main.widget_class_view.view.*

/**
 * Created by EdipoSouza on 11/2/16.
 */
class ClassView(ctx: Context?, attrs: AttributeSet?, defStyleAttr: Int) :
        FrameLayout(ctx, attrs, defStyleAttr) {

    init {
        inflate(context, R.layout.widget_class_view, rootView as ViewGroup)
        if (isInEditMode) {
            setClass(Class.SPELLSWORD)
        }
    }

    constructor(ctx: Context?) : this(ctx, null, 0)

    constructor(ctx: Context?, attrs: AttributeSet) : this(ctx, attrs, 0)

    fun setClass(cls: Class?) {
        class_view_attr1.setImageResource(cls?.attr1?.imageRes ?: 0)
        class_view_attr2.setImageResource(cls?.attr2?.imageRes ?: 0)
    }

}