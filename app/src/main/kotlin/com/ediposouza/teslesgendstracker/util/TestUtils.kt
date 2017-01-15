package com.ediposouza.teslesgendstracker.util

import android.support.v7.widget.RecyclerView
import android.view.ViewGroup
import com.ediposouza.teslesgendstracker.R
import com.ediposouza.teslesgendstracker.data.*
import com.ediposouza.teslesgendstracker.ui.matches.tabs.MatchesHistoryFragment
import com.timehop.stickyheadersrecyclerview.StickyRecyclerHeadersAdapter
import org.threeten.bp.LocalDateTime
import timber.log.Timber

/**
 * Created by EdipoSouza on 1/5/17.
 */
object TestUtils {

    fun getTestMatches(): List<Match> {
        val decks = listOf(
                MatchDeck("", Class.ARCHER, DeckType.AGGRO, ""),
                MatchDeck("", Class.ASSASSIN, DeckType.AGGRO, ""),
                MatchDeck("", Class.BATTLEMAGE, DeckType.AGGRO, ""),
                MatchDeck("", Class.CRUSADER, DeckType.AGGRO, ""),
                MatchDeck("", Class.MAGE, DeckType.COMBO, ""),
                MatchDeck("", Class.MONK, DeckType.COMBO, ""),
                MatchDeck("", Class.SCOUT, DeckType.COMBO, ""),
                MatchDeck("", Class.SORCERER, DeckType.COMBO, ""),
                MatchDeck("", Class.SPELLSWORD, DeckType.CONTROL, ""),
                MatchDeck("", Class.WARRIOR, DeckType.CONTROL, ""),
                MatchDeck("", Class.STRENGTH, DeckType.CONTROL, ""),
                MatchDeck("", Class.INTELLIGENCE, DeckType.CONTROL, ""),
                MatchDeck("", Class.AGILITY, DeckType.MIDRANGE, ""),
                MatchDeck("", Class.WILLPOWER, DeckType.MIDRANGE, ""),
                MatchDeck("", Class.ENDURANCE, DeckType.MIDRANGE, ""),
                MatchDeck("", Class.NEUTRAL, DeckType.MIDRANGE, "")
        )
        return mutableListOf<Match>().apply {
            for ((indexPlayer, playerDeck) in decks.withIndex()) {
                for ((indexOpponent, opponentDeck) in decks.withIndex()) {
                    addAll(mutableListOf<Match>().apply {
                        for (i in 1..indexPlayer + 1) {
                            val uuid = LocalDateTime.now().withNano(0).minusDays(playerDeck.type.ordinal.toLong()).toString()
                            add(Match(uuid, false, playerDeck, opponentDeck, MatchMode.RANKED, "2016_12", 0, false, true))
                        }
                        for (i in 1..indexOpponent + 1) {
                            val uuid = LocalDateTime.now().withNano(0).minusDays(opponentDeck.type.ordinal.toLong()).toString()
                            add(Match(uuid, false, playerDeck, opponentDeck, MatchMode.RANKED, "2016_12", 0, false, false))
                        }
                    })
                }
            }
        }
    }

    fun getTestMatchesHistoryAdapter() = TestMatchesHistoryAdapter()

    class TestMatchesHistoryAdapter : RecyclerView.Adapter<MatchesHistoryFragment.MatchViewHolder>(),
            StickyRecyclerHeadersAdapter<MatchesHistoryFragment.MatchViewHolder> {

        val items = TestUtils.getTestMatches()

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MatchesHistoryFragment.MatchViewHolder {
            return MatchesHistoryFragment.MatchViewHolder(parent.inflate(R.layout.itemlist_match_history))
        }

        override fun onBindViewHolder(holder: MatchesHistoryFragment.MatchViewHolder?, position: Int) {
            holder?.bind(items[position], { notifyItemRemoved(position) })
        }

        override fun getItemCount(): Int = items.size

        override fun onCreateHeaderViewHolder(parent: ViewGroup): MatchesHistoryFragment.MatchViewHolder {
            return MatchesHistoryFragment.MatchViewHolder(parent.inflate(R.layout.itemlist_match_history_section))
        }

        override fun onBindHeaderViewHolder(holder: MatchesHistoryFragment.MatchViewHolder?, position: Int) {
            holder?.bindSection(LocalDateTime.parse(items[position].uuid).toLocalDate())
        }

        override fun getHeaderId(position: Int): Long {
            val date = LocalDateTime.parse(items[position].uuid).toLocalDate()
            return date.year + date.monthValue + date.dayOfMonth.toLong()
        }

        fun getContentCount(): Int = items.size
        fun more() {
            Timber.d("More")
        }
        fun reset() {
            Timber.d("Reset")
            notifyDataSetChanged()
        }

    }


}