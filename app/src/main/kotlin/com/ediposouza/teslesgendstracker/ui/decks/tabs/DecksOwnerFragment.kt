package com.ediposouza.teslesgendstracker.ui.decks.tabs

import android.os.Bundle
import android.view.*
import android.widget.CompoundButton
import com.ediposouza.teslesgendstracker.R
import com.ediposouza.teslesgendstracker.interactor.PrivateInteractor
import com.ediposouza.teslesgendstracker.util.inflate

/**
 * Created by EdipoSouza on 11/18/16.
 */
class DecksOwnerFragment : DecksPublicFragment() {

    private var onlyPrivate: CompoundButton? = null

    override val isDeckPrivate: Boolean
        get() = onlyPrivate?.isChecked ?: false

    override val dataRef = {
        if (isDeckPrivate) PrivateInteractor.getUserPrivateDecksRef() else PrivateInteractor.getUserPublicDecksRef()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return container?.inflate(R.layout.fragment_decks_list_owner)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setHasOptionsMenu(true)
        configLoggedViews()
    }

    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
        menu?.clear()
        inflater?.inflate(R.menu.menu_private, menu)
        inflater?.inflate(R.menu.menu_search, menu)
        onlyPrivate = menu?.findItem(R.id.menu_only_private)?.actionView as CompoundButton
        onlyPrivate?.setOnCheckedChangeListener { _, _ ->
            showDecks()
        }
        super.onCreateOptionsMenu(menu, inflater)
    }

}