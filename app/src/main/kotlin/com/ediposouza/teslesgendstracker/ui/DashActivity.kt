package com.ediposouza.teslesgendstracker.ui

import android.animation.Animator
import android.animation.ValueAnimator
import android.os.Bundle
import android.support.design.widget.BottomSheetBehavior
import android.support.design.widget.CoordinatorLayout
import android.support.design.widget.NavigationView
import android.support.v4.app.Fragment
import android.support.v7.app.ActionBarDrawerToggle
import android.text.format.DateUtils
import android.view.Gravity
import android.view.MenuItem
import android.view.View
import com.bumptech.glide.Glide
import com.ediposouza.teslesgendstracker.R
import com.ediposouza.teslesgendstracker.ui.base.BaseActivity
import com.ediposouza.teslesgendstracker.ui.base.CmdUpdateRarityMagikaFiltersVisibility
import com.ediposouza.teslesgendstracker.ui.cards.CardsFragment
import com.ediposouza.teslesgendstracker.ui.decks.DecksFragment
import com.ediposouza.teslesgendstracker.ui.utils.CircleTransform
import com.ediposouza.teslesgendstracker.ui.widget.filter.CmdFilterMagika
import com.ediposouza.teslesgendstracker.ui.widget.filter.CmdFilterRarity
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.activity_dash.*
import kotlinx.android.synthetic.main.fragment_cards.*
import kotlinx.android.synthetic.main.navigation_drawer_header.view.*
import org.greenrobot.eventbus.Subscribe

class DashActivity : BaseActivity(),
        NavigationView.OnNavigationItemSelectedListener {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dash)
        dash_filter_rarity.filterClick = { eventBus.post(CmdFilterRarity(it)) }
        dash_filter_magika.filterClick = { eventBus.post(CmdFilterMagika(it)) }
    }

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)
        val drawerToggle = object : ActionBarDrawerToggle(this, dash_drawer_layout, toolbar,
                R.string.drawer_open, R.string.drawer_close) {

            override fun onDrawerOpened(drawerView: View) {
                super.onDrawerOpened(drawerView)
                val user = FirebaseAuth.getInstance().currentUser
                dash_navigation_view.menu.findItem(R.id.menu_matches)?.isVisible = user != null
                dash_navigation_view.getHeaderView(0).profile_name.text = user?.displayName
                if (user != null) {
                    Glide.with(this@DashActivity)
                            .load(user.photoUrl)
                            .transform(CircleTransform(this@DashActivity))
                            .into(dash_navigation_view.getHeaderView(0).profile_image)
                }
                BottomSheetBehavior.from(collection_statistics).state = BottomSheetBehavior.STATE_COLLAPSED
            }

        }
        dash_drawer_layout.addDrawerListener(drawerToggle)
        dash_navigation_view.setNavigationItemSelectedListener(this)
        dash_navigation_view.menu.findItem(R.id.menu_cards)?.isChecked = true
        drawerToggle.syncState()
        supportFragmentManager.beginTransaction()
                .add(R.id.dash_content, CardsFragment())
                .setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out)
                .commit()
    }

    override fun onBackPressed() {
        val statisticsBottomSheet = BottomSheetBehavior.from(collection_statistics)
        if (statisticsBottomSheet.state == BottomSheetBehavior.STATE_EXPANDED) {
            statisticsBottomSheet.state = BottomSheetBehavior.STATE_COLLAPSED
            return
        }
        if (dash_drawer_layout.isDrawerOpen(Gravity.START)) {
            dash_drawer_layout.closeDrawer(Gravity.START)
            return
        }
        super.onBackPressed()
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        dash_drawer_layout.closeDrawer(Gravity.START)
        return when (item.itemId) {
            R.id.menu_cards -> showFragment(CardsFragment())
            R.id.menu_decks -> showFragment(DecksFragment())
            else -> false
        }
    }

    fun showFragment(frag: Fragment): Boolean {
        supportFragmentManager.beginTransaction()
                .replace(R.id.dash_content, frag)
                .setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out)
                .addToBackStack(null)
                .commit()
        return true
    }

    @Subscribe
    fun updateRarityMagikaFilters(update: CmdUpdateRarityMagikaFiltersVisibility) {
        val filterMagikaLP = dash_filter_magika.layoutParams as CoordinatorLayout.LayoutParams
        val filterRarityLP = dash_filter_rarity.layoutParams as CoordinatorLayout.LayoutParams
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
                dash_filter_magika.layoutParams = filterMagikaLP
                dash_filter_rarity.layoutParams = filterRarityLP
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