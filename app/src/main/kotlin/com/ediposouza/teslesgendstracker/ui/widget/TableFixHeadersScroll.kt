package com.ediposouza.teslesgendstracker.ui.widget

import android.content.Context
import android.os.Handler
import android.text.format.DateUtils
import android.util.AttributeSet
import com.ediposouza.teslesgendstracker.ui.base.CmdUpdateVisibility
import com.inqbarna.tablefixheaders.TableFixHeaders
import org.greenrobot.eventbus.EventBus
import timber.log.Timber

/**
 * Created by EdipoSouza on 1/14/17.
 */
class TableFixHeadersScroll(ctx: Context, attrs: AttributeSet?) : TableFixHeaders(ctx, attrs) {

    private val runnableUp = {
        Timber.d("Up")
        EventBus.getDefault().post(CmdUpdateVisibility(true))
    }

    private val runnableDown = {
        Timber.d("Down")
        EventBus.getDefault().post(CmdUpdateVisibility(false))
    }

    private val scrollHandler by lazy { Handler() }

    constructor(ctx: Context) : this(ctx, null)

    override fun scrollBy(x: Int, y: Int) {
        super.scrollBy(x, y)
        with(scrollHandler) {
            if (y < 0) {
                removeCallbacksAndMessages(null)
                postDelayed(runnableUp, DateUtils.SECOND_IN_MILLIS / 2)
            } else {
                removeCallbacksAndMessages(null)
                postDelayed(runnableDown, DateUtils.SECOND_IN_MILLIS / 2)
            }
        }
    }
}