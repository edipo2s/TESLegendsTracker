package com.ediposouza.teslesgendstracker.util

import android.app.AlertDialog
import android.content.Context
import android.os.Bundle
import android.support.annotation.IntegerRes
import android.support.design.widget.BottomSheetBehavior
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ListPopupWindow
import android.widget.Spinner
import com.ediposouza.teslesgendstracker.App
import com.ediposouza.teslesgendstracker.R
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.NativeExpressAdView
import com.mixpanel.android.mpmetrics.MixpanelAPI
import org.jetbrains.anko.AlertDialogBuilder

/**
 * Created by ediposouza on 01/11/16.
 */
fun Context.alertThemed(
        message: Int,
        title: Int? = null,
        theme: Int = 0,
        init: (AlertDialogBuilder.() -> Unit)? = null
) = AlertDialogBuilder(this).apply {
    val builderField = AlertDialogBuilder::class.java.getDeclaredField("builder")
    builderField.isAccessible = true
    builderField.set(this, AlertDialog.Builder(ctx, theme))
    if (title != null) title(title)
    message(message)
    if (init != null) init()
}

fun Context.alertThemed(
        message: String,
        title: String? = null,
        theme: Int = 0,
        init: (AlertDialogBuilder.() -> Unit)? = null
) = AlertDialogBuilder(this).apply {
    val builderField = AlertDialogBuilder::class.java.getDeclaredField("builder")
    builderField.isAccessible = true
    builderField.set(this, AlertDialog.Builder(ctx, theme))
    if (title != null) title(title)
    message(message)
    if (init != null) init()
}

fun String.toIntSafely(): Int {
    if (this.trim().isEmpty())
        return 0
    this.forEach {
        if (!it.isDigit())
            return 0
    }
    return Integer.parseInt(this)
}

fun ViewGroup.inflate(@IntegerRes resource: Int): View {
    return LayoutInflater.from(context).inflate(resource, this, false)
}

fun BottomSheetBehavior<*>.toggleExpanded() {
    this.state = if (this.state == BottomSheetBehavior.STATE_COLLAPSED)
        BottomSheetBehavior.STATE_EXPANDED else BottomSheetBehavior.STATE_COLLAPSED
}

fun AdView.load() {
    if (App.hasUserDonate()) {
        layoutParams = layoutParams.apply { height = 0 }
    } else {
        loadAd(createAdRequest(context))
    }
}

fun NativeExpressAdView.load() {
    if (App.hasUserDonate()) {
        layoutParams = layoutParams.apply { height = 0 }
    } else {
        loadAd(createAdRequest(context))
    }
}

private fun createAdRequest(context: Context): AdRequest {
    val devicesId = context.resources.getStringArray(R.array.testing_devices)
    val adRequestBuilder = AdRequest.Builder()
    for (deviceId in devicesId) {
        adRequestBuilder.addTestDevice(deviceId)
    }
    return adRequestBuilder.build()
}

fun MixpanelAPI.trackBundle(eventName: String, bundle: Bundle) {
    trackMap(eventName, bundle.keySet().map { it to bundle[it] }.toMap())
}


fun Spinner.limitHeight(lines: Int? = null) {
    val displayHeight = context.resources.displayMetrics.heightPixels
    val itemHeight = context.resources.getDimensionPixelSize(R.dimen.material_design_default_height)
    Spinner::class.java.getDeclaredField("mPopup")
            ?.apply { isAccessible = true }?.get(this)
            ?.apply popup@ {
                IntArray(2).apply {
                    getLocationOnScreen(this)
                    val listPopupWindow = this@popup as ListPopupWindow
                    listPopupWindow.height = if (lines != null) lines * itemHeight else displayHeight - get(1)
                }
            }
}
