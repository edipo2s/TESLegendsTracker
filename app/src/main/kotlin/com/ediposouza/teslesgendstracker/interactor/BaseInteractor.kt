package com.ediposouza.teslesgendstracker.interactor

import com.ediposouza.teslesgendstracker.data.CardAttribute
import com.ediposouza.teslesgendstracker.data.CardSet
import com.ediposouza.teslesgendstracker.util.ConfigManager
import com.google.firebase.database.*

/**
 * Created by ediposouza on 01/11/16.
 */
open class BaseInteractor {

    companion object {

        val NODE_BASICS = "basics"
        val NODE_CARDS = "cards"
        val NODE_TOKENS = "tokens"
        val NODE_PATCHES = "patches"
        val NODE_SEASONS = "seasons"
        val NODE_SPOILER = "spoiler"
        val NODE_NEWS = "news"
        val NODE_MATCHES_MODE = "mode"

    }

    protected val NODE_USERS = "users"
    protected val NODE_USERS_INFO = "info"
    protected val NODE_DECKS = "decks"
    protected val NODE_DECKS_PUBLIC = "public"

    protected val KEY_CARD_COST = "cost"
    protected val KEY_USER_NAME = "name"
    protected val KEY_USER_PHOTO = "photoUrl"
    protected val KEY_DECK_CLASS = "cls"
    protected val KEY_DECK_UPDATE_AT = "updatedAt"

    protected val database: DatabaseReference by lazy { FirebaseDatabase.getInstance().reference }
    protected val dbDecks: DatabaseReference by lazy { database.child(NODE_DECKS) }
    protected val dbUsers: DatabaseReference by lazy { database.child(NODE_USERS) }

    fun <T> getListFromSets(set: CardSet?, onSuccess: (List<T>) -> Unit,
                            getFromSet: (set: CardSet, onSuccess: (List<T>) -> Unit) -> Unit) {
        getListFromSets(set, CardAttribute.NEUTRAL, onSuccess, { set, _, success ->
            getFromSet(set, success)
        })
    }

    fun <T> getListFromSets(set: CardSet?, attr: CardAttribute, onSuccess: (List<T>) -> Unit,
                            getFromSet: (set: CardSet, attr: CardAttribute, onSuccess: (List<T>) -> Unit) -> Unit) {
        var setIndex = 0
        val items = arrayListOf<T>()
        if (set != null) {
            return getFromSet(set, attr, onSuccess)
        }

        fun getSetsOnSuccess(attr: CardAttribute, onSuccess: (List<T>) -> Unit): (List<T>) -> Unit = {
            items.addAll(it)
            setIndex = setIndex.inc()
            if (CardSet.values()[setIndex] == CardSet.values().last()) {
                onSuccess.invoke(items)
            } else {
                getFromSet(CardSet.values()[setIndex], attr, getSetsOnSuccess(attr, onSuccess))
            }
        }

        getFromSet(CardSet.values()[setIndex], attr, getSetsOnSuccess(attr, onSuccess))
    }

    fun <K, V> getMapFromSets(set: CardSet?, onSuccess: (Map<K, V>) -> Unit,
                              getFromSet: (set: CardSet, onSuccess: (Map<K, V>) -> Unit) -> Unit) {
        getMapFromSets(set, CardAttribute.NEUTRAL, onSuccess, { set, attr, success ->
            getFromSet(set, success)
        })
    }

    fun <K, V> getMapFromSets(set: CardSet?, attr: CardAttribute, onSuccess: (Map<K, V>) -> Unit,
                              getFromSet: (set: CardSet, attr: CardAttribute, onSuccess: (Map<K, V>) -> Unit) -> Unit) {
        var setIndex = 0
        val items = hashMapOf<K, V>()
        if (set != null) {
            return getFromSet(set, attr, onSuccess)
        }

        fun getSetsOnSuccess(attr: CardAttribute, onSuccess: (Map<K, V>) -> Unit): (Map<K, V>) -> Unit = {
            items.putAll(it)
            setIndex = setIndex.inc()
            if (CardSet.values()[setIndex] == CardSet.values().last()) {
                onSuccess.invoke(items)
            } else {
                getFromSet(CardSet.values()[setIndex], attr, getSetsOnSuccess(attr, onSuccess))
            }
        }

        getFromSet(CardSet.values()[setIndex], attr, getSetsOnSuccess(attr, onSuccess))
    }

    fun Query.keepSynced() {
        keepSynced(!ConfigManager.isDBUpdating() && !ConfigManager.isVersionUnsupported())
    }

    abstract class SimpleChildEventListener : ChildEventListener {

        override fun onChildMoved(snapshot: DataSnapshot?, previousChildName: String?) {
        }

        override fun onChildChanged(snapshot: DataSnapshot?, previousChildName: String?) {
        }

        override fun onChildAdded(snapshot: DataSnapshot?, previousChildName: String?) {
        }

        override fun onChildRemoved(snapshot: DataSnapshot?) {
        }

        override fun onCancelled(error: DatabaseError?) {
        }

    }

}
