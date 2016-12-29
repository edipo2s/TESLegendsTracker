package com.ediposouza.teslesgendstracker.ui.decks.widget

import android.app.Activity
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.support.v4.app.ActivityCompat
import android.support.v4.app.ActivityOptionsCompat
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import com.ediposouza.teslesgendstracker.R
import com.ediposouza.teslesgendstracker.data.*
import com.ediposouza.teslesgendstracker.inflate
import com.ediposouza.teslesgendstracker.interactor.PrivateInteractor
import com.ediposouza.teslesgendstracker.interactor.PublicInteractor
import com.ediposouza.teslesgendstracker.ui.CardActivity
import com.ediposouza.teslesgendstracker.ui.decks.CmdRemAttr
import jp.wasabeef.recyclerview.animators.SlideInLeftAnimator
import kotlinx.android.synthetic.main.itemlist_decklist_slot.view.*
import kotlinx.android.synthetic.main.widget_decklist.view.*
import org.greenrobot.eventbus.EventBus
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.runOnUiThread
import java.util.*

/**
 * Created by EdipoSouza on 11/2/16.
 */
class DeckList(ctx: Context?, attrs: AttributeSet?, defStyleAttr: Int) :
        LinearLayout(ctx, attrs, defStyleAttr) {

    var userFavorites = arrayListOf<String>()
    var editMode = false

    private fun showExpandedCard(card: Card, view: View) {
        val favorite = userFavorites.contains(card.shortName)
        val transitionName = context.getString(R.string.card_transition_name)
        ActivityCompat.startActivity(context, CardActivity.newIntent(context, card, favorite),
                ActivityOptionsCompat.makeSceneTransitionAnimation(context as Activity, view, transitionName).toBundle())
    }

    val deckListAdapter by lazy {
        DeckListAdapter({ index -> decklist_recycle_view.scrollToPosition(index) },
                itemClick = { view, card ->
                    if (editMode) {
                        remCard(card)
                    } else {
                        showExpandedCard(card, view)
                    }
                }) {
            view, card ->
            showExpandedCard(card, view)
            true
        }
    }

    init {
        inflate(context, R.layout.widget_decklist, this)
        decklist_recycle_view.adapter = deckListAdapter
        decklist_recycle_view.itemAnimator = SlideInLeftAnimator()
        decklist_recycle_view.layoutManager = LinearLayoutManager(context)
        decklist_recycle_view.setHasFixedSize(true)
        if (isInEditMode) {
            val card = Card("Tyr", "tyr", CardSet.CORE, Attribute.DUAL, Attribute.STRENGTH,
                    Attribute.WILLPOWER, CardRarity.EPIC, false, 0, 0, 0, CardType.ACTION,
                    CardRace.ARGONIAN, emptyList<CardKeyword>(), CardArenaTier.AVERAGE, false)
            val cards = listOf(CardSlot(card, 3), CardSlot(card, 1), CardSlot(card, 2), CardSlot(card, 3))
            deckListAdapter.showDeck(cards)
        }
    }

    constructor(ctx: Context?) : this(ctx, null, 0)

    constructor(ctx: Context?, attrs: AttributeSet) : this(ctx, attrs, 0)

    fun showDeck(deck: Deck, showSoulCost: Boolean = true) {
        decklist_soul.visibility = if (showSoulCost) View.VISIBLE else View.GONE
        doAsync {
            PublicInteractor().getDeckCards(deck) {
                context.runOnUiThread {
                    (decklist_recycle_view.adapter as DeckListAdapter).showDeck(it)
                    onCardListChange()
                }
                userFavorites.clear()
                PrivateInteractor().getFavoriteCards(null, deck.cls.attr1) {
                    userFavorites.addAll(it)
                    PrivateInteractor().getFavoriteCards(null, deck.cls.attr2) {
                        userFavorites.addAll(it)
                    }
                }
            }
        }
    }

    fun showMissingCards(missingCards: List<CardMissing>) {
        deckListAdapter.showMissingCards(missingCards)
    }

    fun addCard(card: Card) {
        deckListAdapter.addCard(card)
        onCardListChange()
    }

    fun remCard(card: Card) {
        deckListAdapter.remCard(card)
        onCardListChange()
    }

    fun getCards(): List<CardSlot> = deckListAdapter.getCards()

    fun getSoulCost(): Int = getCards().sumBy { (it.card.rarity.soulCost * it.qtd).toInt() }

    private fun onCardListChange() {
        val cards = getCards()
        decklist_costs.updateCosts(cards)
        decklist_qtd.text = context.getString(R.string.new_deck_card_list_qtd, cards.sumBy { it.qtd.toInt() })
        decklist_soul.text = getSoulCost().toString()
    }

    class DeckListAdapter(val onAdd: (Int) -> Unit, val itemClick: (View, Card) -> Unit,
                          val itemLongClick: (View, Card) -> Boolean) : RecyclerView.Adapter<DeckListViewHolder>() {

        private val items = arrayListOf<CardSlot>()
        private var missingCards: List<CardMissing> = listOf()

        override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): DeckListViewHolder {
            return DeckListViewHolder(parent?.inflate(R.layout.itemlist_decklist_slot), itemClick, itemLongClick)
        }

        override fun onBindViewHolder(holder: DeckListViewHolder?, position: Int) {
            val cardSlot = items[position]
            val cardMissing = missingCards.find { it.shortName == cardSlot.card.shortName }
            holder?.bind(cardSlot, cardMissing?.qtd ?: 0)
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

        fun addCard(card: Card) {
            val cardSlot = items.find { it.card == card }
            if (cardSlot == null) {
                val newCardSlot = CardSlot(card, 1)
                items.add(newCardSlot)
                Collections.sort(items)
                val newCardIndex = items.indexOf(newCardSlot)
                onAdd(newCardIndex)
                notifyItemInserted(newCardIndex)
            } else {
                val newQtd = if (cardSlot.qtd < 3) cardSlot.qtd.inc() else 3
                val cardIndex = items.indexOf(cardSlot)
                items[cardIndex] = CardSlot(card, if (card.unique) 1 else newQtd)
                onAdd(cardIndex)
                notifyItemChanged(cardIndex)
            }
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
        }

        private fun notifyCardRemoved(card: Card) {
            when {
                card.attr == Attribute.DUAL && items.filter { it.card.attr == card.dualAttr1 }.isEmpty() -> {
                    EventBus.getDefault().post(CmdRemAttr(card.dualAttr1))
                }
                card.attr == Attribute.DUAL && items.filter { it.card.attr == card.dualAttr2 }.isEmpty() -> {
                    EventBus.getDefault().post(CmdRemAttr(card.dualAttr2))
                }
                items.filter { it.card.dualAttr1 == card.attr || it.card.dualAttr2 == card.attr }.isEmpty() -> {
                    EventBus.getDefault().post(CmdRemAttr(card.attr))
                }
            }
        }

        fun getCards(): List<CardSlot> {
            return items
        }

    }

    class DeckListViewHolder(view: View?, val itemClick: (View, Card) -> Unit,
                             val itemLongClick: (View, Card) -> Boolean) : RecyclerView.ViewHolder(view) {

        fun bind(slot: CardSlot, missingQtd: Long) {
            itemView.setOnClickListener { itemClick.invoke(itemView.deckslot_card_image, slot.card) }
            itemView.setOnLongClickListener { itemLongClick.invoke(itemView.deckslot_card_image, slot.card) }
            itemView.deckslot_card_image.setImageBitmap(getCroppedCardImage(slot))
            itemView.decl_slot_card_name.text = slot.card.name
            itemView.deckslot_card_rarity.setImageResource(slot.card.rarity.imageRes)
            itemView.deckslot_card_magika.setImageResource(when (slot.card.cost) {
                0 -> R.drawable.ic_magika_0
                1 -> R.drawable.ic_magika_1
                2 -> R.drawable.ic_magika_2
                3 -> R.drawable.ic_magika_3
                4 -> R.drawable.ic_magika_4
                5 -> R.drawable.ic_magika_5
                6 -> R.drawable.ic_magika_6
                else -> R.drawable.ic_magika_7plus
            })
            itemView.deckslot_card_qtd.text = slot.qtd.toString()
            itemView.deckslot_card_qtd.visibility = if (slot.qtd > 0) View.VISIBLE else View.INVISIBLE
            itemView.deckslot_card_qtd_layout.visibility = if (slot.qtd > 0) View.VISIBLE else View.INVISIBLE
            itemView.deckslot_card_qtd_missing.text = "-$missingQtd"
            itemView.deckslot_card_qtd_missing.visibility = if (missingQtd > 0) View.VISIBLE else View.INVISIBLE
        }

        private fun getCroppedCardImage(slot: CardSlot): Bitmap {
            val resources = itemView.resources
            var cardBitmap: Bitmap
            try {
                cardBitmap = slot.card.imageBitmap(itemView.context)
            } catch (e: Exception) {
                cardBitmap = BitmapFactory.decodeResource(resources, R.drawable.card)
            }
            val bmpWidth = cardBitmap.width
            val bmpHeight = cardBitmap.height
            val leftCropMargin = resources.getInteger(R.integer.decklist_slot_cover_left_crop_margin)
            val rightCropMargin = resources.getInteger(R.integer.decklist_slot_cover_right_crop_margin)
            val cropWidth = bmpWidth - leftCropMargin - rightCropMargin
            val cropeBitmap = Bitmap.createBitmap(cardBitmap, leftCropMargin, 0, cropWidth, bmpHeight * 2 / 3)
            return cropeBitmap
        }

    }

}
