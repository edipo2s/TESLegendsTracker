package com.ediposouza.teslesgendstracker.interactor

import com.ediposouza.teslesgendstracker.data.*
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import org.threeten.bp.Month
import org.threeten.bp.format.TextStyle
import timber.log.Timber
import java.util.*

/**
 * Created by ediposouza on 01/11/16.
 */
class PublicInteractor : BaseInteractor() {

    private val KEY_CARD_EVOLVES = "evolves"
    private val KEY_DECK_VIEWS = "views"

    fun getCards(set: CardSet?, onSuccess: (List<Card>) -> Unit) {
        getListFromSets(set, onSuccess) { set, onEachSuccess ->
            database.child(NODE_CARDS).child(set.db).orderByChild(KEY_CARD_COST)
                    .addListenerForSingleValueEvent(object : ValueEventListener {

                        override fun onDataChange(ds: DataSnapshot) {
                            val cards = ds.children.map {
                                val attr = Attribute.valueOf(it.key.toUpperCase())
                                it.children.map {
                                    it.getValue(FirebaseParsers.CardParser::class.java).toCard(it.key, set, attr)
                                }
                            }.flatMap { it }
                            onEachSuccess.invoke(cards)
                        }

                        override fun onCancelled(de: DatabaseError) {
                            Timber.d("Fail: " + de.message)
                        }

                    })
        }
    }

    fun getCards(set: CardSet?, vararg attrs: Attribute, onSuccess: (List<Card>) -> Unit) {
        var attrIndex = 0
        val cards = arrayListOf<Card>()
        if (attrs.size == 1) {
            return getCards(set, attrs[0], onSuccess)
        }

        fun getCardsOnSuccess(onSuccess: (List<Card>) -> Unit): (List<Card>) -> Unit = {
            cards.addAll(it)
            attrIndex = attrIndex.inc()
            if (attrIndex >= attrs.size) {
                onSuccess.invoke(cards)
            } else {
                getCards(set, attrs[attrIndex], getCardsOnSuccess(onSuccess))
            }
        }

        getCards(set, attrs[attrIndex], getCardsOnSuccess(onSuccess))
    }

    private fun getCards(set: CardSet?, attr: Attribute, onSuccess: (List<Card>) -> Unit) {
        val onFinalSuccess: (List<Card>) -> Unit = { onSuccess(it.sorted()) }
        getListFromSets(set, attr, onFinalSuccess) { set, attr, onEachSuccess ->
            val node_attr = attr.name.toLowerCase()
            database.child(NODE_CARDS).child(set.db).child(node_attr).orderByChild(KEY_CARD_COST)
                    .addListenerForSingleValueEvent(object : ValueEventListener {

                        override fun onDataChange(ds: DataSnapshot) {
                            val cards = ds.children.mapTo(arrayListOf()) {
                                it.getValue(FirebaseParsers.CardParser::class.java).toCard(it.key, set, attr)
                            }
                            Timber.d(cards.toString())
                            onEachSuccess.invoke(cards)
                        }

                        override fun onCancelled(de: DatabaseError) {
                            Timber.d("Fail: " + de.message)
                        }

                    })
        }
    }

    fun getCardsForStatistics(set: CardSet?, onSuccess: (List<CardStatistic>) -> Unit) {
        getListFromSets(set, onSuccess) { set, onEachSuccess ->
            database.child(NODE_CARDS).child(set.db).orderByChild(KEY_CARD_COST)
                    .addListenerForSingleValueEvent(object : ValueEventListener {

                        override fun onDataChange(ds: DataSnapshot) {
                            val cards = ds.children.flatMap { it.children }
                                    .filter { !it.hasChild(KEY_CARD_EVOLVES) }
                                    .mapTo(arrayListOf()) {
                                        it.getValue(FirebaseParsers.CardParser::class.java).toCardStatistic(it.key)
                                    }
                            Timber.d(cards.toString())
                            onEachSuccess.invoke(cards)
                        }

                        override fun onCancelled(de: DatabaseError) {
                            Timber.d("Fail: " + de.message)
                        }

                    })
        }
    }

    fun getCardsForStatistics(set: CardSet?, attr: Attribute, onSuccess: (List<CardStatistic>) -> Unit) {
        getListFromSets(set, attr, onSuccess) { set, attr, onEachSuccess ->
            val node_attr = attr.name.toLowerCase()
            database.child(NODE_CARDS).child(set.db).child(node_attr).orderByChild(KEY_CARD_COST)
                    .addListenerForSingleValueEvent(object : ValueEventListener {

                        override fun onDataChange(ds: DataSnapshot) {
                            val cards = ds.children
                                    .filter { !it.hasChild(KEY_CARD_EVOLVES) }
                                    .mapTo(arrayListOf()) {
                                        it.getValue(FirebaseParsers.CardParser::class.java).toCardStatistic(it.key)
                                    }
                            Timber.d(cards.toString())
                            onEachSuccess.invoke(cards)
                        }

                        override fun onCancelled(de: DatabaseError) {
                            Timber.d("Fail: " + de.message)
                        }

                    })
        }
    }

    fun getPublicDeck(uuid: String, onSuccess: (Deck) -> Unit) {
        with(dbDecks.child(NODE_DECKS_PUBLIC).child(uuid)) {
            keepSynced()
            addListenerForSingleValueEvent(object : ValueEventListener {

                override fun onDataChange(ds: DataSnapshot) {
                    val value = ds.getValue(FirebaseParsers.DeckParser::class.java)
                    val deck = value?.toDeck(ds.key, false)
                    if (deck != null) {
                        Timber.d(deck.toString())
                        onSuccess.invoke(deck)
                    }
                }

                override fun onCancelled(de: DatabaseError) {
                    Timber.d("Fail: " + de.message)
                }

            })
        }
    }

    fun getPublicDecksRef() = dbDecks.child(NODE_DECKS_PUBLIC)
            .orderByChild(KEY_DECK_UPDATE_AT)?.apply {
        keepSynced()
    }

    fun getPublicDecks(cls: Class?, onSuccess: (List<Deck>) -> Unit) {
        val dbPublicDeck = dbDecks.child(NODE_DECKS_PUBLIC)
        dbPublicDeck.keepSynced()
        var query = dbPublicDeck.orderByChild(KEY_DECK_UPDATE_AT)
        if (cls != null) {
            query = dbPublicDeck.orderByChild(KEY_DECK_CLASS).equalTo(cls.ordinal.toDouble())
        }
        query.addListenerForSingleValueEvent(object : ValueEventListener {

            override fun onDataChange(ds: DataSnapshot) {
                Timber.d(ds.value?.toString())
                val decks = ds.children.mapTo(arrayListOf<Deck>()) {
                    it.getValue(FirebaseParsers.DeckParser::class.java).toDeck(it.key, false)
                }
                Timber.d(decks.toString())
                onSuccess.invoke(decks)
            }

            override fun onCancelled(de: DatabaseError) {
                Timber.d("Fail: " + de.message)
            }

        })
    }

    fun incDeckView(deck: Deck, onError: ((e: Exception?) -> Unit)? = null, onSuccess: (Int) -> Unit) {
        with(dbDecks.child(NODE_DECKS_PUBLIC)) {
            val views = deck.views.inc()
            child(deck.uuid).updateChildren(mapOf(KEY_DECK_VIEWS to views)).addOnCompleteListener({
                Timber.d(it.toString())
                if (it.isSuccessful) onSuccess.invoke(views) else onError?.invoke(it.exception)
            })
        }
    }

    fun getDeckCards(deck: Deck, onError: ((e: Exception?) -> Unit)? = null, onSuccess: (List<CardSlot>) -> Unit) {
        with(database.child(NODE_CARDS)) {
            getCards(null, deck.cls.attr1, deck.cls.attr2, Attribute.DUAL, Attribute.NEUTRAL) {
                val deckCards = it.map { CardSlot(it, deck.cards[it.shortName] ?: 0) }
                        .filter { it.qtd > 0 }
                Timber.d(deckCards.toString())
                onSuccess(deckCards)
            }
        }
    }

    fun getPatches(onError: ((e: Exception?) -> Unit)? = null, onSuccess: (List<Patch>) -> Unit) {
        database.child(NODE_PATCHES).addListenerForSingleValueEvent(object : ValueEventListener {

            override fun onDataChange(ds: DataSnapshot) {
                val patches = ds.children.mapTo(arrayListOf()) {
                    it.getValue(FirebaseParsers.PatchParser::class.java).toPatch(it.key)
                }
                Timber.d(patches.toString())
                onSuccess.invoke(patches)
            }

            override fun onCancelled(de: DatabaseError) {
                Timber.d("Fail: " + de.message)
                onError?.invoke(de.toException())
            }

        })
    }

    fun getSeasons(onError: ((e: Exception?) -> Unit)? = null, onSuccess: (List<Season>) -> Unit) {
        database.child(NODE_SEASONS).addListenerForSingleValueEvent(object : ValueEventListener {

            override fun onDataChange(ds: DataSnapshot) {
                val seasons = ds.children.mapTo(arrayListOf()) {
                    val id = it.key.replace("_", "").toInt()
                    val date = it.key.split("_")
                    val month = Month.of(date[1].toInt())
                    val desc = "${month.getDisplayName(TextStyle.FULL, Locale.getDefault())}/${date[0].toInt()}"
                    Season(id, it.key, desc, it.value.toString())
                }
                Timber.d(seasons.toString())
                onSuccess.invoke(seasons)
            }

            override fun onCancelled(de: DatabaseError) {
                Timber.d("Fail: " + de.message)
                onError?.invoke(de.toException())
            }

        })
    }

    fun getUserInfo(uuid: String, onError: ((e: Exception?) -> Unit)? = null,
                    onSuccess: (UserInfo) -> Unit) {
        dbUsers.child(uuid)?.child(NODE_USERS_INFO)?.addListenerForSingleValueEvent(object : ValueEventListener {

            override fun onDataChange(ds: DataSnapshot) {
                val userInfo = UserInfo(ds.child(KEY_USER_NAME)?.value?.toString() ?: "",
                        ds.child(KEY_USER_PHOTO)?.value?.toString() ?: "")
                Timber.d(userInfo.toString())
                onSuccess.invoke(userInfo)
            }

            override fun onCancelled(de: DatabaseError) {
                Timber.d("Fail: " + de.message)
                onError?.invoke(de.toException())
            }

        })
    }

    fun getNewsRef(): DatabaseReference = database.child(NODE_NEWS)

    fun saveNews(news: News, onError: ((e: Exception?) -> Unit)? = null, onSuccess: () -> Unit) {
        getNewsRef().child(news.uuidDate)
                .setValue(FirebaseParsers.NewsParser().fromNews(news))
                .addOnCompleteListener {
                    onSuccess.invoke()
                }
                .addOnFailureListener { e ->
                    Timber.d("Fail: " + e.message)
                    onError?.invoke(e)
                }
    }

}

