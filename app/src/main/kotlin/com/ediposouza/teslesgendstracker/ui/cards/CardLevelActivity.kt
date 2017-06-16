package com.ediposouza.teslesgendstracker.ui.cards

import android.os.Bundle
import android.support.v4.app.ActivityCompat
import com.ediposouza.teslesgendstracker.R
import com.ediposouza.teslesgendstracker.data.Card
import com.ediposouza.teslesgendstracker.ui.base.BaseActivity
import com.ediposouza.teslesgendstracker.util.*
import kotlinx.android.synthetic.main.activity_card_level.*

/**
 * Created by EdipoSouza on 4/2/17.
 */
class CardLevelActivity : BaseActivity() {

    companion object {

        const val EXTRA_CARD = "cardExtra"
        const val EXTRA_CARD_LEVEL = "cardExtraLevel"

    }

    val card: Card by lazy { intent.getParcelableExtra<Card>(EXTRA_CARD) }
    val cardLevel: Int by lazy { intent.getIntExtra(EXTRA_CARD_LEVEL, 0) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_card_level)
        ActivityCompat.postponeEnterTransition(this)

        if (hasNavigationBar()) {
            card_level_content.setBottomPaddingForNavigationBar()
        }
        with(card_level_iv) {
            loadFromCard(card, cardLevel)
            setOnClickListener {
                ActivityCompat.finishAfterTransition(this@CardLevelActivity)
            }
        }
        ActivityCompat.startPostponedEnterTransition(this)
    }

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)
        card_level_ads_view.load()
        MetricsManager.trackScreen(MetricScreen.SCREEN_CARD_FULL_ART())
    }

}