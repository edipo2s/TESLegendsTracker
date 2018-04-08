package com.ediposouza.teslesgendstracker.ui.basics

import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.support.v4.app.ActivityOptionsCompat
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import com.ediposouza.teslesgendstracker.R
import com.ediposouza.teslesgendstracker.data.Card
import com.ediposouza.teslesgendstracker.data.Race
import com.ediposouza.teslesgendstracker.interactor.PublicInteractor
import com.ediposouza.teslesgendstracker.ui.base.BaseFragment
import com.ediposouza.teslesgendstracker.ui.cards.CardActivity
import com.ediposouza.teslesgendstracker.util.inflate
import com.ediposouza.teslesgendstracker.util.loadFromCard
import kotlinx.android.synthetic.main.fragment_basics_races.*
import kotlinx.android.synthetic.main.itemlist_basics_races.view.*

/**
 * Created by EdipoSouza on 6/5/17.
 */
class BasicsRacesFragment : BaseFragment() {

    val racesAdapter by lazy {
        RacesAdapter { card, view ->
            val transitionName = getString(R.string.card_transition_name)
            activity?.let {
                ActivityCompat.startActivity(it, CardActivity.newIntent(it, card),
                        ActivityOptionsCompat.makeSceneTransitionAnimation(it, view, transitionName).toBundle())
            }
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return container?.inflate(R.layout.fragment_basics_races)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        with(basics_races_recycler_view) {
            layoutManager = LinearLayoutManager(context)
            adapter = racesAdapter
        }
        PublicInteractor.getCards(null) { cards ->
            racesAdapter.cards = cards
            PublicInteractor.getRaces { races ->
                racesAdapter.updateList(races)
            }
        }
    }

    class RacesAdapter(val items: MutableList<Race> = mutableListOf(), val onCardClick: (Card, View) -> Unit) : RecyclerView.Adapter<RacesViewHolder>() {

        var cards: List<Card> = listOf()

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RacesViewHolder {
            return RacesViewHolder(parent.inflate(R.layout.itemlist_basics_races))
        }

        override fun onBindViewHolder(holder: RacesViewHolder, position: Int) {
            holder?.bind(items[position], cards, onCardClick)
        }

        override fun getItemCount(): Int = items.size

        fun updateList(races: List<Race>) {
            items.clear()
            items.addAll(races)
            notifyDataSetChanged()
        }

    }

    class RacesViewHolder(view: View?) : RecyclerView.ViewHolder(view) {

        fun bind(race: Race, cards: List<Card>, onCardClick: (Card, View) -> Unit) {
            with(itemView) {
                basics_races_name.text = race.race.name.toLowerCase().replace("_", " ").capitalize()
                basics_races_card1.visibility = View.GONE
                basics_races_card2.visibility = View.GONE
                basics_races_card3.visibility = View.GONE
                race.rewards.map { shortName ->
                    cards.find { it.shortName == shortName }
                }.forEachIndexed { index, card ->
                    if (card != null) {
                        when (index) {
                            0 -> basics_races_card1.loadCard(card, onCardClick)
                            1 -> basics_races_card2.loadCard(card, onCardClick)
                            2 -> basics_races_card3.loadCard(card, onCardClick)
                        }
                    }
                }
            }
        }

        private fun ImageView.loadCard(card: Card, onCardClick: (Card, View) -> Unit) {
            visibility = View.VISIBLE
            loadFromCard(card)
            setOnClickListener {
                onCardClick(card, this)
            }
        }

    }

}