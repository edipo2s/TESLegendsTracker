/*
 * Beauty Date - http://www.beautydate.com
 * Created by ediposouza on 4/12/2016
 * Copyright (c) 2016 Beauty Date. All rights reserved.
 */

package com.ediposouza.teslesgendstracker.ui.base.command

import android.support.annotation.IntDef
import android.support.annotation.StringRes
import android.support.design.widget.Snackbar

/**
 * Created by ediposouza on 3/3/16.
 */
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

    @Snackbar.Duration
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
            actionTextRes = android.R.string.ok
            action = { }
        }
    }

    constructor(@SnackbarType type: Long, @StringRes msgRes: Int) : this(type) {
        this.msgRes = msgRes
    }

    constructor(@SnackbarType type: Long, msg: String) : this(type) {
        this.msg = msg
    }

    fun withDuration(@Snackbar.Duration duration: Int): CmdShowSnackbarMsg {
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
