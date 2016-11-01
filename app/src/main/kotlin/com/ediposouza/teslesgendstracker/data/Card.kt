package com.ediposouza.teslesgendstracker.data

import android.graphics.Color
import java.util.*

/**
 * Created by ediposouza on 10/31/16.
 */
enum class Attribute(val color: Int) {

    AGILITY(Color.GREEN),
    DUAL(Color.LTGRAY),
    ENDURANCE(Color.parseColor("purple")),
    INTELLIGENCE(Color.BLUE),
    NEUTRAL(Color.GRAY),
    STRENGTH(Color.RED),
    WILLPOWER(Color.YELLOW)

}

enum class Class(val attr1: Attribute, val attr2: Attribute = Attribute.NEUTRAL) {

    ARCHER(Attribute.STRENGTH, Attribute.AGILITY),
    ASSASSIN(Attribute.INTELLIGENCE, Attribute.AGILITY),
    BATTLEMAGE(Attribute.STRENGTH, Attribute.INTELLIGENCE),
    CRUSADER(Attribute.STRENGTH, Attribute.WILLPOWER),
    MAGE(Attribute.INTELLIGENCE, Attribute.WILLPOWER),
    MONK(Attribute.WILLPOWER, Attribute.AGILITY),
    SCOUT(Attribute.AGILITY, Attribute.ENDURANCE),
    SORCERER(Attribute.INTELLIGENCE, Attribute.ENDURANCE),
    SPELLSWORD(Attribute.WILLPOWER, Attribute.ENDURANCE),
    WARRIOR(Attribute.STRENGTH, Attribute.ENDURANCE),
    STRENGTH(Attribute.STRENGTH),
    INTELLIGENCE(Attribute.INTELLIGENCE),
    AGILITY(Attribute.AGILITY),
    WILLPOWER(Attribute.WILLPOWER),
    ENDURANCE(Attribute.ENDURANCE),
    NEUTRAL(Attribute.NEUTRAL)

}

enum class CardRarity(val color: Int) {

    COMMON(Color.WHITE),
    RARE(Color.BLUE),
    EPIC(Color.parseColor("purple")),
    LEGENDARY(Color.YELLOW),
    UNIQUE(Color.parseColor("maroon"));

    companion object {

        fun of(value: String): CardRarity {
            return if (value.contains(UNIQUE.name)) UNIQUE else valueOf(value)
        }

    }
}

enum class CardType() {

    ACTION,
    CREATURE,
    ITEM,
    SUPPORT

}

enum class CardRace(val desc: String) {

    ARGONIAN("The Argonians of Black Marsh are possessed of a cool intellect and are well-versed in " +
            "stealth and the use of blades. They often act as the scouts and skirmishers."),
    BRETON("Flamboyant, intelligent, and resourceful, the Bretons of High Rock are renowned craftsmen, " +
            "shrewd merchants, gallant cavaliers, and inventive wizards."),
    DARK_ELF("The Dark Elf homeland of Morrowind has been invaded many times over the millennia. " +
            "This history of conflict has transformed the Dunmer into hardened warriors."),
    HIGH_ELF("The Altmer are one of the longest-lived and most-intelligent races in Tamriel, " +
            "which grants them a natural affinity for spells and magic."),
    IMPERIAL("The Imperial natives of Cyrodiil have proven to be shrewd tacticians and diplomats."),
    KHAJIIT("Khajiit are quick and agile, making them some of the most adept thieves in Tamriel."),
    NORD("Hailing from the inhospitable mountains of Skyrim, Nords are fearsome and hardy warriors."),
    ORC("The Orcs of the Wrothgarian and Dragontail Mountains are renowed as both craftsmen and berserkers."),
    REDGUARD("The Redguard of Hammerfell are Tamriel's most talented and resourceful warriors."),
    WOOD_ELF("The clanfolk of the Valenwood are the finest archers in Tamriel. Wood Elves excel at " +
            "hunting and dispatching the unwary."),
    ASH_CREATURE(""),
    BEAST(""),
    CENTAUR(""),
    CHAURUS(""),
    DAEDRA(""),
    DEFENSE(""),
    DRAGON(""),
    DREUGH(""),
    DWEMER(""),
    FISH(""),
    GIANT(""),
    GOBLIN(""),
    HARPY(""),
    IMP(""),
    KWAMA(""),
    LURCHER(""),
    LYCANTHROPE(""),
    MANTIKORA(""),
    MINOTAUR(""),
    MUDCRAB(""),
    NEREID(""),
    OGRE(""),
    REPTILE(""),
    REACHMAN(""),
    SKELETON(""),
    SPIRIT(""),
    SPIDER(""),
    SPRIGGAN(""),
    WOLF(""),
    WAMASU(""),
    WRAITH(""),
    NONE("");

    companion object {

        fun of(value: String): CardRace {
            return if (value.trim().length == 0) CardRace.NONE else CardRace.valueOf(value)
        }

    }
}

enum class CardKeyword() {

    ACTIVATE,
    BREAKTHROUGH,
    CHARGE,
    COVER,
    DRAIN,
    GUARD,
    LAST_GASP,
    LETHAL,
    PILFER,
    PROPHECY,
    REGENERATE,
    SHACKLE,
    SILENCE,
    SUMMON,
    WARD

}

enum class CardArenaTier() {

    TERRIBLE,
    POOR,
    AVERAGE,
    GOOD,
    EXCELLENT,
    INSANE,
    UNKNOWN

}

data class Card(

        val name: String,
        val cls: Attribute,
        val rarity: CardRarity,
        val cost: Int,
        val attack: Int,
        val health: Int,
        val type: CardType,
        val race: CardRace,
        val keywords: ArrayList<CardKeyword>,
        val arenaTier: CardArenaTier

)