package com.ediposouza.teslesgendstracker.ui.widget

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
import kotlinx.android.synthetic.main.itemlist_decklist_slot.view.*
import kotlinx.android.synthetic.main.widget_decklist.view.*
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.runOnUiThread

/**
 * Created by EdipoSouza on 11/2/16.
 */
class DeckList(ctx: Context?, attrs: AttributeSet?, defStyleAttr: Int) :
        LinearLayout(ctx, attrs, defStyleAttr) {

    var userFavorites = arrayListOf<String>()

    val deckListAdapter by lazy {
        DeckListAdapter { view, card ->
            val favorite = userFavorites.contains(card.shortName)
            val transitionName = context.getString(R.string.card_transition_name)
            ActivityCompat.startActivity(context, CardActivity.newIntent(context, card, favorite),
                    ActivityOptionsCompat.makeSceneTransitionAnimation(context as Activity, view, transitionName).toBundle())
        }
    }

    init {
        inflate(context, R.layout.widget_decklist, this)
        decklist_recycle_view.adapter = deckListAdapter
        decklist_recycle_view.layoutManager = LinearLayoutManager(context)
        decklist_recycle_view.setHasFixedSize(true)
        if (isInEditMode) {
            val card = Card("Tyr", "tyr", Attribute.DUAL, CardRarity.EPIC, false, 0, 0, 0, CardType.ACTION,
                    CardRace.ARGONIAN, emptyList<CardKeyword>(), CardArenaTier.AVERAGE, false)
            val cards = listOf(CardSlot(card, 3), CardSlot(card, 1), CardSlot(card, 2), CardSlot(card, 3))
            deckListAdapter.showDeck(cards)
        }
    }

    constructor(ctx: Context?) : this(ctx, null, 0) {
    }

    constructor(ctx: Context?, attrs: AttributeSet) : this(ctx, attrs, 0) {
    }

    fun showDeck(deck: Deck) {
        doAsync {
            PublicInteractor().getDeckCards(deck) {
                context.runOnUiThread {
                    (decklist_recycle_view.adapter as DeckListAdapter).showDeck(it)
                }
                userFavorites.clear()
                PrivateInteractor().getFavoriteCards(deck.cls.attr1) {
                    userFavorites.addAll(it)
                    PrivateInteractor().getFavoriteCards(deck.cls.attr2) {
                        userFavorites.addAll(it)
                    }
                }
            }
        }
    }

}

class DeckListAdapter(val itemClick: (View, Card) -> Unit) : RecyclerView.Adapter<DeckListViewHolder>() {

    private val items = arrayListOf<CardSlot>()

    override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): DeckListViewHolder {
        return DeckListViewHolder(parent?.inflate(R.layout.itemlist_decklist_slot), itemClick)
    }

    override fun onBindViewHolder(holder: DeckListViewHolder?, position: Int) {
        holder?.bind(items[position])
    }

    override fun getItemCount(): Int = items.size

    fun showDeck(cards: List<CardSlot>) {
        items.clear()
        items.addAll(cards)
        notifyDataSetChanged()
    }

}

class DeckListViewHolder(view: View?, val itemClick: (View, Card) -> Unit) : RecyclerView.ViewHolder(view) {

    fun bind(slot: CardSlot) {
        itemView.setOnClickListener { itemClick.invoke(itemView.deckslot_card_image, slot.card) }
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