package com.ediposouza.teslesgendstracker.ui.decks.new

import android.animation.Animator
import android.animation.ValueAnimator
import android.app.Activity
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.support.design.widget.CoordinatorLayout
import android.support.v4.app.ActivityCompat
import android.text.format.DateUtils
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.LinearLayout
import com.ediposouza.teslesgendstracker.MetricScreen
import com.ediposouza.teslesgendstracker.R
import com.ediposouza.teslesgendstracker.data.Attribute
import com.ediposouza.teslesgendstracker.data.Class
import com.ediposouza.teslesgendstracker.ui.base.BaseActivity
import com.ediposouza.teslesgendstracker.ui.base.CmdShowCardsByAttr
import com.ediposouza.teslesgendstracker.ui.base.CmdUpdateRarityMagikaFiltersVisibility
import com.ediposouza.teslesgendstracker.ui.decks.CmdAddCard
import com.ediposouza.teslesgendstracker.ui.decks.CmdRemAttr
import com.ediposouza.teslesgendstracker.ui.widget.filter.CmdFilterClass
import com.ediposouza.teslesgendstracker.ui.widget.filter.CmdFilterMagika
import com.ediposouza.teslesgendstracker.ui.widget.filter.CmdFilterRarity
import kotlinx.android.synthetic.main.activity_new_deck.*
import org.greenrobot.eventbus.Subscribe
import org.jetbrains.anko.alert
import org.jetbrains.anko.toast

class NewDeckActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_new_deck)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            val layoutParams = toolbar.layoutParams as LinearLayout.LayoutParams
            layoutParams.topMargin = resources.getDimensionPixelSize(R.dimen.status_bar_height)
            toolbar.layoutParams = layoutParams
        }

        setResult(Activity.RESULT_CANCELED, Intent())
    }

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolbar_title.text = getString(R.string.new_deck_title)
        with(new_deck_attr_filter) {
            filterClick = {
                eventBus.post(CmdShowCardsByAttr(it))
                new_deck_attr_filter.selectAttr(it, true)
                new_deck_attr_filter.lastAttrSelected = it
            }
        }
        new_deck_cardlist.setListEditMode(true)
        new_deck_filter_rarity.filterClick = { eventBus.post(CmdFilterRarity(it)) }
        new_deck_filter_magika.filterClick = { eventBus.post(CmdFilterMagika(it)) }
        supportFragmentManager.beginTransaction()
                .replace(R.id.new_deck_fragment_cards, NewDeckCardsListFragment())
                .commit()
        Handler().postDelayed({
            eventBus.post(CmdShowCardsByAttr(Attribute.STRENGTH))
        }, DateUtils.SECOND_IN_MILLIS)
        metricsManager.trackScreen(MetricScreen.SCREEN_NEW_DECKS())
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
                alert {
                    val view = View.inflate(this@NewDeckActivity, R.layout.dialog_new_deck, null)
                    customView(view)
                    positiveButton(R.string.new_deck_save_dialog_save) { saveDeck(view) }
                    cancelButton { }
                }
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun saveDeck(view: View?) {
        toast("Saved!")
    }

    @Subscribe
    fun onCmdCardAdd(cmdCardAdd: CmdAddCard) {
        new_deck_cardlist.addCard(cmdCardAdd.card)
        new_deck_attr_filter.lockAttrs(cmdCardAdd.card.dualAttr1, cmdCardAdd.card.dualAttr2)
        updateDualFilter()
    }

    @Subscribe
    fun onCmdRemAttr(cmdRemAttr: CmdRemAttr) {
        new_deck_attr_filter.unlockAttr(cmdRemAttr.attr)
        updateDualFilter()
    }

    private fun updateDualFilter() {
        val cls = Class.getClasses(listOf(new_deck_attr_filter.lockAttr1 ?: Attribute.NEUTRAL,
                new_deck_attr_filter.lockAttr2 ?: Attribute.NEUTRAL)).first()
        eventBus.post(CmdFilterClass(cls))
    }

    @Subscribe
    fun onCmdUpdateRarityMagikaFilters(update: CmdUpdateRarityMagikaFiltersVisibility) {
        val filterMagikaLP = new_deck_filter_magika.layoutParams as CoordinatorLayout.LayoutParams
        val filterRarityLP = new_deck_filter_rarity.layoutParams as CoordinatorLayout.LayoutParams
        val showBottomMargin = resources.getDimensionPixelSize(R.dimen.large_margin)
        val hideBottomMargin = -resources.getDimensionPixelSize(R.dimen.filter_hide_height)
        if (update.show && filterMagikaLP.bottomMargin == showBottomMargin ||
                !update.show && filterMagikaLP.bottomMargin == hideBottomMargin) {
            return
        }
        val animFrom = if (update.show) hideBottomMargin else showBottomMargin
        val animTo = if (update.show) showBottomMargin else hideBottomMargin
        with(ValueAnimator.ofInt(animFrom, animTo)) {
            duration = DateUtils.SECOND_IN_MILLIS
            addUpdateListener {
                filterRarityLP.bottomMargin = it.animatedValue as Int
                filterMagikaLP.bottomMargin = it.animatedValue as Int
                new_deck_filter_magika.layoutParams = filterMagikaLP
                new_deck_filter_rarity.layoutParams = filterRarityLP
            }
            addListener(object : Animator.AnimatorListener {
                override fun onAnimationRepeat(p0: Animator?) {
                }

                override fun onAnimationEnd(p0: Animator?) {
                }

                override fun onAnimationCancel(p0: Animator?) {
                }

                override fun onAnimationStart(p0: Animator?) {
                }

            })
            start()
        }
    }

}