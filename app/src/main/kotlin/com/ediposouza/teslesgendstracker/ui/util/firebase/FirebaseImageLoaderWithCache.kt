package com.ediposouza.teslesgendstracker.ui.util.firebase

import android.util.Log
import com.bumptech.glide.Priority
import com.bumptech.glide.load.data.DataFetcher
import com.bumptech.glide.load.model.stream.StreamModelLoader
import com.google.android.gms.tasks.Tasks
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.StreamDownloadTask
import java.io.IOException
import java.io.InputStream

/**
 * Created by ediposouza on 24/02/17.
 */
class FirebaseImageLoaderWithCache : StreamModelLoader<StorageReference> {

    override fun getResourceFetcher(model: StorageReference, width: Int, height: Int): DataFetcher<InputStream> {
        return FirebaseStorageFetcher(model)
    }

    private inner class FirebaseStorageFetcher internal constructor(private val mRef: StorageReference) : DataFetcher<InputStream> {
        private var mStreamTask: StreamDownloadTask? = null
        private var mInputStream: InputStream? = null

        @Throws(Exception::class)
        override fun loadData(priority: Priority): InputStream {
            mStreamTask = mRef.stream
            mInputStream = Tasks.await(mStreamTask!!).getStream()

            return mInputStream!!
        }

        override fun cleanup() {
            // Close stream if possible
            if (mInputStream != null) {
                try {
                    mInputStream!!.close()
                    mInputStream = null
                } catch (e: IOException) {
                    Log.w(TAG, "Could not close stream", e)
                }

            }
        }

        override fun getId(): String {
            return mRef.path
        }

        override fun cancel() {
            // Cancel task if possible
            if (mStreamTask != null && mStreamTask!!.isInProgress) {
                mStreamTask!!.cancel()
            }
        }
    }

    companion object {

        private val TAG = "FirebaseImageLoader"
    }
}