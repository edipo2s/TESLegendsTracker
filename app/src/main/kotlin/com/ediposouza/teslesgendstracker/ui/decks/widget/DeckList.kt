package com.ediposouza.teslesgendstracker.ui.decks.widget

import android.app.Activity
import android.content.Context
import android.graphics.Bitmap
import android.support.v4.app.ActivityCompat
import android.support.v4.app.ActivityOptionsCompat
import android.support.v4.app.FragmentManager
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import com.ediposouza.teslesgendstracker.R
import com.ediposouza.teslesgendstracker.data.*
import com.ediposouza.teslesgendstracker.interactor.PublicInteractor
import com.ediposouza.teslesgendstracker.ui.cards.CardActivity
import com.ediposouza.teslesgendstracker.ui.decks.CmdRemAttr
import com.ediposouza.teslesgendstracker.ui.decks.CmdUpdateCardSlot
import com.ediposouza.teslesgendstracker.ui.decks.DeckListCardsFragment
import com.ediposouza.teslesgendstracker.util.alertThemed
import com.ediposouza.teslesgendstracker.util.inflate
import com.ediposouza.teslesgendstracker.util.loadFromCard
import jp.wasabeef.recyclerview.animators.SlideInLeftAnimator
import kotlinx.android.synthetic.main.itemlist_decklist_slot.view.*
import kotlinx.android.synthetic.main.widget_decklist.view.*
import org.greenrobot.eventbus.EventBus
import org.jetbrains.anko.bundleOf
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.runOnUiThread
import java.util.*

/**
 * Created by EdipoSouza on 11/2/16.
 */
class DeckList(ctx: Context?, attrs: AttributeSet?, defStyleAttr: Int) :
        LinearLayout(ctx, attrs, defStyleAttr) {

    var arenaMode = false
        set(value) {
            field = value
            decklist_soul.visibility = View.GONE.takeIf { value } ?: View.VISIBLE
        }
    var editMode = false

    val cardTransitionName by lazy { context.getString(R.string.card_transition_name) }

    val deckListCardsFragment by lazy {
        DeckListCardsFragment().apply {
            arguments = bundleOf(DeckListCardsFragment.EXTRA_DECK_CARDS to getCards(),
                    DeckListCardsFragment.EXTRA_MISSING_CARDS to deckListAdapter.missingCards)
        }
    }

    val deckListAdapter by lazy {
        DeckListAdapter({ index -> decklist_recycle_view.scrollToPosition(index) },
                itemClick = { view, card ->
                    if (editMode) {
                        remCard(card)
                    } else {
                        ActivityCompat.startActivity(context, CardActivity.newIntent(context, card),
                                ActivityOptionsCompat.makeSceneTransitionAnimation(context as Activity,
                                        view, cardTransitionName).toBundle())
                    }
                },
                itemLongClick = { view, card ->
                    if (arenaMode) {
                        context.alertThemed(R.string.new_arena_draft_remove_card, theme = R.style.AppDialog) {
                            positiveButton(android.R.string.yes, { remCard(card) })
                            negativeButton(android.R.string.no, {})
                        }.show()
                    } else {
                        ActivityCompat.startActivity(context, CardActivity.newIntent(context, card),
                                ActivityOptionsCompat.makeSceneTransitionAnimation(context as Activity,
                                        view, cardTransitionName).toBundle())
                    }
                    true
                })
    }

    init {
        inflate(context, R.layout.widget_decklist, this)
        decklist_recycle_view.adapter = deckListAdapter
        decklist_recycle_view.itemAnimator = SlideInLeftAnimator()
        decklist_recycle_view.layoutManager = object : LinearLayoutManager(context) {
            override fun supportsPredictiveItemAnimations(): Boolean = false
        }
        decklist_recycle_view.setHasFixedSize(true)
        if (isInEditMode) {
            val card = Card.DUMMY
            val cards = listOf(CardSlot(card, 3), CardSlot(card, 1), CardSlot(card, 2), CardSlot(card, 3))
            deckListAdapter.showDeck(cards)
        }
    }

    constructor(ctx: Context?) : this(ctx, null, 0)

    constructor(ctx: Context?, attrs: AttributeSet) : this(ctx, attrs, 0)

    fun showDeck(deck: Deck?, showSoulCost: Boolean = true, showMagickaCosts: Boolean = true,
                 showQtd: Boolean = true, cardViewMode: Boolean? = false) {
        decklist_soul.visibility = View.VISIBLE.takeIf { showSoulCost } ?: View.GONE
        decklist_costs.visibility = View.VISIBLE.takeIf { showMagickaCosts } ?: View.GONE
        decklist_qtd.visibility = View.VISIBLE.takeIf { showQtd } ?: View.GONE
        decklist_recycle_view.visibility = View.INVISIBLE.takeIf { cardViewMode == true } ?: View.VISIBLE
        if (deck != null) {
            doAsync {
                PublicInteractor.getDeckCards(deck) {
                    context.runOnUiThread {
                        (decklist_recycle_view.adapter as DeckListAdapter).showDeck(it)
                        onCardListChange()
                    }
                }
            }
        }
    }

    fun showMissingCards(missingCards: List<CardMissing>) {
        deckListAdapter.showMissingCards(missingCards)
    }

    fun addCard(card: Card) {
        deckListAdapter.addCard(card, arenaMode)
        onCardListChange()
    }

    fun addCards(cards: List<CardSlot>) {
        deckListAdapter.addCards(cards)
        onCardListChange()
    }

    fun remCard(card: Card) {
        deckListAdapter.remCard(card)
        onCardListChange()
    }

    fun getCards(): List<CardSlot> = deckListAdapter.getCards()

    fun getSoulCost(): Int = getCards().sumBy { it.card.rarity.soulCost * it.qtd }

    fun setCardViewMode(fragmentManager: FragmentManager?, compact: Boolean) {
        fragmentManager?.beginTransaction()?.apply {
            setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out)
            if (compact) {
                replace(R.id.decklist_cards_container, deckListCardsFragment)
                decklist_recycle_view.visibility = View.GONE
            } else {
                remove(deckListCardsFragment)
                deckListCardsFragment.getListView().visibility = View.GONE
                decklist_recycle_view.visibility = View.VISIBLE
            }
            commit()
        }
    }

    private fun onCardListChange() {
        val cards = getCards()
        decklist_costs.updateCosts(cards)
        val attrGroup = cards
                .flatMap {
                    when (it.qtd) {
                        1 -> listOf(it.card)
                        2 -> listOf(it.card, it.card)
                        3 -> listOf(it.card, it.card, it.card)
                        else -> listOf()
                    }
                }
                .flatMap { listOf(it.dualAttr1, it.dualAttr2, it.dualAttr3) }
                .filter { it.isBasic }
                .groupBy { it }
                .map { it.key to it.value.size }
                .sortedBy { it.first.ordinal }
        val isTriple = attrGroup.size == 3
        val qtdFormat = R.string.new_deck_card_list_arena_qtd.takeIf { arenaMode }
                ?: R.string.new_deck_card_list_triple_qtd.takeIf { isTriple }
                ?: R.string.new_deck_card_list_qtd
        decklist_qtd.text = context.getString(qtdFormat, cards.sumBy { it.qtd })
        decklist_soul.text = getSoulCost().toString()
        decklist_class_attr1.visibility = View.VISIBLE.takeIf { attrGroup.size > 0 } ?: View.GONE
        decklist_class_attr1_qtd.visibility = View.VISIBLE.takeIf { attrGroup.size > 0 } ?: View.GONE
        decklist_class_attr2.visibility = View.VISIBLE.takeIf { attrGroup.size > 1 } ?: View.GONE
        decklist_class_attr2_qtd.visibility = View.VISIBLE.takeIf { attrGroup.size > 1 } ?: View.GONE
        decklist_class_attr3.visibility = View.VISIBLE.takeIf { attrGroup.size > 2 } ?: View.GONE
        decklist_class_attr3_qtd.visibility = View.VISIBLE.takeIf { attrGroup.size > 2 } ?: View.GONE
        if (attrGroup.size > 0) {
            decklist_class_attr1.setImageResource(attrGroup[0].first.imageRes)
            decklist_class_attr1_qtd.text = "${attrGroup[0].second}"
        }
        if (attrGroup.size > 1) {
            decklist_class_attr2.setImageResource(attrGroup[1].first.imageRes)
            decklist_class_attr2_qtd.text = "${attrGroup[1].second}"
        }
        if (attrGroup.size > 2) {
            decklist_class_attr3.setImageResource(attrGroup[2].first.imageRes)
            decklist_class_attr3_qtd.text = "${attrGroup[2].second}"
        }
        val prophecyCardSlots = cards.filter { it.card.keywords.contains(CardKeyword.PROPHECY) }
        decklist_class_prophecy.visibility = View.VISIBLE.takeIf { prophecyCardSlots.size > 0 } ?: View.GONE
        decklist_class_prophecy_qtd.visibility = View.VISIBLE.takeIf { prophecyCardSlots.size > 0 } ?: View.GONE
        decklist_class_prophecy_qtd.text = "${prophecyCardSlots.sumBy { it.qtd }}"
    }

    class DeckListAdapter(val onAdd: (Int) -> Unit, val itemClick: (View, Card) -> Unit,
                          val itemLongClick: (View, Card) -> Boolean) : RecyclerView.Adapter<DeckListViewHolder>() {

        private val items = arrayListOf<CardSlot>()
        private val eventBus by lazy { EventBus.getDefault() }

        var missingCards: List<CardMissing> = listOf()

        var updateMode = false

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DeckListViewHolder {
            return DeckListViewHolder(parent.inflate(R.layout.itemlist_decklist_slot), itemClick, itemLongClick)
        }

        override fun onBindViewHolder(holder: DeckListViewHolder, position: Int) {
            val cardSlot = items[position]
            val cardMissing = missingCards.find { it.shortName == cardSlot.card.shortName }
            holder.bind(cardSlot, cardMissing?.qtd ?: 0, updateMode)
        }

        override fun getItemCount(): Int = items.size

        fun showDeck(cards: List<CardSlot>) {
            items.clear()
            items.addAll(cards.sorted())
            notifyDataSetChanged()
        }

        fun showMissingCards(missingCards: List<CardMissing>) {
            this.missingCards = missingCards
            notifyDataSetChanged()
        }

        fun addCard(card: Card, arenaMode: Boolean) {
            val cardSlot = items.find { it.card == card }
            if (cardSlot == null) {
                val newCardSlot = CardSlot(card, 1)
                items.add(newCardSlot)
                Collections.sort(items)
                val newCardIndex = items.indexOf(newCardSlot)
                onAdd(newCardIndex)
                notifyItemInserted(newCardIndex)
            } else {
                val deckQtdLimit = 3.takeIf { !arenaMode } ?: 30
                val newQtd = cardSlot.qtd.inc().takeIf { cardSlot.qtd < deckQtdLimit } ?: 3
                val cardIndex = items.indexOf(cardSlot)
                items[cardIndex] = CardSlot(card, if (card.unique) 1 else newQtd)
                onAdd(cardIndex)
                notifyItemChanged(cardIndex)
            }
            eventBus.post(CmdUpdateCardSlot(items.find { it.card == card } ?: CardSlot(card, 0)))
        }


        fun addCards(cards: List<CardSlot>) {
            items.addAll(cards)
            notifyDataSetChanged()
        }

        fun remCard(card: Card) {
            val cardSlot = items.find { it.card == card }
            if (cardSlot != null) {
                val newQtd = cardSlot.qtd.dec()
                if (newQtd <= 0) {
                    val cardRemovedIndex = items.indexOf(cardSlot)
                    items.remove(cardSlot)
                    notifyItemRemoved(cardRemovedIndex)
                    notifyCardRemoved(card)
                } else {
                    val cardIndex = items.indexOf(cardSlot)
                    items[cardIndex] = CardSlot(card, newQtd)
                    notifyItemChanged(cardIndex)
                }
            }
            eventBus.post(CmdUpdateCardSlot(items.find { it.card == card } ?: CardSlot(card, 0)))
        }

        private fun notifyCardRemoved(card: Card) {
            val cardAttrs = listOf(card.dualAttr1, card.dualAttr2, card.dualAttr3).filter { it.isBasic }
            cardAttrs.forEach { cardAttr ->
                if (items.none { it.card.dualAttr1 == cardAttr } &&
                        items.none { it.card.dualAttr2 == cardAttr } &&
                        items.none { it.card.dualAttr3 == cardAttr }) {
                    eventBus.post(CmdRemAttr(cardAttr))
                }
            }
        }

        fun getCards(): List<CardSlot> {
            return items
        }

    }

    class DeckListViewHolder(view: View?, val itemClick: (View, Card) -> Unit,
                             val itemLongClick: (View, Card) -> Boolean) : RecyclerView.ViewHolder(view) {

        fun bind(slot: CardSlot, missingQtd: Int, updateMode: Boolean) {
            with(itemView) {
                setOnClickListener { itemClick.invoke(itemView.deckslot_card_image, slot.card) }
                setOnLongClickListener { itemLongClick.invoke(itemView.deckslot_card_image, slot.card) }
                itemView.deckslot_card_image.loadFromCard(slot.card, { cardBitmap ->
                    with(itemView.resources) {
                        val bmpWidth = cardBitmap.width
                        val bmpHeight = cardBitmap.height
                        val leftCropMargin = getInteger(R.integer.decklist_slot_cover_left_crop_margin)
                        val rightCropMargin = getInteger(R.integer.decklist_slot_cover_right_crop_margin)
                        val cropWidth = bmpWidth - leftCropMargin - rightCropMargin
                        Bitmap.createBitmap(cardBitmap, leftCropMargin, 0, cropWidth, bmpHeight * 2 / 3)
                    }
                })
                decl_slot_card_name.text = slot.card.name
                deckslot_card_rarity.setImageResource(slot.card.rarity.imageRes)
                deckslot_card_magicka.setImageResource(when (slot.card.cost) {
                    0 -> R.drawable.ic_magicka_0
                    1 -> R.drawable.ic_magicka_1
                    2 -> R.drawable.ic_magicka_2
                    3 -> R.drawable.ic_magicka_3
                    4 -> R.drawable.ic_magicka_4
                    5 -> R.drawable.ic_magicka_5
                    6 -> R.drawable.ic_magicka_6
                    7 -> R.drawable.ic_magicka_7
                    8 -> R.drawable.ic_magicka_8
                    9 -> R.drawable.ic_magicka_9
                    10 -> R.drawable.ic_magicka_10
                    11 -> R.drawable.ic_magicka_11
                    12 -> R.drawable.ic_magicka_12
                    20 -> R.drawable.ic_magicka_20
                    else -> R.drawable.ic_magicka_7plus
                })
                deckslot_card_qtd.text = "+${slot.qtd}".takeIf { updateMode && slot.qtd > 0 } ?: "${slot.qtd}"
                deckslot_card_qtd.visibility = View.VISIBLE.takeIf { slot.qtd != 0 } ?: View.INVISIBLE
                deckslot_card_qtd_layout.visibility = View.VISIBLE.takeIf { slot.qtd != 0 } ?: View.INVISIBLE
                deckslot_card_qtd_missing.text = "-$missingQtd"
                deckslot_card_qtd_missing.visibility = View.VISIBLE.takeIf { missingQtd > 0 } ?: View.INVISIBLE
            }
        }

    }

}
