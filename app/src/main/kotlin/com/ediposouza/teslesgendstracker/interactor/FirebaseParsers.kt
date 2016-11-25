package com.ediposouza.teslesgendstracker.interactor

import com.ediposouza.teslesgendstracker.data.*
import com.ediposouza.teslesgendstracker.toIntSafely
import org.threeten.bp.LocalDateTime

class CardParser() {

    val name: String = ""
    val rarity: String = ""
    val unique: Boolean = false
    val cost: String = ""
    val attack: String = ""
    val health: String = ""
    val type: String = ""
    val race: String = CardRace.NONE.name
    val keyword: String = ""
    val arenaTier: String = CardArenaTier.NONE.name
    val evolves: Boolean = false

    fun toCard(shortName: String, cls: Attribute): Card {
        return Card(name, shortName, cls,
                CardRarity.valueOf(rarity.trim().toUpperCase()), unique,
                cost.toIntSafely(), attack.toIntSafely(), health.toIntSafely(),
                CardType.valueOf(type.trim().toUpperCase()),
                CardRace.of(race.trim().toUpperCase().replace(" ", "_")),
                keyword.split(",")
                        .filter { it.trim().isNotEmpty() }
                        .mapTo(arrayListOf<CardKeyword>()) {
                            CardKeyword.valueOf(it.trim().toUpperCase().replace(" ", "_"))
                        },
                CardArenaTier.valueOf(arenaTier.trim().toUpperCase()),
                evolves)
    }

    fun toCardStatistic(shortName: String): CardStatistic {
        return CardStatistic(shortName, CardRarity.valueOf(rarity.trim().toUpperCase()), unique)
    }

}

class DeckParser(
        val name: String = "",
        val type: Int = 0,
        val cls: Int = 0,
        val cost: Int = 0,
        val owner: String = "",
        val createdAt: String = "",
        val updatedAt: String = "",
        val patch: String = "",
        val views: Int = 0,
        val likes: List<String> = listOf(),
        val cards: Map<String, Long> = mapOf(),
        val updates: Map<String, Map<String, Int>> = mapOf(),
        val comments: Map<String, Map<String, String>> = mapOf()
) {

    companion object {

        private val KEY_DECK_NAME = "owner"
        private val KEY_DECK_TYPE = "type"
        private val KEY_DECK_CLASS = "cls"
        private val KEY_DECK_PATCH = "patch"
        private val KEY_DECK_COMMENT_OWNER = "owner"
        private val KEY_DECK_COMMENT_MSG = "comment"
        private val KEY_DECK_COMMENT_CREATE_AT = "createdAt"

        fun toNewCommentMap(owner: String, comment: String): Map<String, String> {
            return mapOf(KEY_DECK_COMMENT_OWNER to owner, KEY_DECK_COMMENT_MSG to comment,
                    KEY_DECK_COMMENT_CREATE_AT to LocalDateTime.now().toString())
        }

    }

    fun toDeck(id: String, private: Boolean): Deck {
        return Deck(id, name, owner, private, DeckType.values()[type], Class.values()[cls], cost,
                LocalDateTime.parse(createdAt), LocalDateTime.parse(updatedAt), patch, likes, views, cards,
                updates.map { DeckUpdate(LocalDateTime.parse(it.key), it.value) },
                comments.map {
                    DeckComment(it.key, it.value[KEY_DECK_COMMENT_OWNER] as String,
                            it.value[KEY_DECK_COMMENT_MSG] as String,
                            LocalDateTime.parse(it.value[KEY_DECK_COMMENT_CREATE_AT] as String))
                })
    }

    fun fromDeck(deck: Deck): DeckParser {
        return DeckParser(deck.name, deck.type.ordinal, deck.cls.ordinal, deck.cost, deck.owner,
                deck.createdAt.toString(), deck.updatedAt.toString(), deck.patch, deck.views,
                deck.likes, deck.cards, deck.updates.map { it.date.toString() to it.changes }.toMap(),
                deck.comments.map {
                    it.id to mapOf(
                            KEY_DECK_COMMENT_OWNER to it.owner,
                            KEY_DECK_COMMENT_MSG to it.comment,
                            KEY_DECK_COMMENT_CREATE_AT to it.date.toString())
                }.toMap())
    }

    fun toDeckUpdateMap(): Map<String, Any> {
        return mapOf(KEY_DECK_NAME to name, KEY_DECK_TYPE to type, KEY_DECK_CLASS to cls, KEY_DECK_PATCH to patch)
    }

}