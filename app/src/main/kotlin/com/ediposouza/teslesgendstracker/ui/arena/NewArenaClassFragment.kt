package com.ediposouza.teslesgendstracker.ui.decks.tabs

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.LinearSnapHelper
import android.support.v7.widget.RecyclerView
import android.view.*
import com.ediposouza.teslesgendstracker.R
import com.ediposouza.teslesgendstracker.data.DeckClass
import com.ediposouza.teslesgendstracker.ui.base.BaseFragment
import com.ediposouza.teslesgendstracker.util.inflate
import kotlinx.android.synthetic.main.fragment_arena_class.*
import kotlinx.android.synthetic.main.itemlist_arena_class.view.*

/**
 * Created by EdipoSouza on 11/18/16.
 */
class NewArenaClassFragment : BaseFragment() {

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return container?.inflate(R.layout.fragment_arena_class)
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setHasOptionsMenu(true)
        with(activity as AppCompatActivity) {
            setSupportActionBar(toolbar)
            supportActionBar?.title = ""
            supportActionBar?.setDisplayHomeAsUpEnabled(true)
        }
        with(arena_class_recycler_view) {
            adapter = ClassAdapter() {
                startDraft(it)
            }
            layoutManager = LinearLayoutManager(context, RecyclerView.HORIZONTAL, false)
            LinearSnapHelper().attachToRecyclerView(this)
            setHasFixedSize(true)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
        inflater?.inflate(R.menu.menu_start, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item?.itemId) {
            R.id.menu_start -> {
                with(arena_class_recycler_view) {
                    val lm = layoutManager as LinearLayoutManager
                    val clsAdapter = adapter as ClassAdapter
                    startDraft(clsAdapter.items[lm.findFirstCompletelyVisibleItemPosition()])
                }
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun startDraft(it: DeckClass) {
        fragmentManager.beginTransaction()
                .setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_left,
                        R.anim.slide_in_left, R.anim.slide_out_right)
                .replace(R.id.new_arena_content, NewArenaDraftFragment.newFragment(it))
                .commit()
    }

    class ClassAdapter(val onItemClick: (DeckClass) -> Unit) : RecyclerView.Adapter<ClassViewHolder>() {

        val items = DeckClass.values().filter { it.arenaImageRes != null }

        override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): ClassViewHolder {
            return ClassViewHolder(parent?.inflate(R.layout.itemlist_arena_class))
        }

        override fun onBindViewHolder(holder: ClassViewHolder?, position: Int) {
            holder?.bind(items[position], onItemClick)
        }

        override fun getItemCount(): Int = items.size

    }

    class ClassViewHolder(view: View?) : RecyclerView.ViewHolder(view) {

        init {
            with(itemView) {
                arena_class_item.layoutParams = arena_class_item.layoutParams.apply {
                    width = resources.displayMetrics.widthPixels * 2 / 3
                }
            }
        }

        fun bind(cls: DeckClass, onItemClick: (DeckClass) -> Unit) {
            with(itemView) {
                arena_class_item.setOnClickListener { onItemClick(cls) }
                arena_class_cover.setImageResource(cls.arenaImageRes!!)
                arena_class_name.text = cls.name.toLowerCase().capitalize()
                arena_class_name_shadow.text = cls.name.toLowerCase().capitalize()
            }
        }

    }

}