package com.ediposouza.teslesgendstracker.ui.matches.tabs

import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.*
import com.ediposouza.teslesgendstracker.R
import com.ediposouza.teslesgendstracker.data.DeckClass
import com.ediposouza.teslesgendstracker.data.Match
import com.ediposouza.teslesgendstracker.data.MatchMode
import com.ediposouza.teslesgendstracker.data.Season
import com.ediposouza.teslesgendstracker.interactor.BaseInteractor
import com.ediposouza.teslesgendstracker.interactor.FirebaseParsers
import com.ediposouza.teslesgendstracker.interactor.PrivateInteractor
import com.ediposouza.teslesgendstracker.ui.base.BaseAdsFirebaseAdapter
import com.ediposouza.teslesgendstracker.ui.base.BaseFragment
import com.ediposouza.teslesgendstracker.ui.matches.CmdFilterMode
import com.ediposouza.teslesgendstracker.ui.matches.CmdFilterSeason
import com.ediposouza.teslesgendstracker.ui.matches.CmdUpdateMatches
import com.ediposouza.teslesgendstracker.ui.util.firebase.OnLinearLayoutItemScrolled
import com.ediposouza.teslesgendstracker.util.alertThemed
import com.ediposouza.teslesgendstracker.util.inflate
import com.timehop.stickyheadersrecyclerview.StickyRecyclerHeadersAdapter
import com.timehop.stickyheadersrecyclerview.StickyRecyclerHeadersDecoration
import jp.wasabeef.recyclerview.animators.SlideInLeftAnimator
import kotlinx.android.synthetic.main.fragment_matches_history.*
import kotlinx.android.synthetic.main.itemlist_match_history.view.*
import kotlinx.android.synthetic.main.itemlist_match_history_section.view.*
import org.greenrobot.eventbus.Subscribe
import org.threeten.bp.LocalDateTime
import org.threeten.bp.format.DateTimeFormatter
import org.threeten.bp.format.FormatStyle

/**
 * Created by EdipoSouza on 1/3/17.
 */
open class MatchesHistoryFragment : BaseFragment() {

    val ADS_EACH_ITEMS = 20 //after 10 lines
    val MATCH_PAGE_SIZE = 15

    protected var currentMatchMode = MatchMode.RANKED

    protected var currentSeason: Season? = null

    private val dataRef = {
        PrivateInteractor.getUserMatchesRef()?.
                orderByChild(BaseInteractor.NODE_MATCHES_MODE)?.equalTo(currentMatchMode.ordinal.toDouble())
    }
    private val dataFilter: (FirebaseParsers.MatchParser) -> Boolean = {
        it.season == currentSeason?.uuid || currentSeason == null
    }

    protected val matchesAdapter: BaseAdsFirebaseAdapter<FirebaseParsers.MatchParser, MatchViewHolder> by lazy {
        object : BaseAdsFirebaseAdapter<FirebaseParsers.MatchParser, MatchViewHolder>(
                FirebaseParsers.MatchParser::class.java, dataRef,
                MATCH_PAGE_SIZE, ADS_EACH_ITEMS, R.layout.itemlist_match_history_ads, false, dataFilter),
                StickyRecyclerHeadersAdapter<MatchViewHolder> {

            override fun onCreateDefaultViewHolder(parent: ViewGroup): MatchViewHolder {
                return MatchViewHolder(parent.inflate(R.layout.itemlist_match_history))
            }

            override fun onBindContentHolder(itemKey: String, model: FirebaseParsers.MatchParser, viewHolder: MatchViewHolder) {
                viewHolder.bind(model.toMatch(itemKey), {
                    reset()
                    matches_recycler_view?.scrollToPosition(0)
                })
            }

            override fun onSyncEnd() {
                matches_refresh_layout?.isRefreshing = false
            }

            override fun onCreateHeaderViewHolder(parent: ViewGroup): MatchViewHolder {
                return MatchViewHolder(parent.inflate(R.layout.itemlist_match_history_section))
            }

            override fun onBindHeaderViewHolder(holder: MatchViewHolder?, position: Int) {
                if (position <= getContentCount()) {
                    val header = LocalDateTime.parse(getItemKey(position)).toLocalDate()
                            .format(DateTimeFormatter.ofLocalizedDate(FormatStyle.FULL))
                    holder?.bindSection(header.takeIf { currentMatchMode != MatchMode.ARENA } ?:
                            "${getPlayerClassName(position).name.toLowerCase().capitalize()} - $header")
                }
            }

            override fun getHeaderId(position: Int): Long {
                if (getItemViewType(position) != VIEW_TYPE_CONTENT || position > getContentCount()) {
                    return -1
                }
                val date = LocalDateTime.parse(getItemKey(position)).toLocalDate()
                val headerValue = date.year + date.monthValue + date.dayOfMonth.toLong()
                return headerValue.takeIf { currentMatchMode != MatchMode.ARENA } ?:
                        headerValue + getPlayerClassName(position).ordinal
            }

            fun getPlayerClassName(position: Int): DeckClass {
                val clsPos = getItem(position).player.get(FirebaseParsers.MatchParser.KEY_MATCH_DECK_CLASS)
                return DeckClass.values()[clsPos.toString().toInt()]
            }

        }.apply {
            registerAdapterDataObserver(object : RecyclerView.AdapterDataObserver() {
                override fun onChanged() {
                    sectionDecoration.invalidateHeaders()
                }
            })
        }
    }

    private val sectionDecoration by lazy { StickyRecyclerHeadersDecoration(matchesAdapter as StickyRecyclerHeadersAdapter<*>) }

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
            addItemDecoration(sectionDecoration)
            addOnScrollListener(OnLinearLayoutItemScrolled(matchesAdapter.getContentCount() - 3) {
                view?.post { matchesAdapter.more() }
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
    @Suppress("unused")
    fun onCmdFilterMode(cmdFilterMode: CmdFilterMode) {
        currentMatchMode = cmdFilterMode.mode
        if (isFragmentSelected) {
            updateMatchList()
        }
    }

    @Subscribe
    @Suppress("unused")
    fun onCmdFilterSeason(cmdFilterSeason: CmdFilterSeason) {
        currentSeason = cmdFilterSeason.season
        if (isFragmentSelected) {
            updateMatchList()
        }
    }

    @Subscribe
    @Suppress("unused", "UNUSED_PARAMETER")
    fun onCmdUpdateMatches(cmdUpdateMatches: CmdUpdateMatches) {
        if (isFragmentSelected) {
            matchesAdapter.reset()
        }
    }

    protected fun updateMatchList() {
        matchesAdapter.reset()
        matches_recycler_view.scrollToPosition(0)
    }

    class MatchViewHolder(view: View) : RecyclerView.ViewHolder(view) {

        fun bind(match: Match, onDelete: () -> Unit) {
            with(itemView) {
                match_history_first.visibility = View.VISIBLE.takeIf { match.first } ?: View.INVISIBLE
                match_history_player_class_attr1.setImageResource(match.player.cls.attr1.imageRes)
                match_history_player_class_attr2.setImageResource(match.player.cls.attr2.imageRes)
                match_history_opponent_class_attr1.setImageResource(match.opponent.cls.attr1.imageRes)
                match_history_opponent_class_attr2.setImageResource(match.opponent.cls.attr2.imageRes)
                val resultColor = R.color.green_200.takeIf { match.win } ?: R.color.red_100
                val resultText = R.string.match_win.takeIf { match.win } ?: R.string.match_loss
                match_history_result.setTextColor(ContextCompat.getColor(context, resultColor))
                match_history_result.text = context.getString(resultText)
                match_history_legend.visibility = View.VISIBLE.takeIf { match.legend } ?: View.INVISIBLE
                val rankText = R.string.match_rank_legend.takeIf { match.legend } ?: R.string.match_rank_normal
                match_history_rank.text = context.getString(rankText, match.rank)
                match_history_rank.visibility = View.VISIBLE.takeIf { match.mode == MatchMode.RANKED } ?: View.INVISIBLE
                match_history_delete.setOnClickListener {
                    onHistoryClick(itemView, match, onDelete)
                }
            }
        }

        private fun onHistoryClick(view: View, match: Match, onDelete: () -> Unit) {
            val opponentClass = match.opponent.cls.name.toLowerCase().capitalize()
            val title = view.context.getString(R.string.match_history_delete, opponentClass)
            view.context.alertThemed(title, view.context.getString(R.string.confirm_message), R.style.AppDialog) {
                positiveButton(android.R.string.yes, {
                    PrivateInteractor.deleteMatch(match) {
                        onDelete.invoke()
                    }
                })
                negativeButton(android.R.string.no, { })
            }.show()
        }

        fun bindSection(header: String) {
            itemView.match_history_date.text = header
        }

    }

}