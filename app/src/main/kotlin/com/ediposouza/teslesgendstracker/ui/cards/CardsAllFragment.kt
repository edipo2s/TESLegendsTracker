package com.ediposouza.teslesgendstracker.ui.cards

import android.graphics.BitmapFactory
import android.os.Bundle
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.ediposouza.teslesgendstracker.R
import com.ediposouza.teslesgendstracker.data.Attribute
import com.ediposouza.teslesgendstracker.data.Card
import com.ediposouza.teslesgendstracker.data.CardRarity
import com.ediposouza.teslesgendstracker.interactor.CardInteractor
import com.ediposouza.teslesgendstracker.ui.CmdFilterMagika
import com.ediposouza.teslesgendstracker.ui.CmdFilterRarity
import com.ediposouza.teslesgendstracker.ui.utils.GridSpacingItemDecoration
import kotlinx.android.synthetic.main.fragment_cards_all.*
import kotlinx.android.synthetic.main.itemlist_card.view.*
import org.greenrobot.eventbus.Subscribe
import org.jetbrains.anko.toast
import java.util.*

/**
 * Created by EdipoSouza on 10/30/16.
 */
class CardsAllFragment : BaseFragment() {

    var cardsLoaded: List<Card> = ArrayList()
    var magikaFilter: Int = -1
    var rarityFilter: CardRarity? = null
    val cardsAdapter: CardsAllAdapter = CardsAllAdapter {
        context.toast(it.name)
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater?.inflate(R.layout.fragment_cards_all, container, false)
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        cards_recycler_view.adapter = cardsAdapter
        cards_recycler_view.setHasFixedSize(true)
        cards_recycler_view.addItemDecoration(GridSpacingItemDecoration(3,
                resources.getDimensionPixelSize(R.dimen.card_margin), true, false))
        cards_attr_filter.filterClick = { loadCardsByAttr(it) }
        loadCardsByAttr(Attribute.STRENGTH)
    }

    private fun loadCardsByAttr(attr: Attribute) {
        CardInteractor().getCards(attr, {
            cardsLoaded = it
            cardsAdapter.showCards(cardsLoaded)
            cards_recycler_view.scrollToPosition(0)
        })
    }

    @Subscribe
    fun onFilterRarity(filterRarity: CmdFilterRarity) {
        rarityFilter = filterRarity.rarity
        showCards()
    }

    @Subscribe
    fun onFilterMagika(filterMagika: CmdFilterMagika) {
        magikaFilter = filterMagika.magika
        showCards()
    }

    fun showCards() {
        var cardsToShow = if (rarityFilter == null) cardsLoaded else
            cardsLoaded.filter { it.rarity == rarityFilter }
        cardsToShow = cardsToShow.filter {
            when {
                magikaFilter == -1 -> it.cost > -1
                magikaFilter < 7 -> it.cost == magikaFilter
                else -> it.cost >= magikaFilter
            }
        }
        cardsAdapter.showCards(cardsToShow)
        cards_recycler_view.scrollToPosition(0)
    }

}

class CardsAllAdapter(val itemClick: (Card) -> Unit) : RecyclerView.Adapter<CardsAllViewHolder>() {

    val items: ArrayList<Card> = ArrayList()

    override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): CardsAllViewHolder {
        return CardsAllViewHolder(LayoutInflater.from(parent?.context)
                .inflate(R.layout.itemlist_card, parent, false), itemClick)
    }

    override fun onBindViewHolder(holder: CardsAllViewHolder?, position: Int) {
        holder?.bind(items.get(position))
    }

    override fun getItemCount(): Int = items.size

    fun showCards(cards: List<Card>) {
        items.clear()
        items.addAll(cards)
        notifyDataSetChanged()
    }
}

class CardsAllViewHolder(val view: View, val itemClick: (Card) -> Unit) : RecyclerView.ViewHolder(view) {

    fun bind(card: Card) {
        itemView.setOnClickListener { itemClick(card) }
        val cardImage = BitmapFactory.decodeStream(itemView.resources.assets.open(card.imagePath()))
        itemView.card_imageview.setImageBitmap(cardImage)
    }

}