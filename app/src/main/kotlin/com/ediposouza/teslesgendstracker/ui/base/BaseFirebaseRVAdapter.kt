package com.ediposouza.teslesgendstracker.ui.base

import android.support.annotation.IntegerRes
import android.support.v7.widget.RecyclerView
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import com.ediposouza.teslesgendstracker.R
import com.ediposouza.teslesgendstracker.ui.util.firebase.FirebaseRVAdapter
import com.ediposouza.teslesgendstracker.util.inflate
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.Query
import kotlinx.android.synthetic.main.itemlist_loading.view.*
import timber.log.Timber

/**
 * Created by EdipoSouza on 1/2/17.
 */
abstract class BaseFirebaseRVAdapter<T, VH : RecyclerView.ViewHolder>(@IntegerRes val layout: Int,
                                                                      model: Class<T>,
                                                                      val viewHolder: Class<VH>,
                                                                      ref: Query?, pageSize: Int) :
        FirebaseRVAdapter<T, VH>(layout, model, viewHolder, ref, pageSize, false) {

    var VIEW_TYPE_HEADER = 0
    var VIEW_TYPE_CONTENT = 1
    var VIEW_TYPE_LOADING = 2

    private var synced: Boolean = false

    override val snapShotOffset: Int = 1

    abstract fun onCreateDefaultViewHolder(parent: ViewGroup): VH
    abstract fun onBindContentHolder(model: T, viewHolder: VH)
    abstract fun onSyncEnd()

    fun getContentCount(): Int = super.getItemCount()

    override fun getItemCount(): Int = super.getItemCount() + 2

    override fun getItemViewType(position: Int): Int {
        return when (position) {
            0 -> VIEW_TYPE_HEADER
            itemCount - 1 -> VIEW_TYPE_LOADING
            else -> VIEW_TYPE_CONTENT
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        when (viewType) {
            VIEW_TYPE_CONTENT -> return onCreateDefaultViewHolder(parent)
            else -> {
                val view = if (viewType == VIEW_TYPE_LOADING) parent.inflate(R.layout.itemlist_loading) else
                    LinearLayout(parent.context).apply { minimumHeight = 1 }
                return viewHolder.getConstructor(View::class.java).newInstance(view)
            }
        }
    }

    override fun populateViewHolder(viewHolder: VH, model: T?) {
        if (model != null) {
            onBindContentHolder(model, viewHolder)
        } else {
            viewHolder.itemView.loadingBar?.visibility = if (synced) View.GONE else View.VISIBLE
        }
    }

    override fun onSyncStatusChanged(synced: Boolean) {
        this.synced = synced
        notifyItemChanged(itemCount - 1)
        if (synced) {
            onSyncEnd()
        }
    }

    override fun onArrayError(firebaseError: DatabaseError) {
        Timber.d(firebaseError.toException(), firebaseError.toString())
    }

}