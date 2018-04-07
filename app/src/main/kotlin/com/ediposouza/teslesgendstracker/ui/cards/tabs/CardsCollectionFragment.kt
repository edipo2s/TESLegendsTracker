package com.ediposouza.teslesgendstracker.ui.cards.tabs

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.os.Handler
import android.support.annotation.StringRes
import android.support.design.widget.BottomSheetBehavior
import android.support.v4.app.ActivityOptionsCompat
import android.support.v4.content.ContextCompat
import android.support.v7.app.AlertDialog
import android.support.v7.util.DiffUtil
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.text.format.DateUtils
import android.view.*
import android.webkit.JavascriptInterface
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import com.ediposouza.teslesgendstracker.R
import com.ediposouza.teslesgendstracker.data.Card
import com.ediposouza.teslesgendstracker.data.CardSlot
import com.ediposouza.teslesgendstracker.interactor.PrivateInteractor
import com.ediposouza.teslesgendstracker.interactor.PublicInteractor
import com.ediposouza.teslesgendstracker.ui.base.CmdShowCardsByAttr
import com.ediposouza.teslesgendstracker.ui.cards.CardActivity
import com.ediposouza.teslesgendstracker.ui.cards.widget.CollectionStatistics
import com.ediposouza.teslesgendstracker.ui.util.SimpleDiffCallback
import com.ediposouza.teslesgendstracker.util.*
import jp.wasabeef.recyclerview.animators.ScaleInAnimator
import kotlinx.android.synthetic.main.dialog_import.view.*
import kotlinx.android.synthetic.main.dialog_import_result.view.*
import kotlinx.android.synthetic.main.fragment_cards_list.*
import kotlinx.android.synthetic.main.itemlist_card_collection.view.*
import kotlinx.android.synthetic.main.itemlist_card_imported.view.*
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.findOptional
import org.jetbrains.anko.toast
import org.jetbrains.anko.uiThread
import org.jsoup.Jsoup
import timber.log.Timber
import java.util.*


/**
 * Created by EdipoSouza on 10/30/16.
 */
open class CardsCollectionFragment : CardsAllFragment() {

    override val isCardsCollection: Boolean = true

    var isEditStarted: Boolean = false

    val view_statistics: CollectionStatistics?
        get() = activity?.findOptional<CollectionStatistics>(R.id.cards_collection_statistics)

    val statisticsSheetBehavior: BottomSheetBehavior<CollectionStatistics>?
        get() = if (view_statistics != null) {
            BottomSheetBehavior.from(view_statistics)
        } else {
            null
        }

    open val cardsCollectionAdapter by lazy {
        CardsCollectionAdapter(itemClick = { changeUserCardQtd(it) }) { view, card ->
            showCardExpanded(card, view)
            true
        }
    }

    val sheetBehaviorCallback: BottomSheetBehavior.BottomSheetCallback = object : BottomSheetBehavior.BottomSheetCallback() {
        override fun onSlide(bottomSheet: View, slideOffset: Float) {
        }

        override fun onStateChanged(bottomSheet: View, newState: Int) {
            val expanded = newState == BottomSheetBehavior.STATE_EXPANDED ||
                    newState == BottomSheetBehavior.STATE_SETTLING
            if (expanded) {
                view_statistics?.scrollToTop()
                view_statistics?.updateStatistics()
            }
            when (newState) {
                BottomSheetBehavior.STATE_EXPANDED -> {
                    MetricsManager.trackAction(MetricAction.ACTION_COLLECTION_STATISTICS_EXPAND())
                    MetricsManager.trackScreen(MetricScreen.SCREEN_CARDS_STATISTICS())
                }
                BottomSheetBehavior.STATE_COLLAPSED ->
                    MetricsManager.trackAction(MetricAction.ACTION_COLLECTION_STATISTICS_COLLAPSE())
            }
        }

    }

    var importDialog: AlertDialog? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        with(cards_recycler_view) {
            setPadding(paddingLeft, paddingTop, paddingRight, resources.getDimensionPixelSize(R.dimen.huge_margin))
        }
        statisticsSheetBehavior?.setBottomSheetCallback(sheetBehaviorCallback)
    }

    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
        super.onCreateOptionsMenu(menu, inflater)
        menu?.findItem(R.id.menu_import)?.isVisible = true
        menu?.findItem(R.id.menu_only_favorite)?.isVisible = false
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        BottomSheetBehavior.from(view_statistics).state = BottomSheetBehavior.STATE_COLLAPSED
        if (item?.itemId == R.id.menu_import) {
            showImportDialog()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onResume() {
        super.onResume()
        isEditStarted = false
    }

    override fun onStop() {
        if (importDialog?.isShowing == true) {
            importDialog?.dismiss()
        }
        super.onStop()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == EXPAND_CODE && resultCode == Activity.RESULT_OK) {
            updateCardsList()
        }
    }

    private fun showImportDialog() {
        val dialogView = View.inflate(context, R.layout.dialog_import, null)
        context?.let {
            importDialog = AlertDialog.Builder(it, R.style.AppDialog)
                    .setView(dialogView)
                    .setNegativeButton(android.R.string.cancel, { _, _ ->
                        dialogView.import_dialog_webview.stopLoading()
                        MetricsManager.trackAction(MetricAction.ACTION_IMPORT_COLLECTION_CANCELLED())
                    })
                    .create()
        }
        importDialog?.setOnShowListener {
            dialogView.import_dialog_webview?.apply {
                settings.javaScriptEnabled = true
                addJavascriptInterface(HTMLViewerInterface(), "HtmlViewer")
                loadUrl(getString(R.string.dialog_import_legends_deck_link))
                setWebViewClient(object : WebViewClient() {
                    override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                        Timber.d("onPageStarted: $url")
                        val isCollectionPage = url == getString(R.string.dialog_import_legends_deck_link)
                        settings.loadsImagesAutomatically = !isCollectionPage
                        dialogView.import_dialog_loading.visibility = View.VISIBLE.takeIf { isCollectionPage } ?: View.GONE
                        with(dialogView.import_dialog_webview) {
                            layoutParams = layoutParams.apply {
                                height = 1.takeIf { isCollectionPage } ?: ViewGroup.LayoutParams.WRAP_CONTENT
                            }
                        }
                    }

                    override fun onPageFinished(view: WebView?, url: String?) {
                        Timber.d("onPageFinished: $url")
                        if (url == getString(R.string.dialog_import_legends_deck_link)) {
                            loadUrl("javascript:HtmlViewer.showHTML" +
                                    "('<html>'+document.getElementsByTagName('html')[0].innerHTML+'</html>');")
                        }
                        if (url == getString(R.string.dialog_import_legends_deck_main_link)) {
                            loadUrl(getString(R.string.dialog_import_legends_deck_link))
                        }
                    }
                })
            }
        }
        importDialog?.show()
        importDialog?.window?.clearFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE)
        importDialog?.window?.clearFlags(WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM)
        importDialog?.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)
        MetricsManager.trackScreen(MetricScreen.SCREEN_IMPORT_COLLECTION())
    }

    override fun configRecycleView() {
        super.configRecycleView()
        cards_recycler_view.adapter = cardsCollectionAdapter
        configLoggedViews()
    }

    override fun showCards() {
        val cards = filteredCards()
        PrivateInteractor.getUserCollection(setFilter, currentAttr) { userCards ->
            val slots = cards.map { CardSlot(it, userCards[it.shortName] ?: 0) }
            cards_recycler_view?.itemAnimator = ScaleInAnimator()
            cardsCollectionAdapter.showCards(slots as ArrayList)
            scrollToTop()
        }
    }

    override fun showCardExpanded(card: Card, view: View) {
        activity?.let {
            startActivityForResult(CardActivity.newIntent(it, card), EXPAND_CODE,
                    ActivityOptionsCompat.makeSceneTransitionAnimation(it, view, transitionName).toBundle())
        }
    }

    private fun changeUserCardQtd(cardSlot: CardSlot) {
        if (!isEditStarted) {
            try {
                context?.alertThemed(R.string.card_collection_edit_confirm, theme = R.style.AppDialog) {
                    positiveButton(android.R.string.yes, {
                        isEditStarted = true
                        Toast.makeText(context, R.string.card_collection_edit_success, Toast.LENGTH_SHORT).show()
                    })
                    negativeButton(android.R.string.no, {})
                }?.show()
            } catch (e: Exception) {
                Timber.e(e)
            }
            return
        }
        val newQtd = cardSlot.qtd.inc()
        val cardMaxQtd = 1.takeIf { cardSlot.card.unique } ?: 3
        val finalQtd = newQtd.takeIf { newQtd <= cardMaxQtd } ?: 0
        PrivateInteractor.setUserCardQtd(cardSlot.card, finalQtd) {
            cards_recycler_view?.itemAnimator = null
            cardsCollectionAdapter.updateSlot(cardSlot, finalQtd)
            view_statistics?.updateStatistics(currentAttr)
            MetricsManager.trackAction(MetricAction.ACTION_COLLECTION_CARD_QTD_CHANGE(cardSlot.card, finalQtd))
        }
    }

    inner class HTMLViewerInterface {

        @Suppress("unused")
        @JavascriptInterface
        fun showHTML(html: String) {
            doAsync {
                val legendsSlots = Jsoup.parse(html).select("#table_view tr")?.map {
                    val cardName = it.select(".td_title_card_collection").text()
                    val cardQtd = it.select(".td_total_card_collection").text().toInt()
                    val cardShortName = cardName.replace(" ", "").replace("-", "")
                            .replace("'", "").replace(",", "").toLowerCase()
                    cardShortName to cardQtd
                }?.filter { it.second > 0 }?.toMap() ?: mapOf()
                if (legendsSlots.isNotEmpty()) {
                    importLegendDecksCards(legendsSlots)
                }
            }
        }

        private fun importLegendDecksCards(legendsSlots: Map<String, Int>) {
            doAsync {
                PublicInteractor.getCards(null) { allCards ->
                    val legendsDecksCards = allCards.filter { legendsSlots.keys.contains(it.shortName) }.toMutableList()
                    PrivateInteractor.getUserCollection(null) { userSlots ->
                        val userCards = allCards.filter { userSlots.keys.contains(it.shortName) }.toMutableList()
                        val onlyInLegendsDecks = legendsDecksCards.filter { !userSlots.keys.contains(it.shortName) }
                        val onlyInUserCollection = userCards.filter { !legendsSlots.keys.contains(it.shortName) }
                        val cardsInCommon = userCards.filter { legendsSlots.keys.contains(it.shortName) }
                        val userQtdGreater = cardsInCommon.filter {
                            userSlots[it.shortName] ?: 0 > legendsSlots[it.shortName] ?: 0
                        }
                        val legendsQtdGreater = cardsInCommon.filter {
                            legendsSlots[it.shortName] ?: 0 > userSlots[it.shortName] ?: 0
                        }
                        onlyInLegendsDecks.forEach {
                            val qtd = legendsSlots[it.shortName] ?: 0
                            PrivateInteractor.setUserCardQtd(it, qtd) {
                                Timber.d("$qtd ${it.name} card added")
                            }
                        }
                        legendsQtdGreater.forEach {
                            val qtd = legendsSlots[it.shortName] ?: 0
                            PrivateInteractor.setUserCardQtd(it, qtd) {
                                Timber.d("${it.name} qtd updated to $qtd")
                            }
                        }
                        showImportSummary(onlyInLegendsDecks, onlyInUserCollection, legendsQtdGreater,
                                userQtdGreater, legendsSlots, userSlots)
                    }
                }
                uiThread {
                    context?.toast("Collection imported!")
                    importDialog?.dismiss()
                }
            }
        }

        private fun showImportSummary(onlyInLegendsDecks: List<Card>, onlyUserCollection: List<Card>,
                                      legendsQtdGreater: List<Card>, userQtdGreater: List<Card>,
                                      legendsSlots: Map<String, Int>, userSlots: Map<String, Int>) {
            val dialogView = View.inflate(context, R.layout.dialog_import_result, null)
            with(dialogView.import_dialog_imported_recycler_view) {
                layoutManager = LinearLayoutManager(context)
                adapter = CardsImportedAdapter(mutableListOf<Pair<CardSlot, Int>>().apply {
                    addAll(onlyInLegendsDecks.map {
                        Pair(CardSlot(it, legendsSlots[it.shortName] ?: 0), R.string.dialog_import_result_added)
                    }.sortedBy { it.first.card.attr.ordinal })
                    addAll(legendsQtdGreater.map {
                        Pair(CardSlot(it, legendsSlots[it.shortName] ?: 0), R.string.dialog_import_result_updated)
                    }.sortedBy { it.first.card.attr.ordinal })
                })
                setHasFixedSize(true)
            }
            with(dialogView.import_dialog_difference_recycler_view) {
                layoutManager = LinearLayoutManager(context)
                adapter = CardsImportedAdapter(mutableListOf<Pair<CardSlot, Int>>().apply {
                    addAll(onlyUserCollection.map {
                        Pair(CardSlot(it, userSlots[it.shortName] ?: 0), R.string.dialog_import_result_you_have)
                    }.sortedBy { it.first.card.attr.ordinal })
                    addAll(userQtdGreater.map {
                        Pair(CardSlot(it, userSlots[it.shortName] ?: 0), R.string.dialog_import_result_has_more)
                    }.sortedBy { it.first.card.attr.ordinal })
                })
                setHasFixedSize(true)
            }
            context?.let {
                AlertDialog.Builder(it, R.style.AppDialog)
                        .setView(dialogView)
                        .setPositiveButton(android.R.string.ok, { _, _ ->
                            Handler().postDelayed({
                                eventBus.post(CmdShowCardsByAttr(currentAttr))
                            }, DateUtils.SECOND_IN_MILLIS / 2)
                        })
                        .show()
            }
            val newCardsImported = legendsSlots.filter { onlyInLegendsDecks.map(Card::shortName).contains(it.key) }.values.sum()
            val cardsQtdImported = legendsSlots.filter { legendsQtdGreater.map(Card::shortName).contains(it.key) }
                    .map { it.key to it.value.minus(userSlots[it.key] ?: 0) }.sumBy { it.second }
            MetricsManager.trackAction(MetricAction.ACTION_IMPORT_COLLECTION_FINISH(newCardsImported + cardsQtdImported))
        }
    }

    open class CardsCollectionAdapter(val itemClick: (CardSlot) -> Unit,
                                      val itemLongClick: (View, Card) -> Boolean) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

        var items: MutableList<CardSlot> = mutableListOf()

        override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): RecyclerView.ViewHolder {
            return CardsCollectionViewHolder(parent?.inflate(R.layout.itemlist_card_collection), itemClick, itemLongClick)
        }

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder?, position: Int) {
            (holder as CardsCollectionViewHolder).bind(items[position])
        }

        override fun getItemCount(): Int = items.size

        fun showCards(cardSlots: MutableList<CardSlot>) {
            val oldItems = items
            items = cardSlots
            if (items.isEmpty() || items.minus(oldItems).isEmpty()) {
                notifyDataSetChanged()
                return
            }
            DiffUtil.calculateDiff(SimpleDiffCallback(items, oldItems) { oldItem, newItem ->
                oldItem.card.shortName == newItem.card.shortName && oldItem.qtd == newItem.qtd
            }).dispatchUpdatesTo(this)
        }

        fun updateSlot(cardSlot: CardSlot, newQtd: Int) {
            val slotIndex = items.indexOf(cardSlot)
            if (slotIndex > -1) {
                items[slotIndex] = CardSlot(cardSlot.card, newQtd)
                notifyItemChanged(slotIndex)
            }
        }

    }

    class CardsCollectionViewHolder(val view: View?, val itemClick: (CardSlot) -> Unit,
                                    val itemLongClick: (View, Card) -> Boolean) : RecyclerView.ViewHolder(view) {

        fun bind(cardSlot: CardSlot) {
            with(itemView) {
                setOnClickListener { itemClick(cardSlot) }
                setOnLongClickListener {
                    itemLongClick(itemView.card_collection_image, cardSlot.card)
                }
                if (cardSlot.qtd == 0) {
                    val color = ContextCompat.getColor(itemView.context, R.color.card_zero_qtd)
                    card_collection_image.setColorFilter(color)
                } else {
                    card_collection_image.clearColorFilter()
                }
                card_collection_qtd.setImageResource(when (cardSlot.qtd) {
                    0 -> R.drawable.ic_qtd_zero
                    2 -> R.drawable.ic_qtd_two
                    else -> R.drawable.ic_qtd_three
                })
                card_collection_qtd.visibility = View.GONE.takeIf { cardSlot.qtd == 1 } ?: View.VISIBLE
                card_collection_image.loadFromCard(cardSlot.card)
            }
        }

    }

    class CardsImportedAdapter(val items: List<Pair<CardSlot, Int>>) : RecyclerView.Adapter<CardsImportedViewHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): CardsImportedViewHolder {
            return CardsImportedViewHolder(parent?.inflate(R.layout.itemlist_card_imported))
        }

        override fun onBindViewHolder(holder: CardsImportedViewHolder?, position: Int) {
            holder?.bind(items[position].first, items[position].second)
        }

        override fun getItemCount() = items.size
    }

    class CardsImportedViewHolder(view: View?) : RecyclerView.ViewHolder(view) {

        fun bind(cardSlot: CardSlot, @StringRes status: Int) {
            with(itemView) {
                card_imported_attr.setImageResource(cardSlot.card.attr.imageRes)
                card_imported_name.text = cardSlot.card.name
                card_imported_qtd.text = "${cardSlot.qtd}"
                card_imported_result.text = context.getString(status)
                setOnClickListener {
                    context.startActivity(CardActivity.newIntent(context, cardSlot.card))
                }
            }
        }

    }

}
