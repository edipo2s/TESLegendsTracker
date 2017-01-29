package com.ediposouza.teslesgendstracker.data

import android.os.Parcel
import android.os.Parcelable

/**
 * Created by ediposouza on 25/01/17.
 */
enum class MatchMode {

    RANKED,
    CASUAL,
    ARENA

}

data class MatchDeck(

        val name: String,
        val cls: DeckClass,
        val type: DeckType,
        val deck: String? = null,
        val version: String? = null

) : Parcelable {

    companion object {
        @JvmField val CREATOR: Parcelable.Creator<MatchDeck> = object : Parcelable.Creator<MatchDeck> {
            override fun createFromParcel(source: Parcel): MatchDeck = MatchDeck(source)
            override fun newArray(size: Int): Array<MatchDeck?> = arrayOfNulls(size)
        }
    }

    constructor(source: Parcel) : this(source.readString(), DeckClass.values()[source.readInt()],
            DeckType.values()[source.readInt()], source.readString(), source.readString())

    override fun describeContents() = 0

    override fun writeToParcel(dest: Parcel?, flags: Int) {
        dest?.writeString(name)
        dest?.writeInt(cls.ordinal)
        dest?.writeInt(type.ordinal)
        dest?.writeString(deck)
        dest?.writeString(version)
    }
}

data class Match(

        val uuid: String,
        val first: Boolean,
        val player: MatchDeck,
        val opponent: MatchDeck,
        val mode: MatchMode,
        val season: String,
        val rank: Int,
        val legend: Boolean,
        val win: Boolean

) : Parcelable {

    companion object {
        @JvmField val CREATOR: Parcelable.Creator<Match> = object : Parcelable.Creator<Match> {
            override fun createFromParcel(source: Parcel): Match = Match(source)
            override fun newArray(size: Int): Array<Match?> = arrayOfNulls(size)
        }
    }

    constructor(source: Parcel) : this(source.readString(), 1 == source.readInt(),
            source.readParcelable<MatchDeck>(MatchDeck::class.java.classLoader),
            source.readParcelable<MatchDeck>(MatchDeck::class.java.classLoader),
            MatchMode.values()[source.readInt()], source.readString(), source.readInt(),
            1 == source.readInt(), 1 == source.readInt())

    override fun describeContents() = 0

    override fun writeToParcel(dest: Parcel?, flags: Int) {
        dest?.writeString(uuid)
        dest?.writeInt((if (first) 1 else 0))
        dest?.writeParcelable(player, 0)
        dest?.writeParcelable(opponent, 0)
        dest?.writeInt(mode.ordinal)
        dest?.writeString(season)
        dest?.writeInt(rank)
        dest?.writeInt((if (legend) 1 else 0))
        dest?.writeInt((if (win) 1 else 0))
    }
}