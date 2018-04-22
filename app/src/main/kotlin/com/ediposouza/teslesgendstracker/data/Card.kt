package com.ediposouza.teslesgendstracker.data

import android.app.Activity
import android.content.Context
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Parcel
import android.os.Parcelable
import android.support.annotation.DrawableRes
import android.support.v4.app.Fragment
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.SimpleTarget
import com.bumptech.glide.request.target.Target
import com.bumptech.glide.request.transition.Transition
import com.ediposouza.teslesgendstracker.R
import com.ediposouza.teslesgendstracker.TEXT_UNKNOWN
import com.ediposouza.teslesgendstracker.util.getCurrentVersion
import com.google.firebase.storage.FirebaseStorage
import timber.log.Timber

/**
 * Created by ediposouza on 10/31/16.
 */
enum class CardSet(val title: String) {

    CORE("Core"),
    HEROESOFSKYRIM("Heroes of Skyrim"),
    MADHOUSE("Madhouse Collection"),
    FALLOFTHEDARKBROTHERHOOD("The Fall of the Dark Brotherhood"),
    RETURNTOCLOCKWORKCITY("Return to Clockwork City"),
    FORGOTTENHERO("Forgotten Hero Collection"),
    HOUSESOFMORROWIND("Houses of Morrowind"),
    TOKENS("Tokens"),
    UNKNOWN(TEXT_UNKNOWN);

    var unknownSetName = ""
    var unknownSetTitle = ""

    val db: String
        get() = name.toLowerCase().takeIf { this != UNKNOWN } ?: unknownSetName.toLowerCase()

    override fun toString(): String {
        return name.takeIf { this != UNKNOWN } ?: unknownSetName
    }

    companion object {

        fun of(value: String): CardSet {
            val name = value.trim().toUpperCase().replace(" ", "_")
            return if (values().map { it.name }.contains(name)) valueOf(name) else
                UNKNOWN.apply {
                    unknownSetName = value.toUpperCase()
                    unknownSetTitle = value.capitalize()
                }
        }

    }

}

enum class CardAttribute(@DrawableRes val imageRes: Int, val isBasic: Boolean = true) {

    STRENGTH(R.drawable.attr_strength),
    INTELLIGENCE(R.drawable.attr_intelligence),
    WILLPOWER(R.drawable.attr_willpower),
    AGILITY(R.drawable.attr_agility),
    ENDURANCE(R.drawable.attr_endurance),
    NEUTRAL(R.drawable.attr_neutral, false),
    DUAL(R.drawable.attr_triple, false),
    UNKNOWN(R.drawable.attr_neutral, false);

    companion object {

        fun of(value: String): CardAttribute {
            val name = value.trim().toUpperCase().replace(" ", "_")
            return if (values().map { it.name }.contains(name)) valueOf(name) else UNKNOWN
        }

    }


}

enum class CardRarity(val soulCost: Int, @DrawableRes val imageRes: Int) {

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
    FALMER(""),
    FISH(""),
    GIANT(""),
    GOBLIN(""),
    GOD(""),
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
    NETCH(""),
    OGRE(""),
    PASTRY(""),
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
    WEREWOLF(""),
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
    BATTLE,
    BEAST_FORM,
    BETRAY,
    BREAKTHROUGH,
    CHANGE,
    CHARGE,
    COVER,
    DRAIN,
    EXALT,
    EVOLVES,
    FETCH,
    GUARD,
    LAST_GASP,
    LETHAL,
    PILFER,
    PLOT,
    PROPHECY,
    RALLY,
    REGENERATE,
    ROLL_OVER,
    SHACKLE,
    SILENCE,
    SHOUT,
    SLAY,
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

enum class CardArenaTier(val value: Int = 0) {

    TERRIBLE(10),
    POOR(20),
    AVERAGE(30),
    GOOD(50),
    EXCELLENT(70),
    INSANE(90),
    UNKNOWN(),
    NONE();

    companion object {

        fun of(value: String): CardArenaTier {
            val name = value.trim().toUpperCase().replace(" ", "_")
            return if (values().map { it.name }.contains(name)) valueOf(name) else UNKNOWN
        }

    }

}

data class CardArenaTierPlus(

        val type: CardArenaTierPlusType,
        val operator: CardArenaTierPlusOperator?,
        val value: String

) : Parcelable {

    companion object {
        @JvmField val CREATOR: Parcelable.Creator<CardArenaTierPlus> = object : Parcelable.Creator<CardArenaTierPlus> {
            override fun createFromParcel(source: Parcel): CardArenaTierPlus = CardArenaTierPlus(source)
            override fun newArray(size: Int): Array<CardArenaTierPlus?> = arrayOfNulls(size)
        }
    }

    constructor(source: Parcel) : this(CardArenaTierPlusType.values()[source.readInt()],
            with(source.readInt()) { if (this > -1) CardArenaTierPlusOperator.values()[this] else null },
            source.readString())

    override fun writeToParcel(dest: Parcel?, flags: Int) {
        dest?.writeInt(type.ordinal)
        dest?.writeInt(operator?.ordinal ?: -1)
        dest?.writeString(value)
    }

    override fun describeContents(): Int = 0

}

enum class CardArenaTierPlusOperator {

    EQUALS,
    GREAT,
    MINOR,
    UNKNOWN;

    companion object {

        fun of(value: String): CardArenaTierPlusOperator {
            return when (value) {
                "=" -> EQUALS
                ">" -> GREAT
                "<" -> MINOR
                else -> UNKNOWN
            }
        }

    }

}

enum class CardArenaTierPlusType(val extraPoints: Int = 5) {

    ATTACK(),
    ATTR(2),
    COST(),
    HEALTH(),
    KEYWORD(),
    RACE(),
    STRATEGY(),
    TEXT(),
    TYPE(),
    UNKNOWN();

    companion object {

        fun of(value: String): CardArenaTierPlusType {
            val name = value.trim().toUpperCase().replace(" ", "_")
            return if (values().map { it.name }.contains(name)) valueOf(name) else UNKNOWN
        }

    }

}

data class CardMissing(

        val shortName: String,
        val rarity: CardRarity,
        val qtd: Int

) : Parcelable {

    companion object {
        @JvmField val CREATOR: Parcelable.Creator<CardMissing> = object : Parcelable.Creator<CardMissing> {
            override fun createFromParcel(source: Parcel): CardMissing = CardMissing(source)
            override fun newArray(size: Int): Array<CardMissing?> = arrayOfNulls(size)
        }
    }

    constructor(source: Parcel) : this(source.readString(), CardRarity.values()[source.readInt()], source.readInt())

    override fun writeToParcel(dest: Parcel?, flags: Int) {
        dest?.writeString(shortName)
        dest?.writeInt(rarity.ordinal)
        dest?.writeInt(qtd)
    }

    override fun describeContents(): Int = 0

}

data class CardStatistic(

        val shortName: String,
        val rarity: CardRarity,
        val unique: Boolean

)

data class CardBasicInfo(

        val shortName: String,
        val set: String,
        val attr: String,
        val isToken: Boolean
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
        val dualAttr3: CardAttribute,
        val rarity: CardRarity,
        val unique: Boolean,
        val cost: Int,
        val attack: Int,
        val health: Int,
        val type: CardType,
        val race: CardRace,
        val keywords: List<CardKeyword>,
        val text: String,
        val arenaTier: CardArenaTier,
        val arenaTierPlus: List<CardArenaTierPlus?>,
        val evolves: Boolean,
        val season: String,
        val shout: Int,
        val creators: List<String>,
        val generates: List<String>,
        val generators: List<String>,
        val tokens: List<String>,
        val lore: String,
        val loreLink: String,
        val hasAlternativeArt: Boolean,
        val alternativeArtSource: String

) : Comparable<Card>, Parcelable {

    companion object {
        @JvmField val CREATOR: Parcelable.Creator<Card> = object : Parcelable.Creator<Card> {
            override fun createFromParcel(source: Parcel): Card = Card(source)
            override fun newArray(size: Int): Array<Card?> = arrayOfNulls(size)
        }

        val DUMMY = Card("", "", CardSet.CORE, CardAttribute.DUAL, CardAttribute.STRENGTH,
                CardAttribute.WILLPOWER, CardAttribute.NEUTRAL, CardRarity.EPIC, false, 0, 0, 0,
                CardType.ACTION, CardRace.ARGONIAN, emptyList<CardKeyword>(), "", CardArenaTier.AVERAGE,
                listOf(), false, "", 0, listOf(), listOf(), listOf(), listOf(), "", "", false, "")

        const val ALT_SUFFIX = "_alt"
        const val ARTS_PATH = "Arts"
        const val ARTS_TOKENS_PATH = "TokensArts"
        const val SOUNDS_PATH = "Sounds"
        const val SOUND_TYPE_ATTACK = "attack"
        const val SOUND_TYPE_PLAY = "enter_play"
        const val SOUND_TYPE_EXTRA = "extra"

    }

    constructor(source: Parcel) : this(source.readString(), source.readString(),
            CardSet.values()[source.readInt()],
            CardAttribute.values()[source.readInt()], CardAttribute.values()[source.readInt()],
            CardAttribute.values()[source.readInt()], CardAttribute.values()[source.readInt()],
            CardRarity.values()[source.readInt()], 1 == source.readInt(), source.readInt(),
            source.readInt(), source.readInt(), CardType.values()[source.readInt()], CardRace.values()[source.readInt()],
            mutableListOf<CardKeyword>().apply { source.readList(this, CardKeyword::class.java.classLoader) },
            source.readString(), CardArenaTier.values()[source.readInt()],
            mutableListOf<CardArenaTierPlus>().apply { source.readList(this, CardArenaTierPlus::class.java.classLoader) },
            1 == source.readInt(), source.readString(), source.readInt(),
            mutableListOf<String>().apply { source.readStringList(this) },
            mutableListOf<String>().apply { source.readStringList(this) },
            mutableListOf<String>().apply { source.readStringList(this) },
            mutableListOf<String>().apply { source.readStringList(this) }, source.readString(),
            source.readString(), 1 == source.readInt(), source.readString())

    override fun describeContents() = 0

    fun fullArtPath(): String {
        val setName = set.name.toLowerCase().capitalize()
        val attrName = attr.name.toLowerCase().capitalize()
        val artPath = "${ARTS_PATH.takeUnless { isToken() } ?: ARTS_TOKENS_PATH}/$setName/$attrName/$shortName.webp"
        return artPath
    }

    fun getCardFullArtBitmap(context: Context, onLoaded: (Bitmap?) -> Unit) {
        try {
            onLoaded(BitmapFactory.decodeStream(context.resources.assets.open(fullArtPath())))
        } catch (e: Exception) {
            onLoaded(null)
        }
        val path = "v${context.getCurrentVersion()}/${fullArtPath()}"
        if (context is Activity && context.isDestroyed) {
            return
        }
        if (context is Fragment && context.activity?.isDestroyed ?: true) {
            return
        }
        FirebaseStorage.getInstance().reference.child(path).downloadUrl.addOnSuccessListener { uri ->
            Glide.with(context)
                    .asBitmap()
                    .load(uri)
                    .apply(RequestOptions.diskCacheStrategyOf(DiskCacheStrategy.RESOURCE))
                    .listener(object : RequestListener<Bitmap?> {
                        override fun onResourceReady(resource: Bitmap?, model: Any?, target: Target<Bitmap?>?, dataSource: DataSource?, isFirstResource: Boolean): Boolean {
                            if (resource != null) {
                                onLoaded(resource)
                            }
                            return true
                        }

                        override fun onLoadFailed(e: GlideException?, model: Any?, target: Target<Bitmap?>?, isFirstResource: Boolean): Boolean {
                            Timber.d(e)
                            onLoaded(null)
                            return true
                        }

                    })
                    .into(object : SimpleTarget<Bitmap>() {
                        override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
                            onLoaded(resource)
                        }

                    })
        }
    }

    fun isAlternativeArt(): Boolean = shortName.endsWith(ALT_SUFFIX)

    fun toAlternativeArt(): Card = copy(shortName = "$shortName$ALT_SUFFIX")

    fun canBeGenerated(): Boolean = generators.isNotEmpty()

    fun canGenerateCards(): Boolean = generates.isNotEmpty()

    fun canGenerateTokens(): Boolean = tokens.isNotEmpty()

    fun isToken(): Boolean = creators.isNotEmpty()

    fun hasLocalAttackSound(resources: Resources): Boolean {
        return resources.getAssets().list(getLocalCardSoundPath())
                .contains("${shortName.removeSuffix(ALT_SUFFIX)}_$SOUND_TYPE_ATTACK.mp3")
    }

    fun hasLocalPlaySound(resources: Resources): Boolean {
        return resources.getAssets().list(getLocalCardSoundPath())
                .contains("${shortName.removeSuffix(ALT_SUFFIX)}_$SOUND_TYPE_PLAY.mp3")
    }

    fun hasLocalExtraSound(resources: Resources): Boolean {
        return resources.getAssets().list(getLocalCardSoundPath())
                .contains("${shortName.removeSuffix(ALT_SUFFIX)}_$SOUND_TYPE_EXTRA.mp3")
    }

    private fun getLocalCardSoundPath(): String {
        val setName = set.name.toLowerCase().capitalize()
        val attrName = attr.name.toLowerCase().capitalize()
        return "$SOUNDS_PATH/$setName/$attrName"
    }

    fun attackSoundPath() = soundPath(SOUND_TYPE_ATTACK)

    fun playSoundPath() = soundPath(SOUND_TYPE_PLAY)

    fun extraSoundPath() = soundPath(SOUND_TYPE_EXTRA)

    private fun soundPath(type: String): String {
        val setName = set.name.toLowerCase().capitalize()
        val attrName = attr.name.toLowerCase().capitalize()
        return "$SOUNDS_PATH/$setName/$attrName/${shortName.removeSuffix(ALT_SUFFIX)}_$type.mp3"
    }

    override fun writeToParcel(dest: Parcel?, flags: Int) {
        dest?.writeString(name)
        dest?.writeString(shortName)
        dest?.writeInt(set.ordinal)
        dest?.writeInt(attr.ordinal)
        dest?.writeInt(dualAttr1.ordinal)
        dest?.writeInt(dualAttr2.ordinal)
        dest?.writeInt(dualAttr3.ordinal)
        dest?.writeInt(rarity.ordinal)
        dest?.writeInt((if (unique) 1 else 0))
        dest?.writeInt(cost)
        dest?.writeInt(attack)
        dest?.writeInt(health)
        dest?.writeInt(type.ordinal)
        dest?.writeInt(race.ordinal)
        dest?.writeList(keywords)
        dest?.writeString(text)
        dest?.writeInt(arenaTier.ordinal)
        dest?.writeList(arenaTierPlus)
        dest?.writeInt((if (evolves) 1 else 0))
        dest?.writeString(season)
        dest?.writeInt(shout)
        dest?.writeStringList(creators)
        dest?.writeStringList(generates)
        dest?.writeStringList(generators)
        dest?.writeStringList(tokens)
        dest?.writeString(lore)
        dest?.writeString(loreLink)
        dest?.writeInt((if (hasAlternativeArt) 1 else 0))
        dest?.writeString(alternativeArtSource)
    }

    override fun compareTo(other: Card): Int {
        val compareCost = cost.compareTo(other.cost)
        return if (compareCost != 0) compareCost else shortName.compareTo(other.shortName)
    }
}
