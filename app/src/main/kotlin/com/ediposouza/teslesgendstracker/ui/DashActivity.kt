package com.ediposouza.teslesgendstracker.ui

import android.os.Bundle
import android.support.design.widget.NavigationView
import android.support.v4.app.Fragment
import android.support.v7.app.ActionBarDrawerToggle
import android.view.Gravity
import android.view.MenuItem
import com.ediposouza.teslesgendstracker.R
import com.ediposouza.teslesgendstracker.ui.base.BaseActivity
import com.ediposouza.teslesgendstracker.ui.cards.CardsFragment
import kotlinx.android.synthetic.main.activity_dash.*

class DashActivity : BaseActivity(),
        NavigationView.OnNavigationItemSelectedListener {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dash)
        dash_filter_rarity.filterClick = { mEventBus.post(CmdFilterRarity(it)) }
        dash_filter_magika.filterClick = { mEventBus.post(CmdFilterMagika(it)) }
    }

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)
        val drawerToggle = ActionBarDrawerToggle(this, dash_drawer_layout, dash_toolbar,
                R.string.drawer_open, R.string.drawer_close)
        dash_drawer_layout.addDrawerListener(drawerToggle)
        dash_navigation_view.setNavigationItemSelectedListener(this)
        dash_navigation_view.menu.findItem(R.id.menu_cards)?.isChecked = true
        drawerToggle.syncState()
        supportFragmentManager.beginTransaction()
                .add(R.id.dash_content, CardsFragment())
                .setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out)
                .commit()
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        dash_drawer_layout.closeDrawer(Gravity.START)
        return when (item.itemId) {
            R.id.menu_cards -> showFragment(CardsFragment())
            else -> false
        }
    }

    fun showFragment(frag: Fragment) : Boolean {
        supportFragmentManager.beginTransaction()
                .replace(R.id.dash_content, frag)
                .setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out)
                .addToBackStack(null)
                .commit()
        return true
    }

}