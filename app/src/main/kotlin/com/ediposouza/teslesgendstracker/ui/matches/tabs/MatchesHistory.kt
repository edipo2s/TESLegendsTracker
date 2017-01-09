package com.ediposouza.teslesgendstracker.ui.matches.tabs

import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.*
import com.ediposouza.teslesgendstracker.R
import com.ediposouza.teslesgendstracker.data.Match
import com.ediposouza.teslesgendstracker.data.MatchMode
import com.ediposouza.teslesgendstracker.data.Season
import com.ediposouza.teslesgendstracker.interactor.FirebaseParsers
import com.ediposouza.teslesgendstracker.interactor.PrivateInteractor
import com.ediposouza.teslesgendstracker.ui.base.BaseAdsFirebaseAdapter
import com.ediposouza.teslesgendstracker.ui.base.BaseFragment
import com.ediposouza.teslesgendstracker.ui.matches.CmdFilterMode
import com.ediposouza.teslesgendstracker.ui.matches.CmdFilterSeason
import com.ediposouza.teslesgendstracker.ui.util.firebase.OnLinearLayoutItemScrolled
import com.ediposouza.teslesgendstracker.util.inflate
import jp.wasabeef.recyclerview.animators.SlideInLeftAnimator
import kotlinx.android.synthetic.main.fragment_matches_history.*
import kotlinx.android.synthetic.main.itemlist_match_history.view.*
import org.greenrobot.eventbus.Subscribe

/**
 * Created by EdipoSouza on 1/3/17.
 */
class MatchesHistory : BaseFragment() {

    val ADS_EACH_ITEMS = 20 //after 10 lines
    val MATCH_PAGE_SIZE = 15

    private var currentMatchMode = MatchMode.RANKED
    private var currentSeason: Season? = null

    private val dataFilter: (FirebaseParsers.MatchParser) -> Boolean = {
        it.mode == currentMatchMode.ordinal && (it.season == currentSeason?.uuid || currentSeason == null)
    }

    private val matchesAdapter: BaseAdsFirebaseAdapter<FirebaseParsers.MatchParser, MatchViewHolder> by lazy {
        object : BaseAdsFirebaseAdapter<FirebaseParsers.MatchParser, MatchViewHolder>(
                FirebaseParsers.MatchParser::class.java, { PrivateInteractor().getUserMatchesRef() },
                MATCH_PAGE_SIZE, ADS_EACH_ITEMS, R.layout.itemlist_match_history_ads, false, dataFilter) {

            override fun onCreateDefaultViewHolder(parent: ViewGroup): MatchViewHolder {
                return MatchViewHolder(parent.inflate(R.layout.itemlist_match_history))
            }

            override fun onBindContentHolder(itemKey: String, model: FirebaseParsers.MatchParser, viewHolder: MatchViewHolder) {
                viewHolder.bind(model.toMatch(itemKey))
            }

            override fun onSyncEnd() {
                matches_refresh_layout?.isRefreshing = false
            }

        }
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return container?.inflate(R.layout.fragment_matches_history)
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setHasOptionsMenu(true)
        with(matches_recycler_view) {
            adapter = matchesAdapter
            itemAnimator = SlideInLeftAnimator()
            layoutManager = object : LinearLayoutManager(context) {
                override fun supportsPredictiveItemAnimations(): Boolean = false
            }
            addOnScrollListener(OnLinearLayoutItemScrolled(matchesAdapter.getContentCount() - 3) {
                matchesAdapter.more()
            })
        }
        matches_refresh_layout.setOnRefreshListener {
            matchesAdapter.reset()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
        menu?.findItem(R.id.menu_percent)?.isVisible = false
        super.onCreateOptionsMenu(menu, inflater)
    }

    @Subscribe
    fun onFilterMode(cmdFilterMode: CmdFilterMode) {
        currentMatchMode = cmdFilterMode.mode
        matchesAdapter.reset()
        matches_recycler_view.scrollToPosition(0)
    }

    @Subscribe
    fun onFilterSeason(cmdFilterSeason: CmdFilterSeason) {
        currentSeason = cmdFilterSeason.season
        matchesAdapter.reset()
        matches_recycler_view.scrollToPosition(0)
    }

    class MatchViewHolder(view: View) : RecyclerView.ViewHolder(view) {

        fun bind(match: Match) {
            with(itemView) {
                match_history_first.visibility = if (match.first) View.VISIBLE else View.INVISIBLE
                match_history_player_class_attr1.setImageResource(match.player.cls.attr1.imageRes)
                match_history_player_class_attr2.setImageResource(match.player.cls.attr2.imageRes)
                match_history_opponent_class_attr1.setImageResource(match.opponent.cls.attr1.imageRes)
                match_history_opponent_class_attr2.setImageResource(match.opponent.cls.attr2.imageRes)
                val resultColor = if (match.win) R.color.green_200 else R.color.red_100
                val resultText = if (match.win) R.string.match_win else R.string.match_loss
                match_history_result.setTextColor(ContextCompat.getColor(context, resultColor))
                match_history_result.text = context.getString(resultText)
                match_history_legend.visibility = if (match.legend) View.VISIBLE else View.INVISIBLE
                val rankText = if (match.legend) R.string.match_rank_legend else R.string.match_rank_normal
                match_history_rank.text = context.getString(rankText, match.rank)
                match_history_rank.visibility = if (match.mode == MatchMode.RANKED) View.VISIBLE else View.INVISIBLE
            }
        }

    }

}