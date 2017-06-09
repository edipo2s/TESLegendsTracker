package com.ediposouza.teslesgendstracker.ui

import android.content.DialogInterface
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.preference.PreferenceManager
import android.support.design.widget.BottomSheetBehavior
import android.support.design.widget.NavigationView
import android.support.v4.app.ActivityCompat
import android.support.v4.app.ActivityOptionsCompat
import android.support.v4.app.Fragment
import android.support.v4.content.ContextCompat
import android.support.v7.app.ActionBarDrawerToggle
import android.support.v7.app.AlertDialog
import android.text.format.DateUtils
import android.view.Gravity
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.webkit.CookieManager
import android.webkit.CookieSyncManager
import android.widget.ArrayAdapter
import android.widget.TextView
import com.ediposouza.teslesgendstracker.*
import com.ediposouza.teslesgendstracker.interactor.PrivateInteractor
import com.ediposouza.teslesgendstracker.interactor.PublicInteractor
import com.ediposouza.teslesgendstracker.ui.articles.ArticlesFragment
import com.ediposouza.teslesgendstracker.ui.articles.WabbaTrackFragment
import com.ediposouza.teslesgendstracker.ui.base.BaseFilterActivity
import com.ediposouza.teslesgendstracker.ui.base.CmdLoginSuccess
import com.ediposouza.teslesgendstracker.ui.base.CmdShowSnackbarMsg
import com.ediposouza.teslesgendstracker.ui.base.CmdUpdateTitle
import com.ediposouza.teslesgendstracker.ui.basics.BasicsFragment
import com.ediposouza.teslesgendstracker.ui.cards.CardActivity
import com.ediposouza.teslesgendstracker.ui.cards.CardsFragment
import com.ediposouza.teslesgendstracker.ui.cards.CmdInputSearch
import com.ediposouza.teslesgendstracker.ui.decks.DecksFragment
import com.ediposouza.teslesgendstracker.ui.matches.MatchesFragment
import com.ediposouza.teslesgendstracker.ui.matches.tabs.ArenaFragment
import com.ediposouza.teslesgendstracker.ui.seasons.SeasonsFragment
import com.ediposouza.teslesgendstracker.ui.spoiler.SpoilerFragment
import com.ediposouza.teslesgendstracker.util.*
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

    private val EXTRA_DEEPLINK = "deeplink"
    private val KEY_TITLE = "titleKey"
    private val KEY_MENU_ITEM_SELECTED = "menuIndexKey"
    private val SKU_TEST = "android.test.purchased"
    private val SKU_DONATE_BASIC = "donate_basic"
    private val SKU_DONATE_PRO = "donate_pro"
    private val RC_DONATE = 221

    private var menuItemSelected = 0

    private var iabHelper: IabHelper? = null
    private var iabHelperStarted: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dash)
        snackbarNeedMargin = false
        with(dash_navigation_view.getHeaderView(0)) {
            profile_change_user.setOnClickListener {
                showLogin()
            }
            profile_image.setOnClickListener {
                if (!App.hasUserLogged()) {
                    showLogin()
                }
            }
        }
        dash_navigation_view.getChildAt(0)?.apply {
            isScrollbarFadingEnabled = false
        }
        configIabHelper()
    }

    private fun configIabHelper() {
        iabHelper = IabHelper(this, "$PPKA$PPKB$PPKC$PPKD").apply {
            enableDebugLogging(BuildConfig.DEBUG)
            startSetup {
                if (it.isSuccess) {
                    iabHelperStarted = true
                    queryInventoryAsync { _, inventory ->
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
        checkDeeplinkInfo()
    }

    override fun onSaveInstanceState(outState: Bundle?) {
        outState?.apply {
            putString(KEY_TITLE, dash_toolbar_title.text.toString())
            putInt(KEY_MENU_ITEM_SELECTED, menuItemSelected)
        }
        super.onSaveInstanceState(outState)
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle?) {
        super.onRestoreInstanceState(savedInstanceState)
        savedInstanceState?.apply {
            dash_toolbar_title.text = getString(KEY_TITLE)
            menuItemSelected = getInt(KEY_MENU_ITEM_SELECTED)
            dash_navigation_view.setCheckedItem(menuItemSelected)
        }
    }

    override fun onResume() {
        super.onResume()
        updateUserMenuInfo()
    }

    override fun onDestroy() {
        try {
            iabHelper?.disposeWhenFinished()
        } catch (e: Exception) {
            Timber.d(e)
        }
        super.onDestroy()
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
            R.id.menu_basics -> showFragment(BasicsFragment())
            R.id.menu_cards -> supportFragmentManager.popBackStackImmediate()
            R.id.menu_decks -> showFragment(DecksFragment())
            R.id.menu_matches -> showFragment(MatchesFragment())
            R.id.menu_articles -> showFragment(ArticlesFragment())
            R.id.menu_arena -> showFragment(ArenaFragment())
            R.id.menu_seasons -> showFragment(SeasonsFragment())
            R.id.menu_spoiler -> showFragment(SpoilerFragment())
            R.id.menu_donate -> showDonateDialog()
            R.id.menu_wabbatrack -> showFragment(WabbaTrackFragment())
            R.id.menu_share -> {
                val appLink = getString(R.string.playstore_url_format, packageName)
                val intent = Intent(Intent.ACTION_SEND).apply {
                    type = "text/*"
                    putExtra(Intent.EXTRA_TEXT, "${getString(R.string.share_text)}\n $appLink")
                }
                startActivity(Intent.createChooser(intent, getString(R.string.share_with)))
                MetricsManager.trackAction(MetricAction.ACTION_SHARE())
                true
            }
            R.id.menu_language -> showLanguageDialog()
            R.id.menu_about -> showAboutDialog()
            else -> false
        }
    }

    private fun checkDeeplinkInfo() {
        var data = intent.data
        if (intent?.hasExtra(EXTRA_DEEPLINK) ?: false) {
            data = Uri.parse(intent.extras.getString(EXTRA_DEEPLINK))
        }
        data?.pathSegments?.apply {
            val path = this[0]
            val params = this.minus(path).joinToString { it }
            Timber.d("$path with $params")
            MetricsManager.trackAction(MetricAction.ACTION_DEEPLINK(path, params))
            when (path) {
                getString(R.string.app_deeplink_path_card) -> {
                    PublicInteractor.getCards(null) {
                        val ctx = this@DashActivity
                        val card = it.filter { it.shortName == this[1] }.firstOrNull()
                        card?.let {
                            val anim = ActivityOptionsCompat.makeSceneTransitionAnimation(ctx, dash_toolbar_title,
                                    getString(R.string.card_transition_name))
                            ActivityCompat.startActivity(ctx, CardActivity.newIntent(ctx, card), anim.toBundle())
                        }
                    }
                }
                getString(R.string.app_deeplink_path_spoiler) -> {
                    onNavigationItemSelected(dash_navigation_view.menu.findItem(R.id.menu_spoiler))
                    PublicInteractor.getSpoilerCards {
                        val ctx = this@DashActivity
                        if (size > 1) {
                            val card = it.filter { it.shortName == this[1] }.firstOrNull()
                            if (card != null) {
                                val anim = ActivityOptionsCompat.makeSceneTransitionAnimation(ctx, dash_toolbar_title,
                                        getString(R.string.card_transition_name))
                                ActivityCompat.startActivity(ctx, CardActivity.newIntent(ctx, card), anim.toBundle())
                            }
                        }
                    }
                }
                getString(R.string.app_deeplink_path_basic) -> {
                    onNavigationItemSelected(dash_navigation_view.menu.findItem(R.id.menu_basics))
                }
                getString(R.string.app_deeplink_path_arena) -> {
                    onNavigationItemSelected(dash_navigation_view.menu.findItem(R.id.menu_arena))
                }
                getString(R.string.app_deeplink_path_wabbatrack) -> {
                    onNavigationItemSelected(dash_navigation_view.menu.findItem(R.id.menu_wabbatrack))
                }
                getString(R.string.app_deeplink_path_articles) -> {
                    onNavigationItemSelected(dash_navigation_view.menu.findItem(R.id.menu_articles))
                }
                getString(R.string.app_deeplink_path_season) -> {
                    onNavigationItemSelected(dash_navigation_view.menu.findItem(R.id.menu_seasons))
                }
                getString(R.string.app_deeplink_path_search) -> {
                    dash_content.postDelayed({
                        eventBus.post(CmdInputSearch(this[1]))
                    }, DateUtils.SECOND_IN_MILLIS)
                }
                getString(R.string.app_deeplink_path_update) -> {
                    checkLastVersion {
                        val updateUri = Uri.parse(getString(R.string.playstore_url_format, packageName))
                        startActivity(Intent(Intent.ACTION_VIEW).setData(updateUri))
                    }
                }
            }
        }
    }

    private fun updateUserMenuInfo() {
        val user = FirebaseAuth.getInstance().currentUser
        dash_navigation_view.menu.findItem(R.id.menu_matches)?.isEnabled = App.hasUserLogged()
        dash_navigation_view.menu.findItem(R.id.menu_arena)?.isEnabled = App.hasUserLogged()
        with(dash_navigation_view.getHeaderView(0)) {
            profile_change_user.visibility = View.VISIBLE.takeIf { App.hasUserLogged() } ?: View.GONE
            profile_name.text = user?.displayName ?: getString(R.string.unknown)
            if (user != null) {
                val placeholder = ContextCompat.getDrawable(context, R.drawable.ic_user)
                profile_image.loadFromUrl(user.photoUrl.toString(), placeholder, true)
            }
        }
        PublicInteractor.isSpoilerEnable {
            dash_navigation_view.menu.findItem(R.id.menu_spoiler)?.isVisible = it
        }
    }

    private fun showLanguageDialog(): Boolean {
        val languages = resources.getStringArray(R.array.languages)
        AlertDialog.Builder(this, R.style.AppDialog)
                .setAdapter(object : ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, languages) {
                    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
                        return (super.getView(position, convertView, parent) as TextView).apply {
                            setTextColor(ContextCompat.getColor(context, R.color.primary_text_dark))
                            setCompoundDrawablesWithIntrinsicBounds(when (position) {
                                0 -> R.drawable.lang_en
                                1 -> R.drawable.lang_ptbr
                                2 -> R.drawable.lang_es
                                3 -> R.drawable.lang_de
                                4 -> R.drawable.lang_ru
                                else -> R.drawable.ic_menu_earth
                            }, 0, 0, 0)
                            compoundDrawablePadding = resources.getDimensionPixelSize(R.dimen.default_margin)
                        }
                    }
                }, DialogInterface.OnClickListener { dialog, which ->
                    dialog.dismiss()
                    val language = when (which) {
                        1 -> "pt-br"
                        2 -> "es"
                        3 -> "de"
                        4 -> "ru"
                        else -> "en"
                    }
                    MetricsManager.trackAction(MetricAction.ACTION_DECK_CHANGE_LANGUAGE(language))
                    changeAppLanguage(language)
                })
                .show()
        return true
    }

    private fun showAboutDialog(): Boolean {
        val dialogView = View.inflate(this, R.layout.dialog_about, null).apply {
            about_dialog_version.text = packageManager.getPackageInfo(packageName, 0).versionName
            about_dialog_developer.setOnClickListener {
                val linkUri = Uri.parse(getString(R.string.about_info_developer_link))
                startActivity(Intent(Intent.ACTION_VIEW).setData(linkUri))
                MetricsManager.trackAction(MetricAction.ACTION_ABOUT_DEVELOPER())
            }
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
            about_dialog_thanks_unity_studio_text.setOnClickListener {
                val linkUri = Uri.parse(getString(R.string.about_info_thanks_unity_studio_link))
                startActivity(Intent(Intent.ACTION_VIEW).setData(linkUri))
                MetricsManager.trackAction(MetricAction.ACTION_ABOUT_DIREWOLF())
            }
        }
        AlertDialog.Builder(this, R.style.AppDialog)
                .setView(dialogView)
                .setPositiveButton(R.string.about_rate_app, { _, _ ->
                    startActivity(Intent(Intent.ACTION_VIEW)
                            .setData(Uri.parse(getString(R.string.playstore_url_format, packageName))))
                    MetricsManager.trackAction(MetricAction.ACTION_ABOUT_RATE())
                })
                .show()
        MetricsManager.trackScreen(MetricScreen.SCREEN_ABOUT())
        return true
    }

    private fun showDonateDialog(): Boolean {
        if (iabHelperStarted) {
            iabHelper?.queryInventoryAsync(true, listOf(SKU_DONATE_BASIC, SKU_DONATE_PRO), listOf()) { _, inventory ->
                if (inventory != null) {
                    val skuBasic = inventory.getSkuDetails(SKU_DONATE_BASIC)
                    val skuPro = inventory.getSkuDetails(SKU_DONATE_PRO)
                    showDonateDialog(skuBasic?.price ?: "", skuPro?.price ?: "")
                } else {
                    eventBus.post(CmdShowSnackbarMsg(CmdShowSnackbarMsg.TYPE_ERROR, R.string.app_donate_dialog_payment_error))
                }
            }
        } else {
            eventBus.post(CmdShowSnackbarMsg(CmdShowSnackbarMsg.TYPE_ERROR, R.string.app_donate_dialog_payment_error))
        }
        return true
    }

    private fun showDonateDialog(basicValue: String, proValue: String) {
        alertThemed(R.string.app_donate_dialog_text, R.string.menu_donate, R.style.AppDialog) {
            positiveButton(getString(R.string.app_donate_dialog_value, proValue), {
                processDonate(SKU_DONATE_BASIC)
            })
            negativeButton(getString(R.string.app_donate_dialog_value, basicValue), {
                processDonate(if (BuildConfig.DEBUG) SKU_TEST else SKU_DONATE_PRO)
            })
            neutralButton(R.string.app_donate_dialog_not_now, {
                MetricsManager.trackAction(MetricAction.ACTION_DONATE_NOT_NOW())
            })
        }.show()
        MetricsManager.trackScreen(MetricScreen.SCREEN_DONATE())
    }

    private fun processDonate(skuItem: String) {
        MetricsManager.trackAction(MetricAction.ACTION_START_DONATE())
        iabHelper?.launchPurchaseFlow(this@DashActivity, skuItem, RC_DONATE) { result, info ->
            if (result.isFailure) {
                toast(R.string.app_donate_dialog_payment_fail)
                return@launchPurchaseFlow
            }
            if (info.sku == skuItem) {
                handleDonation()
                toast(R.string.app_donate_dialog_payment_success)
                if (skuItem == SKU_DONATE_BASIC) {
                    MetricsManager.trackAction(MetricAction.ACTION_DONATE_BASIC())
                } else {
                    MetricsManager.trackAction(MetricAction.ACTION_DONATE_PRO())
                }
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
                PublicInteractor.getCardsForStatistics(null) { allAttrCards ->
                    allCardsTotal += allAttrCards.filter { it.unique }.size
                    allCardsTotal += allAttrCards.filter { !it.unique }.size * 3
                    PrivateInteractor.getUserCollection(null) { userCards ->
                        userCardsTotal += userCards.filter {
                            allAttrCards.map { it.shortName }.contains(it.key)
                        }.values.sum()
                        Timber.d("Out: ${userCards.filter { !allAttrCards.map { it.shortName }.contains(it.key) }}")
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
    @Suppress("unused")
    fun onCmdUpdateTitle(cmdUpdateTitle: CmdUpdateTitle) {
        val title = cmdUpdateTitle.title.takeIf(String::isNotEmpty) ?: getString(cmdUpdateTitle.titleRes)
        dash_toolbar_title.setText(title)
    }

    @Subscribe
    @Suppress("unused", "UNUSED_PARAMETER", "DEPRECATION")
    fun onCmdLoginSuccess(cmdLoginSuccess: CmdLoginSuccess) {
        updateUserMenuInfo()
        updateCollectionStatistics()
        dash_navigation_view.getHeaderView(0).profile_clear_cache_webview.clearCache(true)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            CookieManager.getInstance().removeAllCookies(null)
            CookieManager.getInstance().flush()
        } else {
            val cookieSyncMngr = CookieSyncManager.createInstance(this)
            cookieSyncMngr.startSync()
            val cookieManager = CookieManager.getInstance()
            cookieManager.removeAllCookie()
            cookieManager.removeSessionCookie()
            cookieSyncMngr.stopSync()
            cookieSyncMngr.sync()
        }
    }

}