package com.ediposouza.teslesgendstracker.interactor

import com.ediposouza.teslesgendstracker.data.Attribute
import com.ediposouza.teslesgendstracker.data.CardSet
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

/**
 * Created by ediposouza on 01/11/16.
 */
open class BaseInteractor {

    companion object {

        val NODE_CARDS = "cards"
        val NODE_PATCHES = "patches"

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
        getListFromSets(set, Attribute.NEUTRAL, onSuccess, { set, attr, success ->
            getFromSet(set, success)
        })
    }

    fun <T> getListFromSets(set: CardSet?, attr: Attribute, onSuccess: (List<T>) -> Unit,
                            getFromSet: (set: CardSet, attr: Attribute, onSuccess: (List<T>) -> Unit) -> Unit) {
        var setIndex = 0
        val items = arrayListOf<T>()
        if (set != null) {
            return getFromSet(set, attr, onSuccess)
        }

        fun getSetsOnSuccess(attr: Attribute, onSuccess: (List<T>) -> Unit): (List<T>) -> Unit = {
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
        getMapFromSets(set, Attribute.NEUTRAL, onSuccess, { set, attr, success ->
            getFromSet(set, success)
        })
    }

    fun <K, V> getMapFromSets(set: CardSet?, attr: Attribute, onSuccess: (Map<K, V>) -> Unit,
                              getFromSet: (set: CardSet, attr: Attribute, onSuccess: (Map<K, V>) -> Unit) -> Unit) {
        var setIndex = 0
        val items = hashMapOf<K, V>()
        if (set != null) {
            return getFromSet(set, attr, onSuccess)
        }

        fun getSetsOnSuccess(attr: Attribute, onSuccess: (Map<K, V>) -> Unit): (Map<K, V>) -> Unit = {
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

}