package com.ediposouza.teslesgendstracker.ui.decks.tabs

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.ediposouza.teslesgendstracker.R
import com.ediposouza.teslesgendstracker.data.Class
import com.ediposouza.teslesgendstracker.interactor.PrivateInteractor
import com.ediposouza.teslesgendstracker.util.inflate
import timber.log.Timber

/**
 * Created by EdipoSouza on 11/18/16.
 */
class DecksFavoritedFragment : DecksPublicFragment() {

    private val privateInteractor = PrivateInteractor()

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return container?.inflate(R.layout.fragment_decks_list)
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        configLoggedViews()
    }

    override fun getDecks(cls: Class?, last: Boolean) {
        privateInteractor.getFavoriteDecks(cls, {
            it?.forEach { Timber.d("Public: %s", it.toString()) }
            decksAdapter.showDecks(it ?: listOf(), last)
        })
    }

}