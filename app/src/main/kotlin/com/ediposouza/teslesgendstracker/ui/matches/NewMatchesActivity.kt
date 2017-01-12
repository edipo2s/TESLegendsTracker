package com.ediposouza.teslesgendstracker.ui.matches

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.support.design.widget.CollapsingToolbarLayout
import android.support.v4.app.ActivityCompat
import android.view.Menu
import android.view.MenuItem
import android.widget.ArrayAdapter
import android.widget.RelativeLayout
import com.ediposouza.teslesgendstracker.R
import com.ediposouza.teslesgendstracker.data.Class
import com.ediposouza.teslesgendstracker.data.Deck
import com.ediposouza.teslesgendstracker.data.DeckType
import com.ediposouza.teslesgendstracker.data.MatchMode
import com.ediposouza.teslesgendstracker.interactor.PrivateInteractor
import com.ediposouza.teslesgendstracker.ui.base.BaseActivity
import com.ediposouza.teslesgendstracker.util.MetricScreen
import com.ediposouza.teslesgendstracker.util.MetricsManager
import com.ediposouza.teslesgendstracker.util.limitHeight
import kotlinx.android.synthetic.main.activity_new_matches.*
import kotlinx.android.synthetic.main.include_new_matches.*
import org.jetbrains.anko.intentFor

class NewMatchesActivity : BaseActivity() {

    companion object {

        private val EXTRA_CLASS = "classExtra"
        private val EXTRA_DECK = "deckExtra"
        private val EXTRA_MATCH_MODE = "modeExtra"

        fun newIntent(context: Context, cls: Class, deck: Deck?, mode: MatchMode): Intent {
            if (deck != null) {
                return context.intentFor<NewMatchesActivity>(EXTRA_CLASS to cls.ordinal, EXTRA_DECK to deck,
                        EXTRA_MATCH_MODE to mode.ordinal)
            } else {
                return context.intentFor<NewMatchesActivity>(EXTRA_CLASS to cls.ordinal,
                        EXTRA_MATCH_MODE to mode.ordinal)
            }
        }

    }

    private val privateInteractor by lazy { PrivateInteractor() }
    private val cls by lazy { Class.values()[intent.getIntExtra(EXTRA_CLASS, 0)] }
    private val mode by lazy { MatchMode.values()[intent.getIntExtra(EXTRA_MATCH_MODE, 0)] }
    private val deck: Deck? by lazy {
        if (intent.hasExtra(EXTRA_DECK)) intent.getParcelableExtra<Deck>(EXTRA_DECK) else null
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

        configViews()
    }

    private fun configViews() {
        new_matches_deck_class_name.text = if (deck != null) deck?.name else cls.name.toLowerCase().capitalize()
        new_matches_class_cover.setImageResource(cls.imageRes)
        new_matches_class_attr1.setImageResource(cls.attr1.imageRes)
        new_matches_class_attr2.setImageResource(cls.attr2.imageRes)
        new_matches_deck_cardlist.editMode = true
        new_matches_deck_cardlist.showDeck(deck, false, false, false)
        new_match_type_spinner.adapter = ArrayAdapter<String>(this,
                R.layout.widget_spinner_white_text, DeckType.values()
                .filter { it != DeckType.ARENA }.map { it.name.toLowerCase().capitalize() }).apply {
            setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        }
        new_match_class_spinner.apply {
            adapter = MatchesFragment.ClassAdapter(context, R.layout.itemlist_new_match_class, R.color.primary_text)
            limitHeight(8)
        }
    }

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        setResult(Activity.RESULT_CANCELED, Intent())
        MetricsManager.trackScreen(MetricScreen.SCREEN_NEW_MATCHES())
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_done, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item?.itemId) {
            android.R.id.home -> {
                ActivityCompat.finishAfterTransition(this)
                return true
            }
            R.id.menu_done -> {
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

}