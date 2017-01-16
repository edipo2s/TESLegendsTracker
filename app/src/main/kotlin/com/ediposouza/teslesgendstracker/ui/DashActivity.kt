package com.ediposouza.teslesgendstracker.ui

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.preference.PreferenceManager
import android.support.design.widget.BottomSheetBehavior
import android.support.design.widget.NavigationView
import android.support.v4.app.Fragment
import android.support.v4.content.ContextCompat
import android.support.v7.app.ActionBarDrawerToggle
import android.support.v7.app.AlertDialog
import android.view.Gravity
import android.view.MenuItem
import android.view.View
import com.bumptech.glide.Glide
import com.ediposouza.teslesgendstracker.*
import com.ediposouza.teslesgendstracker.interactor.PrivateInteractor
import com.ediposouza.teslesgendstracker.interactor.PublicInteractor
import com.ediposouza.teslesgendstracker.ui.base.BaseFilterActivity
import com.ediposouza.teslesgendstracker.ui.base.CmdLoginSuccess
import com.ediposouza.teslesgendstracker.ui.base.CmdShowSnackbarMsg
import com.ediposouza.teslesgendstracker.ui.base.CmdUpdateTitle
import com.ediposouza.teslesgendstracker.ui.cards.CardsFragment
import com.ediposouza.teslesgendstracker.ui.decks.DecksFragment
import com.ediposouza.teslesgendstracker.ui.matches.MatchesFragment
import com.ediposouza.teslesgendstracker.ui.util.CircleTransform
import com.ediposouza.teslesgendstracker.util.MetricAction
import com.ediposouza.teslesgendstracker.util.MetricScreen
import com.ediposouza.teslesgendstracker.util.MetricsManager
import com.ediposouza.teslesgendstracker.util.alertThemed
import com.google.firebase.auth.FirebaseAuth
import com.google.inapp.util.IabHelper
import kotlinx.android.synthetic.main.activity_dash.*
import kotlinx.android.synthetic.main.dialog_about.view.*
import kotlinx.android.synthetic.main.navigation_drawer_header.view.*
import org.greenrobot.eventbus.Subscribe
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.itemsSequence
import org.jetbrains.anko.toast
import timber.log.Timber

class DashActivity : BaseFilterActivity(),
        NavigationView.OnNavigationItemSelectedListener {

    private val KEY_MENU_ITEM_SELECTED = "menuIndexKey"
    private val SKU_TEST = "android.test.purchased"
    private val SKU_DONATE_BASIC = "donate_basic"
    private val SKU_DONATE_PRO = "donate_pro"
    private val RC_DONATE = 221

    private var menuItemSelected = 0
    private val publicInteractor = PublicInteractor()
    private val privateInteractor = PrivateInteractor()

    private var iabHelper: IabHelper? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dash)
        snackbarNeedMargin = false
        with(dash_navigation_view.getHeaderView(0)) {
            profile_change_user.setOnClickListener { showLogin() }
            profile_image.setOnClickListener {
                if (!App.hasUserLogged()) {
                    showLogin()
                }
            }
        }
        iabHelper = IabHelper(this, "$PPKA$PPKB$PPKC$PPKD").apply {
            enableDebugLogging(BuildConfig.DEBUG)
            startSetup {
                if (it.isSuccess) {
                    queryInventoryAsync { iabResult, inventory ->
                        if (inventory != null) {
                            if (inventory.hasPurchase(SKU_DONATE_BASIC) || inventory.hasPurchase(SKU_DONATE_PRO)) {
                                handleDonation()
                            } else {
                                Timber.d("No donation yet")
                            }
                        }
                    }
                } else {
                    Timber.e("Iab start setup error: ${it.message}")
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
                getStatisticsBottomSheetBehavior()?.apply {
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

    override fun onDestroy() {
        iabHelper?.disposeWhenFinished()
        super.onDestroy()
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
        getStatisticsBottomSheetBehavior()?.apply {
            if (state == BottomSheetBehavior.STATE_EXPANDED) {
                state = BottomSheetBehavior.STATE_COLLAPSED
                return
            }
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

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (iabHelper?.handleActivityResult(requestCode, resultCode, data) ?: false) {
            return
        }
        super.onActivityResult(requestCode, resultCode, data)
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
            R.id.menu_season -> {
                true
            }
            R.id.menu_donate -> showDonateDialog()
            R.id.menu_about -> {
                val dialogView = View.inflate(this, R.layout.dialog_about, null).apply {
                    about_dialog_version.text = packageManager.getPackageInfo(packageName, 0).versionName
                    about_dialog_thanks_cvh_text.setOnClickListener {
                        val linkUri = Uri.parse(getString(R.string.about_info_thanks_cvh_link))
                        startActivity(Intent(Intent.ACTION_VIEW).setData(linkUri))
                        MetricsManager.trackAction(MetricAction.ACTION_ABOUT_CVH())
                    }
                    about_dialog_thanks_direwolf_text.setOnClickListener {
                        val linkUri = Uri.parse(getString(R.string.about_info_thanks_direwolf_link))
                        startActivity(Intent(Intent.ACTION_VIEW).setData(linkUri))
                        MetricsManager.trackAction(MetricAction.ACTION_ABOUT_DIREWOLF())
                    }
                }
                AlertDialog.Builder(this, R.style.AppDialog)
                        .setView(dialogView)
                        .setPositiveButton(R.string.about_rate_app, { di, which ->
                            startActivity(Intent(Intent.ACTION_VIEW)
                                    .setData(Uri.parse(getString(R.string.playstore_url_format, packageName))))
                            MetricsManager.trackAction(MetricAction.ACTION_ABOUT_RATE())
                        })
                        .show()
                MetricsManager.trackScreen(MetricScreen.SCREEN_ABOUT())
                true
            }
            else -> false
        }
    }

    private fun showDonateDialog(): Boolean {
        iabHelper?.queryInventoryAsync(true, listOf(SKU_DONATE_BASIC, SKU_DONATE_PRO), listOf()) { iabResult, inventory ->
            if (inventory != null) {
                val skuBasic = inventory.getSkuDetails(SKU_DONATE_BASIC)
                val skuPro = inventory.getSkuDetails(SKU_DONATE_PRO)
                showDonateDialog(skuBasic?.price ?: "", skuPro?.price ?: "")
            } else {
                eventBus.post(CmdShowSnackbarMsg(CmdShowSnackbarMsg.TYPE_ERROR, R.string.app_donate_dialog_payment_error))
            }
        }
        return true
    }

    private fun showDonateDialog(basicValue: String, proValue: String) {
        alertThemed(R.string.app_donate_dialog_text, R.string.menu_donate, R.style.AppDialog) {
            positiveButton(getString(R.string.app_donate_dialog_value, proValue), {
                processDonate(SKU_DONATE_BASIC)
                MetricsManager.trackAction(MetricAction.ACTION_DONATE_BASIC())
            })
            negativeButton(getString(R.string.app_donate_dialog_value, basicValue), {
                processDonate(if (BuildConfig.DEBUG) SKU_TEST else SKU_DONATE_PRO)
                MetricsManager.trackAction(MetricAction.ACTION_DONATE_PRO())
            })
            neutralButton(R.string.app_donate_dialog_not_now, {
                MetricsManager.trackAction(MetricAction.ACTION_DONATE_NOT_NOW())
            })
        }.show()
        MetricsManager.trackScreen(MetricScreen.SCREEN_DONATE())
    }

    private fun processDonate(skuItem: String) {
        iabHelper?.launchPurchaseFlow(this@DashActivity, skuItem, RC_DONATE) { result, info ->
            if (result.isFailure) {
                toast(R.string.app_donate_dialog_payment_fail)
                return@launchPurchaseFlow
            }
            if (info.sku == skuItem) {
                handleDonation()
                toast(R.string.app_donate_dialog_payment_success)
            }
        }
    }

    private fun handleDonation() {
        dash_drawer_layout.closeDrawer(Gravity.START)
        dash_navigation_view.menu.findItem(R.id.menu_donate)?.apply {
            isEnabled = false
            title = getString(R.string.menu_donate_done)
            icon = ContextCompat.getDrawable(this@DashActivity, R.drawable.ic_no_ads)
        }
        PreferenceManager.getDefaultSharedPreferences(this@DashActivity).edit()
                .putBoolean(PREF_USER_DONATE, true)
                .apply()
        Timber.d("Donated!")
    }

    private fun showFragment(frag: Fragment): Boolean {
        getStatisticsBottomSheetBehavior()?.state = BottomSheetBehavior.STATE_HIDDEN
        if (supportFragmentManager.backStackEntryCount > 0) {
            supportFragmentManager.popBackStackImmediate()
        }
        supportFragmentManager.beginTransaction().apply {
            replace(R.id.dash_content, frag)
            setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out)
            addToBackStack(null)
        }.commit()
        return true
    }

    private fun getStatisticsBottomSheetBehavior(): BottomSheetBehavior<*>? {
        findViewById(R.id.cards_collection_statistics)?.apply {
            return BottomSheetBehavior.from<View>(this)
        }
        return null
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
                        val stringPercent = getString(R.string.collection_statistics_percent,
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
    fun onUpdateTitle(cmdUpdateTitle: CmdUpdateTitle) {
        dash_toolbar_title.setText(cmdUpdateTitle.title)
    }

    @Subscribe
    @Suppress("UNUSED_PARAMETER")
    fun onLoginSuccess(cmdLoginSuccess: CmdLoginSuccess) {
        updateUserMenuInfo()
        updateCollectionStatistics()
    }

}