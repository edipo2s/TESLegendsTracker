package com.ediposouza.teslesgendstracker.ui

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.design.widget.BottomSheetBehavior
import android.support.v4.app.ActivityCompat
import android.view.View
import com.ediposouza.teslesgendstracker.R
import com.ediposouza.teslesgendstracker.data.Card
import com.ediposouza.teslesgendstracker.ui.base.BaseActivity
import com.ediposouza.teslesgendstracker.ui.base.command.CmdShowLogin
import com.ediposouza.teslesgendstracker.ui.base.command.CmdShowSnackbarMsg
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.activity_card.*
import org.jetbrains.anko.intentFor
import org.jetbrains.anko.toast

class CardActivity : BaseActivity() {

    companion object {

        private val EXTRA_CARD = "card"

        fun newIntent(context: Context, card: Card): Intent {
            return context.intentFor<CardActivity>(EXTRA_CARD to card)
        }

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_card)

        loadCardInfo(intent.getParcelableExtra<Card>(EXTRA_CARD))
        card_all_image.setOnClickListener { ActivityCompat.finishAfterTransition(this) }
        card_favorite_btn.setOnClickListener {
            if (FirebaseAuth.getInstance().currentUser != null) {
                toast("Favorite")
            } else {
                eventBus.post(CmdShowSnackbarMsg(CmdShowSnackbarMsg.TYPE_ERROR, R.string.error_auth)
                        .withAction(R.string.action_login, { eventBus.post(CmdShowLogin()) }))
            }
        }
        configureBottomSheet()
    }

    private fun loadCardInfo(card: Card) {
        card_race.text = card.race.name
        card_race_desc.text = card.race.desc
        card_race_desc.visibility = if (card.race.desc.isEmpty()) View.GONE else View.VISIBLE
        card_arena_tier.text = card.arenaTier.name
        card_all_image.setImageBitmap(card.imageBitmap(this))
    }

    private fun configureBottomSheet() {
        val sheetBehavior = BottomSheetBehavior.from(card_bottom_sheet)
        card_bottom_sheet.setOnClickListener {
            if (sheetBehavior.state == BottomSheetBehavior.STATE_COLLAPSED) {
                sheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
            } else {
                sheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
            }
        }
    }

}
