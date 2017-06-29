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

/**
 * Created by ediposouza on 01/11/16.
 */
object PrivateInteractor : BaseInteractor() {

    private val NODE_DECKS_PRIVATE = "private"
    private val NODE_FAVORITE = "favorite"
    private val NODE_MATCHES = "matches"

    private val KEY_CARD_FAVORITE = "favorite"
    private val KEY_CARD_QTD = "qtd"

    private val KEY_DECK_OWNER = "owner"
    private val KEY_DECK_LIKES = "likes"
    private val KEY_DECK_CARDS = "cards"
    private val KEY_DECK_COST = "cost"
    private val KEY_DECK_UPDATES = "updates"
    private val KEY_DECK_COMMENTS = "comments"

    private val KEY_MATCH_SEASON = "season"

    private fun getUserID(): String {
        return FirebaseAuth.getInstance().currentUser?.uid ?: ""
    }

    private fun dbUser(): DatabaseReference? {
        val userID = getUserID()
        return if (userID.isEmpty() || ConfigManager.isDBUpdating()) null else dbUsers.child(userID)
    }

    private fun dbUserCards(set: CardSet, cls: CardAttribute): DatabaseReference? {
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

    fun setUserCardFavorite(card: Card, favorite: Boolean, onSuccess: () -> Unit) {
        dbUserCards(card.set, card.attr)?.apply {
            child(card.shortName).apply {
                execSetUserCardFavorite(this, favorite, onSuccess)
            }
        }
    }

    fun setUserCardRating(card: Card, review: Int, onComplete: () -> Unit) {
        val userUid = FirebaseAuth.getInstance().currentUser?.uid ?: ""
        val attr = card.attr.name.toLowerCase()
        database.child(NODE_REVIEWS).child(card.set.db).child(attr).child(card.shortName)
                .updateChildren(mapOf(userUid to review), object : DatabaseReference.CompletionListener {
                    override fun onComplete(p0: DatabaseError?, p1: DatabaseReference?) {
                        onComplete()
                    }

                })
    }

    private fun execSetUserCardFavorite(dr: DatabaseReference, favorite: Boolean, onSuccess: () -> Unit) {
        val childEventListener = object : SimpleChildEventListener() {
            override fun onChildAdded(snapshot: DataSnapshot?, previousChildName: String?) {
                Timber.d(snapshot.toString())
                if (snapshot?.key == KEY_CARD_FAVORITE) {
                    dr.removeEventListener(this)
                    onSuccess.invoke()
                }
            }

            override fun onChildRemoved(snapshot: DataSnapshot?) {
                dr.removeEventListener(this)
                Timber.d(snapshot.toString())
                onSuccess.invoke()
            }
        }
        dr.addChildEventListener(childEventListener)
        if (favorite) {
            dr.child(KEY_CARD_FAVORITE).setValue(true).addOnFailureListener { dr.removeEventListener(childEventListener) }
        } else {
            dr.child(KEY_CARD_FAVORITE).removeValue().addOnFailureListener { dr.removeEventListener(childEventListener) }
        }
    }

    fun getUserCollection(set: CardSet?, onSuccess: (Map<String, Int>) -> Unit) {
        getMapFromSets(set, onSuccess) { set, onEachSuccess ->
            dbUser()?.child(NODE_CARDS)?.child(set.db)?.addListenerForSingleValueEvent(object : ValueEventListener {

                @Suppress("UNCHECKED_CAST")
                override fun onDataChange(ds: DataSnapshot) {
                    val collection = ds.children.flatMap { it.children }
                            .filter { it.hasChild(KEY_CARD_QTD) && (it.child(KEY_CARD_QTD).value as Long) > 0 }
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

    fun getUserCollection(set: CardSet?, vararg attrs: CardAttribute, onSuccess: (Map<String, Int>) -> Unit) {
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

    private fun getUserCollection(set: CardSet?, attr: CardAttribute, onSuccess: (Map<String, Int>) -> Unit) {
        getMapFromSets(set, attr, onSuccess) { set, attr, onEachSuccess ->
            dbUserCards(set, attr)?.addListenerForSingleValueEvent(object : ValueEventListener {

                @Suppress("UNCHECKED_CAST")
                override fun onDataChange(ds: DataSnapshot) {
                    val collection = ds.children
                            .filter { it.hasChild(KEY_CARD_QTD) && (it.child(KEY_CARD_QTD).value as Long) > 0 }
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

    fun getUserFavoriteCards(set: CardSet?, attr: CardAttribute, onSuccess: (List<String>) -> Unit) {
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

    fun isUserCardFavorite(card: Card, onSuccess: (Boolean) -> Unit) {
        dbUserCards(card.set, card.attr)?.child(card.shortName)?.
                addListenerForSingleValueEvent(object : ValueEventListener {

                    @Suppress("UNCHECKED_CAST")
                    override fun onDataChange(ds: DataSnapshot) {
                        val isFavorite = ds.child(KEY_CARD_FAVORITE)?.value as? Boolean ?: false
                        onSuccess.invoke(isFavorite)
                    }

                    override fun onCancelled(de: DatabaseError) {
                        Timber.d("Fail: " + de.message)
                    }

                })
    }

    fun getUserPublicDecksRef() = dbDecks.child(NODE_DECKS_PUBLIC)
            .orderByChild(KEY_DECK_OWNER).equalTo(getUserID())?.apply {
        keepSynced()
    }

    fun getUserPrivateDecksRef() = dbUser()?.child(NODE_DECKS)?.child(NODE_DECKS_PRIVATE)
            ?.orderByChild(KEY_DECK_UPDATE_AT)?.apply {
        keepSynced()
    }

    fun getUserFavoriteDecksRef() = dbUser()?.child(NODE_DECKS)?.child(NODE_FAVORITE)?.apply {
        keepSynced()
    }

    fun getUserDecks(cls: DeckClass?, onSuccess: (List<Deck>) -> Unit) {
        getUserPublicDecks(cls) { decks ->
            getUserPrivateDecks(cls) {
                onSuccess.invoke(decks.plus(it))
            }
        }
    }

    private fun getUserPublicDecks(cls: DeckClass?, onSuccess: (List<Deck>) -> Unit) {
        getUserPublicDecksRef()?.addListenerForSingleValueEvent(object : ValueEventListener {

            override fun onDataChange(ds: DataSnapshot) {
                Timber.d(ds.value?.toString())
                val decks = ds.children.mapTo(arrayListOf<Deck?>()) {
                    it.getValue(FirebaseParsers.DeckParser::class.java)?.toDeck(it.key, false)
                }.filterNotNull().filter { cls == null || it.cls == cls }
                Timber.d(decks.toString())
                onSuccess.invoke(decks)
            }

            override fun onCancelled(de: DatabaseError) {
                Timber.d("Fail: " + de.message)
            }

        })
    }

    private fun getUserPrivateDecks(cls: DeckClass?, onSuccess: (List<Deck>) -> Unit) {
        getUserPrivateDecksRef()?.addListenerForSingleValueEvent(object : ValueEventListener {

            override fun onDataChange(ds: DataSnapshot) {
                val decks = ds.children.mapTo(arrayListOf<Deck?>()) {
                    it.getValue(FirebaseParsers.DeckParser::class.java)?.toDeck(it.key, true)
                }.filterNotNull().filter { cls == null || it.cls == cls }
                Timber.d(decks.toString())
                onSuccess.invoke(decks)
            }

            override fun onCancelled(de: DatabaseError) {
                Timber.d("Fail: " + de.message)
            }

        })
    }

    fun getUserFavoriteDecks(cls: DeckClass?, onSuccess: (List<Deck>?) -> Unit) {
        PublicInteractor.getPublicDecks(cls) { publicDecks ->
            getUserFavoriteDecksRef()?.apply {
                addListenerForSingleValueEvent(object : ValueEventListener {

                    override fun onDataChange(ds: DataSnapshot) {
                        Timber.d(ds.value?.toString())
                        val decks = ds.children.map {
                            val deckId = it.key
                            publicDecks.find { it.uuid == deckId } ?: Deck()
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

    fun setUserDeckFavorite(deck: Deck, favorite: Boolean, onError: ((e: Exception?) -> Unit)? = null, onSuccess: () -> Unit) {
        dbUser()?.child(NODE_DECKS)?.child(NODE_FAVORITE)?.apply {
            val childEventListener = object : SimpleChildEventListener() {
                override fun onChildAdded(snapshot: DataSnapshot?, previousChildName: String?) {
                    Timber.d(snapshot.toString())
                    if (snapshot?.key == deck.uuid) {
                        removeEventListener(this)
                        onSuccess.invoke()
                    }
                }

                override fun onChildRemoved(snapshot: DataSnapshot?) {
                    removeEventListener(this)
                    Timber.d(snapshot.toString())
                    onSuccess.invoke()
                }
            }
            addChildEventListener(childEventListener)
            if (favorite) {
                val deckFavorite = FirebaseParsers.DeckFavoriteParser(deck.name, deck.cls.ordinal)
                child(deck.uuid)?.setValue(deckFavorite)?.addOnFailureListener {
                    removeEventListener(childEventListener)
                    onError?.invoke(it)
                }
            } else {
                child(deck.uuid)?.removeValue()?.addOnFailureListener {
                    removeEventListener(childEventListener)
                    onError?.invoke(it)
                }
            }
        }
    }

    fun setUserDeckLike(deck: Deck, like: Boolean, onError: ((e: Exception?) -> Unit)? = null, onSuccess: () -> Unit) {
        dbUser()?.apply {
            with(if (deck.private) child(NODE_DECKS).child(NODE_DECKS_PRIVATE) else dbDecks.child(NODE_DECKS_PUBLIC)) {
                val deckLikesUpdated = deck.likes.plus(getUserID()).takeIf { like } ?: deck.likes.minus(getUserID())
                val childEventListener = object : SimpleChildEventListener() {
                    override fun onChildChanged(snapshot: DataSnapshot?, previousChildName: String?) {
                        Timber.d(snapshot.toString())
                        if (snapshot?.key == deck.uuid) {
                            removeEventListener(this)
                            onSuccess.invoke()
                        }
                    }
                }
                addChildEventListener(childEventListener)
                child(deck.uuid).updateChildren(mapOf(KEY_DECK_LIKES to deckLikesUpdated)).addOnFailureListener({
                    removeEventListener(childEventListener)
                    onError?.invoke(it)
                })
            }
        }
    }

    fun getDeckMissingCards(deck: Deck, onError: ((e: Exception?) -> Unit)? = null, onSuccess: (List<CardMissing>) -> Unit) {
        val attr1 = deck.cls.attr1
        val attr2 = deck.cls.attr2
        PublicInteractor.getCards(null, attr1, attr2, CardAttribute.DUAL, CardAttribute.NEUTRAL) {
            val cards = it.map { it.shortName to it.rarity }.toMap()
            getUserCollection(null, attr1, attr2, CardAttribute.DUAL, CardAttribute.NEUTRAL) { userCards ->
                val missing = deck.cards.map { it.key to it.value.minus(userCards[it.key] ?: 0) }
                        .filter { it.second > 0 }
                        .map { CardMissing(it.first, cards[it.first]!!, it.second) }
                        .filter { it.qtd > 0 }
                Timber.d(missing.toString())
                onSuccess.invoke(missing)
            }
        }
    }

    fun saveDeck(name: String, cls: DeckClass, type: DeckType, cost: Int, patch: String, cards: Map<String, Int>,
                 private: Boolean, owner: String = getUserID(), onError: ((e: Exception?) -> Unit)? = null, onSuccess: (deck: Deck) -> Unit) {
        dbUser()?.apply {
            with(if (private) child(NODE_DECKS).child(NODE_DECKS_PRIVATE) else dbDecks.child(NODE_DECKS_PUBLIC)) {
                val deck = Deck(push().key, name, owner, private, type, cls, cost, LocalDateTime.now().withNano(0),
                        LocalDateTime.now().withNano(0), patch, listOf(), 0, cards, listOf(), listOf())
                val childEventListener = object : SimpleChildEventListener() {
                    override fun onChildAdded(snapshot: DataSnapshot?, previousChildName: String?) {
                        Timber.d(snapshot.toString())
                        if (snapshot?.key == deck.uuid) {
                            removeEventListener(this)
                            onSuccess.invoke(deck)
                        }
                    }
                }
                addChildEventListener(childEventListener)
                child(deck.uuid).setValue(FirebaseParsers.DeckParser().fromDeck(deck)).addOnFailureListener({
                    removeEventListener(childEventListener)
                    EventBus.getDefault().post(CmdShowSnackbarMsg(CmdShowSnackbarMsg.TYPE_ERROR, it.message ?: ""))
                    onError?.invoke(it)
                })
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
                    child(deck.uuid).updateChildren(FirebaseParsers.DeckParser().fromDeck(deck)
                            .toDeckUpdateMap()).addOnCompleteListener({
                        Timber.d(it.toString())
                        if (it.isSuccessful) onSuccess.invoke() else onError?.invoke(it.exception)
                    })
                else
                    child(deck.uuid).setValue(FirebaseParsers.DeckParser().fromDeck(deck)).addOnCompleteListener({
                        Timber.d(it.toString())
                        deleteDeck(deck, oldPrivate, onError, onSuccess)
                    })
            }
        }
    }

    fun updateDeckCards(deck: Deck, oldCards: Map<String, Int>, cost: Int, onError: ((e: Exception?) -> Unit)? = null,
                        onSuccess: () -> Unit) {
        dbUser()?.apply {
            with(if (deck.private) child(NODE_DECKS).child(NODE_DECKS_PRIVATE) else dbDecks.child(NODE_DECKS_PUBLIC)) {
                val updateKey = LocalDateTime.now().withNano(0).toString()
                val cardsRem = oldCards.filter { !deck.cards.keys.contains(it.key) }.mapValues { it.value * -1 }
                val cardsDiff = deck.cards.mapValues { it.value.minus(oldCards[it.key] ?: 0) }
                        .filter { it.value != 0 }.plus(cardsRem)
                child(deck.uuid).child(KEY_DECK_UPDATES).child(updateKey).setValue(cardsDiff).addOnCompleteListener({
                    Timber.d(it.toString())
                    if (it.isSuccessful) onSuccess.invoke() else onError?.invoke(it.exception)
                })
                child(deck.uuid).child(KEY_DECK_CARDS).setValue(deck.cards)
                child(deck.uuid).child(KEY_DECK_COST).setValue(cost)
            }
        }
    }

    fun addDeckComment(deck: Deck, msg: String, onError: ((e: Exception?) -> Unit)? = null,
                       onSuccess: (comment: DeckComment) -> Unit) {
        dbUser()?.apply {
            with(if (deck.private) child(NODE_DECKS).child(NODE_DECKS_PRIVATE) else dbDecks.child(NODE_DECKS_PUBLIC)) {
                val comment = FirebaseParsers.DeckParser.toNewCommentMap(getUserID(), msg)
                with(child(deck.uuid).child(KEY_DECK_COMMENTS)) {
                    val commentKey = push().key
                    val childEventListener = object : SimpleChildEventListener() {
                        override fun onChildAdded(snapshot: DataSnapshot?, previousChildName: String?) {
                            Timber.d(snapshot.toString())
                            if (snapshot?.key == commentKey) {
                                removeEventListener(this)
                                onSuccess.invoke(DeckComment(commentKey, getUserID(), msg, LocalDateTime.now().withNano(0)))
                            }
                        }
                    }
                    addChildEventListener(childEventListener)
                    child(commentKey).setValue(comment).addOnFailureListener({
                        removeEventListener(childEventListener)
                        onError?.invoke(it)
                    })
                }
            }
        }
    }

    fun remDeckComment(deck: Deck, commentId: String, onError: ((e: Exception?) -> Unit)? = null, onSuccess: () -> Unit) {
        dbUser()?.apply {
            with(if (deck.private) child(NODE_DECKS).child(NODE_DECKS_PRIVATE) else dbDecks.child(NODE_DECKS_PUBLIC)) {
                child(deck.uuid).child(KEY_DECK_COMMENTS).apply {
                    val childEventListener = object : SimpleChildEventListener() {
                        override fun onChildRemoved(snapshot: DataSnapshot?) {
                            removeEventListener(this)
                            Timber.d(snapshot.toString())
                            onSuccess.invoke()
                        }
                    }
                    addChildEventListener(childEventListener)
                    child(commentId).removeValue().addOnFailureListener({
                        removeEventListener(childEventListener)
                        onError?.invoke(it)
                    })
                }
            }
        }
    }

    fun deleteDeck(deck: Deck, private: Boolean, onError: ((e: Exception?) -> Unit)? = null, onSuccess: () -> Unit) {
        dbUser()?.apply {
            with(if (private) child(NODE_DECKS).child(NODE_DECKS_PRIVATE) else dbDecks.child(NODE_DECKS_PUBLIC)) {
                val childEventListener = object : SimpleChildEventListener() {
                    override fun onChildRemoved(snapshot: DataSnapshot?) {
                        removeEventListener(this)
                        Timber.d(snapshot.toString())
                        onSuccess.invoke()
                    }
                }
                addChildEventListener(childEventListener)
                child(deck.uuid).removeValue().addOnFailureListener({
                    removeEventListener(childEventListener)
                    onError?.invoke(it)
                })
            }
        }
    }

    fun getUserMatchesRef() = dbUser()?.child(NODE_MATCHES)?.apply {
        keepSynced()
    }

    fun getUserMatches(season: Season?, mode: MatchMode? = null, onError: ((e: Exception?) -> Unit)? = null, onSuccess: (List<Match>) -> Unit) {
        getUserMatchesRef()?.apply {
            val query = orderByChild(KEY_MATCH_SEASON).equalTo(season?.uuid)
            (if (season != null) query else this).addListenerForSingleValueEvent(object : ValueEventListener {

                override fun onDataChange(ds: DataSnapshot) {
                    val matches = ds.children.mapTo(arrayListOf<Match?>()) {
                        it.getValue(FirebaseParsers.MatchParser::class.java)?.toMatch(it.key)
                    }.filterNotNull().filter { mode == null || it.mode == mode }
                    Timber.d(matches.toString())
                    onSuccess.invoke(matches)
                }

                override fun onCancelled(de: DatabaseError) {
                    Timber.d("Fail: " + de.message)
                    onError?.invoke(de.toException())
                }

            })
        } ?: onError?.invoke(null)
    }

    fun saveMatch(newMatch: Match, onError: ((e: Exception?) -> Unit)? = null, onSuccess: () -> Unit) {
        getUserMatchesRef()?.apply {
            val childEventListener = object : SimpleChildEventListener() {
                override fun onChildAdded(snapshot: DataSnapshot?, previousChildName: String?) {
                    Timber.d(snapshot.toString())
                    if (snapshot?.key == newMatch.uuid) {
                        removeEventListener(this)
                        onSuccess.invoke()
                    }
                }
            }
            addChildEventListener(childEventListener)
            child(newMatch.uuid).setValue(FirebaseParsers.MatchParser().fromMatch(newMatch)).addOnFailureListener {
                removeEventListener(childEventListener)
                onError?.invoke(it)
            }
        }
    }

    fun deleteMatch(match: Match, onError: ((e: Exception?) -> Unit)? = null, onSuccess: () -> Unit) {
        getUserMatchesRef()?.apply {
            child(match.uuid).removeValue().addOnCompleteListener {
                if (it.isSuccessful) {
                    Timber.d(it.toString())
                    onSuccess.invoke()
                } else
                    onError?.invoke(it.exception)
            }
        }
    }

}