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
import com.ediposouza.teslesgendstracker.data.Attribute
import com.ediposouza.teslesgendstracker.data.Card
import com.ediposouza.teslesgendstracker.data.CardSet
import com.ediposouza.teslesgendstracker.data.Season
import com.ediposouza.teslesgendstracker.interactor.FirebaseParsers
import com.ediposouza.teslesgendstracker.interactor.PrivateInteractor
import com.ediposouza.teslesgendstracker.interactor.PublicInteractor
import com.ediposouza.teslesgendstracker.ui.base.BaseAdsFirebaseAdapter
import com.ediposouza.teslesgendstracker.ui.base.BaseFragment
import com.ediposouza.teslesgendstracker.ui.cards.CardActivity
import com.ediposouza.teslesgendstracker.util.inflate
import jp.wasabeef.recyclerview.animators.SlideInRightAnimator
import kotlinx.android.synthetic.main.fragment_seasons.*
import kotlinx.android.synthetic.main.itemlist_season.view.*

/**
 * Created by EdipoSouza on 1/21/17.
 */
class SeasonsFragment : BaseFragment() {

    private val ADS_EACH_ITEMS = 5
    private val SEASON_PAGE_SIZE = 8

    private val publicInteractor by lazy { PublicInteractor() }
    private val privateInteractor by lazy { PrivateInteractor() }
    private val seasonRef = { publicInteractor.getSeasonsRef() }
    private val transitionName: String by lazy { getString(R.string.card_transition_name) }

    private val seasonsAdapter by lazy {
        object : BaseAdsFirebaseAdapter<FirebaseParsers.SeasonParser, SeasonViewHolder>(
                FirebaseParsers.SeasonParser::class.java, seasonRef, SEASON_PAGE_SIZE,
                ADS_EACH_ITEMS, R.layout.itemlist_season_ads, filter = { true }) {

            override fun onCreateDefaultViewHolder(parent: ViewGroup): SeasonViewHolder {
                return SeasonViewHolder(parent.inflate(R.layout.itemlist_season)) {
                    view, card ->
                    showCardExpanded(card, view)
                }
            }

            override fun onBindContentHolder(itemKey: String, model: FirebaseParsers.SeasonParser, viewHolder: SeasonViewHolder) {
                viewHolder.bind(model.toSeason(itemKey), publicInteractor)
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
        with(seasons_recycler_view) {
            layoutManager = object : LinearLayoutManager(context) {
                override fun supportsPredictiveItemAnimations(): Boolean = false
            }
            adapter = seasonsAdapter
            itemAnimator = SlideInRightAnimator()
            setHasFixedSize(true)
        }
    }

    fun showCardExpanded(card: Card, view: View) {
        privateInteractor.isUserCardFavorite(card) { isFavorite ->
            ActivityCompat.startActivity(activity, CardActivity.newIntent(context, card, isFavorite),
                    ActivityOptionsCompat.makeSceneTransitionAnimation(activity, view, transitionName).toBundle())
        }
    }

    class SeasonViewHolder(view: View, val itemClick: (View, Card) -> Unit) : RecyclerView.ViewHolder(view) {

        fun bind(season: Season, publicInteractor: PublicInteractor) {
            with(itemView) {
                season_month.text = season.month
                season_year.text = season.year
                if (season.rewardCardShortname != null) {
                    val rewardAttr = Attribute.valueOf(season.rewardCardAttr.toUpperCase())
                    publicInteractor.getCard(CardSet.REWARD, rewardAttr, season.rewardCardShortname) { card ->
                        season_card_reward.setImageBitmap(card.imageBitmap(context))
                        season_card_reward.setOnClickListener { itemClick(season_card_reward, card) }
                    }
                }
            }
        }

    }

}