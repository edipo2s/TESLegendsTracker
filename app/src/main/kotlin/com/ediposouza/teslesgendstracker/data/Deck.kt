package com.ediposouza.teslesgendstracker.data

import android.os.Parcel
import android.os.Parcelable
import android.support.annotation.IntegerRes
import com.ediposouza.teslesgendstracker.R
import org.threeten.bp.LocalDateTime

enum class DeckClass(val attr1: CardAttribute, val attr2: CardAttribute = CardAttribute.NEUTRAL, @IntegerRes val imageRes: Int) {

    ARCHER(CardAttribute.STRENGTH, CardAttribute.AGILITY, R.drawable.deck_class_archer),
    ASSASSIN(CardAttribute.INTELLIGENCE, CardAttribute.AGILITY, R.drawable.deck_class_assassin),
    BATTLEMAGE(CardAttribute.STRENGTH, CardAttribute.INTELLIGENCE, R.drawable.deck_class_battlemage),
    CRUSADER(CardAttribute.STRENGTH, CardAttribute.WILLPOWER, R.drawable.deck_class_crusader),
    MAGE(CardAttribute.INTELLIGENCE, CardAttribute.WILLPOWER, R.drawable.deck_class_mage),
    MONK(CardAttribute.WILLPOWER, CardAttribute.AGILITY, R.drawable.deck_class_monk),
    SCOUT(CardAttribute.AGILITY, CardAttribute.ENDURANCE, R.drawable.deck_class_scout),
    SORCERER(CardAttribute.INTELLIGENCE, CardAttribute.ENDURANCE, R.drawable.deck_class_sorcerer),
    SPELLSWORD(CardAttribute.WILLPOWER, CardAttribute.ENDURANCE, R.drawable.deck_class_spellsword),
    WARRIOR(CardAttribute.STRENGTH, CardAttribute.ENDURANCE, R.drawable.deck_class_warrior),
    STRENGTH(CardAttribute.STRENGTH, imageRes = R.drawable.deck_attr_strength),
    INTELLIGENCE(CardAttribute.INTELLIGENCE, imageRes = R.drawable.deck_attr_intelligence),
    AGILITY(CardAttribute.AGILITY, imageRes = R.drawable.deck_attr_agility),
    WILLPOWER(CardAttribute.WILLPOWER, imageRes = R.drawable.deck_attr_willpower),
    ENDURANCE(CardAttribute.ENDURANCE, imageRes = R.drawable.deck_attr_endurance),
    NEUTRAL(CardAttribute.NEUTRAL, imageRes = R.drawable.deck_attr_neutral);

    companion object {

        fun getClasses(attr: List<CardAttribute>): List<DeckClass> {
            return values().filter { attr.contains(it.attr1) && attr.contains(it.attr2) }
        }

    }

}

enum class DeckType {

    AGGRO,
    ARENA,
    COMBO,
    CONTROL,
    MIDRANGE,
    OTHER

}

data class DeckUpdate(

        val date: LocalDateTime,
        val changes: Map<String, Int>

) : Parcelable {
    companion object {
        @Suppress("unused")
        @JvmField val CREATOR: Parcelable.Creator<DeckUpdate> = object : Parcelable.Creator<DeckUpdate> {
            override fun createFromParcel(source: Parcel): DeckUpdate = DeckUpdate(source)
            override fun newArray(size: Int): Array<DeckUpdate?> = arrayOfNulls(size)
        }
    }

    constructor(source: Parcel) : this(source.readSerializable() as LocalDateTime,
            hashMapOf<String, Int>().apply { source.readMap(this, Int::class.java.classLoader) })

    override fun describeContents() = 0

    override fun writeToParcel(dest: Parcel?, flags: Int) {
        dest?.writeSerializable(date)
        dest?.writeMap(changes)
    }
}

data class DeckComment(

        val uuid: String,
        val owner: String,
        val comment: String,
        val date: LocalDateTime

) : Parcelable {
    companion object {
        @Suppress("unused")
        @JvmField val CREATOR: Parcelable.Creator<DeckComment> = object : Parcelable.Creator<DeckComment> {
            override fun createFromParcel(source: Parcel): DeckComment = DeckComment(source)
            override fun newArray(size: Int): Array<DeckComment?> = arrayOfNulls(size)
        }
    }

    constructor(source: Parcel) : this(source.readString(), source.readString(), source.readString(),
            source.readSerializable() as LocalDateTime)

    override fun describeContents() = 0

    override fun writeToParcel(dest: Parcel?, flags: Int) {
        dest?.writeString(uuid)
        dest?.writeString(owner)
        dest?.writeString(comment)
        dest?.writeSerializable(date)
    }

    override fun toString(): String = "DeckComment(id='$uuid', owner='$owner', comment='$comment', date=$date)"

}

data class Deck(

        val uuid: String,
        val name: String,
        val owner: String,
        val private: Boolean,
        val type: DeckType,
        val cls: DeckClass,
        val cost: Int,
        val createdAt: LocalDateTime,
        val updatedAt: LocalDateTime,
        val patch: String,
        val likes: List<String>,
        val views: Int,
        val cards: Map<String, Int>,
        val updates: List<DeckUpdate>,
        val comments: List<DeckComment>

) : Parcelable {

    constructor() : this("", "", "", false, DeckType.OTHER, DeckClass.NEUTRAL, 0, LocalDateTime.now().withNano(0),
            LocalDateTime.now().withNano(0), "", listOf(), 0, mapOf(), listOf(), listOf())

    companion object {
        @Suppress("unused")
        @JvmField val CREATOR: Parcelable.Creator<Deck> = object : Parcelable.Creator<Deck> {
            override fun createFromParcel(source: Parcel): Deck = Deck(source)
            override fun newArray(size: Int): Array<Deck?> = arrayOfNulls(size)
        }
    }

    constructor(source: Parcel) : this(source.readString(), source.readString(), source.readString(),
            1 == source.readInt(), DeckType.values()[source.readInt()], DeckClass.values()[source.readInt()],
            source.readInt(), source.readSerializable() as LocalDateTime, source.readSerializable() as LocalDateTime,
            source.readString(), source.createStringArrayList(), source.readInt(),
            hashMapOf<String, Int>().apply { source.readMap(this, Int::class.java.classLoader) },
            mutableListOf<DeckUpdate>().apply { source.readList(this, DeckUpdate::class.java.classLoader) },
            mutableListOf<DeckComment>().apply { source.readList(this, DeckComment::class.java.classLoader) })

    override fun describeContents() = 0

    override fun writeToParcel(dest: Parcel?, flags: Int) {
        dest?.writeString(uuid)
        dest?.writeString(name)
        dest?.writeString(owner)
        dest?.writeInt((if (private) 1 else 0))
        dest?.writeInt(type.ordinal)
        dest?.writeInt(cls.ordinal)
        dest?.writeInt(cost)
        dest?.writeSerializable(createdAt)
        dest?.writeSerializable(updatedAt)
        dest?.writeString(patch)
        dest?.writeStringList(likes)
        dest?.writeInt(views)
        dest?.writeMap(cards)
        dest?.writeList(updates)
        dest?.writeList(comments)
    }

    override fun toString(): String {
        return "Deck(id='$uuid', name='$name', owner='$owner', private=$private, type=$type, cls=$cls, cost=$cost, createdAt=$createdAt, updatedAt=$updatedAt, patch='$patch', likes=$likes, views=$views, cards=$cards, updates=$updates, comments=$comments)"
    }

}
