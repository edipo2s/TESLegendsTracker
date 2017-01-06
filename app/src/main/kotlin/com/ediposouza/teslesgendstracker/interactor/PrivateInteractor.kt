package com.ediposouza.teslesgendstracker.interactor

import com.ediposouza.teslesgendstracker.data.*
import com.ediposouza.teslesgendstracker.ui.base.CmdShowSnackbarMsg
import com.ediposouza.teslesgendstracker.util.ConfigManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import org.greenrobot.eventbus.EventBus
import org.threeten.bp.LocalDateTime
import timber.log.Timber
import java.util.*

/**
 * Created by ediposouza on 01/11/16.
 */
class PrivateInteractor : BaseInteractor() {

    private val NODE_DECKS_PRIVATE = "private"
    private val NODE_FAVORITE = "favorite"

    private val KEY_CARD_FAVORITE = "favorite"
    private val KEY_CARD_QTD = "qtd"

    private val KEY_DECK_OWNER = "owner"
    private val KEY_DECK_LIKES = "likes"
    private val KEY_DECK_COST = "cost"
    private val KEY_DECK_UPDATES = "updates"
    private val KEY_DECK_COMMENTS = "comments"

    private fun getUserID(): String {
        return FirebaseAuth.getInstance().currentUser?.uid ?: ""
    }

    private fun dbUser(): DatabaseReference? {
        val userID = getUserID()
        return if (userID.isEmpty() || ConfigManager.isDBUpdating()) null else dbUsers.child(userID)
    }

    private fun dbUserCards(set: CardSet, cls: Attribute): DatabaseReference? {
        val dbRef = dbUser()?.child(NODE_CARDS)?.child(set.db)?.child(cls.name.toLowerCase())
        dbRef?.keepSynced()
        return dbRef
    }

    fun setUserInfo() {
        val user = FirebaseAuth.getInstance().currentUser
        dbUser()?.child(NODE_USERS_INFO)?.apply {
            child(KEY_USER_NAME).setValue(user?.displayName ?: "")
            child(KEY_USER_PHOTO).setValue(user?.photoUrl.toString())
        }
    }

    fun setUserCardQtd(card: Card, qtd: Int, onComplete: () -> Unit) {
        dbUserCards(card.set, card.attr)?.apply {
            child(card.shortName).child(KEY_CARD_QTD).setValue(qtd).addOnCompleteListener {
                onComplete.invoke()
            }
        }
    }

    fun setUserCardFavorite(card: Card, favorite: Boolean, onComplete: () -> Unit) {
        dbUserCards(card.set, card.attr)?.apply {
            child(card.shortName).child(KEY_CARD_FAVORITE).apply {
                if (favorite) {
                    setValue(true).addOnCompleteListener { onComplete.invoke() }
                } else {
                    removeValue().addOnCompleteListener { onComplete.invoke() }
                }
            }
        }
    }

    fun getUserCollection(set: CardSet?, onSuccess: (Map<String, Int>) -> Unit) {
        getMapFromSets(set, onSuccess) { set, onEachSuccess ->
            dbUser()?.child(NODE_CARDS)?.child(set.db)?.addListenerForSingleValueEvent(object : ValueEventListener {

                @Suppress("UNCHECKED_CAST")
                override fun onDataChange(ds: DataSnapshot) {
                    val collection = ds.children.flatMap { it.children }
                            .filter { it.hasChild(KEY_CARD_QTD) }
                            .map({ it.key to (it.child(KEY_CARD_QTD).value as Long).toInt() })
                            .toMap()
                    Timber.d(collection.toString())
                    onEachSuccess.invoke(collection)
                }

                override fun onCancelled(de: DatabaseError) {
                    Timber.d("Fail: " + de.message)
                }

            })
        }
    }

    fun getUserCollection(set: CardSet?, vararg attrs: Attribute, onSuccess: (Map<String, Int>) -> Unit) {
        var attrIndex = 0
        val collection = hashMapOf<String, Int>()
        if (attrs.size == 1) {
            return getUserCollection(set, attrs[0], onSuccess)
        }

        fun getUserCollectionOnSuccess(onSuccess: (Map<String, Int>) -> Unit): (Map<String, Int>) -> Unit = {
            collection.putAll(it)
            attrIndex = attrIndex.inc()
            if (attrIndex >= attrs.size) {
                onSuccess.invoke(collection)
            } else {
                getUserCollection(set, attrs[attrIndex], getUserCollectionOnSuccess(onSuccess))
            }
        }

        getUserCollection(set, attrs[attrIndex], getUserCollectionOnSuccess(onSuccess))
    }

    private fun getUserCollection(set: CardSet?, attr: Attribute, onSuccess: (Map<String, Int>) -> Unit) {
        getMapFromSets(set, attr, onSuccess) { set, attr, onEachSuccess ->
            dbUserCards(set, attr)?.addListenerForSingleValueEvent(object : ValueEventListener {

                @Suppress("UNCHECKED_CAST")
                override fun onDataChange(ds: DataSnapshot) {
                    val collection = ds.children
                            .filter { it.hasChild(KEY_CARD_QTD) }
                            .map({ it.key to (it.child(KEY_CARD_QTD).value as Long).toInt() })
                            .toMap()
                    Timber.d(collection.toString())
                    onEachSuccess.invoke(collection)
                }

                override fun onCancelled(de: DatabaseError) {
                    Timber.d("Fail: " + de.message)
                }

            })
        }
    }

    fun getFavoriteCards(set: CardSet?, attr: Attribute, onSuccess: (List<String>) -> Unit) {
        getListFromSets(set, attr, onSuccess) { set, attr, onEachSuccess ->
            dbUserCards(set, attr)?.addListenerForSingleValueEvent(object : ValueEventListener {

                @Suppress("UNCHECKED_CAST")
                override fun onDataChange(ds: DataSnapshot) {
                    val favorites = ds.children.filter { (it.child(KEY_CARD_FAVORITE)?.value ?: false) as Boolean }
                            .map({ it.key })
                    Timber.d(favorites.toString())
                    onEachSuccess.invoke(favorites)
                }

                override fun onCancelled(de: DatabaseError) {
                    Timber.d("Fail: " + de.message)
                }

            })
        }
    }

    fun getOwnedDecks(cls: Class?, onSuccess: (List<Deck>) -> Unit) {
        getOwnedPublicDecks(cls) {
            val decks = it
            getOwnedPrivateDecks(cls) {
                onSuccess.invoke(decks.plus(it))
            }
        }
    }

    fun getOwnedPublicDecksRef() = dbDecks.child(NODE_DECKS_PUBLIC)
            .orderByChild(KEY_DECK_OWNER).equalTo(getUserID())?.apply {
        keepSynced()
    }

    private fun getOwnedPublicDecks(cls: Class?, onSuccess: (List<Deck>) -> Unit) {
        getOwnedPublicDecksRef()?.apply {
            keepSynced()
            addListenerForSingleValueEvent(object : ValueEventListener {

                override fun onDataChange(ds: DataSnapshot) {
                    Timber.d(ds.value?.toString())
                    val decks = ds.children.mapTo(arrayListOf<Deck>()) {
                        it.getValue(FirebaseParsers.DeckParser::class.java).toDeck(it.key, false)
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

    fun getOwnedPrivateDecksRef() = dbUser()?.child(NODE_DECKS)?.child(NODE_DECKS_PRIVATE)
            ?.orderByChild(KEY_DECK_UPDATE_AT)?.apply {
        keepSynced()
    }

    private fun getOwnedPrivateDecks(cls: Class?, onSuccess: (List<Deck>) -> Unit) {
        getOwnedPrivateDecksRef()?.apply {
            keepSynced()
            addListenerForSingleValueEvent(object : ValueEventListener {

                override fun onDataChange(ds: DataSnapshot) {
                    val decks = ds.children.mapTo(arrayListOf<Deck>()) {
                        it.getValue(FirebaseParsers.DeckParser::class.java).toDeck(it.key, true)
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

    fun getFavoriteDecks(cls: Class?, onSuccess: (List<Deck>?) -> Unit) {
        PublicInteractor().getPublicDecks(cls) {
            val publicDecks = it
            dbUser()?.child(NODE_DECKS)?.child(NODE_FAVORITE)?.apply {
                keepSynced()
                addListenerForSingleValueEvent(object : ValueEventListener {

                    override fun onDataChange(ds: DataSnapshot) {
                        Timber.d(ds.value?.toString())
                        val decks = ds.children.map {
                            val deckId = it.key
                            publicDecks.find { it.id == deckId } ?: Deck()
                        }.filter { it.cost > 0 }.filter { cls == null || it.cls == cls }
                        Timber.d(decks.toString())
                        onSuccess.invoke(decks)
                    }

                    override fun onCancelled(de: DatabaseError) {
                        Timber.d("Fail: " + de.message)
                    }

                })
            } ?: onSuccess.invoke(listOf())
        }
    }

    fun getMissingCards(deck: Deck, onError: ((e: Exception?) -> Unit)? = null, onSuccess: (List<CardMissing>) -> Unit) {
        val publicInteractor = PublicInteractor()
        val attr1 = deck.cls.attr1
        val attr2 = deck.cls.attr2
        publicInteractor.getCards(null, attr1, attr2, Attribute.DUAL, Attribute.NEUTRAL) {
            val cards = it.map { it.shortName to it.rarity }.toMap()
            getUserCollection(null, attr1, attr2, Attribute.DUAL, Attribute.NEUTRAL) {
                val userCards = it
                val missing = deck.cards.map { it.key to it.value.minus(userCards[it.key] ?: 0) }
                        .filter { it.second > 0 }
                        .map { CardMissing(it.first, cards[it.first]!!, it.second) }
                        .filter { it.qtd > 0 }
                Timber.d(missing.toString())
                onSuccess.invoke(missing)
            }
        }
    }

    fun saveDeck(name: String, cls: Class, type: DeckType, cost: Int, patch: String, cards: Map<String, Int>,
                 private: Boolean, onError: ((e: Exception?) -> Unit)? = null, onSuccess: (uid: String) -> Unit) {
        dbUser()?.apply {
            with(if (private) child(NODE_DECKS).child(NODE_DECKS_PRIVATE) else dbDecks.child(NODE_DECKS_PUBLIC)) {
                val deck = Deck(push().key, name, getUserID(), private, type, cls, cost, LocalDateTime.now(),
                        LocalDateTime.now(), patch, ArrayList(), 0, cards, ArrayList(), ArrayList())
                child(deck.id).setValue(FirebaseParsers.DeckParser().fromDeck(deck)).addOnCompleteListener({
                    Timber.d(it.toString())
                    if (it.isSuccessful) onSuccess.invoke(deck.id)
                    else {
                        val errorMsg = it.exception?.message ?: it.exception.toString()
                        EventBus.getDefault().post(CmdShowSnackbarMsg(CmdShowSnackbarMsg.TYPE_ERROR, errorMsg))
                        onError?.invoke(it.exception)
                    }
                })
            }
        }
    }

    fun setUserDeckFavorite(deck: Deck, favorite: Boolean, onError: ((e: Exception?) -> Unit)? = null, onSuccess: () -> Unit) {
        dbUser()?.child(NODE_DECKS)?.child(NODE_FAVORITE)?.apply {
            if (favorite) {
                child(deck.id)?.setValue(true)?.addOnCompleteListener { onSuccess.invoke() }
            } else {
                child(deck.id)?.removeValue()?.addOnCompleteListener { onSuccess.invoke() }
            }
        }
    }

    /**
     * Name, Type, Class, Patch, Private
     */
    fun updateDeck(deck: Deck, oldPrivate: Boolean, onError: ((e: Exception?) -> Unit)? = null, onSuccess: () -> Unit) {
        dbUser()?.apply {
            with(if (deck.private) child(NODE_DECKS).child(NODE_DECKS_PRIVATE) else dbDecks.child(NODE_DECKS_PUBLIC)) {
                if (deck.private == oldPrivate)
                    child(deck.id).updateChildren(FirebaseParsers.DeckParser().fromDeck(deck).toDeckUpdateMap()).addOnCompleteListener({
                        Timber.d(it.toString())
                        if (it.isSuccessful) onSuccess.invoke() else onError?.invoke(it.exception)
                    })
                else
                    child(deck.id).setValue(FirebaseParsers.DeckParser().fromDeck(deck)).addOnCompleteListener({
                        Timber.d(it.toString())
                        deleteDeck(deck, oldPrivate, onError, onSuccess)
                    })
            }
        }
    }

    fun updateDeckCards(deck: Deck, oldCards: Map<String, Int>, cost: Int, onSuccess: () -> Unit,
                        onError: ((e: Exception?) -> Unit)? = null) {
        dbUser()?.apply {
            with(if (deck.private) child(NODE_DECKS).child(NODE_DECKS_PRIVATE) else dbDecks.child(NODE_DECKS_PUBLIC)) {
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

    fun deleteDeck(deck: Deck, private: Boolean, onError: ((e: Exception?) -> Unit)? = null, onSuccess: () -> Unit) {
        dbUser()?.apply {
            with(if (private) child(NODE_DECKS).child(NODE_DECKS_PRIVATE) else dbDecks.child(NODE_DECKS_PUBLIC)) {
                child(deck.id).removeValue().addOnCompleteListener({
                    Timber.d(it.toString())
                    if (it.isSuccessful) onSuccess.invoke() else onError?.invoke(it.exception)
                })
            }
        }
    }

    fun setUserDeckLike(deck: Deck, like: Boolean, onError: ((e: Exception?) -> Unit)? = null, onSuccess: () -> Unit) {
        dbUser()?.apply {
            with(if (deck.private) child(NODE_DECKS).child(NODE_DECKS_PRIVATE) else dbDecks.child(NODE_DECKS_PUBLIC)) {
                val deckLikesUpdated = if (like) deck.likes.plus(getUserID()) else deck.likes.minus(getUserID())
                child(deck.id).updateChildren(mapOf(KEY_DECK_LIKES to deckLikesUpdated)).addOnCompleteListener({
                    Timber.d(it.toString())
                    if (it.isSuccessful) onSuccess.invoke() else onError?.invoke(it.exception)
                })
            }
        }
    }

    fun addDeckComment(deck: Deck, msg: String, onError: ((e: Exception?) -> Unit)? = null,
                       onSuccess: (comment: DeckComment) -> Unit) {
        dbUser()?.apply {
            with(if (deck.private) child(NODE_DECKS).child(NODE_DECKS_PRIVATE) else dbDecks.child(NODE_DECKS_PUBLIC)) {
                val comment = FirebaseParsers.DeckParser.toNewCommentMap(getUserID(), msg)
                with(child(deck.id).child(KEY_DECK_COMMENTS)) {
                    val commentKey = push().key
                    child(commentKey).setValue(comment).addOnCompleteListener({
                        Timber.d(it.toString())
                        if (it.isSuccessful) {
                            onSuccess.invoke(DeckComment(commentKey, getUserID(), msg, LocalDateTime.now()))
                        } else
                            onError?.invoke(it.exception)
                    })
                }
            }
        }
    }

    fun remDeckComment(deck: Deck, commentId: String, onError: ((e: Exception?) -> Unit)? = null, onSuccess: () -> Unit) {
        dbUser()?.apply {
            with(if (deck.private) child(NODE_DECKS).child(NODE_DECKS_PRIVATE) else dbDecks.child(NODE_DECKS_PUBLIC)) {
                child(deck.id).child(KEY_DECK_COMMENTS).child(commentId).removeValue().addOnCompleteListener({
                    Timber.d(it.toString())
                    if (it.isSuccessful) onSuccess.invoke() else onError?.invoke(it.exception)
                })
            }
        }
    }

}