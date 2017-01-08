package com.ediposouza.teslesgendstracker.data

import android.os.Parcel
import android.os.Parcelable

/**
 * Created by EdipoSouza on 10/31/16.
 */
data class UserInfo(

        val name: String,
        val photoUrl: String

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

data class Patch(

        val uuidDate: String,
        val desc: String

)

data class Season(

        val id: Int,
        val uuid: String,
        val desc: String,
        val reward: String

)

enum class MatchMode {

    RANKED,
    CASUAL,
    ARENA

}

data class MatchDeck(

        val name: String,
        val cls: Class,
        val type: DeckType,
        val deck: String? = null,
        val version: String? = null

)

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

)