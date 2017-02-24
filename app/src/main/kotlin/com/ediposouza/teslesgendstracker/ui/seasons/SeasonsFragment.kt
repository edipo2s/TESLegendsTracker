package com.ediposouza.teslesgendstracker.ui.seasons

import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.support.v4.app.ActivityOptionsCompat
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.ediposouza.teslesgendstracker.R
import com.ediposouza.teslesgendstracker.SEASON_UUID_PATTERN
import com.ediposouza.teslesgendstracker.data.*
import com.ediposouza.teslesgendstracker.interactor.FirebaseParsers
import com.ediposouza.teslesgendstracker.interactor.PrivateInteractor
import com.ediposouza.teslesgendstracker.interactor.PublicInteractor
import com.ediposouza.teslesgendstracker.ui.base.BaseAdsFirebaseAdapter
import com.ediposouza.teslesgendstracker.ui.base.BaseFragment
import com.ediposouza.teslesgendstracker.ui.base.CmdUpdateTitle
import com.ediposouza.teslesgendstracker.ui.cards.CardActivity
import com.ediposouza.teslesgendstracker.util.MetricScreen
import com.ediposouza.teslesgendstracker.util.MetricsManager
import com.ediposouza.teslesgendstracker.util.inflate
import com.ediposouza.teslesgendstracker.util.toYearMonth
import jp.wasabeef.recyclerview.animators.SlideInRightAnimator
import kotlinx.android.synthetic.main.fragment_seasons.*
import kotlinx.android.synthetic.main.itemlist_season.view.*
import kotlinx.android.synthetic.main.itemlist_season_patch.view.*
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.runOnUiThread
import org.threeten.bp.LocalDate
import org.threeten.bp.format.DateTimeFormatter
import org.threeten.bp.format.TextStyle
import java.util.*

/**
 * Created by EdipoSouza on 1/21/17.
 */
class SeasonsFragment : BaseFragment() {

    private val ADS_EACH_ITEMS = 5
    private val SEASON_PAGE_SIZE = 8

    private val patches = mutableListOf<Patch>()
    private val seasonRef = { PublicInteractor.getSeasonsRef() }
    private val transitionName: String by lazy { getString(R.string.card_transition_name) }
    private val patchTransitionName: String by lazy { getString(R.string.patch_transition_container) }

    val onCardClick: (View, Card) -> Unit = { view, card -> showCardExpanded(card, view) }

    val onPatchClick: (Patch, View) -> Unit = { patch, view ->
        val intent = PatchActivity.newIntent(context, patch, patches)
        ActivityCompat.startActivity(context, intent, ActivityOptionsCompat
                .makeSceneTransitionAnimation(activity, view, patchTransitionName).toBundle())
    }

    private val seasonsAdapter by lazy {
        object : BaseAdsFirebaseAdapter<FirebaseParsers.SeasonParser, SeasonViewHolder>(
                FirebaseParsers.SeasonParser::class.java, seasonRef, SEASON_PAGE_SIZE,
                ADS_EACH_ITEMS, R.layout.itemlist_season_ads) {

            override fun onCreateDefaultViewHolder(parent: ViewGroup): SeasonViewHolder {
                return SeasonViewHolder(parent.inflate(R.layout.itemlist_season), onCardClick, onPatchClick)
            }

            override fun onBindContentHolder(itemKey: String, model: FirebaseParsers.SeasonParser, viewHolder: SeasonViewHolder) {
                viewHolder.bind(model.toSeason(itemKey), patches)
            }

            override fun onSyncEnd() {
                seasons_refresh_layout.isRefreshing = false
            }

        }
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return container?.inflate(R.layout.fragment_seasons)
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        eventBus.post(CmdUpdateTitle(R.string.menu_seasons))
        PublicInteractor.getPatches {
            patches.addAll(it.filter { it.type != PatchType.REWARD })
            configureRecycleView()
        }
        MetricsManager.trackScreen(MetricScreen.SCREEN_SEASONS())
    }

    private fun configureRecycleView() {
        seasons_recycler_view?.apply {
            layoutManager = object : LinearLayoutManager(context) {
                override fun supportsPredictiveItemAnimations(): Boolean = false
            }
            adapter = seasonsAdapter
            itemAnimator = SlideInRightAnimator()
            setHasFixedSize(true)
        }
    }

    fun showCardExpanded(card: Card, view: View) {
        ActivityCompat.startActivity(activity, CardActivity.newIntent(context, card),
                ActivityOptionsCompat.makeSceneTransitionAnimation(activity, view, transitionName).toBundle())
    }

    class SeasonViewHolder(view: View, val itemClick: (View, Card) -> Unit,
                           val onPatchClick: (Patch, View) -> Unit) : RecyclerView.ViewHolder(view) {

        init {
            itemView.season_patches_recycler_view.layoutManager = LinearLayoutManager(itemView.context,
                    LinearLayoutManager.HORIZONTAL, true)
        }

        fun bind(season: Season, patches: List<Patch>) {
            with(itemView) {
                season_number.text = when (season.id) {
                    1 -> "1st"
                    2 -> "2nd"
                    3 -> "3rd"
                    else -> "${season.id}th"
                }
                season_month.text = season.date.month.getDisplayName(TextStyle.FULL, Locale.getDefault())
                season_year.text = season.date.year.toString()
                season_card_reward.setImageBitmap(Card.getDefaultCardImage(context))
                season_card_reward.setOnClickListener { }
                val seasonPatches = patches.filter { it.date.toYearMonth() == season.date }
                season_patches_recycler_view.adapter = PatchAdapter(seasonPatches, onPatchClick)
                doAsync {
                    if (season.rewardCardShortname != null) {
                        val rewardAttr = CardAttribute.valueOf(season.rewardCardAttr.toUpperCase())
                        PublicInteractor.getCard(CardSet.CORE, rewardAttr, season.rewardCardShortname) { card ->
                            context.runOnUiThread {
                                season_card_reward.setImageBitmap(card.imageBitmap(context))
                                season_card_reward.setOnClickListener { itemClick(season_card_reward, card) }
                            }
                        }
                    }
                    val onError: (Exception?) -> Unit = { updateMatchesInfo(listOf(), true) }
                    PrivateInteractor.getUserMatches(season, MatchMode.RANKED, onError) { rankedMatches ->
                        val actualSeasonUuid = LocalDate.now().format(DateTimeFormatter.ofPattern(SEASON_UUID_PATTERN))
                        val noMatches = rankedMatches.isEmpty() && season.uuid != actualSeasonUuid
                        updateMatchesInfo(rankedMatches, noMatches)
                    }
                }
            }
        }

        private fun updateMatchesInfo(rankedMatches: List<Match>, noMatches: Boolean) {
            with(itemView) {
                season_no_matches.visibility = View.VISIBLE.takeIf { noMatches } ?: View.GONE
                season_no_matches_shadow.visibility = View.VISIBLE.takeIf { noMatches } ?: View.GONE
                season_matches_label.visibility = View.GONE.takeIf { noMatches } ?: View.VISIBLE
                season_matches_layout.visibility = View.GONE.takeIf { noMatches } ?: View.VISIBLE
                season_best_rank_label.visibility = View.GONE.takeIf { noMatches } ?: View.VISIBLE
                with(season_best_rank) {
                    val rankedGroup = rankedMatches.groupBy { it.legend }
                    text = "${(rankedGroup[true] ?: rankedGroup[false])?.minBy { it.rank }?.rank ?: 0}"
                    val legendIcon = R.drawable.ic_rank_legend.takeIf { rankedGroup[true] != null } ?: 0
                    setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, legendIcon)
                    visibility = View.GONE.takeIf { noMatches } ?: View.VISIBLE
                }
                with(season_matches_wins) {
                    text = rankedMatches.filter { it.win }.size.toString()
                    visibility = View.GONE.takeIf { noMatches } ?: View.VISIBLE
                }
                with(season_matches_losses) {
                    text = rankedMatches.filter { !it.win }.size.toString()
                    visibility = View.GONE.takeIf { noMatches } ?: View.VISIBLE
                }
                with(season_matches_total) {
                    text = rankedMatches.size.toString()
                    visibility = View.GONE.takeIf { noMatches } ?: View.VISIBLE
                }
                season_opponent_label.visibility = View.GONE.takeIf { noMatches } ?: View.VISIBLE
                season_opponent_layout.visibility = View.GONE.takeIf { noMatches } ?: View.VISIBLE
                with(season_matches_most_seen) {
                    setClass(rankedMatches.groupBy { it.opponent.cls }.maxBy { it.value.size }?.key)
                    visibility = View.GONE.takeIf { noMatches } ?: View.VISIBLE
                }
                with(season_matches_most_defeated) {
                    setClass(rankedMatches.filter { it.win }.groupBy { it.opponent.cls }.maxBy { it.value.size }?.key)
                    visibility = View.GONE.takeIf { noMatches } ?: View.VISIBLE
                }
                with(season_matches_less_defeated) {
                    setClass(rankedMatches.filter { !it.win }.groupBy { it.opponent.cls }.maxBy { it.value.size }?.key)
                    visibility = View.GONE.takeIf { noMatches } ?: View.VISIBLE
                }
            }
        }

    }

    class PatchAdapter(val items: List<Patch>, val onPatchClick: (Patch, View) -> Unit) : RecyclerView.Adapter<PatchViewHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): PatchViewHolder {
            return PatchViewHolder(parent?.inflate(R.layout.itemlist_season_patch))
        }

        override fun onBindViewHolder(holder: PatchViewHolder?, position: Int) {
            holder?.bind(items[position], onPatchClick)
        }

        override fun getItemCount() = items.size

    }

    class PatchViewHolder(view: View?) : RecyclerView.ViewHolder(view) {

        fun bind(patch: Patch, onPatchClick: (Patch, View) -> Unit) {
            with(itemView) {
                patch_name.text = patch.desc
                setOnClickListener { onPatchClick.invoke(patch, patch_container) }
            }
        }

    }

}