package com.ediposouza.teslesgendstracker.ui.arena

import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.text.format.DateUtils
import android.view.MenuItem
import com.ediposouza.teslesgendstracker.R
import com.ediposouza.teslesgendstracker.ui.base.BaseFilterActivity
import com.ediposouza.teslesgendstracker.ui.cards.CmdFilterSet
import com.ediposouza.teslesgendstracker.ui.decks.tabs.NewArenaClassFragment
import com.ediposouza.teslesgendstracker.util.MetricScreen
import com.ediposouza.teslesgendstracker.util.MetricsManager

class NewArenaActivity : BaseFilterActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_new_arena_draft)
    }

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)
        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                    .replace(R.id.new_arena_content, NewArenaClassFragment())
                    .commit()
        }
        handler.postDelayed({
            eventBus.post(CmdFilterSet(null))
        }, DateUtils.SECOND_IN_MILLIS)
        MetricsManager.trackScreen(MetricScreen.SCREEN_NEW_ARENA())
    }

    override fun onBackPressed() {
        if (canExit) {
            super.onBackPressed()
        } else {
            showExitConfirm(R.string.deck_exit_confirm)
        }
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item?.itemId) {
            android.R.id.home -> ActivityCompat.finishAfterTransition(this)
        }
        return super.onOptionsItemSelected(item)
    }

}