package com.ediposouza.teslesgendstracker.ui.widget

import android.app.Activity
import android.content.Context
import android.support.v4.app.ActivityCompat
import android.support.v4.app.ActivityOptionsCompat
import android.support.v7.app.AlertDialog
import android.support.v7.widget.GridLayoutManager
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import com.ediposouza.teslesgendstracker.R
import com.ediposouza.teslesgendstracker.data.*
import com.ediposouza.teslesgendstracker.ui.cards.CardActivity
import com.ediposouza.teslesgendstracker.ui.cards.CmdFilterRarity
import com.ediposouza.teslesgendstracker.ui.cards.tabs.CardsAllFragment
import com.ediposouza.teslesgendstracker.ui.util.GridSpacingItemDecoration
import jp.wasabeef.recyclerview.animators.ScaleInAnimator
import kotlinx.android.synthetic.main.dialog_select_card.view.*
import kotlinx.android.synthetic.main.fragment_arena_draft.view.*
import kotlinx.android.synthetic.main.widget_arena_cards.view.*
import org.greenrobot.eventbus.EventBus

/**
 * Created by EdipoSouza on 11/2/16.
 */
class ArenaDraftCards(ctx: Context?, attrs: AttributeSet?, defStyleAttr: Int) :
        FrameLayout(ctx, attrs, defStyleAttr) {

    private val ADS_EACH_ITEMS = 20
    private val CARDS_PER_ROW = 2
    private val INVALID_TEXT_VALUE = "-"

    val cardTransitionName: String by lazy { context.getString(R.string.card_transition_name) }
    val gridLayoutManager: GridLayoutManager by lazy {
        object : GridLayoutManager(context, CARDS_PER_ROW) {
            override fun supportsPredictiveItemAnimations(): Boolean = false
        }
    }
    var selectDialog: AlertDialog? = null
    var selectedCard: Card? = null
    var currentAttr: CardAttribute? = CardAttribute.STRENGTH
    var currentMagika: Int? = null
    var currentRarity: CardRarity? = null

    init {
        inflate(context, R.layout.widget_arena_cards, rootView as ViewGroup)
        reset()
    }

    constructor(ctx: Context?) : this(ctx, null, 0)

    constructor(ctx: Context?, attrs: AttributeSet) : this(ctx, attrs, 0)

    fun config(activity: Activity, cards: List<Card>, cardOnLongOnClick: (Card) -> Unit) {
        with(arena_draft_card_iv) {
            setOnClickListener {
                val cardsAdapter = CardsAllFragment.CardsAllAdapter(ADS_EACH_ITEMS, gridLayoutManager,
                        R.layout.itemlist_card_ads, { _, card -> onSelect(card) }) {
                    view: View, card: Card ->
                    showCardExpanded(activity, card, view)
                    true
                }
                val dialogView = View.inflate(context, R.layout.dialog_select_card, null)
                with(dialogView.select_card_dialog_attr) {
                    filterClick = { attr ->
                        currentAttr = attr
                        selectAttr(attr, true)
                        updateCardList(cards, cardsAdapter)
                    }
                }
                with(dialogView.select_card_dialog_magika) {
                    open()
                    closeable = false
                    filterClick = { cost ->
                        currentMagika = cost
                        updateCardList(cards, cardsAdapter)
                    }
                }
                with(dialogView.select_card_dialog_rarity) {
                    expand()
                    hideMainButton()
                    filterClick = { rarity ->
                        currentRarity = rarity
                        EventBus.getDefault().post(CmdFilterRarity(rarity))
                        updateCardList(cards, cardsAdapter)
                    }
                }
                with(dialogView.select_card_dialog_recycler_view) {
                    layoutManager = gridLayoutManager
                    itemAnimator = ScaleInAnimator()
                    adapter = cardsAdapter
                    addItemDecoration(GridSpacingItemDecoration(CARDS_PER_ROW,
                            resources.getDimensionPixelSize(R.dimen.card_margin), true))
                }
                cardsAdapter.showCards(cards.filter { it.attr == CardAttribute.STRENGTH }
                        .filter { currentRarity == null || it.rarity == currentRarity })
                selectDialog = AlertDialog.Builder(context, R.style.AppDialog)
                        .setView(dialogView)
                        .setTitle(R.string.matches_class_title)
                        .show()
            }
            setOnLongClickListener {
                if (selectedCard != null) {
                    cardOnLongOnClick.invoke(selectedCard!!)
                }
                true
            }
        }
        arena_draft_card_value.setOnLongClickListener {
            if (selectedCard != null) {
                cardOnLongOnClick.invoke(selectedCard!!)
            }
            true
        }
    }

    private fun updateCardList(cards: List<Card>, cardsAdapter: CardsAllFragment.CardsAllAdapter) {
        cardsAdapter.showCards(cards.filter { it.attr == currentAttr }
                .filter { currentMagika == null || it.cost == currentMagika }
                .filter { currentRarity == null || it.rarity == currentRarity })
    }

    fun reset() {
        arena_draft_card_iv.setImageBitmap(Card.getDefaultCardImage(context))
        arena_draft_card_value.text = INVALID_TEXT_VALUE
        arena_draft_card_value_shadow.text = INVALID_TEXT_VALUE
    }

    private fun onSelect(card: Card) {
        selectedCard = card
        selectDialog?.dismiss()
        arena_draft_card_iv.setImageBitmap(card.imageBitmap(context))
        val calcArenaValue = calcArenaValue(card.arenaTier, card.arenaTierPlus)
        arena_draft_card_value.text = calcArenaValue
        arena_draft_card_value_shadow.text = calcArenaValue
    }

    private fun calcArenaValue(arenaTier: CardArenaTier, arenaTierPlus: CardArenaTierPlus?): String {
        if (arenaTier == CardArenaTier.UNKNOWN || arenaTier == CardArenaTier.NONE) {
            return INVALID_TEXT_VALUE
        }
        val value = arenaTier.value
        if (arenaTierPlus == null) {
            return value.toString()
        }
        var totalValueExtra = 0
        val extraPoints = arenaTierPlus.type.extraPoints
        arena_draft_cardlist.getCards().forEach { (card, _) ->
            totalValueExtra += when (arenaTierPlus.type) {
                CardArenaTierPlusType.ATTACK -> getExtraPointsForIntValue(arenaTierPlus, card.attack)
                CardArenaTierPlusType.COST -> getExtraPointsForIntValue(arenaTierPlus, card.cost)
                CardArenaTierPlusType.HEALTH -> getExtraPointsForIntValue(arenaTierPlus, card.health)
                CardArenaTierPlusType.ATTR -> extraPoints.takeIf {
                    card.attr == CardAttribute.valueOf(arenaTierPlus.value.toUpperCase()) ||
                            card.dualAttr1 == CardAttribute.valueOf(arenaTierPlus.value.toUpperCase()) ||
                            card.dualAttr2 == CardAttribute.valueOf(arenaTierPlus.value.toUpperCase())
                } ?: 0
                CardArenaTierPlusType.KEYWORD -> extraPoints.takeIf {
                    card.keywords.filter { it.name == arenaTierPlus.value.toUpperCase() }.isNotEmpty()
                } ?: 0
                CardArenaTierPlusType.RACE -> extraPoints.takeIf {
                    card.race.name == arenaTierPlus.value.toUpperCase()
                } ?: 0
                CardArenaTierPlusType.STRATEGY -> 0
                CardArenaTierPlusType.TEXT -> extraPoints.takeIf {
                    card.name.contains(arenaTierPlus.value)
                } ?: 0
                CardArenaTierPlusType.TYPE -> extraPoints.takeIf {
                    card.type.name == arenaTierPlus.value.toUpperCase()
                } ?: 0
                else -> 0
            }
        }
        return (value + totalValueExtra).toString()
    }

    private fun getExtraPointsForIntValue(arenaTierPlus: CardArenaTierPlus, numberField: Int): Int {
        return arenaTierPlus.type.extraPoints.takeIf {
            when (arenaTierPlus.operator) {
                CardArenaTierPlusOperator.GREAT -> numberField > arenaTierPlus.value.toInt()
                CardArenaTierPlusOperator.MINOR -> numberField < arenaTierPlus.value.toInt()
                else -> false
            }
        } ?: 0
    }

    fun showCardExpanded(activity: Activity, card: Card, view: View) {
        ActivityCompat.startActivity(context, CardActivity.newIntent(context, card),
                ActivityOptionsCompat.makeSceneTransitionAnimation(activity, view, cardTransitionName).toBundle())
    }

}