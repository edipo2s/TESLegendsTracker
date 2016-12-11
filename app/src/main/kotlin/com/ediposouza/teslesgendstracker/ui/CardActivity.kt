package com.ediposouza.teslesgendstracker.ui

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.design.widget.BottomSheetBehavior
import android.support.v4.app.ActivityCompat
import android.view.View
import com.ediposouza.teslesgendstracker.*
import com.ediposouza.teslesgendstracker.data.Card
import com.ediposouza.teslesgendstracker.interactor.PrivateInteractor
import com.ediposouza.teslesgendstracker.ui.base.BaseActivity
import com.ediposouza.teslesgendstracker.ui.base.CmdShowLogin
import com.ediposouza.teslesgendstracker.ui.base.CmdShowSnackbarMsg
import kotlinx.android.synthetic.main.activity_card.*
import org.jetbrains.anko.intentFor
import org.jetbrains.anko.toast

class CardActivity : BaseActivity() {

    companion object {

        private val EXTRA_CARD = "card"
        private val EXTRA_FAVORITE = "favorite"

        fun newIntent(context: Context, card: Card, favorite: Boolean = false): Intent {
            return context.intentFor<CardActivity>(EXTRA_CARD to card, EXTRA_FAVORITE to favorite)
        }

    }

    val card by lazy { intent.getParcelableExtra<Card>(EXTRA_CARD) }
    var favorite: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_card)

        favorite = intent.getBooleanExtra(EXTRA_FAVORITE, false)
        card_all_image.setOnClickListener {
            ActivityCompat.finishAfterTransition(this)
            metricsManager.trackAction(MetricAction.ACTION_CARD_DETAILS_CLOSE_TAP())
        }
        card_favorite_btn.setOnClickListener { onFavoriteClick() }
        loadCardInfo()
        configureBottomSheet()
        setResult(Activity.RESULT_CANCELED, Intent())
    }

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)
        metricsManager.trackScreen(MetricScreen.SCREEN_CARD_DETAILS())
        metricsManager.trackCardView(card)
        ads_view.load()
    }

    private fun configureBottomSheet() {
        val sheetBehavior = BottomSheetBehavior.from(card_bottom_sheet)
        sheetBehavior.setBottomSheetCallback(object : BottomSheetBehavior.BottomSheetCallback() {
            override fun onSlide(bottomSheet: View, slideOffset: Float) {
            }

            override fun onStateChanged(bottomSheet: View, newState: Int) {
                when (newState) {
                    BottomSheetBehavior.STATE_EXPANDED ->
                        metricsManager.trackAction(MetricAction.ACTION_CARD_DETAILS_EXPAND())
                    BottomSheetBehavior.STATE_COLLAPSED ->
                        metricsManager.trackAction(MetricAction.ACTION_CARD_DETAILS_COLLAPSE())
                }
            }

        })
        card_bottom_sheet.setOnClickListener { sheetBehavior.toogleExpanded() }
    }

    private fun loadCardInfo() {
        val drawableRes = if (favorite) R.drawable.ic_favorite_checked else R.drawable.ic_favorite_unchecked
        card_favorite_btn.setImageResource(drawableRes)
        card_race.text = card.race.name
        card_race_desc.text = card.race.desc
        card_race_desc.visibility = if (card.race.desc.isEmpty()) View.GONE else View.VISIBLE
        card_arena_tier.text = card.arenaTier.name
        card_all_image.setImageBitmap(card.imageBitmap(this))
    }

    private fun onFavoriteClick() {
        if (App.hasUserLogged) {
            PrivateInteractor().setUserCardFavorite(card, !favorite) {
                favorite = !favorite
                metricsManager.trackAction(if (favorite) MetricAction.ACTION_CARD_DETAILS_FAVORITE()
                else MetricAction.ACTION_CARD_DETAILS_UNFAVORITE())
                val stringRes = if (favorite) R.string.card_favorited else R.string.card_unfavorited
                toast(getString(stringRes, card.name))
                loadCardInfo()
                setResult(Activity.RESULT_OK, Intent())
            }
        } else {
            eventBus.post(CmdShowSnackbarMsg(CmdShowSnackbarMsg.TYPE_ERROR, R.string.error_auth)
                    .withAction(R.string.action_login, { eventBus.post(CmdShowLogin()) }))
        }
    }

}
