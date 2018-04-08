package com.ediposouza.teslesgendstracker.ui.widget

import android.app.Activity
import android.content.Context
import android.support.v4.app.ActivityCompat
import android.support.v4.app.ActivityOptionsCompat
import android.support.v4.content.ContextCompat
import android.support.v7.app.AlertDialog
import android.support.v7.widget.GridLayoutManager
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.Toast
import com.bumptech.glide.Glide
import com.ediposouza.teslesgendstracker.R
import com.ediposouza.teslesgendstracker.data.*
import com.ediposouza.teslesgendstracker.ui.cards.CardActivity
import com.ediposouza.teslesgendstracker.ui.cards.CmdFilterAttr
import com.ediposouza.teslesgendstracker.ui.cards.tabs.CardsAllFragment
import com.ediposouza.teslesgendstracker.ui.decks.widget.DeckList
import com.ediposouza.teslesgendstracker.ui.util.GridSpacingItemDecoration
import com.ediposouza.teslesgendstracker.util.loadFromCard
import jp.wasabeef.recyclerview.animators.ScaleInAnimator
import kotlinx.android.synthetic.main.dialog_select_card.view.*
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
    var selectDialog: AlertDialog? = null
    var selectedCard: Card? = null
    var currentAttr: CardAttribute? = CardAttribute.STRENGTH
    var currentMagicka: Int = -1
    var currentRarity: CardRarity? = null
        set(value) {
            field = value
            with(arena_draft_card_iv) {
                isEnabled = field != null
                if (isEnabled) {
                    clearColorFilter()
                } else {
                    setColorFilter(ContextCompat.getColor(context, R.color.card_zero_qtd))
                }
            }
        }
    var draftCardlist: DeckList? = null

    init {
        inflate(context, R.layout.widget_arena_cards, rootView as ViewGroup)
        reset()
    }

    constructor(ctx: Context?) : this(ctx, null, 0)

    constructor(ctx: Context?, attrs: AttributeSet) : this(ctx, attrs, 0)

    fun config(activity: Activity?, cls: DeckClass, cards: List<Card>, cardOnLongOnClick: (Card) -> Unit, arena_draft_cardlist: DeckList) {
        draftCardlist = arena_draft_cardlist
        with(arena_draft_card_iv) {
            setOnClickListener {
                if (isEnabled) {
                    showSelectCardDialog(activity, cls, cards)
                }
            }
            setOnLongClickListener { chooseCard(cardOnLongOnClick) }
        }
        arena_draft_card_value.setOnClickListener {
            val cardSynergy = arena_draft_card_value.tag?.toString() ?: ""
            if (cardSynergy.isNotBlank()) {
                val synergyMsg = context.getString(R.string.new_arena_draft_synergy, cardSynergy)
                Toast.makeText(context, synergyMsg, Toast.LENGTH_LONG).show()
            }
        }
        arena_draft_card_value_shadow.setOnClickListener { arena_draft_card_value.callOnClick() }
        arena_draft_arrow_iv.setOnClickListener { chooseCard(cardOnLongOnClick) }
    }

    fun reset() {
        selectedCard = null
        currentAttr = null
        currentMagicka = -1
        currentRarity = null
        Glide.with(this).clear(arena_draft_card_iv)
        arena_draft_card_iv.setImageResource(R.drawable.card_back)
        arena_draft_card_value.text = INVALID_TEXT_VALUE
        arena_draft_card_value.setTextColor(ContextCompat.getColor(context, android.R.color.white))
        arena_draft_card_value_shadow.text = INVALID_TEXT_VALUE
    }

    private fun showSelectCardDialog(activity: Activity?, cls: DeckClass, cards: List<Card>) {
        val gridLayoutManager: GridLayoutManager = object : GridLayoutManager(context, CARDS_PER_ROW) {
            override fun supportsPredictiveItemAnimations(): Boolean = false
        }
        val cardsAdapter = CardsAllFragment.CardsAllAdapter(itemClick = { _, card -> onSelect(card) }) {
            view: View, card: Card ->
            showCardExpanded(activity, card, view)
            true
        }
        val dialogView = View.inflate(context, R.layout.dialog_select_card, null)
        with(dialogView.select_card_dialog_attr) {
            lockAttrs(cls.attr1, cls.attr2)
            filterClick = { attr ->
                currentAttr = attr
                EventBus.getDefault().post(CmdFilterAttr(attr))
                selectAttr(attr, true)
                updateCardList(cards, cardsAdapter)
            }
        }
        with(dialogView.select_card_dialog_magicka) {
            collapseOnClick = false
            filterClick = { cost ->
                currentMagicka = cost
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
        selectDialog = AlertDialog.Builder(context, R.style.AppDialog)
                .setView(dialogView)
                .setTitle(R.string.new_arena_draft_pick)
                .create().apply {
            setOnShowListener {
                dialogView.select_card_dialog_attr.filterClick?.invoke(currentAttr ?: cls.attr1)
                dialogView.select_card_dialog_magicka.open()
            }
            show()
        }
    }

    private fun chooseCard(cardOnLongOnClick: (Card) -> Unit): Boolean {
        if (selectedCard != null) {
            cardOnLongOnClick.invoke(selectedCard!!)
        }
        return true
    }

    private fun updateCardList(cards: List<Card>, cardsAdapter: CardsAllFragment.CardsAllAdapter) {
        cardsAdapter.showCards(cards.filter { it.attr == currentAttr }
                .filter { currentMagicka == -1 || (if (currentMagicka < 7) it.cost == currentMagicka else it.cost >= currentMagicka) }
                .filter { currentRarity == null || it.rarity == currentRarity }
                .filter { !it.evolves }
                .sortedBy { it.cost })
    }

    private fun onSelect(card: Card) {
        selectedCard = card
        selectDialog?.dismiss()
        arena_draft_card_iv.loadFromCard(card)
        val calcArenaValue = calcArenaValue(card)
        val arenaValue = (calcArenaValue.first.takeIf { it > 0 } ?: INVALID_TEXT_VALUE).toString()
        arena_draft_card_value.text = "$arenaValue" + ("*".takeIf { calcArenaValue.second.isNotEmpty() } ?: "")
        arena_draft_card_value.tag = calcArenaValue.second.joinToString("\n") { "* ${it.name}" }
        arena_draft_card_value.setTextColor(ContextCompat.getColor(context, when (calcArenaValue.first) {
            in 0..CardArenaTier.AVERAGE.value.minus(1) -> R.color.red_500
            in CardArenaTier.AVERAGE.value..CardArenaTier.EXCELLENT.value.minus(1) -> android.R.color.white
            else -> R.color.teal_100
        })
        )
        arena_draft_card_value_shadow.text = arena_draft_card_value.text
    }

    private fun calcArenaValue(card: Card): Pair<Int, List<Card>> {
        val arenaTier: CardArenaTier = card.arenaTier
        val cardsSynergy = mutableListOf<Card>()
        val value = arenaTier.value
        var totalValueExtra = 0
        draftCardlist?.getCards()?.forEach { (draftedCard, _) ->
            var extraValue = calcCardSynergyPoints(card.arenaTierPlus, draftedCard)
            extraValue += calcCardSynergyPoints(draftedCard.arenaTierPlus, card, true)
            if (extraValue > 0) {
                totalValueExtra += extraValue
                cardsSynergy.add(draftedCard)
            }
        }
        return Pair(value + totalValueExtra, cardsSynergy)
    }

    private fun calcCardSynergyPoints(arenaTierPlus: List<CardArenaTierPlus?>, draftedCard: Card, reverseCalc: Boolean = false): Int {
        if (arenaTierPlus.isEmpty()) {
            return 0
        }
        val extraSynergyPoints = arenaTierPlus.map { calcCardSynergyPoints(it, draftedCard, reverseCalc) }
        return 0.takeIf { extraSynergyPoints.contains(0) } ?: extraSynergyPoints.first()
    }

    private fun calcCardSynergyPoints(arenaTierPlus: CardArenaTierPlus?, draftedCard: Card, reverseCalc: Boolean = false): Int {
        if (arenaTierPlus == null) {
            return 0
        }
        val extraPoints = arenaTierPlus.type.extraPoints
        return when (arenaTierPlus.type) {
            CardArenaTierPlusType.ATTACK -> getExtraPointsForIntValue(arenaTierPlus, draftedCard.attack)
            CardArenaTierPlusType.COST -> getExtraPointsForIntValue(arenaTierPlus, draftedCard.cost)
            CardArenaTierPlusType.HEALTH -> getExtraPointsForIntValue(arenaTierPlus, draftedCard.health)
            CardArenaTierPlusType.ATTR -> extraPoints.takeIf {
                !reverseCalc && (draftedCard.attr == CardAttribute.of(arenaTierPlus.value.toUpperCase()) ||
                        draftedCard.dualAttr1 == CardAttribute.of(arenaTierPlus.value.toUpperCase()) ||
                        draftedCard.dualAttr2 == CardAttribute.of(arenaTierPlus.value.toUpperCase()))
            } ?: 0
            CardArenaTierPlusType.KEYWORD -> extraPoints.takeIf {
                draftedCard.keywords.filter { it.name == arenaTierPlus.value.toUpperCase() }.isNotEmpty()
            } ?: 0
            CardArenaTierPlusType.RACE -> extraPoints.takeIf {
                draftedCard.race.name == arenaTierPlus.value.toUpperCase()
            } ?: 0
            CardArenaTierPlusType.STRATEGY -> 0
            CardArenaTierPlusType.TEXT -> extraPoints.takeIf {
                draftedCard.text.contains(arenaTierPlus.value)
            } ?: 0
            CardArenaTierPlusType.TYPE -> extraPoints.takeIf {
                draftedCard.type.name == arenaTierPlus.value.toUpperCase()
            } ?: 0
            else -> 0
        }
    }

    private fun getExtraPointsForIntValue(arenaTierPlus: CardArenaTierPlus, numberField: Int): Int {
        return arenaTierPlus.type.extraPoints.takeIf {
            when (arenaTierPlus.operator) {
                CardArenaTierPlusOperator.EQUALS -> numberField == arenaTierPlus.value.toInt()
                CardArenaTierPlusOperator.GREAT -> numberField > arenaTierPlus.value.toInt()
                CardArenaTierPlusOperator.MINOR -> numberField < arenaTierPlus.value.toInt()
                else -> false
            }
        } ?: 0
    }

    private fun showCardExpanded(activity: Activity?, card: Card, view: View) {
        activity?.let {
            ActivityCompat.startActivity(it, CardActivity.newIntent(it, card),
                    ActivityOptionsCompat.makeSceneTransitionAnimation(it, view, cardTransitionName).toBundle())
        }
    }

}