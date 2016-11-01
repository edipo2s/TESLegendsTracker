package com.ediposouza.teslesgendstracker.data

import com.ediposouza.teslesgendstracker.toIntSafely

/**
 * Created by ediposouza on 01/11/16.
 */
class FirebaseCard() {

    val name: String = ""
    val rarity: String = ""
    val cost: String = ""
    val attack: String = ""
    val health: String = ""
    val type: String = ""
    val race: String = ""
    val keyword: String = ""
    val arenaTier: String = ""

    fun toCard(cls: Attribute): Card {
        return Card(name, cls,
                CardRarity.of(rarity.trim().toUpperCase()),
                cost.toIntSafely(), attack.toIntSafely(), health.toIntSafely(),
                CardType.valueOf(type.trim().toUpperCase()),
                CardRace.of(race.trim().toUpperCase().replace(" ", "_")),
                keyword.split(",")
                        .filter { it.trim().length > 0 }
                        .mapTo(arrayListOf<CardKeyword>()) {
                            CardKeyword.valueOf(it.trim().toUpperCase().replace(" ", "_"))
                        },
                CardArenaTier.valueOf(arenaTier.trim().toUpperCase()))
    }

}