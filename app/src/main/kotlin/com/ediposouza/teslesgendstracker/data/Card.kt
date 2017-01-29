package com.ediposouza.teslesgendstracker.data

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Parcel
import android.os.Parcelable
import android.support.annotation.IntegerRes
import com.ediposouza.teslesgendstracker.R
import com.ediposouza.teslesgendstracker.TEXT_UNKNOWN
import timber.log.Timber
import java.util.*

/**
 * Created by ediposouza on 10/31/16.
 */
enum class CardSet(val db: String) {

    CORE("core"),
    MADHOUSE("madhouse"),
    UNKNOWN(TEXT_UNKNOWN);

    companion object {

        fun of(value: String): CardSet {
            val name = value.trim().toUpperCase().replace(" ", "_")
            return if (values().map { it.name }.contains(name)) valueOf(name) else UNKNOWN
        }

    }

}

enum class CardAttribute(@IntegerRes val imageRes: Int) {

    STRENGTH(R.drawable.attr_strength),
    INTELLIGENCE(R.drawable.attr_intelligence),
    WILLPOWER(R.drawable.attr_willpower),
    AGILITY(R.drawable.attr_agility),
    ENDURANCE(R.drawable.attr_endurance),
    NEUTRAL(R.drawable.attr_neutral),
    DUAL(R.drawable.attr_dual)

}

enum class CardRarity(val soulCost: Int, @IntegerRes val imageRes: Int) {

    COMMON(50, R.drawable.ic_rarity_common),
    RARE(100, R.drawable.ic_rarity_rare),
    EPIC(400, R.drawable.ic_rarity_epic),
    LEGENDARY(1200, R.drawable.ic_rarity_legendary),
    UNKNOWN(0, R.drawable.ic_rarity);

    companion object {

        fun of(value: String): CardRarity {
            val name = value.trim().toUpperCase().replace(" ", "_")
            return if (values().map { it.name }.contains(name)) valueOf(name) else UNKNOWN
        }

    }

}

enum class CardType {

    ACTION,
    CREATURE,
    ITEM,
    SUPPORT,
    UNKNOWN;

    companion object {

        fun of(value: String): CardType {
            val name = value.trim().toUpperCase().replace(" ", "_")
            return if (values().map { it.name }.contains(name)) valueOf(name) else UNKNOWN
        }

    }

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
    UNKNOWN(""),
    NONE("");

    companion object {

        fun of(value: String): CardRace {
            val name = value.trim().toUpperCase().replace(" ", "_")
            return if (value.trim().isEmpty()) CardRace.NONE else
                if (values().map { it.name }.contains(name)) valueOf(name) else UNKNOWN
        }

    }
}

enum class CardKeyword {

    ACTIVATE,
    BREAKTHROUGH,
    CHARGE,
    COVER,
    DRAIN,
    EVOLVES,
    GUARD,
    LAST_GASP,
    LETHAL,
    PILFER,
    PROPHECY,
    REGENERATE,
    SHACKLE,
    SILENCE,
    SUMMON,
    WARD,
    UNKNOWN;

    companion object {

        fun of(value: String): CardKeyword {
            val name = value.trim().toUpperCase().replace(" ", "_")
            return if (values().map { it.name }.contains(name)) valueOf(name) else UNKNOWN
        }

    }
}

enum class CardArenaTier {

    TERRIBLE,
    POOR,
    AVERAGE,
    GOOD,
    EXCELLENT,
    INSANE,
    UNKNOWN,
    NONE;

    companion object {

        fun of(value: String): CardArenaTier {
            val name = value.trim().toUpperCase().replace(" ", "_")
            return if (values().map { it.name }.contains(name)) valueOf(name) else UNKNOWN
        }

    }

}

data class CardMissing(

        val shortName: String,
        val rarity: CardRarity,
        val qtd: Int

)

data class CardStatistic(

        val shortName: String,
        val rarity: CardRarity,
        val unique: Boolean

)

data class CardBasicInfo(

        val shortName: String,
        val set: String,
        val attr: String
)

data class CardSlot(

        val card: Card,
        val qtd: Int

) : Comparable<CardSlot>, Parcelable {

    companion object {
        @JvmField val CREATOR: Parcelable.Creator<CardSlot> = object : Parcelable.Creator<CardSlot> {
            override fun createFromParcel(source: Parcel): CardSlot = CardSlot(source)
            override fun newArray(size: Int): Array<CardSlot?> = arrayOfNulls(size)
        }
    }

    constructor(source: Parcel) : this(source.readParcelable<Card>(Card::class.java.classLoader),
            source.readInt())

    override fun compareTo(other: CardSlot): Int = card.compareTo(other.card)

    override fun describeContents() = 0

    override fun writeToParcel(dest: Parcel?, flags: Int) {
        dest?.writeParcelable(card, 0)
        dest?.writeInt(qtd)
    }
}

data class Card(

        val name: String,
        val shortName: String,
        val set: CardSet,
        val attr: CardAttribute,
        val dualAttr1: CardAttribute,
        val dualAttr2: CardAttribute,
        val rarity: CardRarity,
        val unique: Boolean,
        val cost: Int,
        val attack: Int,
        val health: Int,
        val type: CardType,
        val race: CardRace,
        val keywords: List<CardKeyword>,
        val arenaTier: CardArenaTier,
        val evolves: Boolean,
        val season: String

) : Comparable<Card>, Parcelable {

    companion object {
        @JvmField val CREATOR: Parcelable.Creator<Card> = object : Parcelable.Creator<Card> {
            override fun createFromParcel(source: Parcel): Card = Card(source)
            override fun newArray(size: Int): Array<Card?> = arrayOfNulls(size)
        }

        private val CARD_PATH = "Cards"
        private val CARD_BACK = "card_back.png"

        fun getDefaultCardImage(context: Context): Bitmap {
            return BitmapFactory.decodeStream(context.resources.assets.open(CARD_BACK))
        }

        fun getCardImageBitmap(context: Context, cardSet: String, cardAttr: String,
                               cardShortName: String, onError: (() -> Bitmap)? = null): Bitmap {
            val setName = cardSet.toLowerCase().capitalize()
            val attrName = cardAttr.toLowerCase().capitalize()
            val imagePath = "$CARD_PATH/$setName/$attrName/$cardShortName.png"
            Timber.d(imagePath)
            try {
                return BitmapFactory.decodeStream(context.resources.assets.open(imagePath))
            } catch (e: Exception) {
                if (onError != null) {
                    return onError.invoke()
                } else {
                    return getDefaultCardImage(context)
                }
            }
        }

    }

    constructor(source: Parcel) : this(source.readString(), source.readString(),
            CardSet.values()[source.readInt()],
            CardAttribute.values()[source.readInt()], CardAttribute.values()[source.readInt()],
            CardAttribute.values()[source.readInt()], CardRarity.values()[source.readInt()],
            1 == source.readInt(), source.readInt(), source.readInt(), source.readInt(),
            CardType.values()[source.readInt()], CardRace.values()[source.readInt()],
            ArrayList<CardKeyword>().apply { source.readList(this, CardKeyword::class.java.classLoader) },
            CardArenaTier.values()[source.readInt()], 1 == source.readInt(), source.readString())

    override fun describeContents() = 0

    fun imageBitmap(context: Context): Bitmap {
        val cardAttr = attr.name.toLowerCase().capitalize()
        val cardSet = set.name.toLowerCase().capitalize()
        return Card.getCardImageBitmap(context, cardSet, cardAttr, shortName)
    }

    fun patchVersion(context: Context, patchUuid: String): Card {
        var patchShortName = "${shortName}_$patchUuid"
        getCardImageBitmap(context, set.name, attr.name, patchShortName) {
            patchShortName = shortName
            getDefaultCardImage(context)
        }
        return Card(name, patchShortName, set, attr, dualAttr1, dualAttr2, rarity, unique, cost,
                attack, health, type, race, keywords, arenaTier, evolves, season)
    }

    override fun writeToParcel(dest: Parcel?, flags: Int) {
        dest?.writeString(name)
        dest?.writeString(shortName)
        dest?.writeInt(set.ordinal)
        dest?.writeInt(attr.ordinal)
        dest?.writeInt(dualAttr1.ordinal)
        dest?.writeInt(dualAttr2.ordinal)
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
        dest?.writeString(season)
    }

    override fun compareTo(other: Card): Int {
        val compareCost = cost.compareTo(other.cost)
        return if (compareCost != 0) compareCost else name.compareTo(other.name)
    }
}
