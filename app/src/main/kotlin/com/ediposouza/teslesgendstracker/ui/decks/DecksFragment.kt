package com.ediposouza.teslesgendstracker.ui.decks

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.os.Handler
import android.support.v4.app.ActivityOptionsCompat
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentStatePagerAdapter
import android.support.v4.view.MenuItemCompat
import android.support.v4.view.ViewPager
import android.support.v7.app.AlertDialog
import android.support.v7.widget.SearchView
import android.text.format.DateUtils
import android.view.*
import android.view.inputmethod.InputMethodManager
import android.webkit.JavascriptInterface
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.ProgressBar
import com.ediposouza.teslesgendstracker.App
import com.ediposouza.teslesgendstracker.R
import com.ediposouza.teslesgendstracker.data.CardAttribute
import com.ediposouza.teslesgendstracker.data.Deck
import com.ediposouza.teslesgendstracker.data.DeckClass
import com.ediposouza.teslesgendstracker.data.DeckType
import com.ediposouza.teslesgendstracker.interactor.PrivateInteractor
import com.ediposouza.teslesgendstracker.interactor.PublicInteractor
import com.ediposouza.teslesgendstracker.ui.base.*
import com.ediposouza.teslesgendstracker.ui.cards.CmdFilterSearch
import com.ediposouza.teslesgendstracker.ui.decks.tabs.DecksFavoritedFragment
import com.ediposouza.teslesgendstracker.ui.decks.tabs.DecksOwnerFragment
import com.ediposouza.teslesgendstracker.ui.decks.tabs.DecksPublicFragment
import com.ediposouza.teslesgendstracker.util.MetricAction
import com.ediposouza.teslesgendstracker.util.MetricScreen
import com.ediposouza.teslesgendstracker.util.MetricsManager
import com.ediposouza.teslesgendstracker.util.inflate
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.activity_dash.*
import kotlinx.android.synthetic.main.dialog_import.view.*
import kotlinx.android.synthetic.main.fragment_decks.*
import org.greenrobot.eventbus.Subscribe
import org.jetbrains.anko.*
import org.jsoup.Jsoup
import timber.log.Timber

/**
 * Created by EdipoSouza on 11/12/16.
 */
class DecksFragment : BaseFragment(), SearchView.OnQueryTextListener {

    private val KEY_FAB_NEW_DECK = "newDeckKey"
    private val KEY_PAGE_VIEW_POSITION = "pageViewPositionKey"
    private val RC_NEW_DECK = 125

    private val adapter by lazy { DecksPageAdapter(context, fragmentManager) }

    private val pageChange = object : ViewPager.SimpleOnPageChangeListener() {

        override fun onPageSelected(position: Int) {
            updateActivityTitle(position)
            MetricsManager.trackScreen(when (position) {
                0 -> MetricScreen.SCREEN_DECKS_PUBLIC()
                1 -> MetricScreen.SCREEN_DECKS_OWNED()
                else -> MetricScreen.SCREEN_DECKS_FAVORED()
            })
        }

    }

    private var importDialog: AlertDialog? = null
    private var importDialogWebView: WebView? = null
    private var importDialogProgress: ProgressBar? = null
    private var userDecks: List<Deck> = listOf()
    private var userDecksImported = 0

    private fun updateActivityTitle(position: Int) {
        val title = CmdUpdateTitle(when (position) {
            1 -> R.string.title_tab_decks_owned
            2 -> R.string.title_tab_decks_favorites
            else -> R.string.title_tab_decks_public
        })
        eventBus.post(title)
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return container?.inflate(R.layout.fragment_decks)
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setHasOptionsMenu(true)
        decks_view_pager.adapter = adapter
        activity.dash_navigation_view.setCheckedItem(R.id.menu_decks)
        decks_view_pager.addOnPageChangeListener(pageChange)
        decks_attr_filter.filterClick = {
            if (decks_attr_filter.isAttrSelected(it)) {
                decks_attr_filter.unSelectAttr(it)
            } else {
                decks_attr_filter.selectAttr(it, false)
            }
            eventBus.post(CmdShowDecksByClasses(DeckClass.getClasses(decks_attr_filter.getSelectedAttrs())))
        }
        decks_fab_add.setOnClickListener {
            val anim = ActivityOptionsCompat.makeCustomAnimation(context, R.anim.slide_up, R.anim.slide_down)
            startActivityForResult(context.intentFor<NewDeckActivity>(), RC_NEW_DECK, anim.toBundle())
        }
        MetricsManager.trackScreen(MetricScreen.SCREEN_DECKS_PUBLIC())
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        view?.post { updateActivityTitle(decks_view_pager?.currentItem ?: 0) }
        decks_tab_layout.setupWithViewPager(decks_view_pager)
    }

    override fun onSaveInstanceState(outState: Bundle?) {
        outState?.apply {
            putInt(KEY_PAGE_VIEW_POSITION, decks_view_pager?.currentItem ?: 0)
            putBoolean(KEY_FAB_NEW_DECK, decks_fab_add?.isShown ?: false)
        }
        super.onSaveInstanceState(outState)
    }

    override fun onViewStateRestored(savedInstanceState: Bundle?) {
        super.onViewStateRestored(savedInstanceState)
        savedInstanceState?.apply {
            decks_view_pager.currentItem = getInt(KEY_PAGE_VIEW_POSITION)
        }
    }

    override fun onResume() {
        super.onResume()
        decks_app_bar_layout.setExpanded(true, true)
    }

    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
        menu?.clear()
        inflater?.inflate(R.menu.menu_import, menu)
        inflater?.inflate(R.menu.menu_search, menu)
        val actionView = MenuItemCompat.getActionView(menu?.findItem(R.id.menu_search))
        if (actionView is SearchView) {
            with(actionView) {
                queryHint = getString(R.string.decks_search_hint)
                setOnQueryTextListener(this@DecksFragment)
            }
        }
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        if (item?.itemId == R.id.menu_import) {
            if (App.hasUserLogged()) {
                PrivateInteractor.getUserDecks(null) {
                    userDecks = it
                    userDecksImported = 0
                    showImportDialog()
                }
            } else {
                eventBus.post(CmdShowSnackbarMsg(CmdShowSnackbarMsg.TYPE_ERROR, R.string.error_auth))
            }
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == RC_NEW_DECK && resultCode == Activity.RESULT_OK) {
            val privateExtra = data?.getBooleanExtra(NewDeckActivity.DECK_PRIVATE_EXTRA, false) ?: false
            decks_view_pager.currentItem = 1.takeIf { privateExtra } ?: 0
            Handler().postDelayed({ eventBus.post(CmdUpdateDeckAndShowDeck()) }, DateUtils.SECOND_IN_MILLIS / 2)
        }
    }

    override fun onQueryTextChange(newText: String?): Boolean {
        eventBus.post(CmdFilterSearch(newText))
        return true
    }

    override fun onQueryTextSubmit(query: String?): Boolean {
        eventBus.post(CmdFilterSearch(query))
        val imm = activity.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(activity.currentFocus!!.windowToken, 0)
        return true
    }

    @Subscribe
    @Suppress("unused")
    fun onCmdUpdateVisibility(update: CmdUpdateVisibility) {
        if (update.show) {
            decks_fab_add.show()
        } else {
            decks_fab_add.hide()
        }
    }

    private fun showImportDialog() {
        val htmlViewerInterface = HTMLViewerInterface()
        val dialogView = View.inflate(context, R.layout.dialog_import, null)
        dialogView.import_dialog_text.text = getString(R.string.dialog_import_deck_text)
        importDialog = AlertDialog.Builder(context, R.style.AppDialog)
                .setView(dialogView)
                .setNegativeButton(android.R.string.cancel, { _, _ ->
                    htmlViewerInterface.continueImporting = false
                    dialogView.import_dialog_webview.stopLoading()
                    MetricsManager.trackAction(MetricAction.ACTION_IMPORT_DECKS_CANCELLED())
                })
                .create()
        importDialog?.setOnShowListener {
            dialogView.import_dialog_webview?.apply {
                importDialogWebView = this
                settings.javaScriptEnabled = true
                addJavascriptInterface(htmlViewerInterface, "HtmlViewer")
                loadUrl(getString(R.string.dialog_import_legends_deck_main_link))
                setWebViewClient(object : WebViewClient() {
                    override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                        Timber.d("onPageStarted: $url")
                        val isMyDecksPage = url?.endsWith("/decks") ?: false
                        settings.loadsImagesAutomatically = !isMyDecksPage
                        dialogView.import_dialog_loading.visibility = View.VISIBLE.takeIf { isMyDecksPage } ?: View.GONE
                        with(dialogView.import_dialog_loading_details) {
                            importDialogProgress = this
                            visibility = View.VISIBLE.takeIf { isMyDecksPage } ?: View.GONE
                        }
                        with(dialogView.import_dialog_webview) {
                            layoutParams = layoutParams.apply {
                                height = 1.takeIf { isMyDecksPage } ?: ViewGroup.LayoutParams.WRAP_CONTENT
                            }
                        }
                    }

                    override fun onPageFinished(view: WebView?, url: String?) {
                        Timber.d("onPageFinished: $url")
                        if (url?.endsWith("/decks") ?: false) {
                            loadUrl("javascript:HtmlViewer.showHTML" +
                                    "('<html>'+document.getElementsByTagName('html')[0].innerHTML+'</html>');")
                        }
                        if (url == getString(R.string.dialog_import_legends_deck_main_link)) {
                            loadUrl("javascript:HtmlViewer.showHTMLandCheckLogged" +
                                    "('<html>'+document.getElementsByTagName('html')[0].innerHTML+'</html>');")
                        }
                    }
                })
            }
        }
        importDialog?.show()
        importDialog?.window?.clearFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE)
        importDialog?.window?.clearFlags(WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM)
        importDialog?.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)
        MetricsManager.trackScreen(MetricScreen.SCREEN_IMPORT_DECKS())
    }

    inner class HTMLViewerInterface {

        var decksLinkRemains = listOf<String>()
        var savedDecksLink = ""
        var importingSavedDecks = false
        var continueImporting = true

        @Suppress("unused")
        @JavascriptInterface
        fun showHTML(html: String) {
            doAsync {
                try {
                    val decksTableClass = "td_name_deck_full_with_save".takeIf { importingSavedDecks } ?: "td_name_deck_full"
                    val decksLink = Jsoup.parse(html).select(".table_large tr .$decksTableClass")?.map {
                        it.child(0).attr("href")
                    } ?: listOf()
                    if (decksLink.isNotEmpty()) {
                        Timber.d("SavedDecksLinks: $decksLink".takeIf { importingSavedDecks } ?: "MyDecksLinks: $decksLink")
                        uiThread {
                            importDialogProgress?.max = decksLink.size
                            importLegendDecks(decksLink)
                        }
                    }
                } catch (e: Exception) {
                    Timber.e(e)
                }
            }
        }

        @Suppress("unused")
        @JavascriptInterface
        fun showHTMLandCheckLogged(html: String) {
            doAsync {
                val baseProfileLink = getString(R.string.dialog_import_legends_deck_profile_base_link)
                val profileLink = Jsoup.parse(html).select(".dropdown-menu li a")?.get(1)?.attr("href")
                if (profileLink?.contains(baseProfileLink) ?: false) {
                    val userID = profileLink?.replace(baseProfileLink, "") ?: ""
                    Timber.d("UserID: $userID")
                    if (userID.isNotEmpty()) {
                        uiThread {
                            savedDecksLink = "$baseProfileLink$userID/saved"
                            val myDecksLink = "$baseProfileLink$userID/decks"
                            importDialogWebView?.loadUrl(myDecksLink)
                        }
                    }
                }
            }
        }

        @Suppress("unused")
        @JavascriptInterface
        fun showDeckHTML(html: String) {
            doAsync {
                PublicInteractor.getPatches { patches ->
                    Jsoup.parse(html).select(".wrapper .container")?.map {
                        val deckName = it.select(".col-lg-8 h1").first().text()
                        val deckCls = it.select(".deck_head_image_attributes").map {
                            val cardCls1 = it.child(0).attr("alt").toUpperCase()
                            val cardCls2 = it.child(1).attr("alt").toUpperCase()
                            DeckClass.getClass(CardAttribute.valueOf(cardCls1), CardAttribute.valueOf(cardCls2))
                        }.first()
                        val deckType = DeckType.of(with(it.select(".panel-body .center").first().text()) {
                            substring(indexOfLast { it == ' ' } + 1).replace("-", "")
                        })
                        var deckCost = 0
                        var deckPatchName = ""
                        with(it.select(".inner_deck_head b").map { it.text() }) {
                            val craftingCostIndexOf = indexOf("Crafting Cost:")
                            deckCost = get(craftingCostIndexOf + 1).toInt()
                            deckPatchName = with(get(craftingCostIndexOf - 1)) {
                                substring(indexOfFirst { it == '(' } + 1, indexOfFirst { it == ')' })
                            }
                        }
                        val deckPatch = patches.find { it.legendsDeck == deckPatchName } ?: patches.last()
                        val deckCards = it.select("#deck .card_deck")?.map {
                            val text = it.text().substring(it.text().indexOfFirst { it == ' ' } + 1)
                                    .replace("-1", "").replace("-2", "").replace("-3", "").trim()
                            val cardName = text.substring(0, text.indexOfLast { it == ' ' })
                            cardName.replace(" ", "").replace("-", "").replace("'", "").replace(",", "").toLowerCase() to
                                    text.substring(text.indexOfLast { it == ' ' } + 1).toInt()
                        }?.toMap() ?: mapOf()

                        var deckOwner = FirebaseAuth.getInstance().currentUser?.uid ?: ""
                        if (importingSavedDecks) {
                            deckOwner = it.select(".deck_page_deck_author .inner_deck_head  a").first().text()
                        }

                        Timber.d("Saving Deck: $deckName $deckCls $deckType $deckCost $deckPatch $deckCards $deckOwner")
                        if (userDecks.find { it.name == deckName } == null) {
                            PrivateInteractor.saveDeck(deckName, deckCls, deckType, deckCost, deckPatch.uuidDate,
                                    deckCards, false, deckOwner) { savedDeck ->
                                userDecksImported += 1
                                Timber.d("$deckName Saved")
                                uiThread {
                                    if (importingSavedDecks) {
                                        PrivateInteractor.setUserDeckFavorite(savedDeck, true) {
                                            context.toast("$deckName Favorite Imported!")
                                        }
                                    } else {
                                        context.toast("$deckName Saved!")
                                    }
                                    loadNextDeckPage(decksLinkRemains)
                                }
                            }
                        } else {
                            Timber.d("$deckName is already saved")
                            uiThread {
                                context.toast("$deckName is already saved")
                                loadNextDeckPage(decksLinkRemains)
                            }
                        }
                    }
                }
            }
        }

        private fun importLegendDecks(decksLink: List<String>) {
            importDialogWebView?.apply {
                settings.loadsImagesAutomatically = false
                settings.blockNetworkImage = true
                setWebViewClient(object : WebViewClient() {
                    override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                    }

                    override fun onPageFinished(view: WebView?, url: String?) {
                        Timber.d("importLegendDecks onPageFinished: $url")
                        if (url == savedDecksLink) {
                            importingSavedDecks = true
                            loadUrl("javascript:HtmlViewer.showHTML" +
                                    "('<html>'+document.getElementsByTagName('html')[0].innerHTML+'</html>');")
                        } else {
                            loadUrl("javascript:HtmlViewer.showDeckHTML" +
                                    "('<html>'+document.getElementsByTagName('html')[0].innerHTML+'</html>');")
                        }
                    }
                })
            }
            loadNextDeckPage(decksLink)
        }

        private fun loadNextDeckPage(decksLink: List<String>) {
            context.runOnUiThread {
                if (decksLink.isNotEmpty() && continueImporting) {
                    val nextDeckLink = decksLink.first()
                    decksLinkRemains = decksLink.minus(nextDeckLink)
                    val progressMax = importDialogProgress?.max ?: decksLink.size
                    importDialogProgress?.progress = progressMax - decksLink.size
                    importDialogWebView?.loadUrl(nextDeckLink)
                } else {
                    if (importingSavedDecks) {
                        context.toast("$userDecksImported Favorites imported!")
                        importDialog?.dismiss()
                    } else {
                        context.toast("$userDecksImported Decks imported!")
                        userDecksImported = 0
                        importingSavedDecks = true
                        if (continueImporting) {
                            importDialogWebView?.loadUrl(savedDecksLink)
                        }
                    }
                    MetricsManager.trackAction(MetricAction.ACTION_IMPORT_DECKS_FINISH(userDecksImported))
                }
            }
        }
    }

    class DecksPageAdapter(ctx: Context, fm: FragmentManager) : FragmentStatePagerAdapter(fm) {

        var titles: Array<String> = ctx.resources.getStringArray(R.array.decks_tabs)
        val decksPublicFragment by lazy { DecksPublicFragment() }
        val decksMyFragment by lazy { DecksOwnerFragment() }
        val decksSavedFragment by lazy { DecksFavoritedFragment() }

        override fun getItem(position: Int): BaseFragment {
            return when (position) {
                1 -> decksMyFragment
                2 -> decksSavedFragment
                else -> decksPublicFragment
            }
        }

        override fun getCount(): Int {
            return titles.size
        }

        override fun getPageTitle(position: Int): CharSequence {
            return titles[position]
        }

    }
}
