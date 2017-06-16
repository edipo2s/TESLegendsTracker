package com.ediposouza.teslesgendstracker.util

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.support.annotation.LayoutRes
import android.support.design.widget.BottomSheetBehavior
import android.support.v4.app.Fragment
import android.support.v4.content.ContextCompat
import android.view.*
import android.widget.ImageView
import android.widget.ListPopupWindow
import android.widget.Spinner
import com.bumptech.glide.Glide
import com.bumptech.glide.load.Transformation
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.engine.Resource
import com.bumptech.glide.load.resource.bitmap.BitmapResource
import com.bumptech.glide.load.resource.drawable.GlideDrawable
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.ediposouza.teslesgendstracker.App
import com.ediposouza.teslesgendstracker.PREF_USER_LANGUAGE
import com.ediposouza.teslesgendstracker.R
import com.ediposouza.teslesgendstracker.data.Card
import com.ediposouza.teslesgendstracker.data.PatchChange
import com.ediposouza.teslesgendstracker.ui.DashActivity
import com.ediposouza.teslesgendstracker.ui.util.CircleTransform
import com.firebase.ui.storage.images.FirebaseImageLoader
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.InterstitialAd
import com.google.android.gms.ads.NativeExpressAdView
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.mixpanel.android.mpmetrics.MixpanelAPI
import org.jetbrains.anko.*
import org.jsoup.Jsoup
import org.threeten.bp.LocalDate
import org.threeten.bp.YearMonth
import timber.log.Timber
import java.io.File
import java.io.FileOutputStream
import java.util.*

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

fun ViewGroup.inflate(@LayoutRes resource: Int): View {
    return LayoutInflater.from(context).inflate(resource, this, false)
}

fun <T : View> BottomSheetBehavior<T>.toggleExpanded() {
    this.state = BottomSheetBehavior.STATE_EXPANDED
            .takeIf { this.state == BottomSheetBehavior.STATE_COLLAPSED } ?: BottomSheetBehavior.STATE_COLLAPSED
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

fun InterstitialAd.load(context: Context) {
    loadAd(createAdRequest(context))
}

private fun createAdRequest(context: Context): AdRequest {
    val devicesId = context.resources.getStringArray(R.array.testing_devices)
    val adRequestBuilder = AdRequest.Builder()
    for (deviceId in devicesId) {
        adRequestBuilder.addTestDevice(deviceId)
    }
    return adRequestBuilder.build()
}

@Suppress("unused")
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
                    val ln = lines ?: 0
                    listPopupWindow.height = (ln * itemHeight).takeIf { lines != null } ?: displayHeight - get(1)
                }
            }
}

fun ImageView.loadFromUrl(imageUrl: String, placeholder: Drawable? = null,
                          circleTransformation: Boolean = false, onImageDownload: (() -> Unit)? = null) {
    if (imageUrl.startsWith("http")) {
        with(Glide.with(context).load(imageUrl)) {
            crossFade(500)
            if (placeholder != null) {
                placeholder(placeholder)
            }
            if (circleTransformation) {
                transform(CircleTransform(context))
            }
            listener(object : RequestListener<String, GlideDrawable> {
                override fun onException(e: Exception?, model: String?, target: Target<GlideDrawable>?, isFirstResource: Boolean): Boolean {
                    return false
                }

                override fun onResourceReady(resource: GlideDrawable?, model: String?, target: Target<GlideDrawable>?, isFromMemoryCache: Boolean, isFirstResource: Boolean): Boolean {
                    onImageDownload?.invoke()
                    return false
                }

            })
            into(this@loadFromUrl)
        }
    } else {
        setImageResource(R.drawable.article_cover)
    }
}

fun ImageView.loadFromCard(card: Card, transform: ((Bitmap) -> Bitmap)? = null, onNotFound: (() -> Unit)? = null) {
    loadFromCard(card, 0, transform, onNotFound)
}

fun ImageView.loadFromCard(card: Card, shoutLevel: Int = 0, transform: ((Bitmap) -> Bitmap)? = null, onNotFound: (() -> Unit)? = null) {
    with(card) {
        if (name.isEmpty() || shortName.isEmpty()) {
            setImageResource(R.drawable.card_back)
        } else {
            val cardShortName = "${shortName}_lv$shoutLevel".takeIf { shoutLevel > 1 } ?: shortName
            loadFromCard(set.toString(), attr.name, cardShortName, transform, onNotFound)
        }
    }
}

fun ImageView.loadFromCard(cardSet: String, cardAttr: String, cardShortName: String,
                           transform: ((Bitmap) -> Bitmap)? = null, onNotFound: (() -> Unit)? = null) {
    if (cardShortName.isEmpty()) {
        setImageResource(R.drawable.card_back)
        return
    }
    val setName = cardSet.toLowerCase().capitalize()
    val attrName = cardAttr.toLowerCase().capitalize()
    val imagePath = "${Card.CARD_PATH}/$setName/$attrName/$cardShortName.webp"
    val remotePath = imagePath.takeIf { cardShortName.contains("_201") } ?: "v${context.getCurrentVersion()}/$imagePath"
    Timber.d("Local: $imagePath - Remote: $remotePath")
    Glide.with(context)
            .using(FirebaseImageLoader())
            .load(FirebaseStorage.getInstance().reference.child(remotePath))
            .placeholder(getLocalCardBitmap(context, imagePath, transform))
            .crossFade()
            .bitmapTransform(object : Transformation<Bitmap> {
                override fun transform(resource: Resource<Bitmap>, outWidth: Int, outHeight: Int): Resource<Bitmap> {
                    return transform?.let {
                        BitmapResource.obtain(transform.invoke(resource.get()), Glide.get(context).getBitmapPool())
                    } ?: resource
                }

                override fun getId(): String = imagePath

            })
            .diskCacheStrategy(DiskCacheStrategy.SOURCE)
            .listener(object : RequestListener<StorageReference, GlideDrawable> {
                override fun onResourceReady(resource: GlideDrawable?, model: StorageReference?, target: Target<GlideDrawable>?, isFromMemoryCache: Boolean, isFirstResource: Boolean): Boolean {
                    return false
                }

                override fun onException(e: java.lang.Exception?, model: StorageReference?, target: Target<GlideDrawable>?, isFirstResource: Boolean): Boolean {
                    onNotFound?.invoke()
                    return false
                }
            })
            .into(this)
}

fun ImageView.loadFromPatch(patch: PatchChange, patchUuid: String, newImage: Boolean) {
    with(patch) {
        loadFromCard(set.capitalize(), attr.capitalize(), shortName + "_" + patchUuid) {
            if (newImage) {
                loadFromCard(set.capitalize(), attr.capitalize(), shortName)
            }
        }
    }
}

private fun getLocalCardBitmap(context: Context, imagePath: String, transform: ((Bitmap) -> Bitmap)? = null): Drawable {
    return try {
        var cardBitmap = BitmapFactory.decodeStream(context.resources.assets.open(imagePath))
        transform?.apply {
            cardBitmap = invoke(cardBitmap)
        }
        BitmapDrawable(context.resources, cardBitmap)
    } catch (e: Exception) {
        ContextCompat.getDrawable(context, R.drawable.card_back)
    }
}

fun LocalDate.toYearMonth(): YearMonth = YearMonth.of(year, month)

@Suppress("DEPRECATION")
fun Activity.changeAppLanguage(language: String) {
    Timber.d("Changing language to: $language")
    var locale = if (!language.contains("-")) Locale(language) else
        Locale(language.substringBefore("-"), language.substringAfter("-"))
    try {
        with(locale) {
            Locale.setDefault(this)
            resources.updateConfiguration(Configuration().apply {
                setLocale(this@with)
            }, null)
        }
    } catch (e: Exception) {
        Timber.e(e)
    }
    defaultSharedPreferences.edit().putString(PREF_USER_LANGUAGE, language).apply()
    App.currentLanguage = language
    startActivity(intentFor<DashActivity>().clearTask().newTask())
    finish()
}

fun Context.checkLastVersion(onNewVersion: (String?) -> Unit) {
    doAsync {
        try {
            val newer = Jsoup.connect(getString(R.string.playstore_url_format, packageName))
                    .timeout(resources.getInteger(R.integer.jsoup_timeout))
                    .userAgent(getString(R.string.jsoup_user_agent))
                    .referrer(getString(R.string.jsoup_referrer))
                    .get()
                    .select("div[itemprop=softwareVersion]")
                    .first()
                    .ownText()
            val actualVersion = getCurrentVersion().replace(".", "")
            val newerVersion = newer.replace(".", "")
            Timber.d("Versions - remote: %s, local: %s", newerVersion, actualVersion)
            uiThread {
                if (Integer.parseInt(newerVersion) > Integer.parseInt(actualVersion)) {
                    onNewVersion(newer)
                }
            }
        } catch (e: Exception) {
            Timber.e(e.message)
        }
    }
}

fun Context.getCurrentVersion(): String {
    val pInfo = packageManager.getPackageInfo(packageName, 0)
    return pInfo.versionName
}

fun Context.hasNavigationBar(): Boolean {
    val hasBackKey = KeyCharacterMap.deviceHasKey(KeyEvent.KEYCODE_BACK);
    val hasHomeKey = KeyCharacterMap.deviceHasKey(KeyEvent.KEYCODE_HOME);
    return (!(hasBackKey && hasHomeKey))
}

fun Activity.hasPermission(permission: String): Boolean {
    return ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED
}

fun Fragment.hasPermission(permission: String): Boolean {
    return ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED
}

fun View.setBottomPaddingForNavigationBar() {
    var result = 0
    val resourceId = resources.getIdentifier("navigation_bar_height", "dimen", "android")
    if (resourceId > 0) {
        result = resources.getDimensionPixelSize(resourceId)
    }
    setPadding(paddingLeft, paddingTop, paddingRight, result)
}

fun ByteArray.saveToFile(file: File) {
    if (file.exists()) {
        file.delete()
    } else {
        file.createNewFile()
    }
    FileOutputStream(file).apply {
        write(this@saveToFile)
        flush()
        close()
    }
}
