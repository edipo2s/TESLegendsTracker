package com.ediposouza.teslesgendstracker.ui.decks.tabs

import android.os.Bundle
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.ediposouza.teslesgendstracker.R
import com.ediposouza.teslesgendstracker.data.Class
import com.ediposouza.teslesgendstracker.data.Deck
import com.ediposouza.teslesgendstracker.inflate
import com.ediposouza.teslesgendstracker.interactor.PublicInteractor
import com.ediposouza.teslesgendstracker.ui.cards.BaseFragment
import jp.wasabeef.recyclerview.animators.SlideInLeftAnimator
import kotlinx.android.synthetic.main.fragment_decks_list.*
import kotlinx.android.synthetic.main.itemlist_deck.view.*
import org.jetbrains.anko.toast
import timber.log.Timber
import java.text.NumberFormat
import java.util.*

/**
 * Created by EdipoSouza on 11/18/16.
 */
class DecksPublicFragment : BaseFragment() {

    private val publicInteractor = PublicInteractor()

    private val decksAdapter = DecksAllAdapter({ view: View, deck: Deck -> activity.toast(deck.name) }) {
        view: View, deck: Deck ->
        true
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return container?.inflate(R.layout.fragment_decks_list)
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        decks_recycler_view.adapter = decksAdapter
        decks_recycler_view.itemAnimator = SlideInLeftAnimator()
        publicInteractor.getPublicDecks(Class.ASSASSIN, {
            it.forEach { Timber.d("Public: %s", it.toString()) }
            decksAdapter.showDecks(it)
        })
//        var cards = mapOf("dunmernightblade" to 3, "tazkadthepackmaster" to 1, "elusiveschemer" to 2)
//        publicInteractor.saveDeck("DeckTest", Class.ASSASSIN, DeckType.MIDRANGE, 8750, "2016_11_03", cards, false){
//            Timber.d("Saved")
//        }
//        cards = mapOf("dunmernightblade" to 3, "rapidshot" to 1, "elusiveschemer" to 2)
//        publicInteractor.saveDeck("DeckPrivate", Class.BATTLEMAGE, DeckType.AGGRO, 7750, "2016_11_03", cards, true){
//            Timber.d("Saved")
//        }
        publicInteractor.getMyPrivateDecks {
            it.forEach {
                Timber.d("Private: %s", it.toString())
            }
        }
    }

}

class DecksAllAdapter(val itemClick: (View, Deck) -> Unit,
                      val itemLongClick: (View, Deck) -> Boolean) : RecyclerView.Adapter<DecksAllViewHolder>() {

    var items: List<Deck> = ArrayList()

    override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): DecksAllViewHolder {
        return DecksAllViewHolder(LayoutInflater.from(parent?.context)
                .inflate(R.layout.itemlist_deck, parent, false), itemClick, itemLongClick)
    }

    override fun onBindViewHolder(holder: DecksAllViewHolder?, position: Int) {
        holder?.bind(items[position])
    }

    override fun getItemCount(): Int = items.size

    fun showDecks(decks: List<Deck>) {
        items = decks
        notifyDataSetChanged()
//        val oldItems = items
//        items = cards
//        DiffUtil.calculateDiff(object : DiffUtil.Callback() {
//            override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
//                return oldItems[oldItemPosition] == items[newItemPosition]
//            }
//
//            override fun getOldListSize(): Int = oldItems.size
//
//            override fun getNewListSize(): Int = items.size
//
//            override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
//                return oldItems[oldItemPosition].shortName == items[newItemPosition].shortName
//            }
//
//        }, false).dispatchUpdatesTo(this)
    }
}

class DecksAllViewHolder(val view: View, val itemClick: (View, Deck) -> Unit,
                         val itemLongClick: (View, Deck) -> Boolean) : RecyclerView.ViewHolder(view) {

    fun bind(deck: Deck) {
        itemView.setOnClickListener { itemClick(itemView.deck_bg, deck) }
        itemView.setOnLongClickListener { itemLongClick(itemView.deck_bg, deck) }
        itemView.deck_name.text = deck.name
        itemView.deck_type.text = deck.type.name.toUpperCase()
        itemView.deck_date.text = deck.createdAt.toString()
        itemView.deck_soul_cost.text = NumberFormat.getNumberInstance().format(deck.cost)
        itemView.deck_comments.text = NumberFormat.getNumberInstance().format(deck.comments.size)
        itemView.deck_likes.text = NumberFormat.getNumberInstance().format(deck.likes.size)
        itemView.deck_views.text = NumberFormat.getNumberInstance().format(deck.views)
    }

}