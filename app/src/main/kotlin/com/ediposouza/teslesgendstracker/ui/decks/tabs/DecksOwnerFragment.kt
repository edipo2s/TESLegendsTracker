package com.ediposouza.teslesgendstracker.ui.decks.tabs

import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.*
import android.widget.LinearLayout
import android.widget.Switch
import com.ediposouza.teslesgendstracker.R
import com.ediposouza.teslesgendstracker.data.Class
import com.ediposouza.teslesgendstracker.data.Deck
import com.ediposouza.teslesgendstracker.interactor.FirebaseParsers
import com.ediposouza.teslesgendstracker.interactor.PrivateInteractor
import com.ediposouza.teslesgendstracker.ui.util.firebase.FirebaseRecyclerViewAdapter
import com.ediposouza.teslesgendstracker.util.inflate
import com.google.firebase.database.DatabaseError
import kotlinx.android.synthetic.main.fragment_decks_list.*
import kotlinx.android.synthetic.main.itemlist_loading.view.*
import timber.log.Timber

/**
 * Created by EdipoSouza on 11/18/16.
 */
class DecksOwnerFragment : DecksPublicFragment() {

    override val isDeckOwned: Boolean = true

    private val privateInteractor = PrivateInteractor()
    private var onlyPrivate: Switch? = null

    val firebaseDecksAdapter = OwnerFirebaseAdapter(itemClick, itemLongClick, privateInteractor, DECK_PAGE_SIZE) {
        decks_refresh_layout.isRefreshing = false
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return container?.inflate(R.layout.fragment_decks_list_owner)
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        decks_recycler_view.adapter = firebaseDecksAdapter
        decks_recycler_view.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView?, dx: Int, dy: Int) {
                if (dy < 0) {
                    return
                }
                val layoutManager = recyclerView?.layoutManager as LinearLayoutManager
                if (layoutManager.findLastVisibleItemPosition() >= firebaseDecksAdapter.getContentCount() - 3) {
                    firebaseDecksAdapter.more()
                }
            }
        })
        decks_refresh_layout.setOnRefreshListener { firebaseDecksAdapter.reset() }
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

    class OwnerFirebaseAdapter(val itemClick: (View, Deck) -> Unit, val itemLongClick: (View, Deck) -> Boolean,
                               val privateInteractor: PrivateInteractor, pageSize: Int,
                               val onSyncEnd: (() -> Unit)? = null) :
            FirebaseRecyclerViewAdapter<FirebaseParsers.DeckParser, DecksAllViewHolder>(
                    FirebaseParsers.DeckParser::class.java, R.layout.itemlist_deck, DecksAllViewHolder::class.java,
                    privateInteractor.getOwnedPrivateDecksRef(), pageSize, false) {

        var VIEW_TYPE_HEADER = 0
        var VIEW_TYPE_CONTENT = 1
        var VIEW_TYPE_LOADING = 2

        private var synced: Boolean = false

        override val snapShotOffset: Int = 1

        fun getContentCount(): Int = super.getItemCount()

        override fun getItemCount(): Int = super.getItemCount() + 2

        override fun getItemViewType(position: Int): Int {
            return when (position) {
                0 -> VIEW_TYPE_HEADER
                itemCount - 1 -> VIEW_TYPE_LOADING
                else -> VIEW_TYPE_CONTENT
            }
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DecksAllViewHolder {
            val view = when (viewType) {
                VIEW_TYPE_HEADER -> LinearLayout(parent.context).apply { minimumHeight = 1 }
                VIEW_TYPE_LOADING -> parent.inflate(R.layout.itemlist_loading)
                else -> parent.inflate(R.layout.itemlist_deck)
            }
            return DecksAllViewHolder(view, itemClick, itemLongClick)
        }

        override fun populateViewHolder(viewHolder: DecksAllViewHolder, model: FirebaseParsers.DeckParser?, position: Int) {
            if (model != null) {
                viewHolder.bind(model.toDeck("", true), privateInteractor)
            } else {
                Timber.d("Loading: " + synced)
                viewHolder.itemView.loadingBar?.visibility = if (synced) View.GONE else View.VISIBLE
            }
        }

        override fun onSyncStatusChanged(synced: Boolean) {
            this.synced = synced
            notifyItemChanged(itemCount - 1)
            if (synced) {
                onSyncEnd?.invoke()
            }
        }

        override fun onArrayError(firebaseError: DatabaseError) {
            Timber.d(firebaseError.toException(), firebaseError.toString())
        }

    }

}