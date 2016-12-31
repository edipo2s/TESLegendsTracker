package com.ediposouza.teslesgendstracker.ui.decks.widget

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.RelativeLayout
import com.ediposouza.teslesgendstracker.R
import com.ediposouza.teslesgendstracker.data.CardSlot
import kotlinx.android.synthetic.main.widget_magika_costs.view.*

/**
 * Created by EdipoSouza on 12/20/16.
 */
class MagikaCosts(ctx: Context?, attrs: AttributeSet?, defStyleAttr: Int) :
        LinearLayout(ctx, attrs, defStyleAttr) {

    var magikaCost0Qtd = 0
        set(value) {
            field = value
            updateMagikaCostBars()
        }
    var magikaCost1Qtd = 0
        set(value) {
            field = value
            updateMagikaCostBars()
        }
    var magikaCost2Qtd = 0
        set(value) {
            field = value
            updateMagikaCostBars()
        }
    var magikaCost3Qtd = 0
        set(value) {
            field = value
            updateMagikaCostBars()
        }
    var magikaCost4Qtd = 0
        set(value) {
            field = value
            updateMagikaCostBars()
        }
    var magikaCost5Qtd = 0
        set(value) {
            field = value
            updateMagikaCostBars()
        }
    var magikaCost6Qtd = 0
        set(value) {
            field = value
            updateMagikaCostBars()
        }
    var magikaCost7PlusQtd = 0
        set(value) {
            field = value
            updateMagikaCostBars()
        }

    init {
        inflate(context, R.layout.widget_magika_costs, rootView as ViewGroup)
        magikaCost0Qtd = 0
        if (isInEditMode) {
            magikaCost1Qtd = 1
            magikaCost2Qtd = 2
            magikaCost3Qtd = 3
            magikaCost4Qtd = 4
            magikaCost5Qtd = 5
            magikaCost6Qtd = 6
            magikaCost7PlusQtd = 7
        }
    }

    constructor(ctx: Context?) : this(ctx, null, 0)

    constructor(ctx: Context?, attrs: AttributeSet) : this(ctx, attrs, 0)

    fun updateCosts(cards: List<CardSlot>) {
        magikaCost0Qtd = cards.filter { it.card.cost == 0 }.sumBy { it.qtd.toInt() }
        magikaCost1Qtd = cards.filter { it.card.cost == 1 }.sumBy { it.qtd.toInt() }
        magikaCost2Qtd = cards.filter { it.card.cost == 2 }.sumBy { it.qtd.toInt() }
        magikaCost3Qtd = cards.filter { it.card.cost == 3 }.sumBy { it.qtd.toInt() }
        magikaCost4Qtd = cards.filter { it.card.cost == 4 }.sumBy { it.qtd.toInt() }
        magikaCost5Qtd = cards.filter { it.card.cost == 5 }.sumBy { it.qtd.toInt() }
        magikaCost6Qtd = cards.filter { it.card.cost == 6 }.sumBy { it.qtd.toInt() }
        magikaCost7PlusQtd = cards.filter { it.card.cost >= 7 }.sumBy { it.qtd.toInt() }
    }

    private fun updateMagikaCostBars() {
        val magikaCostsMax = arrayOf(magikaCost0Qtd, magikaCost1Qtd, magikaCost2Qtd, magikaCost3Qtd,
                magikaCost4Qtd, magikaCost5Qtd, magikaCost6Qtd, magikaCost7PlusQtd).max() ?: 0
        updateMagikaCostBar(rootView.magika_costs_0, magikaCost0Qtd, magikaCostsMax)
        updateMagikaCostBar(rootView.magika_costs_1, magikaCost1Qtd, magikaCostsMax)
        updateMagikaCostBar(rootView.magika_costs_2, magikaCost2Qtd, magikaCostsMax)
        updateMagikaCostBar(rootView.magika_costs_3, magikaCost3Qtd, magikaCostsMax)
        updateMagikaCostBar(rootView.magika_costs_4, magikaCost4Qtd, magikaCostsMax)
        updateMagikaCostBar(rootView.magika_costs_5, magikaCost5Qtd, magikaCostsMax)
        updateMagikaCostBar(rootView.magika_costs_6, magikaCost6Qtd, magikaCostsMax)
        updateMagikaCostBar(rootView.magika_costs_7_plus, magikaCost7PlusQtd, magikaCostsMax)
    }

    //magikaCostViewLP.topMargin = 48 - 48*0/4 = 48
    //magikaCostViewLP.topMargin = 48 - 48*1/4 = 36
    //magikaCostViewLP.topMargin = 48 - 48*2/4 = 24
    //magikaCostViewLP.topMargin = 48 - 48*4/4 = 0
    private fun updateMagikaCostBar(magikaCostView: View?, magikaCostQtd: Int, maxMagikaCostQtd: Int) {
        val resources = rootView.context.resources
        val zeroMargin = resources.getDimensionPixelSize(R.dimen.deck_new_magika_costs_bar_min_height)
        val magikaCostViewLP = magikaCostView?.layoutParams as RelativeLayout.LayoutParams
        val factor: Float = if (maxMagikaCostQtd == 0) 0f else magikaCostQtd / maxMagikaCostQtd.toFloat()
        magikaCostViewLP.topMargin = (zeroMargin - zeroMargin * factor).toInt()
        magikaCostView?.layoutParams = magikaCostViewLP
    }

}