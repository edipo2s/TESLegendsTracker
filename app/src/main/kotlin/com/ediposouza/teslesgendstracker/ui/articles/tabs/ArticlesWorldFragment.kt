package com.ediposouza.teslesgendstracker.ui.articles.tabs

import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.ediposouza.teslesgendstracker.R
import com.ediposouza.teslesgendstracker.data.Article
import com.ediposouza.teslesgendstracker.data.ArticleCategory
import com.ediposouza.teslesgendstracker.ui.base.BaseFragment
import com.ediposouza.teslesgendstracker.util.inflate
import jp.wasabeef.recyclerview.adapters.SlideInRightAnimationAdapter
import kotlinx.android.synthetic.main.fragment_articles_world.*

/**
 * Created by EdipoSouza on 1/21/17.
 */
class ArticlesWorldFragment : BaseFragment() {

    private val worldAdapter by lazy {
        WorldAdapter(listOf(
                Article(getString(R.string.article_world_races_title), ArticleCategory.WORLD,
                        getString(R.string.article_world_races_cover),
                        getString(R.string.article_world_races_link), null),
                Article(getString(R.string.article_world_classes_title), ArticleCategory.WORLD,
                        getString(R.string.article_world_classes_cover),
                        getString(R.string.article_world_classes_link), null),
                Article(getString(R.string.article_world_attributes_title), ArticleCategory.WORLD,
                        getString(R.string.article_world_attributes_cover),
                        getString(R.string.article_world_attributes_link), null)
        ))
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return container?.inflate(R.layout.fragment_articles_world)
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        with(articles_world_recycler_view) {
            layoutManager = object : LinearLayoutManager(context) {
                override fun supportsPredictiveItemAnimations(): Boolean = false
            }
            adapter = SlideInRightAnimationAdapter(worldAdapter).apply {
                setDuration(300)
                setFirstOnly(false)
            }
            setHasFixedSize(true)
        }
    }

    class WorldAdapter(val items: List<Article>) : RecyclerView.Adapter<ArticleViewHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): ArticleViewHolder {
            return ArticleViewHolder(parent?.inflate(R.layout.itemlist_article_world))
        }

        override fun onBindViewHolder(holder: ArticleViewHolder?, position: Int) {
            holder?.bind(items[position])
        }

        override fun getItemCount(): Int = items.size

    }

}