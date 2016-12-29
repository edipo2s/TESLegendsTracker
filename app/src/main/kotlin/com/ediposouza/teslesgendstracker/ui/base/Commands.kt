package com.ediposouza.teslesgendstracker.ui.base

import android.R
import android.support.annotation.IntDef
import android.support.annotation.StringRes
import android.support.design.widget.BaseTransientBottomBar
import android.support.design.widget.Snackbar
import com.ediposouza.teslesgendstracker.data.Attribute
import com.ediposouza.teslesgendstracker.data.Class

/**
 * Created by EdipoSouza on 11/6/16.
 */
class CmdShowTabs

class CmdShowLogin

class CmdLoginSuccess

class CmdUpdateDeckAndShowDeck

data class CmdShowCardsByAttr(val attr: Attribute)

data class CmdShowDecksByClasses(val classes: List<Class>)

data class CmdUpdateRarityMagikaFiltersPosition(val high: Boolean)

data class CmdUpdateVisibility(val show: Boolean)

class CmdShowSnackbarMsg private constructor(type: Long) {

    companion object {

        const val TYPE_INFO = 0L
        const val TYPE_INFO_FIXED = 1L
        const val TYPE_ERROR = 2L
    }

    @IntDef(TYPE_INFO, TYPE_INFO_FIXED, TYPE_ERROR)
    annotation class SnackbarType

    var msg: String = ""

    @StringRes
    var msgRes: Int = 0

    @BaseTransientBottomBar.Duration
    var duration: Int = 0
        private set

    var actionText: String? = null
        private set

    @StringRes
    var actionTextRes: Int = 0
        private set

    var action: (() -> Unit)? = null
        private set

    init {
        duration = if (type == TYPE_INFO) Snackbar.LENGTH_LONG else Snackbar.LENGTH_INDEFINITE
        if (type == TYPE_ERROR) {
            actionTextRes = R.string.ok
            action = { }
        }
    }

    constructor(@SnackbarType type: Long, @StringRes msgRes: Int) : this(type) {
        this.msgRes = msgRes
    }

    constructor(@SnackbarType type: Long, msg: String) : this(type) {
        this.msg = msg
    }

    fun withDuration(@BaseTransientBottomBar.Duration duration: Int): CmdShowSnackbarMsg {
        this.duration = duration
        return this
    }

    fun withAction(@StringRes actionTextRes: Int, action: () -> Unit): CmdShowSnackbarMsg {
        this.actionTextRes = actionTextRes
        this.action = action
        return this
    }

    fun withAction(actionText: String, action: () -> Unit): CmdShowSnackbarMsg {
        this.actionText = actionText
        this.action = action
        return this
    }

}