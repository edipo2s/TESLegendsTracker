package com.ediposouza.teslesgendstracker.ui.cards.tabs

import android.animation.ValueAnimator
import android.os.Bundle
import android.support.design.widget.BottomSheetBehavior
import android.support.v4.content.ContextCompat
import android.support.v7.util.DiffUtil
import android.support.v7.widget.CardView
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import com.ediposouza.teslesgendstracker.R
import com.ediposouza.teslesgendstracker.data.Card
import com.ediposouza.teslesgendstracker.data.CardSlot
import com.ediposouza.teslesgendstracker.inflate
import com.ediposouza.teslesgendstracker.toogleExpanded
import com.ediposouza.teslesgendstracker.ui.cards.CmdHideStatistics
import com.ediposouza.teslesgendstracker.ui.cards.CmdShowStatistics
import com.ediposouza.teslesgendstracker.ui.cards.CmdUpdateFiltersBottomMargin
import jp.wasabeef.recyclerview.animators.ScaleInAnimator
import kotlinx.android.synthetic.main.activity_dash.*
import kotlinx.android.synthetic.main.fragment_cards_list_collection.*
import kotlinx.android.synthetic.main.itemlist_card_collection.view.*
import org.greenrobot.eventbus.Subscribe
import timber.log.Timber
import java.util.*

/**
 * Created by EdipoSouza on 10/30/16.
 */
class CardsCollectionFragment : CardsAllFragment() {

    private val ANIM_DURATION = 500L
    private var bottomSheetBehaviorHiding: Boolean = false
    private var bottomSheetBehaviorExpanded: Boolean? = null

    val statisticsSheetBehavior: BottomSheetBehavior<CardView> by lazy {
        BottomSheetBehavior.from(collection_statistics_bottom_sheet)
    }

    val cardsCollectionAdapter = CardsCollectionAdapter({ changeUserCardQtd(it) }) { view: View, card: Card ->
        showCardExpanded(card, view)
        true
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return container?.inflate(R.layout.fragment_cards_list_collection)
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        collection_statistics_bottom_sheet.setOnClickListener { statisticsSheetBehavior.toogleExpanded() }
        collection_statistics_back.setOnClickListener { statisticsSheetBehavior.toogleExpanded() }
        statisticsSheetBehavior.setBottomSheetCallback(sheetBehaviorCallback)
        statisticsSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN
        hideStatisticsBack()
    }

    override fun configRecycleView() {
        super.configRecycleView()
        cards_recycler_view.adapter = cardsCollectionAdapter
        collection_statistics.updateStatistics()
    }

    override fun showCards() {
        val cards = filteredCards()
        privateInteractor.getUserCollection(currentAttr) {
            val userCards = it
            val slots = cards.map { CardSlot(it, userCards[it.shortName] ?: 0L) }
            cards_recycler_view.itemAnimator = ScaleInAnimator()
            cardsCollectionAdapter.showCards(slots as ArrayList)
            cards_recycler_view.scrollToPosition(0)
        }
    }

    private fun changeUserCardQtd(cardSlot: CardSlot) {
        val newQtd = cardSlot.qtd.inc()
        val finalQtd = if (newQtd <= 3) newQtd else 0
        privateInteractor.setUserCardQtd(cardSlot.card, finalQtd) {
            cards_recycler_view.itemAnimator = null
            cardsCollectionAdapter.updateSlot(cardSlot, finalQtd)
            collection_statistics.updateStatistics(currentAttr)
        }
    }

    override fun updateCardsList() {
        super.updateCardsList()
        collection_statistics?.updateStatistics()
    }

    @Subscribe
    fun hideStatistics(hideStatistics: CmdHideStatistics) {
        bottomSheetBehaviorHiding = true
        statisticsSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN
    }

    @Subscribe
    fun showStatistics(showStatistics: CmdShowStatistics) {
        statisticsSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
    }

    private fun showStatisticsBack() {
        val showAnimation = AnimationUtils.loadAnimation(context, R.anim.fab_scale_up)
        showAnimation.fillAfter = true
        collection_statistics_back.startAnimation(showAnimation)
    }

    private fun hideStatisticsBack() {
        val hideAnimation = AnimationUtils.loadAnimation(context, R.anim.fab_scale_down)
        hideAnimation.fillAfter = true
        collection_statistics_back.startAnimation(hideAnimation)
    }

    val sheetBehaviorCallback: BottomSheetBehavior.BottomSheetCallback = object : BottomSheetBehavior.BottomSheetCallback() {
        override fun onSlide(bottomSheet: View, slideOffset: Float) {
        }

        override fun onStateChanged(bottomSheet: View, newState: Int) {
            if (bottomSheetBehaviorHiding) {
                if (newState != BottomSheetBehavior.STATE_SETTLING) {
                    Timber.d("State: %d", newState)
                    bottomSheetBehaviorHiding = newState != BottomSheetBehavior.STATE_COLLAPSED
                    eventBus.post(CmdUpdateFiltersBottomMargin(newState == BottomSheetBehavior.STATE_COLLAPSED))
                }
                return
            }
            val expanded = newState == BottomSheetBehavior.STATE_EXPANDED ||
                    newState == BottomSheetBehavior.STATE_SETTLING
            if (bottomSheetBehaviorExpanded == null && newState == BottomSheetBehavior.STATE_COLLAPSED) {
                bottomSheetBehaviorExpanded = false
            }
            if (bottomSheetBehaviorExpanded != null) {
                activity.dash_filter_rarity.visibility = if (expanded) View.INVISIBLE else View.VISIBLE
                activity.dash_filter_magika.visibility = if (expanded) View.INVISIBLE else View.VISIBLE
            }
            animBackButton(newState)
        }

        private fun animBackButton(newState: Int) {
            if (newState == BottomSheetBehavior.STATE_EXPANDED) {
                bottomSheetBehaviorExpanded = true
                with(ValueAnimator.ofFloat(1f, 0f)) {
                    duration = ANIM_DURATION
                    addUpdateListener { collection_statistics_title.alpha = it.animatedValue as Float }
                    start()
                }
                showStatisticsBack()
            }
            if (newState == BottomSheetBehavior.STATE_COLLAPSED && bottomSheetBehaviorExpanded ?: false) {
                bottomSheetBehaviorExpanded = false
                with(ValueAnimator.ofFloat(0f, 1f)) {
                    duration = ANIM_DURATION
                    addUpdateListener { collection_statistics_title.alpha = it.animatedValue as Float }
                    start()
                }
                hideStatisticsBack()
            }
        }

    }
}

class CardsCollectionAdapter(val itemClick: (CardSlot) -> Unit,
                             val itemLongClick: (View, Card) -> Boolean) : RecyclerView.Adapter<CardsCollectionViewHolder>() {

    var items: ArrayList<CardSlot> = ArrayList()

    override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): CardsCollectionViewHolder {
        return CardsCollectionViewHolder(LayoutInflater.from(parent?.context)
                .inflate(R.layout.itemlist_card_collection, parent, false), itemClick, itemLongClick)
    }

    override fun onBindViewHolder(holder: CardsCollectionViewHolder?, position: Int) {
        holder?.bind(items[position])
    }

    override fun getItemCount(): Int = items.size

    fun showCards(cardSlots: ArrayList<CardSlot>) {
        val oldItems = items
        items = cardSlots
        DiffUtil.calculateDiff(object : DiffUtil.Callback() {
            override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
                return oldItems[oldItemPosition] == items[newItemPosition]
            }

            override fun getOldListSize(): Int = oldItems.size

            override fun getNewListSize(): Int = items.size

            override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
                return oldItems[oldItemPosition].card.shortName == items[newItemPosition].card.shortName
            }

        }, false).dispatchUpdatesTo(this)
    }

    fun updateSlot(cardSlot: CardSlot, newQtd: Long) {
        val slotIndex = items.indexOf(cardSlot)
        if (slotIndex > -1) {
            items[slotIndex] = CardSlot(cardSlot.card, newQtd)
            notifyItemChanged(slotIndex)
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