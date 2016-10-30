package com.ediposouza.teslesgendstracker

import android.os.Build
import android.os.Bundle
import android.support.design.widget.NavigationView
import android.support.v7.app.ActionBarDrawerToggle
import android.support.v7.app.AppCompatActivity
import android.view.MenuItem
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_dash.*

class DashActivity : AppCompatActivity(),
        NavigationView.OnNavigationItemSelectedListener {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dash)
        if(Build.VERSION.SDK_INT == Build.VERSION_CODES.KITKAT){
            //window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
            //window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
        }

        setSupportActionBar(dash_toolbar)
        val drawerToggle = ActionBarDrawerToggle(this, dash_drawer_layout, dash_toolbar,
                R.string.drawer_open, R.string.drawer_close)
        dash_drawer_layout.addDrawerListener(drawerToggle)
        dash_navigation_view.setNavigationItemSelectedListener(this)
        drawerToggle.syncState()
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.menu_cards -> {
                Toast.makeText(this, "cards", Toast.LENGTH_SHORT).show()
                return true
            }
            R.id.menu_collection -> {
                Toast.makeText(this, "collection", Toast.LENGTH_SHORT).show()
                return true
            }
        }
        return false
    }

}
