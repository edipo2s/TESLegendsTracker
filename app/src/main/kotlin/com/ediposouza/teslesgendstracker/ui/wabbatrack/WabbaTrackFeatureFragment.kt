package com.ediposouza.teslesgendstracker.ui.wabbatrack

import android.os.Bundle
import android.support.annotation.DrawableRes
import android.support.annotation.StringRes
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.ediposouza.teslesgendstracker.R
import com.ediposouza.teslesgendstracker.ui.base.BaseFragment
import com.ediposouza.teslesgendstracker.util.inflate
import kotlinx.android.synthetic.main.fragment_wabbatrack_feature.*

/**
 * Created by EdipoSouza on 6/4/17.
 */
class WabbaTrackFeatureFragment(@DrawableRes val featureImage: Int, @StringRes val featureDesc: Int) : BaseFragment() {

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return container?.inflate(R.layout.fragment_wabbatrack_feature)
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        wabbatrack_feature_image.setImageResource(featureImage)
        wabbatrack_feature_desc.setText(featureDesc)
    }

}