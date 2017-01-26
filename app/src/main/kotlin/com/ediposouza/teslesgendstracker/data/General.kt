package com.ediposouza.teslesgendstracker.data

import android.os.Parcel
import android.os.Parcelable
import android.support.annotation.IntegerRes
import com.ediposouza.teslesgendstracker.NEWS_UUID_PATTERN
import com.ediposouza.teslesgendstracker.R
import org.threeten.bp.LocalDate
import org.threeten.bp.YearMonth
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

data class Season(

        val id: Int,
        val uuid: String,
        val date: YearMonth,
        val rewardCardAttr: String,
        val rewardCardShortname: String?

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