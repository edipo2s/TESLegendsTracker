package com.ediposouza.teslesgendstracker.ui.decks.tabs

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.ediposouza.teslesgendstracker.R
import com.ediposouza.teslesgendstracker.inflate
import com.ediposouza.teslesgendstracker.interactor.PrivateInteractor
import timber.log.Timber

/**
 * Created by EdipoSouza on 11/18/16.
 */
class DecksFavoritedFragment : DecksPublicFragment() {

    private val privateInteractor = PrivateInteractor()

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return container?.inflate(R.layout.fragment_decks_list)
    }

    override fun getDecks() {
        privateInteractor.getFavoriteDecks(null, {
            it?.forEach { Timber.d("Public: %s", it.toString()) }
            decksAdapter.showDecks(it ?: listOf())
        })
    }

}