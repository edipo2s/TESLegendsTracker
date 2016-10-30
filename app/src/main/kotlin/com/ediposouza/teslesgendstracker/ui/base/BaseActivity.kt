package com.ediposouza.teslesgendstracker.ui.base

import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v7.app.AppCompatActivity
import com.ediposouza.teslesgendstracker.ui.base.command.CmdShowSnackbarMsg
import icepick.Icepick
import kotlinx.android.synthetic.main.activity_dash.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe

/**
 * Created by EdipoSouza on 10/30/16.
 */
open class BaseActivity : AppCompatActivity(){

    protected val mEventBus by lazy { EventBus.getDefault() }

    private var snackbar: Snackbar? = null;

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Icepick.restoreInstanceState(this, savedInstanceState)
    }

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)
        setSupportActionBar(dash_toolbar)
        supportActionBar?.title = ""
    }

    public override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        Icepick.saveInstanceState(this, outState)
    }

    override fun onStart() {
        super.onStart()
        mEventBus.register(this)
    }

    override fun onStop() {
        super.onStop()
        mEventBus.unregister(this)
    }

    @SuppressWarnings("ResourceType")
    @Subscribe
    fun showSnackMsg(cmdShowSnackbarMsg: CmdShowSnackbarMsg) {
        snackbar?.dismiss()
        val msgRes = cmdShowSnackbarMsg.msgRes
        val msg = if (msgRes > 0) getString(msgRes) else cmdShowSnackbarMsg.msg
        snackbar = Snackbar.make(dash_coordinatorLayout, msg, cmdShowSnackbarMsg.duration)
        if (cmdShowSnackbarMsg.action != null) {
            val actionTextRes = cmdShowSnackbarMsg.actionTextRes
            val actionText = if (actionTextRes > 0) getString(actionTextRes) else cmdShowSnackbarMsg.actionText
            snackbar?.setAction(actionText, cmdShowSnackbarMsg.action)
        }
        snackbar?.show()
    }

}