package com.ediposouza.teslesgendstracker.interactor

import com.ediposouza.teslesgendstracker.data.*
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import timber.log.Timber
import java.util.*

/**
 * Created by ediposouza on 01/11/16.
 */
class PublicInteractor() : BaseInteractor() {

    private val KEY_CARD_EVOLVES = "evolves"
    private val KEY_DECK_VIEWS = "views"

    fun getCards(attr: Attribute, onSuccess: (List<Card>) -> Unit) {
        val node_attr = attr.name.toLowerCase()
        database.child(NODE_CARDS).child(NODE_CORE).child(node_attr).orderByChild(KEY_CARD_COST)
                .addListenerForSingleValueEvent(object : ValueEventListener {

                    override fun onDataChange(ds: DataSnapshot) {
                        val cards = ds.children.mapTo(arrayListOf()) {
                            it.getValue(CardParser::class.java).toCard(it.key, attr)
                        }
                        Timber.d(cards.toString())
                        onSuccess.invoke(cards)
                    }

                    override fun onCancelled(de: DatabaseError) {
                        Timber.d("Fail: " + de.message)
                    }

                })
    }

    fun getCardsForStatistics(attr: Attribute, onSuccess: (List<CardStatistic>) -> Unit) {
        val node_attr = attr.name.toLowerCase()
        database.child(NODE_CARDS).child(NODE_CORE).child(node_attr).orderByChild(KEY_CARD_COST)
                .addListenerForSingleValueEvent(object : ValueEventListener {

                    override fun onDataChange(ds: DataSnapshot) {
                        val cards = ds.children.filter { !it.hasChild(KEY_CARD_EVOLVES) }.mapTo(arrayListOf()) {
                            it.getValue(CardParser::class.java).toCardStatistic(it.key)
                        }
                        Timber.d(cards.toString())
                        onSuccess.invoke(cards)
                    }

                    override fun onCancelled(de: DatabaseError) {
                        Timber.d("Fail: " + de.message)
                    }

                })
    }

    fun getPublicDecks(cls: Class?, onSuccess: (List<Deck>) -> Unit) {
        val dbPublicDeck = dbDecks.child(NODE_PUBLIC)
        dbPublicDeck.keepSynced(true)
        var query = dbPublicDeck.orderByChild(KEY_DECK_UPDATE_AT)
        if (cls != null) {
            query = dbPublicDeck.orderByChild(KEY_DECK_CLASS).equalTo(cls.ordinal.toDouble())
        }
        query.addListenerForSingleValueEvent(object : ValueEventListener {

            override fun onDataChange(ds: DataSnapshot) {
                Timber.d(ds.value.toString())
                val decks = ds.children.mapTo(arrayListOf<Deck>()) {
                    it.getValue(DeckParser::class.java).toDeck(it.key, false)
                }
                Timber.d(decks.toString())
                onSuccess.invoke(decks)
            }

            override fun onCancelled(de: DatabaseError) {
                Timber.d("Fail: " + de.message)
            }

        })
    }

    fun incDeckView(deck: Deck, onSuccess: () -> Unit, onError: (e: Exception?) -> Unit) {
        with(dbDecks.child(NODE_PUBLIC)) {
            child(deck.id).updateChildren(mapOf(KEY_DECK_VIEWS to deck.views.inc())).addOnCompleteListener({
                Timber.d(it.toString())
                if (it.isSuccessful) onSuccess.invoke() else onError.invoke(it.exception)
            })
        }
    }

    fun getDeckCards(deck: Deck, onError: ((e: Exception?) -> Unit)? = null, onSuccess: (List<CardSlot>) -> Unit) {
        with(database.child(NODE_CARDS).child(NODE_CORE)) {
            getAttrCards(deck.cls.attr1, onError) {
                val cards = it
                getAttrCards(deck.cls.attr2, onError) {
                    cards.addAll(it)
                    Timber.d(cards.toString())
                    onSuccess.invoke(cards.map { CardSlot(it, deck.cards[it.shortName] ?: 0) })
                }
            }
        }
    }

    private fun DatabaseReference.getAttrCards(attr: Attribute, onError: ((e: Exception?) -> Unit)? = null,
                                               onSuccess: (ArrayList<Card>) -> Unit) {
        val nodeAttr = attr.name.toLowerCase()
        child(nodeAttr).orderByChild(KEY_CARD_COST).addListenerForSingleValueEvent(object : ValueEventListener {

            override fun onDataChange(ds: DataSnapshot) {
                val cards = ds.children.mapTo(arrayListOf()) {
                    it.getValue(CardParser::class.java).toCard(it.key, attr)
                }
                Timber.d(cards.toString())
                onSuccess.invoke(cards)
            }

            override fun onCancelled(de: DatabaseError) {
                Timber.d("Fail: " + de.message)
                onError?.invoke(de.toException())
            }

        })
    }

}

