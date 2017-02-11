package com.ediposouza.teslesgendstracker.ui.decks.tabs

import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.ediposouza.teslesgendstracker.R
import com.ediposouza.teslesgendstracker.interactor.FirebaseParsers
import com.ediposouza.teslesgendstracker.interactor.PrivateInteractor
import com.ediposouza.teslesgendstracker.ui.base.BaseAdsFirebaseAdapter
import com.ediposouza.teslesgendstracker.util.inflate
import kotlinx.android.synthetic.main.fragment_decks_list.*

/**
 * Created by EdipoSouza on 11/18/16.
 */
class DecksFavoritedFragment : DecksPublicFragment() {

    override val dataRef = {
        PrivateInteractor.getUserFavoriteDecksRef()
    }

    private val dataFilter: (FirebaseParsers.DeckFavoriteParser) -> Boolean = {
        currentClasses.map { it.ordinal }.contains(it.cls) &&
                it.name.toLowerCase().trim().contains(searchFilter ?: "")
    }

    override val decksAdapter: BaseAdsFirebaseAdapter<FirebaseParsers.DeckFavoriteParser, DecksAllViewHolder> by lazy {
        object : BaseAdsFirebaseAdapter<FirebaseParsers.DeckFavoriteParser, DecksAllViewHolder>(
                FirebaseParsers.DeckFavoriteParser::class.java, dataRef, DECK_PAGE_SIZE, ADS_EACH_ITEMS,
                R.layout.itemlist_deck_ads, false, dataFilter) {

            override fun onCreateDefaultViewHolder(parent: ViewGroup): DecksAllViewHolder {
                return DecksAllViewHolder(parent.inflate(R.layout.itemlist_deck), itemClick, itemLongClick)
            }

            override fun onBindContentHolder(itemKey: String, model: FirebaseParsers.DeckFavoriteParser, viewHolder: DecksAllViewHolder) {
                if (!TextUtils.isEmpty(itemKey)) {
                    viewHolder.bind(itemKey)
                }
            }

            override fun onSyncEnd() {
                decks_refresh_layout?.isRefreshing = false
            }

        }
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return container?.inflate(R.layout.fragment_decks_list)
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        configLoggedViews()
    }

}