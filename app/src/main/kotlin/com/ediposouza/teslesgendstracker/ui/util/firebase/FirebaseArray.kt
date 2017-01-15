/*
 * Firebase UI Bindings Android Library
 *
 * Copyright © 2015 Firebase - All Rights Reserved
 * https://www.firebase.com
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this
 * list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binaryform must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY FIREBASE AS IS AND ANY EXPRESS OR
 * IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
 * MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO
 * EVENT SHALL FIREBASE BE LIABLE FOR ANY DIRECT,
 * INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING,
 * BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE
 * OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF
 * ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package com.ediposouza.teslesgendstracker.ui.util.firebase

import com.google.firebase.database.*
import java.util.*

/**
 * This class implements an array-like collection on top of a Firebase location.
 */
class FirebaseArray<T>(var mModel: Class<T>, val mOriginalQuery: () -> Query?, pageSize: Int = 0,
                       val mOrderASC: Boolean = true) : ChildEventListener, ValueEventListener {

    enum class EventType {
        Added, Changed, Removed, Moved, Reset
    }

    enum class SyncType {
        UnSynced, Synced
    }

    interface OnChangedListener {
        fun onChanged(type: EventType, index: Int, oldIndex: Int)
    }

    interface OnErrorListener {
        fun onError(firebaseError: DatabaseError)
    }

    interface OnSyncStatusChangedListener {
        fun onChanged(type: SyncType)
    }

    private var mQuery: Query? = null
    private val mSnapshots: ArrayList<Pair<String, T>> = ArrayList()
    private val mPageSize: Int
    private var mCurrentSize: Int = 0
    private var isSyncing: Boolean = false
        set(syncing) {
            if (syncing == isSyncing) {
                return
            }
            field = syncing
            if (syncing) {
                notifyOnSyncChangedListeners(SyncType.UnSynced)
            } else {
                notifyOnSyncChangedListeners(SyncType.Synced)
            }
        }
    var mOnChangedListener: OnChangedListener? = null
    var mOnErrorListener: OnErrorListener? = null
    var mOnSyncStatusListener: OnSyncStatusChangedListener? = null
        set(listener) {
            field = listener
            notifyOnSyncChangedListeners(if (isSyncing) SyncType.UnSynced else SyncType.Synced)
        }

    init {
        mCurrentSize = Math.abs(pageSize)
        mPageSize = mCurrentSize

        setup()
    }

    fun reset() {
        mCurrentSize = mPageSize
        mSnapshots.clear()
        setup()
        notifyChangedListeners(EventType.Reset, 0)
    }

    fun more() {
        if (mPageSize > 0 && !isSyncing) {
            mCurrentSize += mPageSize
            setup()
        }
    }

    fun cleanup() {
        mQuery?.removeEventListener(this as ChildEventListener)
        mQuery?.removeEventListener(this as ValueEventListener)
    }

    val count: Int
        get() = mSnapshots.size

    fun getCount(cond: (T) -> Boolean): Int = mSnapshots.filter { cond.invoke(it.second) }.size

    fun getItem(index: Int, cond: (T) -> Boolean): Pair<String, T> {
        return mSnapshots.filter { cond.invoke(it.second) }[index]
    }

    private fun setup() {
        if (mQuery != null) {
            cleanup()
        }
        if (mPageSize == 0) {
            mQuery = mOriginalQuery()
        } else if (mOrderASC) {
            mQuery = mOriginalQuery()?.limitToFirst(mCurrentSize)
        } else {
            mQuery = mOriginalQuery()?.limitToLast(mCurrentSize)
        }
        isSyncing = true
        mQuery?.addChildEventListener(this)
        mQuery?.addListenerForSingleValueEvent(this)
    }

    private fun getIndexForKey(key: String): Int {
        var index = 0
        for (snapshot in mSnapshots) {
            if (snapshot.first == key) {
                return index
            } else {
                index++
            }
        }
        throw IllegalArgumentException("Key not found")
    }

    // Start of ChildEventListener and ValueEventListener methods

    override fun onChildAdded(snapshot: DataSnapshot, previousChildKey: String?) {
        var index = if (mOrderASC) 0 else count
        if (previousChildKey != null) {
            if (mOrderASC) {
                index = getIndexForKey(previousChildKey) + 1
            } else {
                index = getIndexForKey(previousChildKey)
            }
        }
        if (mOrderASC &&
                index < count &&
                mSnapshots[index].first == snapshot.key) {
            return
        } else if (!mOrderASC &&
                index < count + 1 &&
                index > 0 &&
                mSnapshots[index - 1].first == snapshot.key) {
            return
        }

        mSnapshots.add(index, Pair(snapshot.key, snapshot.getValue(mModel)))
        notifyChangedListeners(EventType.Added, index)
    }

    override fun onChildChanged(snapshot: DataSnapshot, previousChildKey: String?) {
        val index = getIndexForKey(snapshot.key)
        mSnapshots[index] = Pair(snapshot.key, snapshot.getValue(mModel))
        notifyChangedListeners(EventType.Changed, index)
    }

    override fun onChildRemoved(snapshot: DataSnapshot) {
        val index = getIndexForKey(snapshot.key)
        mSnapshots.removeAt(index)
        notifyChangedListeners(EventType.Removed, index)
    }

    override fun onChildMoved(snapshot: DataSnapshot, previousChildKey: String?) {
        val oldIndex = getIndexForKey(snapshot.key)
        mSnapshots.removeAt(oldIndex)
        val newIndex = if (previousChildKey == null) 0 else getIndexForKey(previousChildKey) + 1
        mSnapshots.add(newIndex, Pair(snapshot.key, snapshot.getValue(mModel)))
        notifyChangedListeners(EventType.Moved, newIndex, oldIndex)
    }

    override fun onDataChange(dataSnapshot: DataSnapshot) {
        isSyncing = false
    }

    override fun onCancelled(firebaseError: DatabaseError) {
        notifyOnErrorListeners(firebaseError)
    }

    // End of ChildEventListener and ValueEventListener methods

    fun notifyChangedListeners(type: EventType, index: Int, oldIndex: Int = -1) {
        if (mOnChangedListener != null) {
            mOnChangedListener!!.onChanged(type, index, oldIndex)
        }
    }

    fun notifyOnErrorListeners(firebaseError: DatabaseError) {
        if (mOnErrorListener != null) {
            mOnErrorListener!!.onError(firebaseError)
        }
    }

    fun notifyOnSyncChangedListeners(type: SyncType) {
        if (mOnSyncStatusListener != null) {
            mOnSyncStatusListener!!.onChanged(type)
        }
    }
}
