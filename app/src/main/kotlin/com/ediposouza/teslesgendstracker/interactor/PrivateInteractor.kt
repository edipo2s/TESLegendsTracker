package com.ediposouza.teslesgendstracker.interactor

import com.ediposouza.teslesgendstracker.data.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
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
            getOwnedPrivateDecks(cls) {
                onSuccess.invoke(it)
            }
        }
    }

    fun getOwnedPublicDecks(cls: Class?, onSuccess: (List<Deck>) -> Unit) {
        val dbPublicDecks = dbDecks.child(NODE_PUBLIC)
        dbPublicDecks.setPriority(KEY_DECK_OWNER)
        val dbPrivatePublicDecks = dbPublicDecks.equalTo(userID)
        dbPrivatePublicDecks.keepSynced(true)
        var query = dbPrivatePublicDecks.orderByChild(KEY_DECK_UPDATE_AT)
        if (cls != null) {
            query = dbPrivatePublicDecks.orderByChild(KEY_DECK_CLASS).equalTo(cls.ordinal.toDouble())
        }
        query.addListenerForSingleValueEvent(object : ValueEventListener {

            override fun onDataChange(ds: DataSnapshot) {
                Timber.d(ds.value?.toString())
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

}