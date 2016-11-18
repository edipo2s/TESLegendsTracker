package com.ediposouza.teslesgendstracker.interactor

import com.ediposouza.teslesgendstracker.data.*
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import org.threeten.bp.LocalDate
import org.threeten.bp.LocalDateTime
import timber.log.Timber
import java.util.*

/**
 * Created by ediposouza on 01/11/16.
 */
class PublicInteractor() : BaseInteractor() {

    private val NODE_DECKS = "decks"
    private val NODE_PUBLIC = "public"
    private val NODE_PRIVATE = "private"
    private val KEY_DECK_UPDATE_AT = "updatedAt"
    private val KEY_DECK_CLASS = "cls"
    private val KEY_DECK_OWNER = "owner"
    private val KEY_DECK_VIEWS = "views"
    private val KEY_DECK_LIKES = "likes"
    private val KEY_DECK_COST = "cost"
    private val KEY_DECK_UPDATES = "updates"
    private val KEY_DECK_COMMENTS = "comments"
    private val KEY_EVOLVES = "evolves"

    val dbDecks by lazy { database.child(NODE_DECKS) }

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
                        val cards = ds.children.filter { !it.hasChild(KEY_EVOLVES) }.mapTo(arrayListOf()) {
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

    fun getPublicDecks(cls: Class, onSuccess: (List<Deck>) -> Unit) {
        with(dbDecks.child(NODE_PUBLIC).orderByChild(KEY_DECK_CLASS).equalTo(cls.ordinal.toDouble())) {
            keepSynced(true)
            addListenerForSingleValueEvent(object : ValueEventListener {

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
    }

    fun getMyPublicDecks(cls: Class, onSuccess: (List<Deck>) -> Unit) {
        with(dbDecks.child(NODE_PUBLIC).orderByChild(KEY_DECK_OWNER).equalTo(userID).
                orderByChild(KEY_DECK_CLASS).equalTo(cls.ordinal.toDouble())) {
            keepSynced(true)
            addListenerForSingleValueEvent(object : ValueEventListener {

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
    }

    fun getMyPrivateDecks(onSuccess: (List<Deck>) -> Unit) {
        with(dbDecks.child(NODE_PRIVATE).orderByChild(KEY_DECK_UPDATE_AT)) {
            keepSynced(true)
            addListenerForSingleValueEvent(object : ValueEventListener {

                override fun onDataChange(ds: DataSnapshot) {
                    val decks = ds.children.mapTo(arrayListOf<Deck>()) {
                        it.getValue(DeckParser::class.java).toDeck(it.key, true)
                    }
                    Timber.d(decks.toString())
                    onSuccess.invoke(decks)
                }

                override fun onCancelled(de: DatabaseError) {
                    Timber.d("Fail: " + de.message)
                }

            })
        }
    }

    fun saveDeck(name: String, cls: Class, type: DeckType, cost: Int, patch: String,
                 cards: Map<String, Int>, private: Boolean, onSuccess: () -> Unit, onError: (e: Exception?) -> Unit) {
        with(if (private) dbDecks.child(NODE_PRIVATE) else dbDecks.child(NODE_PUBLIC)) {
            val deck = Deck(push().key, name, userID, private, type, cls, cost, LocalDate.now(),
                    LocalDateTime.now(), patch, ArrayList(), 0, cards, ArrayList(), ArrayList())
            child(deck.id).setValue(DeckParser().fromDeck(deck)).addOnCompleteListener({
                Timber.d(it.toString())
                if (it.isSuccessful) onSuccess.invoke() else onError.invoke(it.exception)
            })
        }
    }

    /**
     * Name, Type, Class, Patch, Private
     */
    fun updateDeck(deck: Deck, oldPrivate: Boolean, onSuccess: () -> Unit, onError: (e: Exception?) -> Unit) {
        with(if (deck.private) dbDecks.child(NODE_PRIVATE) else dbDecks.child(NODE_PUBLIC)) {
            if (deck.private == oldPrivate)
                child(deck.id).updateChildren(DeckParser().fromDeck(deck).toDeckUpdateMap()).addOnCompleteListener({
                    Timber.d(it.toString())
                    if (it.isSuccessful) onSuccess.invoke() else onError.invoke(it.exception)
                })
            else
                child(deck.id).setValue(DeckParser().fromDeck(deck)).addOnCompleteListener({
                    timber.log.Timber.d(it.toString())
                    if (it.isSuccessful) {
                        with(if (oldPrivate) dbDecks.child(NODE_PRIVATE) else dbDecks.child(NODE_PUBLIC)) {
                            child(deck.id).removeValue()
                        }
                        onSuccess.invoke()
                    } else onError.invoke(it.exception)
                })
        }
    }

    fun updateDeckCards(deck: Deck, oldCards: Map<String, Int>, cost: Int, onSuccess: () -> Unit,
                        onError: (e: Exception?) -> Unit) {
        with(if (deck.private) dbDecks.child(NODE_PRIVATE) else dbDecks.child(NODE_PUBLIC)) {
            val updateKey = LocalDateTime.now().toString()
            val cardsRem = oldCards.filter { !deck.cards.keys.contains(it.key) }.mapValues { it.key to it.value * -1 }
            val cardsDiff = deck.cards.mapValues { it.key to it.value.minus(oldCards[it.key] ?: 0) }.plus(cardsRem)
            child(deck.id).child(KEY_DECK_UPDATES).child(updateKey).setValue(cardsDiff).addOnCompleteListener({
                Timber.d(it.toString())
                if (it.isSuccessful) onSuccess.invoke() else onError.invoke(it.exception)
            })
            child(deck.id).child(KEY_DECK_COST).setValue(cost)
        }
    }

    fun incDeckView(deck: Deck, onSuccess: () -> Unit, onError: (e: Exception?) -> Unit) {
        with(if (deck.private) dbDecks.child(NODE_PRIVATE) else dbDecks.child(NODE_PUBLIC)) {
            child(deck.id).updateChildren(mapOf(KEY_DECK_VIEWS to deck.views.inc())).addOnCompleteListener({
                Timber.d(it.toString())
                if (it.isSuccessful) onSuccess.invoke() else onError.invoke(it.exception)
            })
        }
    }

    fun addDeckLike(deck: Deck, onSuccess: () -> Unit, onError: (e: Exception?) -> Unit) {
        with(if (deck.private) dbDecks.child(NODE_PRIVATE) else dbDecks.child(NODE_PUBLIC)) {
            child(deck.id).updateChildren(mapOf(KEY_DECK_LIKES to deck.likes.plus(userID))).addOnCompleteListener({
                Timber.d(it.toString())
                if (it.isSuccessful) onSuccess.invoke() else onError.invoke(it.exception)
            })
        }
    }

    fun remDeckLike(deck: Deck, onSuccess: () -> Unit, onError: (e: Exception?) -> Unit) {
        with(if (deck.private) dbDecks.child(NODE_PRIVATE) else dbDecks.child(NODE_PUBLIC)) {
            child(deck.id).updateChildren(mapOf(KEY_DECK_LIKES to deck.likes.minus(userID))).addOnCompleteListener({
                Timber.d(it.toString())
                if (it.isSuccessful) onSuccess.invoke() else onError.invoke(it.exception)
            })
        }
    }

    fun addDeckComment(deck: Deck, msg: String, onSuccess: () -> Unit, onError: (e: Exception?) -> Unit) {
        with(if (deck.private) dbDecks.child(NODE_PRIVATE) else dbDecks.child(NODE_PUBLIC)) {
            val comment = DeckParser.toNewCommentMap(userID, msg)
            with(child(deck.id).child(KEY_DECK_COMMENTS)) {
                child(push().key).setValue(comment).addOnCompleteListener({
                    Timber.d(it.toString())
                    if (it.isSuccessful) onSuccess.invoke() else onError.invoke(it.exception)
                })
            }
        }
    }

    fun remDeckComment(deck: Deck, commentId: String, msg: String, onSuccess: () -> Unit, onError: (e: Exception?) -> Unit) {
        with(if (deck.private) dbDecks.child(NODE_PRIVATE) else dbDecks.child(NODE_PUBLIC)) {
            child(deck.id).child(KEY_DECK_COMMENTS).child(commentId).removeValue().addOnCompleteListener({
                Timber.d(it.toString())
                if (it.isSuccessful) onSuccess.invoke() else onError.invoke(it.exception)
            })
        }
    }

}

