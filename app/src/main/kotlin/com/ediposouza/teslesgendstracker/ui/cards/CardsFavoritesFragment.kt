package com.ediposouza.teslesgendstracker.ui.cards

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.ediposouza.teslesgendstracker.R
import kotlinx.android.synthetic.main.activity_dash.*

/**
 * Created by EdipoSouza on 10/30/16.
 */
class CardsFavoritesFragment : BaseFragment() {

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater?.inflate(R.layout.fragment_cards_cards, container, false)
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        activity.dash_toolbar_title.setText(R.string.tab_favorites)
    }

}