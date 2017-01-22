package com.ediposouza.teslesgendstracker.data

import android.os.Parcel
import android.os.Parcelable
import android.support.annotation.IntegerRes
import com.ediposouza.teslesgendstracker.NEWS_UUID_PATTERN
import com.ediposouza.teslesgendstracker.R
import org.threeten.bp.LocalDate
import org.threeten.bp.format.DateTimeFormatter

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

data class Article(

        val title: String,
        val category: ArticleCategory,
        val cover: String,
        val link: String,
        val date: LocalDate?

) : Comparable<Article> {

    val uuidDate: String = date?.format(DateTimeFormatter.ofPattern(NEWS_UUID_PATTERN)) ?: title

    override fun compareTo(other: Article): Int = date?.compareTo(other.date) ?: title.compareTo(other.title)
}

enum class ArticleCategory(@IntegerRes val text: Int) {

    ANNOUNCEMENTS(R.string.article_news_category_announcements),
    BATTLE_TACTICS(R.string.article_news_category_battle),
    FORGING_LEGENDS(R.string.article_news_category_forging),
    IMPERIAL_LIBRARY(R.string.article_news_category_imperial),
    LEGENDARY_BEGINNINGS(R.string.article_news_category_legendary),
    THE_ARENA_DISTRICT(R.string.article_news_category_arena),
    WORLD(R.string.article_world_type)

}

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

) : Parcelable {

    companion object {
        @JvmField val CREATOR: Parcelable.Creator<MatchDeck> = object : Parcelable.Creator<MatchDeck> {
            override fun createFromParcel(source: Parcel): MatchDeck = MatchDeck(source)
            override fun newArray(size: Int): Array<MatchDeck?> = arrayOfNulls(size)
        }
    }

    constructor(source: Parcel) : this(source.readString(), Class.values()[source.readInt()],
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