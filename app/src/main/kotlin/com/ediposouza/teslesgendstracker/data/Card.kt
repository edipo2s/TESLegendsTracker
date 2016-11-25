package com.ediposouza.teslesgendstracker.data

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.os.Parcel
import android.os.Parcelable
import android.support.annotation.IntegerRes
import com.ediposouza.teslesgendstracker.R
import java.util.*

/**
 * Created by ediposouza on 10/31/16.
 */
enum class Attribute(val color: Int, @IntegerRes val imageRes: Int) {

    STRENGTH(Color.RED, R.drawable.attr_strength),
    INTELLIGENCE(Color.BLUE, R.drawable.attr_intelligence),
    WILLPOWER(Color.YELLOW, R.drawable.attr_willpower),
    AGILITY(Color.GREEN, R.drawable.attr_agility),
    ENDURANCE(Color.parseColor("purple"), R.drawable.attr_endurance),
    NEUTRAL(Color.GRAY, R.drawable.attr_neutral),
    DUAL(Color.LTGRAY, R.drawable.attr_dual)

}

enum class Class(val attr1: Attribute, val attr2: Attribute = Attribute.NEUTRAL, @IntegerRes val imageRes: Int) {

    ARCHER(Attribute.STRENGTH, Attribute.AGILITY, R.drawable.deck_class_archer),
    ASSASSIN(Attribute.INTELLIGENCE, Attribute.AGILITY, R.drawable.deck_class_assassin),
    BATTLEMAGE(Attribute.STRENGTH, Attribute.INTELLIGENCE, R.drawable.deck_class_battlemage),
    CRUSADER(Attribute.STRENGTH, Attribute.WILLPOWER, R.drawable.deck_class_crusader),
    MAGE(Attribute.INTELLIGENCE, Attribute.WILLPOWER, R.drawable.deck_class_mage),
    MONK(Attribute.WILLPOWER, Attribute.AGILITY, R.drawable.deck_class_monk),
    SCOUT(Attribute.AGILITY, Attribute.ENDURANCE, R.drawable.deck_class_scout),
    SORCERER(Attribute.INTELLIGENCE, Attribute.ENDURANCE, R.drawable.deck_class_sorcerer),
    SPELLSWORD(Attribute.WILLPOWER, Attribute.ENDURANCE, R.drawable.deck_class_spellsword),
    WARRIOR(Attribute.STRENGTH, Attribute.ENDURANCE, R.drawable.deck_class_warrior),
    STRENGTH(Attribute.STRENGTH, imageRes = R.drawable.deck_attr_strength),
    INTELLIGENCE(Attribute.INTELLIGENCE, imageRes = R.drawable.deck_attr_intelligence),
    AGILITY(Attribute.AGILITY, imageRes = R.drawable.deck_attr_agility),
    WILLPOWER(Attribute.WILLPOWER, imageRes = R.drawable.deck_attr_willpower),
    ENDURANCE(Attribute.ENDURANCE, imageRes = R.drawable.deck_attr_endurance),
    NEUTRAL(Attribute.NEUTRAL, imageRes = R.drawable.deck_attr_neutral)

}

enum class CardRarity(val color: Int, val soulCost: Int, @IntegerRes val imageRes: Int) {

    COMMON(Color.WHITE, 50, R.drawable.ic_rarity_common),
    RARE(Color.BLUE, 100, R.drawable.ic_rarity_rare),
    EPIC(Color.parseColor("purple"), 400, R.drawable.ic_rarity_epic),
    LEGENDARY(Color.YELLOW, 1200, R.drawable.ic_rarity_legendary);

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
            return if (value.trim().isEmpty()) CardRace.NONE else CardRace.valueOf(value)
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
    UNKNOWN,
    NONE

}

data class CardMissing(

        val shortName: String,
        val rarity: CardRarity,
        val qtd: Long

)

data class CardStatistic(

        val shortName: String,
        val rarity: CardRarity,
        val unique: Boolean

)

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
        val arenaTier: CardArenaTier,
        val evolves: Boolean

) : Parcelable {

    private val CARD_PATH = "Cards"

    companion object {
        @JvmField val CREATOR: Parcelable.Creator<Card> = object : Parcelable.Creator<Card> {
            override fun createFromParcel(source: Parcel): Card = Card(source)
            override fun newArray(size: Int): Array<Card?> = arrayOfNulls(size)
        }
    }

    constructor(source: Parcel) : this(source.readString(), source.readString(),
            Attribute.values()[source.readInt()], CardRarity.values()[source.readInt()],
            1 == source.readInt(), source.readInt(), source.readInt(), source.readInt(),
            CardType.values()[source.readInt()], CardRace.values()[source.readInt()],
            ArrayList<CardKeyword>().apply { source.readList(this, CardKeyword::class.java.classLoader) },
            CardArenaTier.values()[source.readInt()], 1 == source.readInt())

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
        dest?.writeInt((if (evolves) 1 else 0))
    }
}