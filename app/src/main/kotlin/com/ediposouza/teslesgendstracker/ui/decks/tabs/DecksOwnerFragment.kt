package com.ediposouza.teslesgendstracker.ui.decks.tabs

import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.*
import android.widget.Switch
import com.ediposouza.teslesgendstracker.R
import com.ediposouza.teslesgendstracker.data.Class
import com.ediposouza.teslesgendstracker.data.Deck
import com.ediposouza.teslesgendstracker.interactor.FirebaseParsers
import com.ediposouza.teslesgendstracker.interactor.PrivateInteractor
import com.ediposouza.teslesgendstracker.ui.base.BaseFirebaseRVAdapter
import com.ediposouza.teslesgendstracker.util.inflate
import kotlinx.android.synthetic.main.fragment_decks_list.*
import timber.log.Timber

/**
 * Created by EdipoSouza on 11/18/16.
 */
class DecksOwnerFragment : DecksPublicFragment() {

    override val isDeckOwned: Boolean = true

    private val privateInteractor = PrivateInteractor()
    private var onlyPrivate: Switch? = null

    val ownerDecksAdapter = object : BaseFirebaseRVAdapter<FirebaseParsers.DeckParser, DecksAllViewHolder>(
            R.layout.itemlist_deck, FirebaseParsers.DeckParser::class.java, DecksAllViewHolder::class.java,
            privateInteractor.getOwnedPrivateDecksRef(), DECK_PAGE_SIZE) {
        override fun onCreateDefaultViewHolder(parent: ViewGroup): DecksAllViewHolder {
            return DecksAllViewHolder(parent.inflate(R.layout.itemlist_deck), itemClick, itemLongClick)
        }

        override fun onBindContentHolder(model: FirebaseParsers.DeckParser, viewHolder: DecksAllViewHolder) {
            viewHolder.bind(model.toDeck("", true), privateInteractor)
        }

        override fun onSyncEnd() {
            decks_refresh_layout.isRefreshing = false
        }

    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return container?.inflate(R.layout.fragment_decks_list_owner)
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        decks_recycler_view.adapter = ownerDecksAdapter
        decks_recycler_view.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView?, dx: Int, dy: Int) {
                if (dy < 0) {
                    return
                }
                val layoutManager = recyclerView?.layoutManager as LinearLayoutManager
                if (layoutManager.findLastVisibleItemPosition() >= ownerDecksAdapter.getContentCount() - 3) {
                    ownerDecksAdapter.more()
                }
            }
        })
        decks_refresh_layout.setOnRefreshListener { ownerDecksAdapter.reset() }
        setHasOptionsMenu(true)
        configLoggedViews()
    }

    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
        menu?.clear()
        inflater?.inflate(R.menu.menu_decks_owned, menu)
        onlyPrivate = menu?.findItem(R.id.menu_only_private)?.actionView as Switch
        onlyPrivate?.setOnCheckedChangeListener { button, checked -> showDecks() }
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun getDecks(cls: Class?, last: Boolean) {
        privateInteractor.getOwnedDecks(cls, {
            val decksToShow = if (onlyPrivate?.isChecked ?: false) it.filter(Deck::private) else it
            decksToShow.forEach { Timber.d("Decks: %s", it.toString()) }
            decksAdapter.showDecks(decksToShow, last)
        })
    }

}