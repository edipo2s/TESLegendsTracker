package com.ediposouza.teslesgendstracker.interactor

import com.ediposouza.teslesgendstracker.data.*
import com.ediposouza.teslesgendstracker.util.toIntSafely
import org.threeten.bp.LocalDateTime

abstract class FirebaseParsers {

    class CardParser {

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
        val attr1: String = ""
        val attr2: String = ""

        fun toCard(shortName: String, set: CardSet, attr: Attribute): Card {
            var clsAttr1 = attr
            var clsAttr2 = attr
            if (attr == Attribute.DUAL) {
                clsAttr1 = Attribute.valueOf(attr1.trim().toUpperCase())
                clsAttr2 = Attribute.valueOf(attr2.trim().toUpperCase())
            }
            return Card(name, shortName, set, attr, clsAttr1, clsAttr2, CardRarity.of(rarity), unique,
                    cost.toIntSafely(), attack.toIntSafely(), health.toIntSafely(),
                    CardType.of(type), CardRace.of(race),
                    keyword.split(",")
                            .filter { it.trim().isNotEmpty() }
                            .mapTo(arrayListOf<CardKeyword>()) {
                                CardKeyword.of(it)
                            },
                    CardArenaTier.of(arenaTier),
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
            val cards: Map<String, Int> = mapOf(),
            val updates: Map<String, Map<String, Int>> = mapOf(),
            val comments: Map<String, Map<String, String>> = mapOf()
    ) {

        companion object {

            private const val KEY_DECK_NAME = "owner"
            private const val KEY_DECK_TYPE = "type"
            private const val KEY_DECK_CLASS = "cls"
            private const val KEY_DECK_PATCH = "patch"
            private const val KEY_DECK_COMMENT_OWNER = "owner"
            private const val KEY_DECK_COMMENT_MSG = "comment"
            private const val KEY_DECK_COMMENT_CREATE_AT = "createdAt"

            fun toNewCommentMap(owner: String, comment: String): Map<String, String> {
                return mapOf(KEY_DECK_COMMENT_OWNER to owner, KEY_DECK_COMMENT_MSG to comment,
                        KEY_DECK_COMMENT_CREATE_AT to LocalDateTime.now().withNano(0).toString())
            }

        }

        fun toDeck(uuid: String, private: Boolean): Deck {
            return Deck(uuid, name, owner, private, DeckType.values()[type], Class.values()[cls], cost,
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
                        it.uuid to mapOf(
                                KEY_DECK_COMMENT_OWNER to it.owner,
                                KEY_DECK_COMMENT_MSG to it.comment,
                                KEY_DECK_COMMENT_CREATE_AT to it.date.toString())
                    }.toMap())
        }

        fun toDeckUpdateMap(): Map<String, Any> {
            return mapOf(KEY_DECK_NAME to name, KEY_DECK_TYPE to type, KEY_DECK_CLASS to cls, KEY_DECK_PATCH to patch)
        }

        override fun toString(): String {
            return "DeckParser(name='$name', type=$type, cls=$cls, cost=$cost, owner='$owner', createdAt='$createdAt', updatedAt='$updatedAt', patch='$patch', views=$views, likes=$likes, cards=$cards, updates=$updates, comments=$comments)"
        }

    }

    class DeckFavoriteParser(

            val name: String = "",
            val cls: Int = 0

    )

    class PatchParser {

        val desc: String = ""

        fun toPatch(uuidDate: String): Patch {
            return Patch(uuidDate, desc)
        }

    }

    class MatchParser(

            val first: Boolean = false,
            val player: Map<String, Any> = mapOf(),
            val opponent: Map<String, Any> = mapOf(),
            val legend: Boolean = false,
            val mode: Int = 0,
            val rank: Int = 0,
            val season: String = "",
            val win: Boolean = false

    ) {

        companion object {

            private const val KEY_MATCH_DECK_CLASS = "cls"
            private const val KEY_MATCH_DECK_DECK_UUID = "deck"
            private const val KEY_MATCH_DECK_NAME = "name"
            private const val KEY_MATCH_DECK_TYPE = "type"
            private const val KEY_MATCH_DECK_VERSION = "version"

        }

        fun toMatch(uuid: String): Match {
            val playerDeck = MatchDeck(player[KEY_MATCH_DECK_NAME].toString(),
                    Class.values()[player[KEY_MATCH_DECK_CLASS].toString().toInt()],
                    DeckType.values()[player[KEY_MATCH_DECK_TYPE].toString().toInt()],
                    player[KEY_MATCH_DECK_DECK_UUID].toString(),
                    player[KEY_MATCH_DECK_VERSION].toString())
            val opponentDeck = MatchDeck(opponent[KEY_MATCH_DECK_NAME].toString(),
                    Class.values()[opponent[KEY_MATCH_DECK_CLASS].toString().toInt()],
                    DeckType.values()[opponent[KEY_MATCH_DECK_TYPE].toString().toInt()])
            val matchMode = MatchMode.values()[mode]
            return Match(uuid, first, playerDeck, opponentDeck, matchMode, season, rank, legend, win)
        }

        fun fromMatch(match: Match): MatchParser {
            val player = with(match.player) {
                mapOf(KEY_MATCH_DECK_NAME to name, KEY_MATCH_DECK_CLASS to cls.ordinal,
                        KEY_MATCH_DECK_TYPE to type.ordinal, KEY_MATCH_DECK_DECK_UUID to (deck ?: ""),
                        KEY_MATCH_DECK_VERSION to (version ?: ""))
            }
            val opponent = with(match.opponent) {
                mapOf(KEY_MATCH_DECK_NAME to name, KEY_MATCH_DECK_CLASS to cls.ordinal,
                        KEY_MATCH_DECK_TYPE to type.ordinal)
            }
            return MatchParser(match.first, player, opponent, match.legend, match.mode.ordinal,
                    match.rank, match.season, match.win)
        }

        override fun toString(): String {
            return "MatchParser(first=$first, player=$player, opponent=$opponent, legend=$legend, mode=$mode, rank=$rank, win=$win)"
        }

    }
}
