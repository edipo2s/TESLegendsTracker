package com.ediposouza.teslesgendstracker.ui.cards.tabs

import android.os.Bundle
import android.support.annotation.LayoutRes
import android.support.design.widget.BottomSheetBehavior
import android.support.v4.content.ContextCompat
import android.support.v7.util.DiffUtil
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import com.ediposouza.teslesgendstracker.MetricAction
import com.ediposouza.teslesgendstracker.MetricScreen
import com.ediposouza.teslesgendstracker.R
import com.ediposouza.teslesgendstracker.data.Card
import com.ediposouza.teslesgendstracker.data.CardSlot
import com.ediposouza.teslesgendstracker.inflate
import com.ediposouza.teslesgendstracker.manager.MetricsManager
import com.ediposouza.teslesgendstracker.ui.base.BaseAdsAdapter
import com.ediposouza.teslesgendstracker.ui.utils.SimpleDiffCallback
import jp.wasabeef.recyclerview.animators.ScaleInAnimator
import kotlinx.android.synthetic.main.activity_dash.*
import kotlinx.android.synthetic.main.fragment_cards_list.*
import kotlinx.android.synthetic.main.itemlist_card_collection.view.*
import java.util.*

/**
 * Created by EdipoSouza on 10/30/16.
 */
class CardsCollectionFragment : CardsAllFragment() {

    override val isCardsCollection: Boolean = true

    val view_statistics by lazy { activity.collection_statistics }
    val statisticsSheetBehavior by lazy {
        BottomSheetBehavior.from(view_statistics)
    }

    val cardsCollectionAdapter by lazy {
        val gridLayoutManager = cards_recycler_view.layoutManager as GridLayoutManager
        CardsCollectionAdapter(ADS_EACH_ITEMS, gridLayoutManager, R.layout.itemlist_card_ads,
                { changeUserCardQtd(it) }) { view, card ->
            showCardExpanded(card, view)
            true
        }
    }

    val sheetBehaviorCallback: BottomSheetBehavior.BottomSheetCallback = object : BottomSheetBehavior.BottomSheetCallback() {
        override fun onSlide(bottomSheet: View, slideOffset: Float) {
        }

        override fun onStateChanged(bottomSheet: View, newState: Int) {
            val expanded = newState == BottomSheetBehavior.STATE_EXPANDED ||
                    newState == BottomSheetBehavior.STATE_SETTLING
            if (expanded) {
                view_statistics?.scrollToTop()
                view_statistics?.updateStatistics()
            }
            when (newState) {
                BottomSheetBehavior.STATE_EXPANDED -> {
                    MetricsManager.trackAction(MetricAction.ACTION_COLLECTION_STATISTICS_EXPAND())
                    MetricsManager.trackScreen(MetricScreen.SCREEN_CARDS_STATISTICS())
                }
                BottomSheetBehavior.STATE_COLLAPSED ->
                    MetricsManager.trackAction(MetricAction.ACTION_COLLECTION_STATISTICS_COLLAPSE())
            }
        }

    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        statisticsSheetBehavior.setBottomSheetCallback(sheetBehaviorCallback)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        BottomSheetBehavior.from(view_statistics).state = BottomSheetBehavior.STATE_COLLAPSED
        return super.onOptionsItemSelected(item)
    }

    override fun configRecycleView() {
        super.configRecycleView()
        cards_recycler_view.adapter = cardsCollectionAdapter
        configLoggedViews()
    }

    override fun showCards() {
        val cards = filteredCards()
        privateInteractor.getUserCollection(setFilter, currentAttr) {
            val userCards = it
            val slots = cards.map { CardSlot(it, userCards[it.shortName] ?: 0L) }
            cards_recycler_view?.itemAnimator = ScaleInAnimator()
            cardsCollectionAdapter.showCards(slots as ArrayList)
            cards_recycler_view?.scrollToPosition(0)
        }
    }

    private fun changeUserCardQtd(cardSlot: CardSlot) {
        val newQtd = cardSlot.qtd.inc()
        val finalQtd = if (newQtd <= 3) newQtd else 0
        privateInteractor.setUserCardQtd(cardSlot.card, finalQtd) {
            cards_recycler_view?.itemAnimator = null
            cardsCollectionAdapter.updateSlot(cardSlot, finalQtd)
            view_statistics?.updateStatistics(currentAttr)
            MetricsManager.trackAction(MetricAction.ACTION_COLLECTION_CARD_QTD_CHANGE(), finalQtd.toString())
        }
    }

}

class CardsCollectionAdapter(adsEachItems: Int, layoutManager: GridLayoutManager,
                             @LayoutRes adsLayout: Int, val itemClick: (CardSlot) -> Unit,
                             val itemLongClick: (View, Card) -> Boolean) : BaseAdsAdapter(adsEachItems, layoutManager, adsLayout) {

    var items: ArrayList<CardSlot> = ArrayList()

    override fun onCreateDefaultViewHolder(parent: ViewGroup): RecyclerView.ViewHolder {
        return CardsCollectionViewHolder(parent.inflate(R.layout.itemlist_card_collection), itemClick, itemLongClick)
    }

    override fun onBindDefaultViewHolder(holder: RecyclerView.ViewHolder?, position: Int) {
        (holder as CardsCollectionViewHolder).bind(items[position])
    }

    override fun getDefaultItemCount(): Int = items.size

    fun showCards(cardSlots: ArrayList<CardSlot>) {
        val oldItems = items
        items = cardSlots
        if (items.isEmpty() || items.minus(oldItems).isEmpty()) {
            notifyDataSetChanged()
            return
        }
        DiffUtil.calculateDiff(SimpleDiffCallback(items, oldItems) { oldItem, newItem ->
            oldItem.card.shortName == newItem.card.shortName
        }).dispatchUpdatesTo(this)
    }

    fun updateSlot(cardSlot: CardSlot, newQtd: Long) {
        val slotIndex = items.indexOf(cardSlot)
        if (slotIndex > -1) {
            items[slotIndex] = CardSlot(cardSlot.card, newQtd)
            notifyItemChanged(slotIndex + getAdsQtdBeforeDefaultPosition(slotIndex))
        }
    }

}

class CardsCollectionViewHolder(val view: View, val itemClick: (CardSlot) -> Unit,
                                val itemLongClick: (View, Card) -> Boolean) : RecyclerView.ViewHolder(view) {

    fun bind(cardSlot: CardSlot) {
        itemView.setOnClickListener { itemClick(cardSlot) }
        itemView.setOnLongClickListener {
            itemLongClick(itemView.card_collection_image, cardSlot.card)
        }
        itemView.card_collection_image.setImageBitmap(cardSlot.card.imageBitmap(itemView.context))
        if (cardSlot.qtd == 0L) {
            val color = ContextCompat.getColor(itemView.context, R.color.card_zero_qtd)
            itemView.card_collection_image.setColorFilter(color)
        } else {
            itemView.card_collection_image.clearColorFilter()
        }
        itemView.card_collection_qtd.setImageResource(when (cardSlot.qtd) {
            0L -> R.drawable.ic_qtd_zero
            2L -> R.drawable.ic_qtd_two
            else -> R.drawable.ic_qtd_three
        })
        itemView.card_collection_qtd.visibility = if (cardSlot.qtd == 1L) View.GONE else View.VISIBLE
    }

}