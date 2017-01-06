package com.ediposouza.teslesgendstracker.ui.decks.tabs

import android.os.Bundle
import android.view.*
import android.widget.Switch
import com.ediposouza.teslesgendstracker.R
import com.ediposouza.teslesgendstracker.data.Class
import com.ediposouza.teslesgendstracker.interactor.FirebaseParsers
import com.ediposouza.teslesgendstracker.interactor.PrivateInteractor
import com.ediposouza.teslesgendstracker.ui.base.BaseAdsFirebaseAdapter
import com.ediposouza.teslesgendstracker.ui.util.firebase.FirebaseRVAdapter
import com.ediposouza.teslesgendstracker.ui.util.firebase.OnLinearLayoutItemScrolled
import com.ediposouza.teslesgendstracker.util.inflate
import kotlinx.android.synthetic.main.fragment_decks_list.*

/**
 * Created by EdipoSouza on 11/18/16.
 */
class DecksOwnerFragment : DecksPublicFragment() {

    override val isDeckOwned: Boolean = true

    private val privateInteractor = PrivateInteractor()
    private var onlyPrivate: Switch? = null

    val ownerPrivateDecksAdapter = object : BaseAdsFirebaseAdapter<FirebaseParsers.DeckParser, DecksAllViewHolder>(
            ADS_EACH_ITEMS, R.layout.itemlist_deck_ads, FirebaseParsers.DeckParser::class.java,
            privateInteractor.getOwnedPrivateDecksRef(), DECK_PAGE_SIZE) {

        override fun onCreateDefaultViewHolder(parent: ViewGroup): DecksAllViewHolder {
            return DecksAllViewHolder(parent.inflate(R.layout.itemlist_deck), itemClick, itemLongClick)
        }

        override fun onBindContentHolder(itemKey: String, model: FirebaseParsers.DeckParser, viewHolder: DecksAllViewHolder) {
            viewHolder.bind(model.toDeck(itemKey, true), privateInteractor)
        }

        override fun onSyncEnd() {
            decks_refresh_layout?.isRefreshing = false
        }

    }

    val ownerPublicDecksAdapter = object : BaseAdsFirebaseAdapter<FirebaseParsers.DeckParser, DecksAllViewHolder>(
            ADS_EACH_ITEMS, R.layout.itemlist_deck_ads, FirebaseParsers.DeckParser::class.java,
            privateInteractor.getOwnedPublicDecksRef(), DECK_PAGE_SIZE) {

        override fun onCreateDefaultViewHolder(parent: ViewGroup): DecksAllViewHolder {
            return DecksAllViewHolder(parent.inflate(R.layout.itemlist_deck), itemClick, itemLongClick)
        }

        override fun onBindContentHolder(itemKey: String, model: FirebaseParsers.DeckParser, viewHolder: DecksAllViewHolder) {
            viewHolder.bind(model.toDeck(itemKey, false), privateInteractor)
        }

        override fun onSyncEnd() {
            decks_refresh_layout?.isRefreshing = false
        }

    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return container?.inflate(R.layout.fragment_decks_list_owner)
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        decks_recycler_view.adapter = ownerPrivateDecksAdapter
        with(decks_recycler_view.adapter as FirebaseRVAdapter<*, *>) {
            decks_recycler_view.addOnScrollListener(OnLinearLayoutItemScrolled(getContentCount() - 3) {
                more()
            })
            decks_refresh_layout.setOnRefreshListener {
                reset()
            }
        }
        setHasOptionsMenu(true)
        configLoggedViews()
    }

    override fun showDecks() {
        (decks_recycler_view.adapter as FirebaseRVAdapter<*, *>).reset()
    }

    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
        menu?.clear()
        inflater?.inflate(R.menu.menu_decks_owned, menu)
        onlyPrivate = menu?.findItem(R.id.menu_only_private)?.actionView as Switch
        onlyPrivate?.setOnCheckedChangeListener { button, checked ->
            decks_recycler_view.adapter = if (checked) ownerPublicDecksAdapter else ownerPrivateDecksAdapter
        }
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun getDecks(cls: Class?, last: Boolean) {
//        privateInteractor.getOwnedDecks(cls, {
//            val decksToShow = if (onlyPrivate?.isChecked ?: false) it.filter(Deck::private) else it
//            decksToShow.forEach { Timber.d("Decks: %s", it.toString()) }
//            decksAdapter.showDecks(decksToShow, last)
//        })
    }

}