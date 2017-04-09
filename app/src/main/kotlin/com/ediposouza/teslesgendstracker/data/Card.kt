package com.ediposouza.teslesgendstracker.data

import android.content.Context
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import android.os.Parcel
import android.os.Parcelable
import android.support.annotation.IntegerRes
import android.support.v4.content.ContextCompat
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.Transformation
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.engine.Resource
import com.bumptech.glide.load.resource.bitmap.BitmapResource
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.animation.GlideAnimation
import com.bumptech.glide.request.target.SimpleTarget
import com.bumptech.glide.request.target.Target
import com.ediposouza.teslesgendstracker.R
import com.ediposouza.teslesgendstracker.TEXT_UNKNOWN
import com.firebase.ui.storage.images.FirebaseImageLoader
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import timber.log.Timber

/**
 * Created by ediposouza on 10/31/16.
 */
enum class CardSet(val title: String) {

    CORE("Core"),
    MADHOUSE("Madhouse Collection"),
    FALLOFTHEDARKBROTHERHOOD("Fall of the Dark Brotherhood"),
    TOKENS("Tokens"),
    UNKNOWN(TEXT_UNKNOWN);

    var unknownSetName = ""

    val db = name.toLowerCase()

    override fun toString(): String {
        return name.takeIf { this != UNKNOWN } ?: unknownSetName
    }

    companion object {

        fun of(value: String): CardSet {
            val name = value.trim().toUpperCase().replace(" ", "_")
            return if (values().map { it.name }.contains(name)) valueOf(name) else UNKNOWN
        }

    }

}

enum class CardAttribute(@IntegerRes val imageRes: Int, val isBasic: Boolean = true) {

    STRENGTH(R.drawable.attr_strength),
    INTELLIGENCE(R.drawable.attr_intelligence),
    WILLPOWER(R.drawable.attr_willpower),
    AGILITY(R.drawable.attr_agility),
    ENDURANCE(R.drawable.attr_endurance),
    NEUTRAL(R.drawable.attr_neutral, false),
    DUAL(R.drawable.attr_dual, false)

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
    CHANGES,
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
        val text: String,
        val arenaTier: CardArenaTier,
        val arenaTierPlus: CardArenaTierPlus?,
        val evolves: Boolean,
        val season: String

) : Comparable<Card>, Parcelable {

    companion object {
        @JvmField val CREATOR: Parcelable.Creator<Card> = object : Parcelable.Creator<Card> {
            override fun createFromParcel(source: Parcel): Card = Card(source)
            override fun newArray(size: Int): Array<Card?> = arrayOfNulls(size)
        }

        val DUMMY = Card("", "", CardSet.CORE, CardAttribute.DUAL, CardAttribute.STRENGTH,
                CardAttribute.WILLPOWER, CardRarity.EPIC, false, 0, 0, 0, CardType.ACTION,
                CardRace.ARGONIAN, emptyList<CardKeyword>(), "", CardArenaTier.AVERAGE,
                CardArenaTierPlus(CardArenaTierPlusType.ATTACK, CardArenaTierPlusOperator.GREAT, "5"), false, "")

        private const val ARTS_PATH = "Arts"
        private const val CARD_PATH = "Cards"
        private const val SOUNDS_PATH = "Sounds"
        private const val CARD_BACK = "card_back.png"
        private const val SOUND_TYPE_ATTACK = "attack"
        private const val SOUND_TYPE_PLAY = "enter_play"
        private const val SOUND_TYPE_EXTRA = "extra"

        fun getDefaultCardImage(context: Context): Bitmap {
            try {
                val cardBackDrawable = ContextCompat.getDrawable(context, R.drawable.card_back)
                return (cardBackDrawable as BitmapDrawable).bitmap
            } catch (e: Exception) {
                return BitmapFactory.decodeStream(context.resources.assets.open(CARD_BACK))
            }
        }

        fun loadCardImageInto(view: ImageView, cardSet: String, cardAttr: String,
                              cardShortName: String, transform: ((Bitmap) -> Bitmap)? = null,
                              onLoadDefault: (() -> Unit)? = null) {
            if (cardShortName.isEmpty()) {
                return
            }
            val imagePath = getImagePath(cardAttr, cardSet, cardShortName)
            Timber.d(imagePath)
            with(view.context) {
                Glide.with(this)
                        .using(FirebaseImageLoader())
                        .load(FirebaseStorage.getInstance().reference.child(imagePath))
                        .placeholder(BitmapDrawable(resources, getCardImageBitmap(this, imagePath, transform, onLoadDefault)))
                        .bitmapTransform(object : Transformation<Bitmap> {
                            override fun transform(resource: Resource<Bitmap>, outWidth: Int, outHeight: Int): Resource<Bitmap> {
                                if (transform == null) {
                                    return resource
                                }
                                val newBitmap = transform.invoke(resource.get())
                                return BitmapResource.obtain(newBitmap, Glide.get(this@with).getBitmapPool())
                            }

                            override fun getId(): String = imagePath

                        })
                        .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                        .into(view)
            }
        }

        fun loadCardImageInto(context: Context, cardSet: String, cardAttr: String, cardShortName: String,
                              onLoaded: ((Boolean) -> Unit)? = null) {
            val imagePath = getImagePath(cardAttr, cardSet, cardShortName)
            Glide.with(context)
                    .using(FirebaseImageLoader())
                    .load(FirebaseStorage.getInstance().reference.child(imagePath))
                    .asBitmap()
                    .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                    .listener(object : RequestListener<StorageReference?, Bitmap?> {
                        override fun onException(e: java.lang.Exception?, model: StorageReference?, target: Target<Bitmap?>?, isFirstResource: Boolean): Boolean {
                            onLoaded?.invoke(false)
                            return true
                        }

                        override fun onResourceReady(resource: Bitmap?, model: StorageReference?, target: Target<Bitmap?>?, isFromMemoryCache: Boolean, isFirstResource: Boolean): Boolean {
                            return true
                        }
                    })
                    .into(object : SimpleTarget<Bitmap>() {
                        override fun onResourceReady(resource: Bitmap?, glideAnimation: GlideAnimation<in Bitmap>?) {
                            onLoaded?.invoke(true)
                        }
                    })
        }

        private fun getImagePath(cardAttr: String, cardSet: String, cardShortName: String): String {
            val setName = cardSet.toLowerCase().capitalize()
            val attrName = cardAttr.toLowerCase().capitalize()
            val imagePath = "$CARD_PATH/$setName/$attrName/$cardShortName.webp"
            Timber.d(imagePath)
            return imagePath
        }

        private fun getCardImageBitmap(context: Context, imagePath: String, transform: ((Bitmap) -> Bitmap)? = null,
                                       onLoadDefault: (() -> Unit)? = null): Bitmap {
            var localBmp: Bitmap
            try {
                localBmp = BitmapFactory.decodeStream(context.resources.assets.open(imagePath))
            } catch (e: Exception) {
                localBmp = getDefaultCardImage(context)
                onLoadDefault?.invoke()
            }
            return localBmp.takeUnless { transform != null } ?: transform!!.invoke(localBmp)
        }

    }

    constructor(source: Parcel) : this(source.readString(), source.readString(),
            CardSet.values()[source.readInt()],
            CardAttribute.values()[source.readInt()], CardAttribute.values()[source.readInt()],
            CardAttribute.values()[source.readInt()], CardRarity.values()[source.readInt()],
            1 == source.readInt(), source.readInt(), source.readInt(), source.readInt(),
            CardType.values()[source.readInt()], CardRace.values()[source.readInt()],
            mutableListOf<CardKeyword>().apply { source.readList(this, CardKeyword::class.java.classLoader) },
            source.readString(), CardArenaTier.values()[source.readInt()],
            source.readParcelable<CardArenaTierPlus>(CardArenaTierPlus::class.java.classLoader),
            1 == source.readInt(), source.readString())

    override fun describeContents() = 0

    fun loadCardImageInto(view: ImageView, transform: ((Bitmap) -> Bitmap)? = null) {
        Card.loadCardImageInto(view, set.toString(), attr.name, shortName, transform)
    }

    fun patchVersion(context: Context, patchUuid: String, onGetCard: (Card) -> Unit) {
        var patchShortName = "${shortName}_$patchUuid"
        loadCardImageInto(context, set.toString(), attr.name, patchShortName) { patchImageFound ->
            if (!patchImageFound) {
                patchShortName = shortName
            }
            onGetCard(Card(name, patchShortName, set, attr, dualAttr1, dualAttr2, rarity, unique, cost,
                    attack, health, type, race, keywords, text, arenaTier, arenaTierPlus, evolves, season))
        }
    }

    fun fullArtPath(): String {
        val setName = set.name.toLowerCase().capitalize()
        val attrName = attr.name.toLowerCase().capitalize()
        val artPath = "$ARTS_PATH/$setName/$attrName/$shortName.webp"
        Timber.d(artPath)
        return artPath
    }

    fun getCardFullArtBitmap(context: Context, onLoaded: (Bitmap?) -> Unit) {
        try {
            onLoaded(BitmapFactory.decodeStream(context.resources.assets.open(fullArtPath())))
        } catch (e: Exception) {
            onLoaded(null)
        }
        Glide.with(context)
                .using(FirebaseImageLoader())
                .load(FirebaseStorage.getInstance().reference.child(fullArtPath()))
                .asBitmap()
                .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                .listener(object : RequestListener<StorageReference?, Bitmap?> {
                    override fun onException(e: java.lang.Exception?, model: StorageReference?, target: Target<Bitmap?>?, isFirstResource: Boolean): Boolean {
                        onLoaded(null)
                        return true
                    }

                    override fun onResourceReady(resource: Bitmap?, model: StorageReference?, target: Target<Bitmap?>?, isFromMemoryCache: Boolean, isFirstResource: Boolean): Boolean {
                        if (resource != null) {
                            onLoaded(resource)
                        }
                        return true
                    }
                })
                .into(object : SimpleTarget<Bitmap>() {
                    override fun onResourceReady(resource: Bitmap?, glideAnimation: GlideAnimation<in Bitmap>?) {
                        if (resource != null) {
                            onLoaded(resource)
                        }
                    }
                })
    }

    fun hasLocalAttackSound(resources: Resources): Boolean {
        return resources.getAssets().list(getLocalCardSoundPath()).contains("${shortName}_$SOUND_TYPE_ATTACK.mp3")
    }

    fun hasLocalPlaySound(resources: Resources): Boolean {
        return resources.getAssets().list(getLocalCardSoundPath()).contains("${shortName}_$SOUND_TYPE_PLAY.mp3")
    }

    fun hasLocalExtraSound(resources: Resources): Boolean {
        return resources.getAssets().list(getLocalCardSoundPath()).contains("${shortName}_$SOUND_TYPE_EXTRA.mp3")
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
        return "$SOUNDS_PATH/$setName/$attrName/${shortName}_$type.mp3"
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
        dest?.writeString(text)
        dest?.writeInt(arenaTier.ordinal)
        dest?.writeParcelable(arenaTierPlus, 0)
        dest?.writeInt((if (evolves) 1 else 0))
        dest?.writeString(season)
    }

    override fun compareTo(other: Card): Int {
        val compareCost = cost.compareTo(other.cost)
        return if (compareCost != 0) compareCost else name.compareTo(other.name)
    }
}
