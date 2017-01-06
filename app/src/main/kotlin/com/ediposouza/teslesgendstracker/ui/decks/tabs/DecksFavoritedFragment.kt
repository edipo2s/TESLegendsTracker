package com.ediposouza.teslesgendstracker.ui.decks.tabs

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.ediposouza.teslesgendstracker.R
import com.ediposouza.teslesgendstracker.util.inflate

/**
 * Created by EdipoSouza on 11/18/16.
 */
class DecksFavoritedFragment : DecksPublicFragment() {

    override val dataRef = {
        privateInteractor.getFavoriteDecks()
    }

//    override val dataFilter: (FirebaseParsers.DeckParser) -> Boolean = {
//        currentClasses.map { it.ordinal }.contains(it.cls)
//    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return container?.inflate(R.layout.fragment_decks_list)
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        configLoggedViews()
    }

}