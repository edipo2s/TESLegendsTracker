package com.ediposouza.teslesgendstracker.ui.cards

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.design.widget.BottomSheetBehavior
import android.support.v4.app.ActivityCompat
import android.support.v7.widget.CardView
import android.view.View
import com.ediposouza.teslesgendstracker.App
import com.ediposouza.teslesgendstracker.R
import com.ediposouza.teslesgendstracker.data.Card
import com.ediposouza.teslesgendstracker.interactor.PrivateInteractor
import com.ediposouza.teslesgendstracker.ui.base.BaseActivity
import com.ediposouza.teslesgendstracker.util.*
import kotlinx.android.synthetic.main.activity_card.*
import kotlinx.android.synthetic.main.include_card_info.*
import org.jetbrains.anko.intentFor
import org.jetbrains.anko.toast

class CardActivity : BaseActivity() {

    companion object {

        private val EXTRA_CARD = "cardExtra"
        private val EXTRA_FAVORITE = "favoriteExtra"

        fun newIntent(context: Context, card: Card): Intent {
            return context.intentFor<CardActivity>(EXTRA_CARD to card)
        }

        fun newIntent(context: Context, card: Card, favorite: Boolean): Intent {
            return context.intentFor<CardActivity>(EXTRA_CARD to card, EXTRA_FAVORITE to favorite)
        }

    }

    private val privateInteractor by lazy { PrivateInteractor() }
    private val card: Card by lazy { intent.getParcelableExtra<Card>(EXTRA_CARD) }
    private val cardInfoSheetBehavior: BottomSheetBehavior<CardView> by lazy { BottomSheetBehavior.from(card_bottom_sheet) }
    private var favorite: Boolean = false
    private var userCardQtd = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_card)
        snackbarNeedMargin = false

        favorite = intent.getBooleanExtra(EXTRA_FAVORITE, false)
        card_favorite_btn.visibility = if (intent.hasExtra(EXTRA_FAVORITE)) View.VISIBLE else View.GONE
        card_all_image.setOnClickListener {
            ActivityCompat.finishAfterTransition(this)
            MetricsManager.trackAction(MetricAction.ACTION_CARD_DETAILS_CLOSE_TAP())
        }
        card_favorite_btn.setOnClickListener { onFavoriteClick() }
        loadCardInfo()
        configureBottomSheet()
        setResult(Activity.RESULT_CANCELED, Intent())
    }

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)
        MetricsManager.trackScreen(MetricScreen.SCREEN_CARD_DETAILS())
        MetricsManager.trackCardView(card)
        card_ads_view.load()
        if (App.hasUserLogged()) {
            showUserCardQtd()
        }
    }

    private fun showUserCardQtd() {
        card_collection_qtd_layout.visibility = View.VISIBLE
        privateInteractor.getUserCollection(card.set, card.attr) {
            userCardQtd = it[card.shortName] ?: 0
            updateChangeCardQtdButtons()
            card_collection_qtd_loading.visibility = View.GONE
            card_collection_qtd.text = userCardQtd.toString()
            card_collection_qtd.visibility = View.VISIBLE
            with(card_collection_qtd_plus_btn) {
                visibility = View.VISIBLE
                setOnClickListener { updateCardQtd(userCardQtd.plus(1)) }
            }
            with(card_collection_qtd_minus_btn) {
                visibility = View.VISIBLE
                setOnClickListener { updateCardQtd(userCardQtd.minus(1)) }
            }
        }
    }

    private fun updateChangeCardQtdButtons() {
        val cardMaxQtd = if (card.unique) 1 else 3
        card_collection_qtd_plus_btn.isEnabled = userCardQtd < cardMaxQtd
        card_collection_qtd_minus_btn.isEnabled = userCardQtd > 0
    }

    private fun updateCardQtd(newCardQtd: Int) {
        privateInteractor.setUserCardQtd(card, newCardQtd) {
            userCardQtd = newCardQtd
            card_collection_qtd.text = newCardQtd.toString()
            updateChangeCardQtdButtons()
            setResult(Activity.RESULT_OK, Intent())
        }
    }

    override fun onSaveInstanceState(outState: Bundle?) {
        super.onSaveInstanceState(outState)
        ActivityCompat.finishAfterTransition(this)
    }

    override fun onBackPressed() {
        if (cardInfoSheetBehavior.state == BottomSheetBehavior.STATE_EXPANDED) {
            cardInfoSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
        } else {
            super.onBackPressed()
        }
    }

    private fun configureBottomSheet() {
        cardInfoSheetBehavior.setBottomSheetCallback(object : BottomSheetBehavior.BottomSheetCallback() {
            override fun onSlide(bottomSheet: View, slideOffset: Float) {
            }

            override fun onStateChanged(bottomSheet: View, newState: Int) {
                when (newState) {
                    BottomSheetBehavior.STATE_EXPANDED ->
                        MetricsManager.trackAction(MetricAction.ACTION_CARD_DETAILS_EXPAND())
                    BottomSheetBehavior.STATE_COLLAPSED ->
                        MetricsManager.trackAction(MetricAction.ACTION_CARD_DETAILS_COLLAPSE())
                }
            }

        })
        card_bottom_sheet.setOnClickListener { cardInfoSheetBehavior.toggleExpanded() }
    }

    private fun loadCardInfo() {
        val drawableRes = if (favorite) R.drawable.ic_favorite_checked else R.drawable.ic_favorite_unchecked
        card_favorite_btn.setImageResource(drawableRes)
        card_set.text = card.set.name.toLowerCase().capitalize()
        card_race.text = card.race.name.toLowerCase().capitalize()
        card_race_desc.text = card.race.desc
        card_race_desc.visibility = if (card.race.desc.isEmpty()) View.GONE else View.VISIBLE
        card_arena_tier.text = card.arenaTier.name.toLowerCase().capitalize()
        card_all_image.setImageBitmap(card.imageBitmap(this))
    }

    private fun onFavoriteClick() {
        if (App.hasUserLogged()) {
            PrivateInteractor().setUserCardFavorite(card, !favorite) {
                favorite = !favorite
                val stringRes = if (favorite) R.string.action_favorited else R.string.action_unfavorited
                toast(getString(stringRes, card.name))
                loadCardInfo()
                setResult(Activity.RESULT_OK, Intent())
                MetricsManager.trackAction(if (favorite)
                    MetricAction.ACTION_CARD_DETAILS_FAVORITE() else MetricAction.ACTION_CARD_DETAILS_UNFAVORITE())
            }
        } else {
            showErrorUserNotLogged()
        }
    }

}
