package com.ediposouza.teslesgendstracker.ui.matches.tabs

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.ediposouza.teslesgendstracker.R
import com.ediposouza.teslesgendstracker.ui.base.BaseFragment
import com.ediposouza.teslesgendstracker.ui.matches.tabs.basic.BasicTableFixHeader
import com.ediposouza.teslesgendstracker.util.inflate
import kotlinx.android.synthetic.main.fragment_matches_statistics.*

/**
 * Created by EdipoSouza on 1/3/17.
 */
class MatchesStatistics : BaseFragment() {

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return container?.inflate(R.layout.fragment_matches_statistics)
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        matches_statistics_table.adapter = BasicTableFixHeader(context).instance
    }

}