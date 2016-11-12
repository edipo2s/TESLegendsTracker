package com.ediposouza.teslesgendstracker.ui.cards

import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.support.v4.app.ActivityOptionsCompat
import android.support.v7.util.DiffUtil
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.ediposouza.teslesgendstracker.R
import com.ediposouza.teslesgendstracker.data.Attribute
import com.ediposouza.teslesgendstracker.data.Card
import com.ediposouza.teslesgendstracker.data.CardRarity
import com.ediposouza.teslesgendstracker.interactor.PrivateInteractor
import com.ediposouza.teslesgendstracker.interactor.PublicInteractor
import com.ediposouza.teslesgendstracker.ui.CardActivity
import com.ediposouza.teslesgendstracker.ui.utils.GridSpacingItemDecoration
import com.ediposouza.teslesgendstracker.ui.widget.CmdFilterMagika
import com.ediposouza.teslesgendstracker.ui.widget.CmdFilterRarity
import com.ediposouza.teslesgendstracker.ui.widget.CmdFilterSearch
import com.ediposouza.teslesgendstracker.ui.widget.CmdShowCardsByAttr
import jp.wasabeef.recyclerview.animators.ScaleInAnimator
import kotlinx.android.synthetic.main.fragment_cards_all.*
import kotlinx.android.synthetic.main.itemlist_card.view.*
import org.greenrobot.eventbus.Subscribe
import java.util.*

/**
 * Created by EdipoSouza on 10/30/16.
 */
open class CardsAllFragment : BaseFragment() {

    var currentAttr: Attribute = Attribute.STRENGTH
    var cardsLoaded: List<Card> = ArrayList()
    var userFavorites: List<String> = ArrayList()
    var magikaFilter: Int = -1
    var rarityFilter: CardRarity? = null
    var searchFilter: String? = null

    val privateInteractor: PrivateInteractor by lazy { PrivateInteractor() }
    val transitionName: String by lazy { getString(R.string.card_transition_name) }

    open val cardsAdapter = CardsAllAdapter({ view: View, card: Card -> showCardExpanded(card, view) }) {
        view: View, card: Card ->
        showCardExpanded(card, view)
        true
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater?.inflate(R.layout.fragment_cards_all, container, false)
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        configRecycleView()
    }

    open fun configRecycleView() {
        cards_recycler_view.adapter = cardsAdapter
        cards_recycler_view.layoutManager = GridLayoutManager(context, 3)
        cards_recycler_view.itemAnimator = ScaleInAnimator()
        cards_recycler_view.setHasFixedSize(true)
        cards_recycler_view.addItemDecoration(GridSpacingItemDecoration(3,
                resources.getDimensionPixelSize(R.dimen.card_margin), true, false))
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

    @Subscribe
    open fun loadCardsByAttr(showCardsByAttr: CmdShowCardsByAttr) {
        currentAttr = showCardsByAttr.attr
        PublicInteractor().getCards(showCardsByAttr.attr, {
            cardsLoaded = it
            showCards()
        })
        privateInteractor.getUserFavorites(currentAttr) {
            userFavorites = it
        }
    }

    open fun showCards() {
        cardsAdapter.showCards(filteredCards())
        cards_recycler_view.scrollToPosition(0)
    }

    open fun updateCardsList() {
        if (cards_recycler_view != null) {
            loadCardsByAttr(CmdShowCardsByAttr(currentAttr))
        }
    }

    open fun filteredCards(): List<Card> {
        return cardsLoaded
                .filter {
                    when (searchFilter) {
                        null -> it is Card
                        else -> {
                            val search = searchFilter!!.toLowerCase().trim()
                            it.name.toLowerCase().contains(search) ||
                                    it.race.name.toLowerCase().contains(search) ||
                                    it.type.name.toLowerCase().contains(search) ||
                                    it.keywords.filter { it.name.toLowerCase().contains(search) }.isNotEmpty()
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
                }
    }

    open fun showCardExpanded(card: Card, view: View) {
        val favorite = userFavorites.contains(card.shortName)
        ActivityCompat.startActivity(activity, CardActivity.newIntent(context, card, favorite),
                ActivityOptionsCompat.makeSceneTransitionAnimation(activity, view, transitionName).toBundle())
    }

}

class CardsAllAdapter(val itemClick: (View, Card) -> Unit,
                      val itemLongClick: (View, Card) -> Boolean) : RecyclerView.Adapter<CardsAllViewHolder>() {

    var items: List<Card> = ArrayList()

    override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): CardsAllViewHolder {
        return CardsAllViewHolder(LayoutInflater.from(parent?.context)
                .inflate(R.layout.itemlist_card, parent, false), itemClick, itemLongClick)
    }

    override fun onBindViewHolder(holder: CardsAllViewHolder?, position: Int) {
        holder?.bind(items[position])
    }

    override fun getItemCount(): Int = items.size

    fun showCards(cards: List<Card>) {
        val oldItems = items
        items = cards
        DiffUtil.calculateDiff(object : DiffUtil.Callback() {
            override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
                return oldItems[oldItemPosition] == items[newItemPosition]
            }

            override fun getOldListSize(): Int = oldItems.size

            override fun getNewListSize(): Int = items.size

            override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
                return oldItems[oldItemPosition].shortName == items[newItemPosition].shortName
            }

        }, false).dispatchUpdatesTo(this)
    }
}

class CardsAllViewHolder(val view: View, val itemClick: (View, Card) -> Unit,
                         val itemLongClick: (View, Card) -> Boolean) : RecyclerView.ViewHolder(view) {

    fun bind(card: Card) {
        itemView.setOnClickListener { itemClick(itemView.card_all_image, card) }
        itemView.setOnLongClickListener {
            itemLongClick(itemView.card_all_image, card)
        }
        itemView.card_all_image.setImageBitmap(card.imageBitmap(itemView.context))
    }

}