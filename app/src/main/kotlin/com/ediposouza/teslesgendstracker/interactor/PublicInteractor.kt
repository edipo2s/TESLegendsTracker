package com.ediposouza.teslesgendstracker.interactor

import com.ediposouza.teslesgendstracker.data.*
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import timber.log.Timber

/**
 * Created by ediposouza on 01/11/16.
 */
object PublicInteractor : BaseInteractor() {

    private val NODE_BASICS_LEVEL = "level"
    private val NODE_BASICS_RACES = "races"
    private val NODE_BASICS_RANKED = "ranked"
    private val NODE_SPOILER_CARDS = "cards"

    private val KEY_SPOILER_ENABLE = "enabled"
    private val KEY_SPOILER_TITLE = "title"
    private val KEY_SPOILER_SET = "set"
    private val KEY_CARD_EVOLVES = "evolves"
    private val KEY_DECK_VIEWS = "views"

    fun getCard(set: CardSet, attribute: CardAttribute, shortname: String, onSuccess: (Card) -> Unit) {
        val attr = attribute.name.toLowerCase()
        with(database.child(NODE_CARDS).child(set.db).child(attr).child(shortname)) {
            keepSynced()
            addListenerForSingleValueEvent(object : ValueEventListener {

                override fun onDataChange(ds: DataSnapshot) {
                    val card = ds.getValue(FirebaseParsers.CardParser::class.java)
                            ?.toCard(shortname, set, attribute)
                    onSuccess.invoke(card ?: Card.DUMMY)
                }

                override fun onCancelled(de: DatabaseError) {
                    Timber.d("Fail: " + de.message)
                }

            })
        }
    }

    fun getCards(set: CardSet?, onSuccess: (List<Card>) -> Unit) {
        getListFromSets(set, onSuccess) { set, onEachSuccess ->
            with(database.child(NODE_CARDS).child(set.db).orderByChild(KEY_CARD_COST)) {
                keepSynced()
                addListenerForSingleValueEvent(object : ValueEventListener {

                    override fun onDataChange(ds: DataSnapshot) {
                        val cards = ds.children.map {
                            val attr = CardAttribute.of(it.key.toUpperCase())
                            it.children.map {
                                it.getValue(FirebaseParsers.CardParser::class.java)?.toCard(it.key, set, attr)
                            }.filterNotNull()
                        }.flatMap { it }
                        onEachSuccess.invoke(cards)
                    }

                    override fun onCancelled(de: DatabaseError) {
                        Timber.d("Fail: " + de.message)
                    }

                })
            }
        }
    }

    fun getCards(set: CardSet?, vararg attrs: CardAttribute, onSuccess: (List<Card>) -> Unit) {
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

    private fun getCards(set: CardSet?, attr: CardAttribute, onSuccess: (List<Card>) -> Unit) {
        val onFinalSuccess: (List<Card>) -> Unit = { onSuccess(it.sorted()) }
        getListFromSets(set, attr, onFinalSuccess) { set, attr, onEachSuccess ->
            val node_attr = attr.name.toLowerCase()
            database.child(NODE_CARDS).child(set.db).child(node_attr).orderByChild(KEY_CARD_COST)
                    .addListenerForSingleValueEvent(object : ValueEventListener {

                        override fun onDataChange(ds: DataSnapshot) {
                            val cards = ds.children.mapTo(arrayListOf()) {
                                it.getValue(FirebaseParsers.CardParser::class.java)?.toCard(it.key, set, attr)
                            }.filterNotNull()
                            Timber.d(cards.toString())
                            val cardsAlternativeArt = cards.filter { it.hasAlternativeArt }.map { it.toAlternativeArt() }
                            onEachSuccess.invoke(cards.plus(cardsAlternativeArt))
                        }

                        override fun onCancelled(de: DatabaseError) {
                            Timber.d("Fail: " + de.message)
                        }

                    })
        }
    }

    fun getCardRatings(card: Card, onSuccess: (List<Pair<String, Int>>) -> Unit) {
        val attr = card.attr.name.toLowerCase()
        with(database.child(NODE_REVIEWS).child(card.set.db).child(attr).child(card.shortName)) {
            keepSynced()
            addListenerForSingleValueEvent(object : ValueEventListener {

                override fun onDataChange(ds: DataSnapshot) {
                    val reviews = ds.children.map {
                        it.key to (it.getValue(Int::class.java) ?: -1)
                    }.filter { it.second >= 0 }.toList()
                    onSuccess.invoke(reviews)
                }

                override fun onCancelled(de: DatabaseError) {
                    Timber.d("Fail: " + de.message)
                }

            })
        }
    }

    fun getTokens(set: CardSet?, onSuccess: (List<Card>) -> Unit) {
        getListFromSets(set, onSuccess) { set, onEachSuccess ->
            with(database.child(NODE_TOKENS).child(set.db).orderByChild(KEY_CARD_COST)) {
                keepSynced()
                addListenerForSingleValueEvent(object : ValueEventListener {

                    override fun onDataChange(ds: DataSnapshot) {
                        val cards = ds.children.map {
                            val attr = CardAttribute.of(it.key.toUpperCase())
                            it.children.map {
                                it.getValue(FirebaseParsers.CardParser::class.java)?.toCard(it.key, set, attr)
                            }.filterNotNull()
                        }.flatMap { it }
                        onEachSuccess.invoke(cards)
                    }

                    override fun onCancelled(de: DatabaseError) {
                        Timber.d("Fail: " + de.message)
                    }

                })
            }
        }
    }

    fun getTokens(set: CardSet?, attr: CardAttribute, onSuccess: (List<Card>) -> Unit) {
        val onFinalSuccess: (List<Card>) -> Unit = { onSuccess(it.sorted()) }
        getListFromSets(set, attr, onFinalSuccess) { set, attr, onEachSuccess ->
            val node_attr = attr.name.toLowerCase()
            database.child(NODE_TOKENS).child(set.db).child(node_attr).orderByChild(KEY_CARD_COST)
                    .addListenerForSingleValueEvent(object : ValueEventListener {

                        override fun onDataChange(ds: DataSnapshot) {
                            val cards = ds.children.mapTo(arrayListOf()) {
                                it.getValue(FirebaseParsers.CardParser::class.java)?.toCard(it.key, set, attr)
                            }.filterNotNull()
                            Timber.d(cards.toString())
                            onEachSuccess.invoke(cards)
                        }

                        override fun onCancelled(de: DatabaseError) {
                            Timber.d("Fail: " + de.message)
                        }

                    })
        }
    }

    fun isSpoilerEnable(onSuccess: (Boolean) -> Unit) {
        database.child(NODE_SPOILER).child(KEY_SPOILER_ENABLE)
                .addListenerForSingleValueEvent(object : ValueEventListener {

                    override fun onDataChange(ds: DataSnapshot) {
                        val name = ds.getValue(Boolean::class.java)
                        onSuccess.invoke(name ?: false)
                    }

                    override fun onCancelled(de: DatabaseError) {
                        Timber.d("Fail: " + de.message)
                    }

                })
    }

    fun getSpoilerTitle(onSuccess: (String) -> Unit) {
        database.child(NODE_SPOILER).child(KEY_SPOILER_TITLE)
                .addListenerForSingleValueEvent(object : ValueEventListener {

                    override fun onDataChange(ds: DataSnapshot) {
                        val name = "${ds.value}"
                        Timber.d(name)
                        onSuccess.invoke(name.takeIf { it != "null" } ?: "")
                    }

                    override fun onCancelled(de: DatabaseError) {
                        Timber.d("Fail: " + de.message)
                    }

                })
    }

    private fun getSpoilerSet(onSuccess: (String) -> Unit) {
        database.child(NODE_SPOILER).child(KEY_SPOILER_SET)
                .addListenerForSingleValueEvent(object : ValueEventListener {

                    override fun onDataChange(ds: DataSnapshot) {
                        val name = "${ds.value}"
                        Timber.d(name)
                        onSuccess.invoke(name.takeIf { it != "null" } ?: "")
                    }

                    override fun onCancelled(de: DatabaseError) {
                        Timber.d("Fail: " + de.message)
                    }

                })
    }

    fun getSpoilerCards(onSuccess: (List<Card>) -> Unit) {
        with(database.child(NODE_SPOILER).child(NODE_SPOILER_CARDS).orderByChild(KEY_CARD_COST)) {
            keepSynced()
            addValueEventListener(object : ValueEventListener {

                override fun onDataChange(ds: DataSnapshot) {
                    getSpoilerTitle { spoilerTitle ->
                        getSpoilerSet { spoilerSet ->
                            val set = CardSet.UNKNOWN.apply {
                                unknownSetName = spoilerSet
                                unknownSetTitle = spoilerTitle
                            }
                            val cards = ds.children.map {
                                val attr = CardAttribute.of(it.key.toUpperCase())
                                it.children.map {
                                    it.getValue(FirebaseParsers.CardParser::class.java)?.toCard(it.key, set, attr)
                                }.filterNotNull()
                            }.flatMap { it }
                            onSuccess.invoke(cards)
                        }
                    }
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
                                        it.getValue(FirebaseParsers.CardParser::class.java)?.toCardStatistic(it.key)
                                    }.filterNotNull()
                            Timber.d(cards.toString())
                            onEachSuccess.invoke(cards)
                        }

                        override fun onCancelled(de: DatabaseError) {
                            Timber.d("Fail: " + de.message)
                        }

                    })
        }
    }

    fun getCardsForStatistics(set: CardSet?, attr: CardAttribute, onSuccess: (List<CardStatistic>) -> Unit) {
        getListFromSets(set, attr, onSuccess) { set, attr, onEachSuccess ->
            val node_attr = attr.name.toLowerCase()
            database.child(NODE_CARDS).child(set.db).child(node_attr).orderByChild(KEY_CARD_COST)
                    .addListenerForSingleValueEvent(object : ValueEventListener {

                        override fun onDataChange(ds: DataSnapshot) {
                            val cards = ds.children
                                    .filter { !it.hasChild(KEY_CARD_EVOLVES) }
                                    .mapTo(arrayListOf()) {
                                        it.getValue(FirebaseParsers.CardParser::class.java)?.toCardStatistic(it.key)
                                    }.filterNotNull()
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

    fun getPublicDecks(cls: DeckClass?, onSuccess: (List<Deck>) -> Unit) {
        val dbPublicDeck = dbDecks.child(NODE_DECKS_PUBLIC)
        dbPublicDeck.keepSynced()
        var query = dbPublicDeck.orderByChild(KEY_DECK_UPDATE_AT)
        if (cls != null) {
            query = dbPublicDeck.orderByChild(KEY_DECK_CLASS).equalTo(cls.ordinal.toDouble())
        }
        query.addListenerForSingleValueEvent(object : ValueEventListener {

            override fun onDataChange(ds: DataSnapshot) {
                Timber.d(ds.value?.toString())
                val decks = ds.children.mapTo(arrayListOf<Deck?>()) {
                    it.getValue(FirebaseParsers.DeckParser::class.java)?.toDeck(it.key, false)
                }.filterNotNull()
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
            getCards(null, deck.cls.attr1, deck.cls.attr2, CardAttribute.DUAL, CardAttribute.NEUTRAL) {
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
                    it.getValue(FirebaseParsers.PatchParser::class.java)?.toPatch(it.key)
                }.filterNotNull()
                Timber.d(patches.toString())
                onSuccess.invoke(patches)
            }

            override fun onCancelled(de: DatabaseError) {
                Timber.d("Fail: " + de.message)
                onError?.invoke(de.toException())
            }

        })
    }

    fun getSeasonsRef() = database.child(NODE_SEASONS)?.apply {
        keepSynced()
    }

    fun getSeasons(onError: ((e: Exception?) -> Unit)? = null, onSuccess: (List<Season>) -> Unit) {
        getSeasonsRef()?.addListenerForSingleValueEvent(object : ValueEventListener {

            override fun onDataChange(ds: DataSnapshot) {
                val seasons = ds.children.mapTo(arrayListOf()) {
                    it.getValue(FirebaseParsers.SeasonParser::class.java)?.toSeason(it.key)
                }.filterNotNull()
                Timber.d(seasons.toString())
                onSuccess.invoke(seasons)
            }

            override fun onCancelled(de: DatabaseError) {
                Timber.d("Fail: " + de.message)
                onError?.invoke(de.toException())
            }

        })
    }

    fun getSets(onError: ((e: Exception?) -> Unit)? = null, onSuccess: (List<CardSet>) -> Unit) {
        database.child(NODE_CARDS).addListenerForSingleValueEvent(object : ValueEventListener {

            override fun onDataChange(ds: DataSnapshot) {
                val seasons = ds.children.mapTo(arrayListOf()) {
                    CardSet.of(it.key)
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

    fun saveNews(news: Article, onError: ((e: Exception?) -> Unit)? = null, onSuccess: () -> Unit) {
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

    fun getLevels(onError: ((e: Exception?) -> Unit)? = null, onSuccess: (List<LevelUp>) -> Unit) {
        with(database.child(NODE_BASICS).child(NODE_BASICS_LEVEL)) {
            keepSynced()
            addListenerForSingleValueEvent(object : ValueEventListener {

                override fun onDataChange(ds: DataSnapshot) {
                    val levels = ds.children.mapTo(arrayListOf()) {
                        Timber.d(it.key)
                        it.getValue(FirebaseParsers.LevelUpParser::class.java)?.toLevelUp(it.key)
                    }.filterNotNull()
                    Timber.d(levels.toString())
                    onSuccess.invoke(levels)
                }

                override fun onCancelled(de: DatabaseError) {
                    Timber.d("Fail: " + de.message)
                    onError?.invoke(de.toException())
                }

            })
        }
    }

    fun getRaces(onError: ((e: Exception?) -> Unit)? = null, onSuccess: (List<Race>) -> Unit) {
        with(database.child(NODE_BASICS).child(NODE_BASICS_RACES)) {
            keepSynced()
            addListenerForSingleValueEvent(object : ValueEventListener {

                override fun onDataChange(ds: DataSnapshot) {
                    val races = ds.children.mapTo(arrayListOf()) {
                        FirebaseParsers.RaceParser().toRace(it.key to it.value as List<String>)
                    }
                    Timber.d(races.toString())
                    onSuccess.invoke(races)
                }

                override fun onCancelled(de: DatabaseError) {
                    Timber.d("Fail: " + de.message)
                    onError?.invoke(de.toException())
                }

            })
        }
    }

    fun getRanked(onError: ((e: Exception?) -> Unit)? = null, onSuccess: (List<Ranked>) -> Unit) {
        with(database.child(NODE_BASICS).child(NODE_BASICS_RANKED)) {
            keepSynced()
            addListenerForSingleValueEvent(object : ValueEventListener {

                override fun onDataChange(ds: DataSnapshot) {
                    val rankeds = ds.children.mapTo(arrayListOf()) {
                        it.getValue(FirebaseParsers.RankedParser::class.java)?.toRanked(it.key)
                    }.filterNotNull()
                    Timber.d(rankeds.toString())
                    onSuccess.invoke(rankeds)
                }

                override fun onCancelled(de: DatabaseError) {
                    Timber.d("Fail: " + de.message)
                    onError?.invoke(de.toException())
                }

            })
        }
    }

}

