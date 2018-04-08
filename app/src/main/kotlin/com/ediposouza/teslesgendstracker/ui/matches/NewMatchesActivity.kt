package com.ediposouza.teslesgendstracker.ui.matches

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.support.design.widget.CollapsingToolbarLayout
import android.support.v4.app.ActivityCompat
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.RelativeLayout
import com.ediposouza.teslesgendstracker.R
import com.ediposouza.teslesgendstracker.SEASON_UUID_PATTERN
import com.ediposouza.teslesgendstracker.data.*
import com.ediposouza.teslesgendstracker.interactor.PrivateInteractor
import com.ediposouza.teslesgendstracker.ui.base.BaseActivity
import com.ediposouza.teslesgendstracker.ui.matches.tabs.MatchesHistoryFragment
import com.ediposouza.teslesgendstracker.util.*
import jp.wasabeef.recyclerview.adapters.SlideInLeftAnimationAdapter
import kotlinx.android.synthetic.main.activity_new_matches.*
import kotlinx.android.synthetic.main.include_new_matches.*
import org.jetbrains.anko.intentFor
import org.jetbrains.anko.toast
import org.threeten.bp.LocalDate
import org.threeten.bp.LocalDateTime
import org.threeten.bp.format.DateTimeFormatter

class NewMatchesActivity : BaseActivity() {

    companion object {

        private val EXTRA_DECK_NAME = "className"
        private val EXTRA_DECK_CLASS = "classExtra"
        private val EXTRA_DECK_TYPE = "classType"
        private val EXTRA_DECK = "deckExtra"
        private val EXTRA_MATCH_MODE = "modeExtra"

        fun newIntent(context: Context, deckName: String?, deckCls: DeckClass, deckType: DeckType, mode: MatchMode, deck: Deck?): Intent {
            val name = deckName ?: deckCls.name.toLowerCase().capitalize()
            return context.intentFor<NewMatchesActivity>(
                    EXTRA_DECK_NAME to name,
                    EXTRA_DECK_CLASS to deckCls.ordinal,
                    EXTRA_DECK_TYPE to deckType.ordinal,
                    EXTRA_MATCH_MODE to mode.ordinal).apply {
                if (deck != null) {
                    putExtra(EXTRA_DECK, deck)
                }
            }
        }

    }

    private val deckName by lazy { intent.getStringExtra(EXTRA_DECK_NAME) }
    private val deckCls by lazy { DeckClass.values()[intent.getIntExtra(EXTRA_DECK_CLASS, 0)] }
    private val deckType by lazy { DeckType.values()[intent.getIntExtra(EXTRA_DECK_TYPE, 0)] }
    private val mode by lazy { MatchMode.values()[intent.getIntExtra(EXTRA_MATCH_MODE, 0)] }
    private val deck: Deck? by lazy {
        if (intent.hasExtra(EXTRA_DECK)) intent.getParcelableExtra<Deck>(EXTRA_DECK) else null
    }

    private val matchesAddedAdapter = object : RecyclerView.Adapter<MatchesHistoryFragment.MatchViewHolder>() {

        val items = mutableListOf<Match>()

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MatchesHistoryFragment.MatchViewHolder {
            return MatchesHistoryFragment.MatchViewHolder(parent.inflate(R.layout.itemlist_match_history))
        }

        override fun onBindViewHolder(holder: MatchesHistoryFragment.MatchViewHolder, position: Int) {
            holder.bind(items[position], {
                items.removeAt(position)
                notifyItemRemoved(position)
            })
        }

        override fun getItemCount(): Int = items.size

        fun addMatch(match: Match) {
            items.add(match)
            notifyItemInserted(itemCount - 1)
            new_matches_recycler_view.scrollToPosition(itemCount - 1)
        }

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_new_matches)
        val statusBarHeight = resources.getDimensionPixelSize(R.dimen.status_bar_height)
        if (Build.VERSION.SDK_INT == Build.VERSION_CODES.KITKAT) {
            val coverLP = new_matches_class_cover.layoutParams as RelativeLayout.LayoutParams
            coverLP.height = coverLP.height - statusBarHeight
            new_matches_class_cover.layoutParams = coverLP
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            val layoutParams = toolbar.layoutParams as CollapsingToolbarLayout.LayoutParams
            layoutParams.topMargin = statusBarHeight
            toolbar.layoutParams = layoutParams
        }
    }

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        setResult(Activity.RESULT_CANCELED, Intent())

        configViews()
        MetricsManager.trackScreen(MetricScreen.SCREEN_NEW_MATCHES())
    }

    private fun configViews() {
        new_matches_deck_class_name.text = deckName
        new_matches_class_cover.setImageResource(deckCls.imageRes)
        new_matches_class_attr1.setImageResource(deckCls.attr1.imageRes)
        new_matches_class_attr2.setImageResource(deckCls.attr2.imageRes)
        new_matches_deck_cardlist.editMode = true
        new_matches_deck_cardlist.showDeck(deck, false, false, false)
        new_matches_deck_cardlist.visibility = View.VISIBLE.takeIf { deck != null } ?: View.GONE
        new_matches_space_start.visibility = View.GONE.takeIf { deck != null } ?: View.VISIBLE
        new_matches_space_end.visibility = View.GONE.takeIf { deck != null } ?: View.VISIBLE
        new_matches_cards_remains.visibility = View.VISIBLE.takeIf { deck != null } ?: View.GONE
        new_matches_legend.visibility = View.VISIBLE.takeIf { mode == MatchMode.RANKED } ?: View.GONE
        new_matches_rank_label.visibility = View.VISIBLE.takeIf { mode == MatchMode.RANKED } ?: View.GONE
        new_matches_rank.visibility = View.VISIBLE.takeIf { mode == MatchMode.RANKED } ?: View.GONE
        new_matches_rank.setText("".takeIf { mode == MatchMode.RANKED } ?: "0")
        new_matches_opt_type_spinner.adapter = ArrayAdapter<String>(this,
                R.layout.widget_spinner_white_text, DeckType.values()
                .filter { it != DeckType.ARENA }.map { it.name.toLowerCase().capitalize() }).apply {
            setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        }
        new_matches_opt_class_spinner.apply {
            adapter = MatchesFragment.ClassAdapter(context, R.layout.itemlist_new_match_class, R.color.primary_text)
            limitHeight(8)
        }
        new_matches_recycler_view.apply {
            adapter = SlideInLeftAnimationAdapter(matchesAddedAdapter).apply {
                setDuration(300)
                setFirstOnly(false)
            }
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, true)
            setHasFixedSize(true)
        }
        new_matches_win.rippleDuration = 200
        new_matches_win.setOnRippleCompleteListener { addNewMatch(true) }
        new_matches_loss.rippleDuration = 200
        new_matches_loss.setOnRippleCompleteListener { addNewMatch(false) }
        new_matches_ads_view.load()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_done, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item?.itemId) {
            android.R.id.home,
            R.id.menu_done -> {
                ActivityCompat.finishAfterTransition(this)
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun addNewMatch(win: Boolean) {
        val myDeckCls = deck?.cls ?: deckCls
        val myDeckType = deck?.type ?: deckType
        val myDeckUpdates = deck?.updates ?: listOf()
        val myDeckVersion = "v1 (${deck?.createdAt})".takeIf { myDeckUpdates.isEmpty() } ?:
            "v${myDeckUpdates.size + 1} (${myDeckUpdates.last().date.toLocalDate()}"
        val optDeckCls = DeckClass.values()[new_matches_opt_class_spinner.selectedItemPosition]
        val optDeckType = DeckType.values()[new_matches_opt_type_spinner.selectedItemPosition]
        val currentSeason = LocalDate.now().format(DateTimeFormatter.ofPattern(SEASON_UUID_PATTERN))
        val currentRank = new_matches_rank.text.toString()
        if (currentRank.isEmpty()) {
            new_matches_rank.requestFocus()
            new_matches_rank.error = getString(R.string.new_match_save_rank_error)
            return
        }
        val legendRank = new_matches_legend.isChecked
        val newMatch = Match(LocalDateTime.now().withNano(0).toString(), new_matches_first.isChecked,
                MatchDeck(deck?.name ?: deckName, myDeckCls, myDeckType, deck?.uuid, myDeckVersion),
                MatchDeck(cls = optDeckCls, type = optDeckType),
                mode, currentSeason, currentRank.toInt(), legendRank, win)
        PrivateInteractor.saveMatch(newMatch) {
            setResult(Activity.RESULT_OK)
            new_matches_first.isChecked = false
            new_matches_opt_class_spinner.setSelection(0)
            new_matches_opt_type_spinner.setSelection(0)
            new_matches_deck_cardlist.showDeck(deck, false, false, false)
            matchesAddedAdapter.addMatch(newMatch)
            toast(R.string.new_match_saved)
            val deckTrackerUsed = new_matches_deck_cardlist.getCards().size < deck?.cards?.size ?: 0
            MetricsManager.trackAction(MetricAction.ACTION_NEW_MATCH_SAVE(myDeckCls, myDeckType,
                    optDeckCls, optDeckType, mode, currentSeason, legendRank, deckTrackerUsed))
        }
    }

}