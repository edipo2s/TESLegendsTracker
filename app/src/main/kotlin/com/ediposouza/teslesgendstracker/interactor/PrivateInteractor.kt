package com.ediposouza.teslesgendstracker.interactor

import com.ediposouza.teslesgendstracker.data.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import org.threeten.bp.LocalDateTime
import timber.log.Timber
import java.util.*

/**
 * Created by ediposouza on 01/11/16.
 */
class PrivateInteractor() : BaseInteractor() {

    private val NODE_USERS = "users"
    private val NODE_PRIVATE = "private"
    private val NODE_SAVED = "saved"

    private val KEY_CARD_NAME: String = "name"
    private val KEY_CARD_PHOTO: String = "photoUrl"
    private val KEY_CARD_FAVORITE = "favorite"
    private val KEY_CARD_QTD = "qtd"

    private val KEY_DECK_RARITY = "rarity"
    private val KEY_DECK_OWNER = "owner"
    private val KEY_DECK_LIKES = "likes"
    private val KEY_DECK_COST = "cost"
    private val KEY_DECK_UPDATES = "updates"
    private val KEY_DECK_COMMENTS = "comments"

    private fun dbUser(): DatabaseReference? {
        return if (userID.isNotEmpty()) database.child(NODE_USERS).child(userID) else null
    }

    private fun dbUserCards(cls: Attribute): DatabaseReference? {
        val dbRef = dbUser()?.child(NODE_CARDS)?.child(NODE_CORE)?.child(cls.name.toLowerCase())
        dbRef?.keepSynced(true)
        return dbRef
    }

    fun setUserInfo() {
        val user = FirebaseAuth.getInstance().currentUser
        dbUser()?.apply {
            child(KEY_CARD_NAME).setValue(user?.displayName ?: "")
            child(KEY_CARD_PHOTO).setValue(user?.photoUrl.toString())
        }
    }

    fun setUserCardQtd(card: Card, qtd: Long, onComplete: () -> Unit) {
        dbUserCards(card.cls)?.apply {
            child(card.shortName).child(KEY_CARD_QTD).setValue(qtd).addOnCompleteListener {
                onComplete.invoke()
            }
        }
    }

    fun setUserCardFavorite(card: Card, favorite: Boolean, onComplete: () -> Unit) {
        dbUserCards(card.cls)?.apply {
            child(card.shortName).child(KEY_CARD_FAVORITE).apply {
                if (favorite) {
                    setValue(true).addOnCompleteListener { onComplete.invoke() }
                } else {
                    removeValue().addOnCompleteListener { onComplete.invoke() }
                }
            }
        }
    }

    fun getUserCollection(attr: Attribute, onSuccess: (Map<String, Long>) -> Unit) {
        dbUserCards(attr)?.addListenerForSingleValueEvent(object : ValueEventListener {

            @Suppress("UNCHECKED_CAST")
            override fun onDataChange(ds: DataSnapshot) {
                val collection = ds.children.filter { it.hasChild(KEY_CARD_QTD) }.map({
                    it.key to it.child(KEY_CARD_QTD).value as Long
                }).toMap()
                Timber.d(collection.toString())
                onSuccess.invoke(collection)
            }

            override fun onCancelled(de: DatabaseError) {
                Timber.d("Fail: " + de.message)
            }

        })
    }

    fun getUserFavorites(attr: Attribute, onSuccess: (List<String>) -> Unit) {
        dbUserCards(attr)?.addListenerForSingleValueEvent(object : ValueEventListener {

            @Suppress("UNCHECKED_CAST")
            override fun onDataChange(ds: DataSnapshot) {
                val favorites = ds.children.filter { (it.child(KEY_CARD_FAVORITE)?.value ?: false) as Boolean }
                        .map({ it.key })
                Timber.d(favorites.toString())
                onSuccess.invoke(favorites)
            }

            override fun onCancelled(de: DatabaseError) {
                Timber.d("Fail: " + de.message)
            }

        })
    }

    fun getOwnedDecks(cls: Class?, onSuccess: (List<Deck>) -> Unit) {
        getOwnedPublicDecks(cls) {
            val decks = it
            getOwnedPrivateDecks(cls) {
                onSuccess.invoke(decks.plus(it))
            }
        }
    }

    fun getOwnedPublicDecks(cls: Class?, onSuccess: (List<Deck>) -> Unit) {
        with(dbDecks.child(NODE_PUBLIC).orderByChild(KEY_DECK_OWNER).equalTo(userID)){
            keepSynced(true)
            addListenerForSingleValueEvent(object : ValueEventListener {

                override fun onDataChange(ds: DataSnapshot) {
                    Timber.d(ds.value?.toString())
                    val decks = ds.children.mapTo(arrayListOf<Deck>()) {
                        it.getValue(DeckParser::class.java).toDeck(it.key, false)
                    }.filter { cls == null || it.cls == cls }.sortedBy(Deck::updatedAt)
                    Timber.d(decks.toString())
                    onSuccess.invoke(decks)
                }

                override fun onCancelled(de: DatabaseError) {
                    Timber.d("Fail: " + de.message)
                }

            })
        }
    }

    fun getOwnedPrivateDecks(cls: Class?, onSuccess: (List<Deck>) -> Unit) {
        dbUser()?.child(NODE_DECKS)?.child(NODE_PRIVATE)?.orderByChild(KEY_DECK_UPDATE_AT)?.apply {
            keepSynced(true)
            addListenerForSingleValueEvent(object : ValueEventListener {

                override fun onDataChange(ds: DataSnapshot) {
                    val decks = ds.children.mapTo(arrayListOf<Deck>()) {
                        it.getValue(DeckParser::class.java).toDeck(it.key, true)
                    }.filter { cls == null || it.cls == cls }
                    Timber.d(decks.toString())
                    onSuccess.invoke(decks)
                }

                override fun onCancelled(de: DatabaseError) {
                    Timber.d("Fail: " + de.message)
                }

            })
        }
    }

    fun getSavedDecks(cls: Class?, onSuccess: (List<Deck?>) -> Unit) {
        PublicInteractor().getPublicDecks(cls) {
            val publicDecks = it
            dbUser()!!.child(NODE_DECKS).child(NODE_SAVED).apply {
                keepSynced(true)
                addListenerForSingleValueEvent(object : ValueEventListener {

                    override fun onDataChange(ds: DataSnapshot) {
                        Timber.d(ds.value?.toString())
                        val decks = ArrayList<Deck>()
                        ds.value?.apply {
                            decks.addAll((this as List<*>).map {
                                val deckId = it
                                publicDecks.find { it.id == deckId } ?: Deck()
                            }.filter { it.cost > 0 }.filter { cls == null || it.cls == cls })
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
    }

    fun getMissingCards(deck: Deck, onError: ((e: Exception?) -> Unit)? = null, onSuccess: (List<CardMissing>) -> Unit) {
        with(database.child(NODE_CARDS).child(NODE_CORE)) {
            getAttrCards(deck.cls.attr1.name.toLowerCase(), onError) {
                val cards = HashMap(it)
                getAttrCards(deck.cls.attr2.name.toLowerCase(), onError) {
                    cards.putAll(it)
                    getUserCollection(deck.cls.attr1) {
                        val userCards = HashMap(it)
                        getUserCollection(deck.cls.attr2) {
                            userCards.putAll(it)
                            val missing = deck.cards.map { it.key to it.value.minus(userCards[it.key] ?: 0) }
                                    .map { CardMissing(it.first, cards[it.first]!!, it.second) }
                            Timber.d(missing.toString())
                            onSuccess.invoke(missing)
                        }
                    }
                }
            }
        }
    }

    private fun DatabaseReference.getAttrCards(nodeAttr: String, onError: ((e: Exception?) -> Unit)? = null,
                                               onSuccess: (Map<String, CardRarity>) -> Unit) {
        child(nodeAttr).orderByChild(KEY_CARD_COST).addListenerForSingleValueEvent(object : ValueEventListener {

            override fun onDataChange(ds: DataSnapshot) {
                val cardsAttr = ds.children.map({
                    it.key to CardRarity.valueOf(it.child(KEY_DECK_RARITY).value.toString().toUpperCase())
                }).toMap()
                Timber.d(cardsAttr.toString())
                onSuccess(cardsAttr)
            }

            override fun onCancelled(de: DatabaseError) {
                Timber.d("Fail: " + de.message)
                onError?.invoke(de.toException())
            }

        })
    }

    fun saveDeck(name: String, cls: Class, type: DeckType, cost: Int, patch: String,
                 cards: Map<String, Int>, private: Boolean, onError: ((e: Exception?) -> Unit)? = null, onSuccess: () -> Unit) {
        dbUser()?.apply {
            with(if (private) child(NODE_DECKS).child(NODE_PRIVATE) else dbDecks.child(NODE_PUBLIC)) {
                val deck = Deck(push().key, name, userID, private,
                        type, cls, cost, LocalDateTime.now(), LocalDateTime.now(), patch, ArrayList(), 0,
                        cards, ArrayList(), ArrayList())
                child(deck.id).setValue(DeckParser().fromDeck(deck)).addOnCompleteListener({
                    Timber.d(it.toString())
                    if (it.isSuccessful) onSuccess.invoke() else onError?.invoke(it.exception)
                })
            }
        }
    }

    /**
     * Name, Type, Class, Patch, Private
     */
    fun updateDeck(deck: Deck, oldPrivate: Boolean, onError: ((e: Exception?) -> Unit)? = null, onSuccess: () -> Unit) {
        dbUser()?.apply {
            with(if (deck.private) child(NODE_DECKS).child(NODE_PRIVATE) else dbDecks.child(NODE_PUBLIC)) {
                if (deck.private == oldPrivate)
                    child(deck.id).updateChildren(DeckParser().fromDeck(deck).toDeckUpdateMap()).addOnCompleteListener({
                        Timber.d(it.toString())
                        if (it.isSuccessful) onSuccess.invoke() else onError?.invoke(it.exception)
                    })
                else
                    child(deck.id).setValue(DeckParser().fromDeck(deck)).addOnCompleteListener({
                        Timber.d(it.toString())
                        if (it.isSuccessful) {
                            dbUser()?.apply {
                                with(if (oldPrivate) child(NODE_DECKS).child(NODE_PRIVATE) else dbDecks.child(NODE_PUBLIC)) {
                                    child(deck.id).removeValue()
                                }
                                onSuccess.invoke()
                            }
                        } else onError?.invoke(it.exception)
                    })
            }
        }
    }

    fun updateDeckCards(deck: Deck, oldCards: Map<String, Int>, cost: Int, onSuccess: () -> Unit,
                        onError: ((e: Exception?) -> Unit)? = null) {
        dbUser()?.apply {
            with(if (deck.private) child(NODE_DECKS).child(NODE_PRIVATE) else dbDecks.child(NODE_PUBLIC)) {
                val updateKey = org.threeten.bp.LocalDateTime.now().toString()
                val cardsRem = oldCards.filter { !deck.cards.keys.contains(it.key) }.mapValues { it.key to it.value * -1 }
                val cardsDiff = deck.cards.mapValues { it.key to it.value.minus(oldCards[it.key] ?: 0) }.plus(cardsRem)
                child(deck.id).child(KEY_DECK_UPDATES).child(updateKey).setValue(cardsDiff).addOnCompleteListener({
                    Timber.d(it.toString())
                    if (it.isSuccessful) onSuccess.invoke() else onError?.invoke(it.exception)
                })
                child(deck.id).child(KEY_DECK_COST).setValue(cost)
            }
        }
    }

    fun addDeckLike(deck: Deck, onError: ((e: Exception?) -> Unit)? = null, onSuccess: () -> Unit) {
        dbUser()?.apply {
            with(if (deck.private) child(NODE_DECKS).child(NODE_PRIVATE) else dbDecks.child(NODE_PUBLIC)) {
                child(deck.id).updateChildren(mapOf(KEY_DECK_LIKES to deck.likes.plus(userID))).addOnCompleteListener({
                    Timber.d(it.toString())
                    if (it.isSuccessful) onSuccess.invoke() else onError?.invoke(it.exception)
                })
            }
        }
    }

    fun remDeckLike(deck: Deck, onError: ((e: Exception?) -> Unit)? = null, onSuccess: () -> Unit) {
        dbUser()?.apply {
            with(if (deck.private) child(NODE_DECKS).child(NODE_PRIVATE) else dbDecks.child(NODE_PUBLIC)) {
                child(deck.id).updateChildren(mapOf(KEY_DECK_LIKES to deck.likes.minus(userID))).addOnCompleteListener({
                    Timber.d(it.toString())
                    if (it.isSuccessful) onSuccess.invoke() else onError?.invoke(it.exception)
                })
            }
        }
    }

    fun addDeckComment(deck: Deck, msg: String, onError: ((e: Exception?) -> Unit)? = null, onSuccess: () -> Unit) {
        dbUser()?.apply {
            with(if (deck.private) child(NODE_DECKS).child(NODE_PRIVATE) else dbDecks.child(NODE_PUBLIC)) {
                val comment = DeckParser.toNewCommentMap(userID, msg)
                with(child(deck.id).child(KEY_DECK_COMMENTS)) {
                    child(push().key).setValue(comment).addOnCompleteListener({
                        Timber.d(it.toString())
                        if (it.isSuccessful) onSuccess.invoke() else onError?.invoke(it.exception)
                    })
                }
            }
        }
    }

    fun remDeckComment(deck: Deck, commentId: String, msg: String, onError: ((e: Exception?) -> Unit)? = null, onSuccess: () -> Unit) {
        dbUser()?.apply {
            with(if (deck.private) child(NODE_DECKS).child(NODE_PRIVATE) else dbDecks.child(NODE_PUBLIC)) {
                child(deck.id).child(KEY_DECK_COMMENTS).child(commentId).removeValue().addOnCompleteListener({
                    Timber.d(it.toString())
                    if (it.isSuccessful) onSuccess.invoke() else onError?.invoke(it.exception)
                })
            }
        }
    }

}