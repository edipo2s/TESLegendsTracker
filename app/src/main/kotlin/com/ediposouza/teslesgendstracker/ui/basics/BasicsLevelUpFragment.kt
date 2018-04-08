package com.ediposouza.teslesgendstracker.ui.basics

import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.support.v4.app.ActivityOptionsCompat
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.ediposouza.teslesgendstracker.R
import com.ediposouza.teslesgendstracker.data.*
import com.ediposouza.teslesgendstracker.interactor.PublicInteractor
import com.ediposouza.teslesgendstracker.ui.base.BaseFragment
import com.ediposouza.teslesgendstracker.ui.cards.CardActivity
import com.ediposouza.teslesgendstracker.util.inflate
import com.ediposouza.teslesgendstracker.util.loadFromCard
import kotlinx.android.synthetic.main.fragment_basics_levelup.*
import kotlinx.android.synthetic.main.itemlist_basics_levelup_evolve.view.*

/**
 * Created by EdipoSouza on 6/5/17.
 */
class BasicsLevelUpFragment : BaseFragment() {

    val levelUpAdapter by lazy {
        LevelUpAdapter { card, view ->
            val transitionName = getString(R.string.card_transition_name)
            activity?.let {
                ActivityCompat.startActivity(it, CardActivity.newIntent(it, card),
                        ActivityOptionsCompat.makeSceneTransitionAnimation(it, view, transitionName).toBundle())
            }
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return container?.inflate(R.layout.fragment_basics_levelup)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        with(basics_levelup_recycler_view) {
            layoutManager = LinearLayoutManager(context)
            adapter = levelUpAdapter
        }
        PublicInteractor.getCards(null) { cards ->
            levelUpAdapter.cards = cards
            PublicInteractor.getLevels { levels ->
                levelUpAdapter.updateList(levels)
            }
        }
    }

    class LevelUpAdapter(val items: MutableList<LevelUp> = mutableListOf(), val onCardClick: (Card, View) -> Unit) : RecyclerView.Adapter<LevelUpViewHolder>() {

        var cards: List<Card> = listOf()

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LevelUpViewHolder {
            val isEvolveType = viewType == LevelUpType.EVOLVE.ordinal
            val layout = R.layout.itemlist_basics_levelup_evolve.takeIf { isEvolveType }
                    ?: R.layout.itemlist_basics_levelup_card
            return LevelUpViewHolder(parent.inflate(layout))
        }

        override fun onBindViewHolder(holder: LevelUpViewHolder, position: Int) {
            val levelUp = items[position]
            holder?.bind(levelUp)
            if (levelUp.target.isNotEmpty()) {
                holder?.bindEvolveCards(levelUp, cards, onCardClick)
            }
        }

        override fun getItemCount(): Int = items.size

        override fun getItemViewType(position: Int): Int = items[position].type.ordinal

        fun updateList(levels: List<LevelUp>) {
            items.clear()
            items.addAll(levels)
            notifyDataSetChanged()
        }

    }

    class LevelUpViewHolder(view: View?) : RecyclerView.ViewHolder(view) {

        fun bind(levelUp: LevelUp) {
            with(itemView) {
                basics_levelup_level.text = "${levelUp.level}"
                val extraCard = levelUp.extra == LevelUpExtra.CARD || levelUp.source == LevelUpSource.RARITY || levelUp.legendary
                basics_levelup_extra_card_value.visibility = View.VISIBLE.takeIf { extraCard } ?: View.INVISIBLE
                basics_levelup_extra_card_rarity.visibility = View.VISIBLE.takeIf { extraCard } ?: View.GONE
                basics_levelup_extra_card_racial.visibility = View.VISIBLE.takeIf { levelUp.racial } ?: View.GONE
                basics_levelup_extra_card_racial.setOnClickListener {
                    Toast.makeText(context, R.string.basics_portrait, Toast.LENGTH_SHORT).show()
                }
                if (extraCard) {
                    val rarity = CardRarity.LEGENDARY.name.takeIf { levelUp.legendary } ?:
                            levelUp.sourceValue.takeIf { levelUp.source == LevelUpSource.RARITY } ?: levelUp.extraValue
                    basics_levelup_extra_card_rarity.setImageResource(CardRarity.of(rarity).imageRes)
                }
                basics_levelup_extra_gold.visibility = View.VISIBLE.takeIf { levelUp.gold > 0 } ?: View.GONE
                basics_levelup_extra_gold_value.visibility = View.VISIBLE.takeIf { levelUp.gold > 0 } ?: View.GONE
                basics_levelup_extra_gold_value.setText("${levelUp.gold}")
                val extraPack = levelUp.extra == LevelUpExtra.PACK
                basics_levelup_extra_pack.visibility = View.VISIBLE.takeIf { extraPack } ?: View.GONE
                basics_levelup_extra_pack_value.visibility = View.VISIBLE.takeIf { extraPack } ?: View.GONE
                val anyExtras = extraCard || extraPack || levelUp.gold > 0
                basics_levelup_extras?.visibility = View.VISIBLE.takeIf { anyExtras } ?: View.INVISIBLE
            }
        }

        fun bindEvolveCards(levelUp: LevelUp, cards: List<Card>, onCardClick: (Card, View) -> Unit) {
            with(itemView) {
                cards.find { it.shortName == levelUp.sourceValue }?.apply {
                    basics_levelup_source.loadFromCard(this)
                    basics_levelup_source.setOnClickListener {
                        onCardClick(this, basics_levelup_source)
                    }
                }
                cards.find { it.shortName == levelUp.target.first() }?.apply {
                    basics_levelup_target1.loadFromCard(this)
                    basics_levelup_target1.setOnClickListener {
                        onCardClick(this, basics_levelup_target1)
                    }
                }
                val secondTarget = levelUp.target.size > 1
                val cardHeigth = R.dimen.card_height_nano.takeIf { secondTarget } ?: R.dimen.card_height_micro
                basics_levelup_target1.layoutParams = basics_levelup_target1.layoutParams.apply {
                    height = resources.getDimensionPixelSize(cardHeigth)
                }
                basics_levelup_target2.layoutParams = basics_levelup_target2.layoutParams.apply {
                    height = resources.getDimensionPixelSize(cardHeigth)
                }
                basics_levelup_target2.visibility = View.VISIBLE.takeIf { secondTarget } ?: View.GONE
                cards.find { it.shortName == levelUp.target.last() }?.apply {
                    basics_levelup_target2.loadFromCard(this)
                    basics_levelup_target2.setOnClickListener {
                        onCardClick(this, basics_levelup_target2)
                    }
                }
            }
        }

    }

}