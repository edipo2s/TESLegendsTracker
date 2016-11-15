package com.ediposouza.teslesgendstracker.data

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.os.Parcel
import android.os.Parcelable
import java.util.*

/**
 * Created by ediposouza on 10/31/16.
 */
enum class Attribute(val color: Int) {

    STRENGTH(Color.RED),
    INTELLIGENCE(Color.BLUE),
    AGILITY(Color.GREEN),
    WILLPOWER(Color.YELLOW),
    ENDURANCE(Color.parseColor("purple")),
    NEUTRAL(Color.GRAY),
    DUAL(Color.LTGRAY)

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
    LEGENDARY(Color.YELLOW);

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
    MAMMOTH(""),
    MANTIKORA(""),
    MINOTAUR(""),
    MUDCRAB(""),
    MUMMY(""),
    NEREID(""),
    OGRE(""),
    REPTILE(""),
    REACHMAN(""),
    SKELETON(""),
    SPIRIT(""),
    SPIDER(""),
    SPRIGGAN(""),
    TROLL(""),
    VAMPIRE(""),
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
        val shortName: String,
        val cls: Attribute,
        val rarity: CardRarity,
        val unique: Boolean,
        val cost: Int,
        val attack: Int,
        val health: Int,
        val type: CardType,
        val race: CardRace,
        val keywords: List<CardKeyword>,
        val arenaTier: CardArenaTier

) : Parcelable {

    private val CARD_PATH = "Cards"

    companion object {
        @JvmField val CREATOR: Parcelable.Creator<Card> = object : Parcelable.Creator<Card> {
            override fun createFromParcel(source: Parcel): Card = Card(source)
            override fun newArray(size: Int): Array<Card?> = arrayOfNulls(size)
        }
    }

    constructor(source: Parcel) : this(source.readString(), source.readString(), Attribute.values()[source.readInt()], CardRarity.values()[source.readInt()], 1.equals(source.readInt()), source.readInt(), source.readInt(), source.readInt(), CardType.values()[source.readInt()], CardRace.values()[source.readInt()], ArrayList<CardKeyword>().apply { source.readList(this, CardKeyword::class.java.classLoader) }, CardArenaTier.values()[source.readInt()])

    override fun describeContents() = 0

    fun imageBitmap(context: Context): Bitmap {
        val cardAttr = cls.name.toLowerCase().capitalize()
        val imagePath = "$CARD_PATH/$cardAttr/$shortName.png"
        return BitmapFactory.decodeStream(context.resources.assets.open(imagePath))
    }

    override fun writeToParcel(dest: Parcel?, flags: Int) {
        dest?.writeString(name)
        dest?.writeString(shortName)
        dest?.writeInt(cls.ordinal)
        dest?.writeInt(rarity.ordinal)
        dest?.writeInt((if (unique) 1 else 0))
        dest?.writeInt(cost)
        dest?.writeInt(attack)
        dest?.writeInt(health)
        dest?.writeInt(type.ordinal)
        dest?.writeInt(race.ordinal)
        dest?.writeList(keywords)
        dest?.writeInt(arenaTier.ordinal)
    }
}