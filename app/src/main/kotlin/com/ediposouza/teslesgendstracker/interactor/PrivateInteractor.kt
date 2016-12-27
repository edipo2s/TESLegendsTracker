package com.ediposouza.teslesgendstracker.interactor

import com.ediposouza.teslesgendstracker.data.*
import com.ediposouza.teslesgendstracker.ui.base.CmdShowSnackbarMsg
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
class PrivateInteractor() : BaseInteractor() {

    private val NODE_DECKS_PRIVATE = "private"
    private val NODE_FAVORITE = "favorite"

    private val KEY_CARD_FAVORITE = "favorite"
    private val KEY_CARD_QTD = "qtd"

    private val KEY_DECK_RARITY = "rarity"
    private val KEY_DECK_OWNER = "owner"
    private val KEY_DECK_LIKES = "likes"
    private val KEY_DECK_COST = "cost"
    private val KEY_DECK_UPDATES = "updates"
    private val KEY_DECK_COMMENTS = "comments"

    private fun getUserID(): String {
        return FirebaseAuth.getInstance().currentUser?.uid ?: ""
    }

    private fun dbUser(): DatabaseReference? {
        return if (getUserID().isNotEmpty()) dbUsers.child(getUserID()) else null
    }

    private fun dbUserCards(set: CardSet, cls: Attribute): DatabaseReference? {
        val dbRef = dbUser()?.child(NODE_CARDS)?.child(set.db)?.child(cls.name.toLowerCase())
        dbRef?.keepSynced(true)
        return dbRef
    }

    fun setUserInfo() {
        val user = FirebaseAuth.getInstance().currentUser
        dbUser()?.child(NODE_USERS_INFO)?.apply {
            child(KEY_USER_NAME).setValue(user?.displayName ?: "")
            child(KEY_USER_PHOTO).setValue(user?.photoUrl.toString())
        }
    }

    fun setUserCardQtd(card: Card, qtd: Long, onComplete: () -> Unit) {
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

    fun getUserCollection(set: CardSet?, onSuccess: (Map<String, Long>) -> Unit) {
        getMapFromSets(set, onSuccess) { set, onEachSuccess ->
            dbUser()?.child(NODE_CARDS)?.child(set.db)?.addListenerForSingleValueEvent(object : ValueEventListener {

                @Suppress("UNCHECKED_CAST")
                override fun onDataChange(ds: DataSnapshot) {
                    val collection = ds.children.flatMap { it.children }
                            .filter { it.hasChild(KEY_CARD_QTD) }
                            .map({ it.key to it.child(KEY_CARD_QTD).value as Long })
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

    fun getUserCollection(set: CardSet?, attr: Attribute, onSuccess: (Map<String, Long>) -> Unit) {
        getMapFromSets(set, attr, onSuccess) { set, attr, onEachSuccess ->
            dbUserCards(set, attr)?.addListenerForSingleValueEvent(object : ValueEventListener {

                @Suppress("UNCHECKED_CAST")
                override fun onDataChange(ds: DataSnapshot) {
                    val collection = ds.children
                            .filter { it.hasChild(KEY_CARD_QTD) }
                            .map({ it.key to it.child(KEY_CARD_QTD).value as Long })
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

    private fun getOwnedPublicDecks(cls: Class?, onSuccess: (List<Deck>) -> Unit) {
        with(dbDecks.child(NODE_DECKS_PUBLIC).orderByChild(KEY_DECK_OWNER).equalTo(getUserID())) {
            keepSynced(true)
            addListenerForSingleValueEvent(object : ValueEventListener {

                override fun onDataChange(ds: DataSnapshot) {
                    Timber.d(ds.value?.toString())
                    val decks = ds.children.mapTo(arrayListOf<Deck>()) {
                        it.getValue(DeckParser::class.java).toDeck(it.key, false)
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

    private fun getOwnedPrivateDecks(cls: Class?, onSuccess: (List<Deck>) -> Unit) {
        dbUser()?.child(NODE_DECKS)?.child(NODE_DECKS_PRIVATE)?.orderByChild(KEY_DECK_UPDATE_AT)?.apply {
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

    fun getFavoriteDecks(cls: Class?, onSuccess: (List<Deck>?) -> Unit) {
        PublicInteractor().getPublicDecks(cls) {
            val publicDecks = it
            dbUser()?.child(NODE_DECKS)?.child(NODE_FAVORITE)?.apply {
                keepSynced(true)
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
        val cards = hashMapOf<String, CardRarity>()
        val userCards = hashMapOf<String, Long>()
        publicInteractor.getCards(null, Attribute.DUAL) {
            cards.putAll(it.map { it.shortName to it.rarity })
            publicInteractor.getCards(null, Attribute.NEUTRAL) {
                cards.putAll(it.map { it.shortName to it.rarity })
                publicInteractor.getCards(null, attr1) {
                    cards.putAll(it.map { it.shortName to it.rarity })
                    publicInteractor.getCards(null, attr2) {
                        cards.putAll(it.map { it.shortName to it.rarity })
                        getUserCollection(null, attr1) {
                            userCards.putAll(it)
                            getUserCollection(null, attr2) {
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
    }

    fun saveDeck(name: String, cls: Class, type: DeckType, cost: Int, patch: String, cards: Map<String, Long>,
                 private: Boolean, onError: ((e: Exception?) -> Unit)? = null, onSuccess: (uid: String) -> Unit) {
        dbUser()?.apply {
            with(if (private) child(NODE_DECKS).child(NODE_DECKS_PRIVATE) else dbDecks.child(NODE_DECKS_PUBLIC)) {
                val deck = Deck(push().key, name, getUserID(), private, type, cls, cost, LocalDateTime.now(),
                        LocalDateTime.now(), patch, ArrayList(), 0, cards, ArrayList(), ArrayList())
                child(deck.id).setValue(DeckParser().fromDeck(deck)).addOnCompleteListener({
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
                    child(deck.id).updateChildren(DeckParser().fromDeck(deck).toDeckUpdateMap()).addOnCompleteListener({
                        Timber.d(it.toString())
                        if (it.isSuccessful) onSuccess.invoke() else onError?.invoke(it.exception)
                    })
                else
                    child(deck.id).setValue(DeckParser().fromDeck(deck)).addOnCompleteListener({
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

    fun addDeckComment(deck: Deck, msg: String, onError: ((e: Exception?) -> Unit)? = null, onSuccess: () -> Unit) {
        dbUser()?.apply {
            with(if (deck.private) child(NODE_DECKS).child(NODE_DECKS_PRIVATE) else dbDecks.child(NODE_DECKS_PUBLIC)) {
                val comment = DeckParser.toNewCommentMap(getUserID(), msg)
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
            with(if (deck.private) child(NODE_DECKS).child(NODE_DECKS_PRIVATE) else dbDecks.child(NODE_DECKS_PUBLIC)) {
                child(deck.id).child(KEY_DECK_COMMENTS).child(commentId).removeValue().addOnCompleteListener({
                    Timber.d(it.toString())
                    if (it.isSuccessful) onSuccess.invoke() else onError?.invoke(it.exception)
                })
            }
        }
    }

}