package com.ediposouza.teslesgendstracker.ui.decks.widget

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.RelativeLayout
import com.ediposouza.teslesgendstracker.R
import com.ediposouza.teslesgendstracker.data.CardSlot
import kotlinx.android.synthetic.main.widget_magicka_costs.view.*

/**
 * Created by EdipoSouza on 12/20/16.
 */
class MagickaCosts(ctx: Context?, attrs: AttributeSet?, defStyleAttr: Int) :
        LinearLayout(ctx, attrs, defStyleAttr) {

    var magickaCost0Qtd = 0
        set(value) {
            field = value
            updateMagickaCostBars()
        }
    var magickaCost1Qtd = 0
        set(value) {
            field = value
            updateMagickaCostBars()
        }
    var magickaCost2Qtd = 0
        set(value) {
            field = value
            updateMagickaCostBars()
        }
    var magickaCost3Qtd = 0
        set(value) {
            field = value
            updateMagickaCostBars()
        }
    var magickaCost4Qtd = 0
        set(value) {
            field = value
            updateMagickaCostBars()
        }
    var magickaCost5Qtd = 0
        set(value) {
            field = value
            updateMagickaCostBars()
        }
    var magickaCost6Qtd = 0
        set(value) {
            field = value
            updateMagickaCostBars()
        }
    var magickaCost7PlusQtd = 0
        set(value) {
            field = value
            updateMagickaCostBars()
        }

    init {
        inflate(context, R.layout.widget_magicka_costs, rootView as ViewGroup)
        magickaCost0Qtd = 0
        if (isInEditMode) {
            magickaCost1Qtd = 1
            magickaCost2Qtd = 2
            magickaCost3Qtd = 3
            magickaCost4Qtd = 4
            magickaCost5Qtd = 5
            magickaCost6Qtd = 6
            magickaCost7PlusQtd = 7
        }
    }

    constructor(ctx: Context?) : this(ctx, null, 0)

    constructor(ctx: Context?, attrs: AttributeSet) : this(ctx, attrs, 0)

    fun updateCosts(cards: List<CardSlot>) {
        magickaCost0Qtd = cards.filter { it.card.cost == 0 }.sumBy { it.qtd }
        magickaCost1Qtd = cards.filter { it.card.cost == 1 }.sumBy { it.qtd }
        magickaCost2Qtd = cards.filter { it.card.cost == 2 }.sumBy { it.qtd }
        magickaCost3Qtd = cards.filter { it.card.cost == 3 }.sumBy { it.qtd }
        magickaCost4Qtd = cards.filter { it.card.cost == 4 }.sumBy { it.qtd }
        magickaCost5Qtd = cards.filter { it.card.cost == 5 }.sumBy { it.qtd }
        magickaCost6Qtd = cards.filter { it.card.cost == 6 }.sumBy { it.qtd }
        magickaCost7PlusQtd = cards.filter { it.card.cost >= 7 }.sumBy { it.qtd }
    }

    private fun updateMagickaCostBars() {
        val magickaCostsMax = arrayOf(magickaCost0Qtd, magickaCost1Qtd, magickaCost2Qtd, magickaCost3Qtd,
                magickaCost4Qtd, magickaCost5Qtd, magickaCost6Qtd, magickaCost7PlusQtd).max() ?: 0
        updateMagickaCostBar(rootView.magicka_costs_0, magickaCost0Qtd, magickaCostsMax)
        updateMagickaCostBar(rootView.magicka_costs_1, magickaCost1Qtd, magickaCostsMax)
        updateMagickaCostBar(rootView.magicka_costs_2, magickaCost2Qtd, magickaCostsMax)
        updateMagickaCostBar(rootView.magicka_costs_3, magickaCost3Qtd, magickaCostsMax)
        updateMagickaCostBar(rootView.magicka_costs_4, magickaCost4Qtd, magickaCostsMax)
        updateMagickaCostBar(rootView.magicka_costs_5, magickaCost5Qtd, magickaCostsMax)
        updateMagickaCostBar(rootView.magicka_costs_6, magickaCost6Qtd, magickaCostsMax)
        updateMagickaCostBar(rootView.magicka_costs_7_plus, magickaCost7PlusQtd, magickaCostsMax)
    }

    //magikaCostViewLP.topMargin = 48 - 48*0/4 = 48
    //magikaCostViewLP.topMargin = 48 - 48*1/4 = 36
    //magikaCostViewLP.topMargin = 48 - 48*2/4 = 24
    //magikaCostViewLP.topMargin = 48 - 48*4/4 = 0
    private fun updateMagickaCostBar(magickaCostView: View?, magickaCostQtd: Int, maxMagickaCostQtd: Int) {
        val resources = rootView.context.resources
        val zeroMargin = resources.getDimensionPixelSize(R.dimen.deck_new_magicka_costs_bar_min_height)
        val magickaCostViewLP = magickaCostView?.layoutParams as RelativeLayout.LayoutParams
        val factor: Float = 0f.takeIf { maxMagickaCostQtd == 0 } ?: magickaCostQtd / maxMagickaCostQtd.toFloat()
        val topMargin = (zeroMargin - zeroMargin * factor).toInt()
        magickaCostViewLP.topMargin = (topMargin - 5).takeIf { topMargin == zeroMargin } ?: topMargin
        magickaCostView.layoutParams = magickaCostViewLP
    }

}