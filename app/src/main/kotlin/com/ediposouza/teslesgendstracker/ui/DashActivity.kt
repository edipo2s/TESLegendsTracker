package com.ediposouza.teslesgendstracker.ui

import android.os.Bundle
import android.support.design.widget.BottomSheetBehavior
import android.support.design.widget.NavigationView
import android.support.v4.app.Fragment
import android.support.v7.app.ActionBarDrawerToggle
import android.view.Gravity
import android.view.MenuItem
import android.view.View
import com.bumptech.glide.Glide
import com.ediposouza.teslesgendstracker.App
import com.ediposouza.teslesgendstracker.R
import com.ediposouza.teslesgendstracker.interactor.PrivateInteractor
import com.ediposouza.teslesgendstracker.interactor.PublicInteractor
import com.ediposouza.teslesgendstracker.ui.base.BaseFilterActivity
import com.ediposouza.teslesgendstracker.ui.base.CmdLoginSuccess
import com.ediposouza.teslesgendstracker.ui.base.CmdShowTabs
import com.ediposouza.teslesgendstracker.ui.cards.CardsFragment
import com.ediposouza.teslesgendstracker.ui.cards.CmdFilterMagika
import com.ediposouza.teslesgendstracker.ui.cards.CmdFilterRarity
import com.ediposouza.teslesgendstracker.ui.decks.DecksFragment
import com.ediposouza.teslesgendstracker.ui.matches.MatchesFragment
import com.ediposouza.teslesgendstracker.ui.util.CircleTransform
import com.ediposouza.teslesgendstracker.ui.widget.CollectionStatistics
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.activity_dash.*
import kotlinx.android.synthetic.main.navigation_drawer_header.view.*
import org.greenrobot.eventbus.Subscribe
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.itemsSequence
import timber.log.Timber

class DashActivity : BaseFilterActivity(),
        NavigationView.OnNavigationItemSelectedListener {

    private val KEY_MENU_ITEM_SELECTED = "menuIndexKey"

    private var menuItemSelected = 0
    private val publicInteractor = PublicInteractor()
    private val privateInteractor = PrivateInteractor()

    private val statisticsSheetBehavior: BottomSheetBehavior<CollectionStatistics> by lazy {
        BottomSheetBehavior.from(cards_collection_statistics)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dash)
        decks_fab_add.hide()
        snackbarNeedMargin = false
        filter_rarity.filterClick = { eventBus.post(CmdFilterRarity(it)) }
        filter_magika.filterClick = { eventBus.post(CmdFilterMagika(it)) }
        with(dash_navigation_view.getHeaderView(0)) {
            profile_change_user.setOnClickListener { showLogin() }
            profile_image.setOnClickListener {
                if (!App.hasUserLogged()) {
                    showLogin()
                }
            }
        }
    }

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)
        val drawerToggle = object : ActionBarDrawerToggle(this, dash_drawer_layout, toolbar,
                R.string.drawer_open, R.string.drawer_close) {

            override fun onDrawerOpened(drawerView: View) {
                super.onDrawerOpened(drawerView)
                with(statisticsSheetBehavior) {
                    if (state == BottomSheetBehavior.STATE_EXPANDED) {
                        state = BottomSheetBehavior.STATE_COLLAPSED
                    }
                }
                updateCollectionStatistics()
            }

        }
        dash_drawer_layout.addDrawerListener(drawerToggle)
        dash_navigation_view.setNavigationItemSelectedListener(this)
        drawerToggle.syncState()
        if (savedInstanceState == null) {
            dash_navigation_view.setCheckedItem(R.id.menu_cards)
            supportFragmentManager.beginTransaction()
                    .add(R.id.dash_content, CardsFragment())
                    .setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out)
                    .commit()
        }
    }

    override fun onSaveInstanceState(outState: Bundle?) {
        outState?.apply { putInt(KEY_MENU_ITEM_SELECTED, menuItemSelected) }
        super.onSaveInstanceState(outState)
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle?) {
        super.onRestoreInstanceState(savedInstanceState)
        savedInstanceState?.apply {
            menuItemSelected = getInt(KEY_MENU_ITEM_SELECTED)
            dash_navigation_view.setCheckedItem(menuItemSelected)
        }
    }

    override fun onResume() {
        super.onResume()
        updateUserMenuInfo()
    }

    private fun updateUserMenuInfo() {
        val user = FirebaseAuth.getInstance().currentUser
        dash_navigation_view.menu.findItem(R.id.menu_matches)?.isVisible = App.hasUserLogged()
        with(dash_navigation_view.getHeaderView(0)) {
            profile_change_user.visibility = if (App.hasUserLogged()) View.VISIBLE else View.GONE
            profile_name.text = user?.displayName ?: getString(R.string.unknown)
            if (user != null) {
                Glide.with(this@DashActivity)
                        .load(user.photoUrl)
                        .transform(CircleTransform(this@DashActivity))
                        .into(profile_image)
            }
        }
    }

    override fun onBackPressed() {
        if (statisticsSheetBehavior.state == BottomSheetBehavior.STATE_EXPANDED) {
            statisticsSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
            return
        }
        if (dash_drawer_layout.isDrawerOpen(Gravity.START)) {
            dash_drawer_layout.closeDrawer(Gravity.START)
            return
        }
        if (canExit || !dash_navigation_view.menu.findItem(R.id.menu_cards).isChecked) {
            super.onBackPressed()
        } else {
            showExitConfirm()
        }
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        menuItemSelected = dash_navigation_view.menu.itemsSequence().indexOf(item)
        dash_drawer_layout.closeDrawer(Gravity.START)
        if (item.isChecked) {
            return true
        }
        return when (item.itemId) {
            R.id.menu_cards -> supportFragmentManager.popBackStackImmediate()
            R.id.menu_decks -> showFragment(DecksFragment())
            R.id.menu_matches -> showFragment(MatchesFragment())
            R.id.menu_arena,
            R.id.menu_season,
            R.id.menu_about -> {
                true
            }
            else -> false
        }
    }

    private fun showFragment(frag: Fragment): Boolean {
        statisticsSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN
        supportFragmentManager.beginTransaction()
                .replace(R.id.dash_content, frag)
                .setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out)
                .addToBackStack(null)
                .commit()
        return true
    }

    private fun updateCollectionStatistics() {
        var allCardsTotal = 0
        var userCardsTotal = 0
        with(dash_navigation_view.getHeaderView(0)) {
            profile_collection.visibility = View.INVISIBLE
            profile_collection_loading.visibility = View.VISIBLE
            doAsync {
                publicInteractor.getCardsForStatistics(null) {
                    val allAttrCards = it
                    allCardsTotal += allAttrCards.filter { it.unique }.size
                    allCardsTotal += allAttrCards.filter { !it.unique }.size * 3
                    privateInteractor.getUserCollection(null) {
                        userCardsTotal += it.filter {
                            allAttrCards.map { it.shortName }.contains(it.key)
                        }.values.sum()
                        Timber.d("Out: ${it.filter { !allAttrCards.map { it.shortName }.contains(it.key) }}")
                        val stringPercent = getString(R.string.statistics_percent,
                                if (allCardsTotal > 0)
                                    userCardsTotal.toFloat() / allCardsTotal.toFloat() * 100f
                                else 0f)
                        runOnUiThread {
                            profile_collection_loading.visibility = View.GONE
                            profile_collection.visibility = View.VISIBLE
                            profile_collection.text = stringPercent
                            Timber.d("All: %d, User: %d", allCardsTotal, userCardsTotal)
                        }
                    }
                }
            }
        }
    }

    @Subscribe
    fun onLoginSuccess(cmdLoginSuccess: CmdLoginSuccess) {
        updateUserMenuInfo()
        updateCollectionStatistics()
    }

    @Subscribe
    fun onCmdShowTabs(cmdShowTabs: CmdShowTabs) {
        dash_app_bar_layout.setExpanded(true, true)
    }

}