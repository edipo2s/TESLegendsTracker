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
import org.jetbrains.anko.bundleOf

/**
 * Created by EdipoSouza on 6/4/17.
 */
class WabbaTrackFeatureFragment : BaseFragment() {

    private val featureImage by lazy { arguments?.getInt(EXTRA_IMAGE_RES) ?: 0 }
    private val featureDesc by lazy { arguments?.getInt(EXTRA_DESC_RES) ?: 0 }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return container?.inflate(R.layout.fragment_wabbatrack_feature)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        wabbatrack_feature_image.setImageResource(featureImage)
        wabbatrack_feature_desc.setText(featureDesc)
    }

    companion object {

        private const val EXTRA_IMAGE_RES = "imageResExtra"
        private const val EXTRA_DESC_RES = "descResExtra"

        fun instance(@DrawableRes featureImage: Int, @StringRes featureDesc: Int): WabbaTrackFeatureFragment {
            return WabbaTrackFeatureFragment().apply {
                arguments = bundleOf(EXTRA_IMAGE_RES to featureImage,
                        EXTRA_DESC_RES to featureDesc)
            }
        }

    }

}