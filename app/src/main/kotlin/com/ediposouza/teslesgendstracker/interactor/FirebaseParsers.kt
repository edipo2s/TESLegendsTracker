package com.ediposouza.teslesgendstracker.interactor

import com.ediposouza.teslesgendstracker.data.*
import com.ediposouza.teslesgendstracker.toIntSafely
import org.threeten.bp.LocalDate
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

class DeckParser() {

    private val COMMENT_OWNER_KEY = "owner"
    private val COMMENT_COMMENT_KEY = "comment"
    private val COMMENT_CREATE_AT_KEY = "createdAt"

    val name: String = ""
    val type: Int = 0
    val cost: Int = 0
    val owner: String = ""
    val createdAt: String = ""
    val updatedAt: String = ""
    val patch: String = ""
    val likes: List<String> = listOf()
    val views: Int = 0
    val cards: Map<String, Int> = mapOf()
    val updates: Map<String, Map<String, Int>> = mapOf()
    val comments: Map<String, Map<String, String>> = mapOf()

    fun toCard(id: String, cls: Class): Deck {
        return Deck(id, name, owner, DeckType.values()[type], cls, cost, LocalDate.parse(createdAt),
                LocalDateTime.parse(updatedAt), patch, likes, views, cards,
                updates.map { DeckUpdate(LocalDateTime.parse(it.key), it.value) },
                comments.map {
                    DeckComment(it.key, it.value[COMMENT_OWNER_KEY] as String,
                            it.value[COMMENT_COMMENT_KEY] as String,
                            LocalDateTime.parse(it.value[COMMENT_CREATE_AT_KEY] as String))
                })
    }

}