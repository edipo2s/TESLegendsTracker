package com.ediposouza.teslesgendstracker.ui.articles.tabs

import android.net.Uri
import android.support.customtabs.CustomTabsIntent
import android.support.v4.content.ContextCompat
import android.support.v7.widget.RecyclerView
import android.view.View
import android.widget.ImageView
import com.ediposouza.teslesgendstracker.R
import com.ediposouza.teslesgendstracker.data.Article
import com.ediposouza.teslesgendstracker.util.MetricAction
import com.ediposouza.teslesgendstracker.util.MetricsManager
import com.ediposouza.teslesgendstracker.util.loadFromUrl
import kotlinx.android.synthetic.main.itemlist_article_news.view.*
import org.jetbrains.anko.find
import org.threeten.bp.format.DateTimeFormatter
import org.threeten.bp.format.FormatStyle

class ArticleViewHolder(view: View?) : RecyclerView.ViewHolder(view) {

    fun bind(article: Article) {
        with(itemView) {
            article_title.text = article.title
            article_type?.text = context.getString(article.category.text)
            article_date?.text = article.date?.format(DateTimeFormatter.ofLocalizedDate(FormatStyle.SHORT))
            article_cover_loading?.visibility = View.VISIBLE
            val placeholderDrawable = ContextCompat.getDrawable(context, R.drawable.article_cover)
            find<ImageView>(R.id.article_cover).loadFromUrl(article.cover, placeholderDrawable) {
                article_cover_loading?.visibility = View.GONE
            }
            article_item.setOnClickListener {
                CustomTabsIntent.Builder()
                        .setToolbarColor(ContextCompat.getColor(context, R.color.colorPrimary))
                        .setStartAnimations(context, R.anim.slide_in_right, R.anim.slide_out_left)
                        .setExitAnimations(context, R.anim.slide_in_left, R.anim.slide_out_right)
                        .build()
                        .launchUrl(context, Uri.parse(article.link))
                MetricsManager.trackAction(if (article.date == null)
                    MetricAction.ACTION_ARTICLES_VIEW_WORLD(article) else MetricAction.ACTION_ARTICLES_VIEW_NEWS(article))
            }
        }
    }

}