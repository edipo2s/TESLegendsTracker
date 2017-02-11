package com.ediposouza.teslesgendstracker.data

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