package com.ediposouza.teslesgendstracker.data

/**
 * Created by ediposouza on 06/06/17.
 */
enum class LevelUpSource {

    CARD,
    RARITY;

    companion object {

        fun of(value: String): LevelUpSource {
            return RARITY.takeIf { CardRarity.of(value) != CardRarity.UNKNOWN } ?: CARD
        }

    }

}

enum class LevelUpType {

    EVOLVE,
    CARD,
    UNKNOWN;

    companion object {

        fun of(value: String): LevelUpType {
            val name = value.trim().toUpperCase().replace(" ", "_")
            return if (values().map { it.name }.contains(name)) valueOf(name) else UNKNOWN
        }

    }


}

enum class LevelUpExtra {

    CARD,
    PACK,
    NONE;

    companion object {

        fun of(value: String): LevelUpExtra {
            return NONE.takeIf { value.isEmpty() } ?:
                    CARD.takeIf { CardRarity.of(value) != CardRarity.UNKNOWN } ?: PACK
        }

    }


}

data class LevelUp(

        val level: Int,
        val source: LevelUpSource,
        val sourceValue: String,
        val target: List<String>,
        val type: LevelUpType,
        val extra: LevelUpExtra,
        val extraValue: String,
        val gold: Int,
        val legendary: Boolean,
        val racial: Boolean

)

data class Race(

        val race: CardRace,
        val rewards: List<String>

)

data class Ranked(

        val level: Int,
        val name: String,
        val monthly: Int,
        val reset: Int,
        val stars: Int,
        val gems: Int,
        val gold: Int

)