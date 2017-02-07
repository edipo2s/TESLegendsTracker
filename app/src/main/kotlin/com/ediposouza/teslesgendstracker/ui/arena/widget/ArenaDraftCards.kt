package com.ediposouza.teslesgendstracker.ui.widget

import android.content.Context
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.LinearSnapHelper
import android.support.v7.widget.RecyclerView
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import com.ediposouza.teslesgendstracker.data.*
import com.ediposouza.teslesgendstracker.ui.cards.tabs.CardsAllFragment
import com.ediposouza.teslesgendstracker.util.inflate
import kotlinx.android.synthetic.main.fragment_arena_draft.view.*
import kotlinx.android.synthetic.main.widget_arena_cards.view.*

/**
 * Created by EdipoSouza on 11/2/16.
 */
class ArenaDraftCards(ctx: Context?, attrs: AttributeSet?, defStyleAttr: Int) :
        FrameLayout(ctx, attrs, defStyleAttr) {

    private val INVALID_TEXT_VALUE = "-"
    private var lastSnapedPosition: Int? = null

    init {
        inflate(context, R.layout.widget_arena_cards, rootView as ViewGroup)
        config(listOf<Card>(), { _, _ -> }, { _, _ -> true })
        LinearSnapHelper().attachToRecyclerView(arena_draft_card_recycler_view)
    }

    constructor(ctx: Context?) : this(ctx, null, 0)

    constructor(ctx: Context?, attrs: AttributeSet) : this(ctx, attrs, 0)

    fun config(cards: List<Card>, cardListOnClick: (View, Card) -> Unit, cardOnLongOnClick: (View, Card) -> Boolean) {
        with(arena_draft_card_recycler_view) {
            adapter = CardsAdapter(cards, cardListOnClick, cardOnLongOnClick)
            layoutManager = LinearLayoutManager(context, RecyclerView.HORIZONTAL, false)
            setHasFixedSize(true)
            addOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrollStateChanged(recyclerView: RecyclerView?, newState: Int) {
                    super.onScrollStateChanged(recyclerView, newState)
                    if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                        val layoutManager = arena_draft_card_recycler_view.layoutManager as LinearLayoutManager
                        val position = layoutManager.findLastCompletelyVisibleItemPosition()
                        if (position != lastSnapedPosition &&
                                position >= 0 && position < arena_draft_card_recycler_view.adapter.itemCount) {
                            onSnap(position)
                        }
                    }
                }
            })
        }
    }

    private fun onSnap(position: Int) {
        lastSnapedPosition = position
        val card = (arena_draft_card_recycler_view.adapter as CardsAdapter).items[position]
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

    class CardsAdapter(val allCards: List<Card>, val itemClick: (View, Card) -> Unit,
                       val itemLongClick: (View, Card) -> Boolean) : RecyclerView.Adapter<CardsAllFragment.CardsAllViewHolder>() {

        var items: List<Card> = listOf()

        init {
            filterRarity(null)
        }

        override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): CardsAllFragment.CardsAllViewHolder {
            return CardsAllFragment.CardsAllViewHolder(parent?.inflate(R.layout.itemlist_card_arena), itemClick, itemLongClick)
        }


        override fun onBindViewHolder(holder: CardsAllFragment.CardsAllViewHolder?, position: Int) {
            holder?.bind(items[position])
        }

        override fun getItemCount(): Int = items.size

        fun filterRarity(rarity: CardRarity?) {
            items = allCards.filter { rarity == null || it.rarity == rarity }
            notifyDataSetChanged()
        }

    }

}