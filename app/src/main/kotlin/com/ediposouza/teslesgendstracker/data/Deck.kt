package com.ediposouza.teslesgendstracker.data

import android.os.Parcel
import android.os.Parcelable
import org.threeten.bp.LocalDateTime
import java.util.*

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

        val id: String,
        val owner: String,
        val comment: String,
        val date: LocalDateTime

) : Parcelable {
    companion object {
        @JvmField val CREATOR: Parcelable.Creator<DeckComment> = object : Parcelable.Creator<DeckComment> {
            override fun createFromParcel(source: Parcel): DeckComment = DeckComment(source)
            override fun newArray(size: Int): Array<DeckComment?> = arrayOfNulls(size)
        }
    }

    constructor(source: Parcel) : this(source.readString(), source.readString(), source.readString(),
            source.readSerializable() as LocalDateTime)

    override fun describeContents() = 0

    override fun writeToParcel(dest: Parcel?, flags: Int) {
        dest?.writeString(id)
        dest?.writeString(owner)
        dest?.writeString(comment)
        dest?.writeSerializable(date)
    }
}

data class Deck(

        val id: String,
        val name: String,
        val owner: String,
        val private: Boolean,
        val type: DeckType,
        val cls: Class,
        val cost: Int,
        val createdAt: LocalDateTime,
        val updatedAt: LocalDateTime,
        val patch: String,
        val likes: List<String>,
        val views: Int,
        val cards: Map<String, Long>,
        val updates: List<DeckUpdate>,
        val comments: List<DeckComment>

) : Parcelable {

    constructor() : this("", "", "", false, DeckType.OTHER, Class.NEUTRAL, 0, LocalDateTime.now(),
            LocalDateTime.now(), "", listOf(), 0, mapOf(), listOf(), listOf())

    companion object {
        @JvmField val CREATOR: Parcelable.Creator<Deck> = object : Parcelable.Creator<Deck> {
            override fun createFromParcel(source: Parcel): Deck = Deck(source)
            override fun newArray(size: Int): Array<Deck?> = arrayOfNulls(size)
        }
    }

    constructor(source: Parcel) : this(source.readString(), source.readString(), source.readString(),
            1 == source.readInt(), DeckType.values()[source.readInt()], Class.values()[source.readInt()],
            source.readInt(), source.readSerializable() as LocalDateTime, source.readSerializable() as LocalDateTime,
            source.readString(), source.createStringArrayList(), source.readInt(),
            hashMapOf<String, Long>().apply { source.readMap(this, Long::class.java.classLoader) },
            ArrayList<DeckUpdate>().apply { source.readList(this, DeckUpdate::class.java.classLoader) },
            ArrayList<DeckComment>().apply { source.readList(this, DeckComment::class.java.classLoader) })

    override fun describeContents() = 0

    override fun writeToParcel(dest: Parcel?, flags: Int) {
        dest?.writeString(id)
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
}