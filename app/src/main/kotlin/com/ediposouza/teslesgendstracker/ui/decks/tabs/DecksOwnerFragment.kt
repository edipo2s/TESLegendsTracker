package com.ediposouza.teslesgendstracker.ui.decks.tabs

import android.os.Bundle
import android.view.*
import android.widget.Switch
import com.ediposouza.teslesgendstracker.R
import com.ediposouza.teslesgendstracker.data.Deck
import com.ediposouza.teslesgendstracker.inflate
import com.ediposouza.teslesgendstracker.interactor.PrivateInteractor
import timber.log.Timber

/**
 * Created by EdipoSouza on 11/18/16.
 */
class DecksOwnerFragment : DecksPublicFragment() {

    private val privateInteractor = PrivateInteractor()
    private var onlyPrivate: Switch? = null

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return container?.inflate(R.layout.fragment_decks_list_owner)
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
        menu?.clear()
        inflater?.inflate(R.menu.menu_decks_owner, menu)
        onlyPrivate = menu?.findItem(R.id.menu_only_private)?.actionView as Switch
        onlyPrivate?.setOnCheckedChangeListener { button, checked -> getDecks() }
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun getDecks() {
        privateInteractor.getOwnedDecks(null, {
            val decksToShow = if (onlyPrivate?.isChecked ?: false) it.filter(Deck::private) else it
            decksToShow.forEach { Timber.d("Decks: %s", it.toString()) }
            decksAdapter.showDecks(decksToShow)
        })
    }

}