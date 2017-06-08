package com.ediposouza.teslesgendstracker.ui.basics

import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.ediposouza.teslesgendstracker.R
import com.ediposouza.teslesgendstracker.data.*
import com.ediposouza.teslesgendstracker.interactor.PublicInteractor
import com.ediposouza.teslesgendstracker.ui.base.BaseFragment
import com.ediposouza.teslesgendstracker.util.inflate
import kotlinx.android.synthetic.main.fragment_basics_ranked.*
import kotlinx.android.synthetic.main.itemlist_basics_ranked.view.*

/**
 * Created by EdipoSouza on 6/5/17.
 */
class BasicsRankedFragment : BaseFragment() {

    val rankedAdapter by lazy { RankedAdapter() }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return container?.inflate(R.layout.fragment_basics_ranked)
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        with(basics_ranked_recycler_view) {
            layoutManager = LinearLayoutManager(context)
            adapter = rankedAdapter
        }
        PublicInteractor.getRanked {
            rankedAdapter.updateList(it.reversed())
        }
    }

    class RankedAdapter(val items: MutableList<Ranked> = mutableListOf()) : RecyclerView.Adapter<RankedViewHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): RankedViewHolder {
            return RankedViewHolder(parent?.inflate(R.layout.itemlist_basics_ranked))
        }

        override fun onBindViewHolder(holder: RankedViewHolder?, position: Int) {
            holder?.bind(items[position])
        }

        override fun getItemCount(): Int = items.size

        fun updateList(ranks: List<Ranked>) {
            items.clear()
            items.addAll(ranks)
            notifyDataSetChanged()
        }

    }

    class RankedViewHolder(view: View?) : RecyclerView.ViewHolder(view) {

        fun bind(ranked: Ranked) {
            with(itemView) {
                val legend = ranked.rank == 0
                basics_ranked_level.setText("Legend".takeIf { legend } ?: "${ranked.rank}")
                basics_ranked_name.setText(ranked.name)
                basics_ranked_name.visibility = View.VISIBLE.takeUnless { legend } ?: View.GONE
                basics_ranked_reset_value.setText("${ranked.reset}")
                basics_ranked_stars_value.setText("${ranked.stars}")
                basics_ranked_stars_value.visibility = View.VISIBLE.takeUnless { legend } ?: View.INVISIBLE
                basics_ranked_stars.visibility = View.VISIBLE.takeUnless { legend } ?: View.INVISIBLE
                basics_ranked_monthly_value.setText("${ranked.monthly}x")
                basics_ranked_gems_value.setText("${ranked.gems}")
                basics_ranked_gold_value.setText("${ranked.gold}")
            }
        }

    }

}