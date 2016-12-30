package com.ediposouza.teslesgendstracker

import android.content.Context
import android.os.Bundle
import android.support.annotation.IntegerRes
import android.support.design.widget.BottomSheetBehavior
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.NativeExpressAdView
import com.mixpanel.android.mpmetrics.MixpanelAPI

/**
 * Created by ediposouza on 01/11/16.
 */
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
    this.loadAd(createAdRequest(context))
}

fun NativeExpressAdView.load() {
    this.loadAd(createAdRequest(context))
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