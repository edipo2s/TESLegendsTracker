package com.ediposouza.teslesgendstracker.ui.cards

import android.Manifest
import android.app.WallpaperManager
import android.graphics.Bitmap
import android.os.Bundle
import android.os.Environment
import android.support.v4.app.ActivityCompat
import android.view.MenuItem
import android.widget.ImageView
import com.ediposouza.teslesgendstracker.R
import com.ediposouza.teslesgendstracker.data.Card
import com.ediposouza.teslesgendstracker.ui.base.BaseActivity
import com.ediposouza.teslesgendstracker.ui.base.CmdShowSnackbarMsg
import com.ediposouza.teslesgendstracker.util.*
import kotlinx.android.synthetic.main.activity_card_full_art.*
import java.io.File
import java.io.FileOutputStream
import java.io.IOException


/**
 * Created by EdipoSouza on 4/2/17.
 */
class CardFullArtActivity : BaseActivity() {

    companion object {

        const val EXTRA_CARD = "cardExtra"

    }

    private val RC_WRITE_STORAGE_PERMISSION = 125
    private val PERMISSION_WRITE_STORAGE = Manifest.permission.WRITE_EXTERNAL_STORAGE

    val card: Card? by lazy { intent.getParcelableExtra<Card>(EXTRA_CARD) }
    var cardImage: Bitmap? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_card_full_art)
        ActivityCompat.postponeEnterTransition(this)

        snackbarNeedMargin = false
        if (hasNavigationBar()) {
            card_full_content.setBottomPaddingForNavigationBar()
        }
        card?.getCardFullArtBitmap(this) { image ->
            if (image != null) {
                cardImage = image
                card_full_art_iv.setImageBitmap(image)
                card_full_art_iv.scaleType = ImageView.ScaleType.CENTER_CROP
                ActivityCompat.startPostponedEnterTransition(this)
            }
        }
    }

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)
        card_full_art_ads_view.load()
        card_full_toolbar.inflateMenu(R.menu.menu_card_full_art)
        card_full_toolbar.setOnMenuItemClickListener { item ->
            when (item?.itemId) {
                R.id.menu_wallpaper -> setCardFullImageAsWallpaper(item)
                R.id.menu_download -> downloadCardFullImage(item)
            }
            true
        }
        MetricsManager.trackScreen(MetricScreen.SCREEN_CARD_FULL_ART())
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        if (requestCode == RC_WRITE_STORAGE_PERMISSION) {
            if (hasPermission(PERMISSION_WRITE_STORAGE)) {
                downloadCardFullImage(null)
            } else {
                if (ActivityCompat.shouldShowRequestPermissionRationale(this, PERMISSION_WRITE_STORAGE)) {
                    eventBus.post(CmdShowSnackbarMsg(CmdShowSnackbarMsg.TYPE_INFO, R.string.card_full_permission_write_storage)
                            .withAction(android.R.string.ok, { requestWriteStoragePermission() }))
                } else {
                    eventBus.post(CmdShowSnackbarMsg(CmdShowSnackbarMsg.TYPE_INFO, R.string.card_full_permission_write_storage_denied))
                }
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    private fun requestWriteStoragePermission() {
        requestPermissions(arrayOf(PERMISSION_WRITE_STORAGE), RC_WRITE_STORAGE_PERMISSION)
    }

    private fun setCardFullImageAsWallpaper(item: MenuItem?) {
        item?.isEnabled = false
        val wpm = WallpaperManager.getInstance(applicationContext)
        try {
            with(card_full_art_iv) {
                isDrawingCacheEnabled = true
                buildDrawingCache()
                val bitmap = drawingCache
                wpm.setBitmap(bitmap)
                isDrawingCacheEnabled = false
                eventBus.post(CmdShowSnackbarMsg(CmdShowSnackbarMsg.TYPE_INFO, R.string.card_full_wallpaper_set)
                        .withAction(android.R.string.ok, {}))
                MetricsManager.trackAction(MetricAction.ACTION_CARD_FULL_ART_SET_WALLPAPER(card!!))
            }
        } catch (e: IOException) {
            eventBus.post(CmdShowSnackbarMsg(CmdShowSnackbarMsg.TYPE_ERROR, e.message ?: ""))
        } finally {
            item?.isEnabled = true
        }
    }

    private fun downloadCardFullImage(item: MenuItem?) {
        if (!hasPermission(PERMISSION_WRITE_STORAGE)) {
            requestWriteStoragePermission()
            return
        }

        item?.isEnabled = false
        val dirOut = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
        FileOutputStream(File(dirOut, "${card?.name}.png")).apply {
            try {
                cardImage?.compress(Bitmap.CompressFormat.PNG, 100, this)
                flush()
                close()
                eventBus.post(CmdShowSnackbarMsg(CmdShowSnackbarMsg.TYPE_INFO, R.string.card_full_downloaded)
                        .withAction(android.R.string.ok, {}))
                MetricsManager.trackAction(MetricAction.ACTION_CARD_FULL_ART_DOWNLOAD(card!!))
            } catch (e: Exception) {
                eventBus.post(CmdShowSnackbarMsg(CmdShowSnackbarMsg.TYPE_ERROR, e.message ?: ""))
            } finally {
                item?.isEnabled = true
            }
        }
    }

}