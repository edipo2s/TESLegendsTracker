package com.ediposouza.teslesgendstracker.ui.base

import android.support.annotation.LayoutRes
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.ViewGroup
import com.ediposouza.teslesgendstracker.R
import com.ediposouza.teslesgendstracker.util.inflate
import com.ediposouza.teslesgendstracker.util.load
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.NativeExpressAdView
import com.google.firebase.database.Query

/**
 * Created by ediposouza on 08/12/16.
 */
abstract class BaseAdsFirebaseAdapter<T, VH : RecyclerView.ViewHolder>(val adsEachItems: Int,
                                                                       @LayoutRes val adsLayout: Int,
                                                                       model: Class<T>,
                                                                       ref: Query?,
                                                                       pageSize: Int) :
        BaseFirebaseRVAdapter<T, VH>(model, ref, pageSize) {

    val VIEW_TYPE_ADS = 3

    constructor(adsEachItems: Int,
                layoutManager: GridLayoutManager,
                @LayoutRes adsLayout: Int,
                model: Class<T>,
                ref: Query?,
                pageSize: Int) : this(adsEachItems, adsLayout, model, ref, pageSize) {
        onRestoreState(layoutManager)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        if (viewType == VIEW_TYPE_ADS) {
            val adsItemView = parent.inflate(adsLayout)
            val ads = adsItemView.findViewById(R.id.ads_view)
            when (ads) {
                is AdView -> ads.load()
                is NativeExpressAdView -> ads.load()
            }
            return object : RecyclerView.ViewHolder(adsItemView) {}
        }
        return super.onCreateViewHolder(parent, viewType)
    }

    override fun onBindViewHolder(viewHolder: RecyclerView.ViewHolder, position: Int) {
        val adsQtdBeforePosition = getAdsQtdBeforePosition(position)
        var itemKey: String? = null
        var model: T? = null
        val arrayPosition = position - snapShotOffset
        if (arrayPosition < getContentCount() + adsQtdBeforePosition && arrayPosition >= 0) {
            itemKey = getItemKey(position - adsQtdBeforePosition)
            model = getItem(position - adsQtdBeforePosition)
        }
        populateViewHolder(itemKey, viewHolder, model, position)
    }

    @Suppress("UNCHECKED_CAST")
    override fun populateViewHolder(itemKey: String?, viewHolder: RecyclerView.ViewHolder, model: T?, position: Int) {
        if (getItemViewType(position) != VIEW_TYPE_ADS) {
            super.populateViewHolder(itemKey, viewHolder, model)
        }
    }

    override fun getItemCount(): Int {
        val contentItemCount = super.getContentCount()
        val adsContentItemCount = contentItemCount + if (adsEachItems > 0) contentItemCount.div(adsEachItems) else 0
        return adsContentItemCount + 2
    }

    override fun getItemViewType(position: Int): Int {
        var viewType = super.getItemViewType(position)
        if (viewType == VIEW_TYPE_CONTENT) {
            val qtdAdsBefore = getAdsQtdBeforePosition(position)
            val nextEachItems = adsEachItems * (qtdAdsBefore + 1) + qtdAdsBefore
            viewType = if (position == nextEachItems) VIEW_TYPE_ADS else VIEW_TYPE_CONTENT
        }
        return viewType
    }

    private fun getAdsQtdBeforePosition(position: Int): Int {
        val qtdAds = position.div(adsEachItems)
        return (position - qtdAds).div(adsEachItems)
    }

    protected fun getAdsQtdBeforeDefaultPosition(position: Int): Int {
        return position.div(adsEachItems)
    }

    fun onRestoreState(layoutManager: GridLayoutManager) {
        layoutManager.apply {
            spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
                override fun getSpanSize(position: Int): Int {
                    val itemType = getItemViewType(position)
                    return if (itemType == VIEW_TYPE_ADS) spanCount else 1
                }
            }
        }
    }

}