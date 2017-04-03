package com.ediposouza.teslesgendstracker.ui.cards

import android.app.WallpaperManager
import android.graphics.Bitmap
import android.os.Bundle
import android.os.Environment
import android.support.v4.app.ActivityCompat
import com.ediposouza.teslesgendstracker.R
import com.ediposouza.teslesgendstracker.data.Card
import com.ediposouza.teslesgendstracker.ui.base.BaseActivity
import com.ediposouza.teslesgendstracker.ui.base.CmdShowSnackbarMsg
import com.ediposouza.teslesgendstracker.util.MetricScreen
import com.ediposouza.teslesgendstracker.util.MetricsManager
import com.ediposouza.teslesgendstracker.util.load
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

    val card: Card? by lazy { intent.getParcelableExtra<Card>(EXTRA_CARD) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_card_full_art)
        ActivityCompat.postponeEnterTransition(this)
        card?.getCardFullArtBitmap(this) { cardImage ->
            if (cardImage != null) {
                card_full_art_iv.setImageBitmap(cardImage)
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
                R.id.menu_wallpaper -> setCardFullImageAsWallpaper()
                R.id.menu_download -> downloadCardFullImage()
            }
            true
        }
        MetricsManager.trackScreen(MetricScreen.SCREEN_CARD_FULL_ART())
    }

    private fun setCardFullImageAsWallpaper() {
        val bitmap = card_full_art_iv.getDrawingCache()
        val wpm = WallpaperManager.getInstance(applicationContext)
        try {
            wpm.setBitmap(bitmap)
        } catch (e: IOException) {
            eventBus.post(CmdShowSnackbarMsg(CmdShowSnackbarMsg.TYPE_ERROR, e.message ?: ""))
        }
    }

    private fun downloadCardFullImage() {
        val dirOut = File(Environment.DIRECTORY_DOWNLOADS)
        FileOutputStream(File(dirOut, "${card?.name}.png")).apply {
            try {
                val bitmap = card_full_art_iv.getDrawingCache()
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, this)
                flush()
                close()
            } catch (e: Exception) {
                eventBus.post(CmdShowSnackbarMsg(CmdShowSnackbarMsg.TYPE_ERROR, e.message ?: ""))
            }
        }
    }

}