package com.ediposouza.teslesgendstracker.ui.cards

import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.support.v4.app.ActivityOptionsCompat
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.ediposouza.teslesgendstracker.R
import com.ediposouza.teslesgendstracker.data.Attribute
import com.ediposouza.teslesgendstracker.data.Card
import com.ediposouza.teslesgendstracker.data.CardRarity
import com.ediposouza.teslesgendstracker.interactor.CardInteractor
import com.ediposouza.teslesgendstracker.ui.CardActivity
import com.ediposouza.teslesgendstracker.ui.utils.GridSpacingItemDecoration
import com.ediposouza.teslesgendstracker.ui.widget.CmdFilterMagika
import com.ediposouza.teslesgendstracker.ui.widget.CmdFilterRarity
import com.ediposouza.teslesgendstracker.ui.widget.CmdFilterSearch
import com.ediposouza.teslesgendstracker.ui.widget.CmdShowCardsByAttr
import kotlinx.android.synthetic.main.fragment_cards_all.*
import kotlinx.android.synthetic.main.itemlist_card.view.*
import org.greenrobot.eventbus.Subscribe
import java.util.*

/**
 * Created by EdipoSouza on 10/30/16.
 */
class CardsAllFragment : BaseFragment() {

    var cardsLoaded: List<Card> = ArrayList()
    var magikaFilter: Int = -1
    var rarityFilter: CardRarity? = null
    var searchFilter: String? = null

    val transitionName: String by lazy { getString(R.string.card_transition_name) }
    val cardsAdapter: CardsAllAdapter = CardsAllAdapter { view: View, card: Card ->
        ActivityCompat.startActivity(activity, CardActivity.newIntent(context, card),
                ActivityOptionsCompat.makeSceneTransitionAnimation(activity, view, transitionName).toBundle())
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
        loadCardsByAttr(CmdShowCardsByAttr(Attribute.STRENGTH))
    }

    @Subscribe
    fun loadCardsByAttr(showCardsByAttr: CmdShowCardsByAttr) {
        CardInteractor().getCards(showCardsByAttr.attr, {
            cardsLoaded = it
            showCards()
        })
    }

    @Subscribe
    fun onFilterSearch(filterSearch: CmdFilterSearch) {
        searchFilter = filterSearch.search
        showCards()
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
        cardsAdapter.showCards(cardsLoaded
                .filter {
                    when (searchFilter) {
                        null -> it is Card
                        else -> {
                            val search = searchFilter!!.toLowerCase().trim()
                            it.name.toLowerCase().contains(search) ||
                                    it.race.name.toLowerCase().contains(search) ||
                                    it.type.name.toLowerCase().contains(search) ||
                                    it.keywords.filter { it.name.toLowerCase().contains(search) }.size > 0
                        }
                    }
                }
                .filter {
                    when (rarityFilter) {
                        null -> it.rarity is CardRarity
                        else -> it.rarity == rarityFilter
                    }
                }
                .filter {
                    when {
                        magikaFilter == -1 -> it.cost is Int
                        magikaFilter < 7 -> it.cost == magikaFilter
                        else -> it.cost >= magikaFilter
                    }
                })
        cards_recycler_view.scrollToPosition(0)
    }

}

class CardsAllAdapter(val itemClick: (View, Card) -> Unit) : RecyclerView.Adapter<CardsAllViewHolder>() {

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

class CardsAllViewHolder(val view: View, val itemClick: (View, Card) -> Unit) : RecyclerView.ViewHolder(view) {

    fun bind(card: Card) {
        itemView.setOnClickListener { itemClick(itemView.card_imageview, card) }
        itemView.card_imageview.setImageBitmap(card.imageBitmap(itemView.context))
    }

}