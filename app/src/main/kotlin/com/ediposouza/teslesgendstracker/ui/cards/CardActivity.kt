package com.ediposouza.teslesgendstracker.ui.cards

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.design.widget.BottomSheetBehavior
import android.support.v4.app.ActivityCompat
import android.support.v4.view.ViewCompat
import android.support.v7.widget.CardView
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.LinearSnapHelper
import android.support.v7.widget.RecyclerView
import android.text.format.DateUtils
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import com.ediposouza.teslesgendstracker.App
import com.ediposouza.teslesgendstracker.R
import com.ediposouza.teslesgendstracker.SEASON_UUID_PATTERN
import com.ediposouza.teslesgendstracker.data.Card
import com.ediposouza.teslesgendstracker.data.CardBasicInfo
import com.ediposouza.teslesgendstracker.interactor.PrivateInteractor
import com.ediposouza.teslesgendstracker.interactor.PublicInteractor
import com.ediposouza.teslesgendstracker.ui.base.BaseActivity
import com.ediposouza.teslesgendstracker.util.*
import kotlinx.android.synthetic.main.activity_card.*
import kotlinx.android.synthetic.main.include_card_info.*
import kotlinx.android.synthetic.main.itemlist_card_full.view.*
import org.jetbrains.anko.intentFor
import org.jetbrains.anko.toast
import org.threeten.bp.YearMonth
import org.threeten.bp.format.DateTimeFormatter
import org.threeten.bp.format.TextStyle
import timber.log.Timber
import java.util.*

class CardActivity : BaseActivity() {

    companion object {

        private val EXTRA_CARD = "cardExtra"

        fun newIntent(context: Context, card: Card): Intent {
            return context.intentFor<CardActivity>(EXTRA_CARD to card)
        }

    }

    private val card: Card by lazy { intent.getParcelableExtra<Card>(EXTRA_CARD) }
    private val cardInfoSheetBehavior: BottomSheetBehavior<CardView> by lazy { BottomSheetBehavior.from(card_bottom_sheet) }
    private val cardVersions by lazy {
        val cardBasicInfo = CardBasicInfo(card.shortName, card.set.name, card.attr.name)
        mutableListOf(Pair(cardBasicInfo, getString(R.string.card_patch_current)))
    }

    private var favorite: Boolean = false
    private var userCardQtd = 0
    private val onCardClick = {
        finishAndAnimateBack()
        MetricsManager.trackAction(MetricAction.ACTION_CARD_DETAILS_CLOSE_TAP())
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_card)
        ActivityCompat.postponeEnterTransition(this)
        snackbarNeedMargin = false
        if (hasNavigationBar()) {
            card_content.setBottomPaddingForNavigationBar()
            card_info_content.setBottomPaddingForNavigationBar()
            cardInfoSheetBehavior.peekHeight = resources.getDimensionPixelOffset(R.dimen.card_bottom_sheet_peek_height) +
                    resources.getDimensionPixelOffset(R.dimen.navigation_bar_height)
        }

        configureRecycleView()
        loadCardInfo()
        configureBottomSheet()
        card_favorite_btn.setOnClickListener { onFavoriteClick() }
        setResult(Activity.RESULT_CANCELED, Intent())
    }

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)
        MetricsManager.trackScreen(MetricScreen.SCREEN_CARD_DETAILS())
        MetricsManager.trackCardView(card)
        card_ads_view.load()
        if (App.hasUserLogged()) {
            showUserCardQtd()
        }
        PrivateInteractor.isUserCardFavorite(card) {
            favorite = it
            updateFavoriteButton()
        }
    }

    override fun onSaveInstanceState(outState: Bundle?) {
        super.onSaveInstanceState(outState)
        ActivityCompat.finishAfterTransition(this)
    }

    override fun onBackPressed() {
        if (cardInfoSheetBehavior.state == BottomSheetBehavior.STATE_EXPANDED) {
            cardInfoSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
        } else {
            finishAndAnimateBack()
        }
    }

    private fun finishAndAnimateBack() {
        cardVersions.removeAll { it.first.shortName != card.shortName }
        with(card_recycler_view) {
            adapter.notifyDataSetChanged()
            post({ ActivityCompat.finishAfterTransition(this@CardActivity) })
        }
    }

    private fun showUserCardQtd() {
        card_collection_qtd_layout.visibility = View.VISIBLE
        PrivateInteractor.getUserCollection(card.set, card.attr) {
            userCardQtd = it[card.shortName] ?: 0
            updateChangeCardQtdButtons()
            card_collection_qtd_loading.visibility = View.GONE
            card_collection_qtd.text = userCardQtd.toString()
            card_collection_qtd.visibility = View.VISIBLE
            with(card_collection_qtd_plus_btn) {
                visibility = View.VISIBLE
                setOnClickListener { updateCardQtd(userCardQtd.plus(1)) }
            }
            with(card_collection_qtd_minus_btn) {
                visibility = View.VISIBLE
                setOnClickListener { updateCardQtd(userCardQtd.minus(1)) }
            }
        }
    }

    private fun updateChangeCardQtdButtons() {
        val cardMaxQtd = 1.takeIf { card.unique } ?: 3
        card_collection_qtd_plus_btn.isEnabled = userCardQtd < cardMaxQtd
        card_collection_qtd_minus_btn.isEnabled = userCardQtd > 0
    }

    private fun updateCardQtd(newCardQtd: Int) {
        PrivateInteractor.setUserCardQtd(card, newCardQtd) {
            userCardQtd = newCardQtd
            card_collection_qtd.text = newCardQtd.toString()
            updateChangeCardQtdButtons()
            setResult(Activity.RESULT_OK, Intent())
        }
    }

    private fun configureBottomSheet() {
        cardInfoSheetBehavior.setBottomSheetCallback(object : BottomSheetBehavior.BottomSheetCallback() {
            override fun onSlide(bottomSheet: View, slideOffset: Float) {
            }

            override fun onStateChanged(bottomSheet: View, newState: Int) {
                when (newState) {
                    BottomSheetBehavior.STATE_EXPANDED ->
                        MetricsManager.trackAction(MetricAction.ACTION_CARD_DETAILS_EXPAND())
                    BottomSheetBehavior.STATE_COLLAPSED ->
                        MetricsManager.trackAction(MetricAction.ACTION_CARD_DETAILS_COLLAPSE())
                }
            }

        })
        card_bottom_sheet.setOnClickListener { cardInfoSheetBehavior.toggleExpanded() }
    }

    private fun loadCardInfo() {
        updateFavoriteButton()
        card_set.text = card.set.name.toLowerCase().capitalize()
        if (card.season.isNotEmpty()) {
            val yearMonth = YearMonth.parse(card.season, DateTimeFormatter.ofPattern(SEASON_UUID_PATTERN))
            val month = yearMonth.month.getDisplayName(TextStyle.FULL, Locale.getDefault())
            card_reward.text = "$month/${yearMonth.year} ${getString(R.string.season_reward)}"
            card_reward_label.visibility = View.VISIBLE
        }
        card_race.text = card.race.name.toLowerCase().capitalize()
        card_race_desc.text = card.race.desc
        card_race_desc.visibility = View.GONE.takeIf { card.race.desc.isEmpty() } ?: View.VISIBLE
        card_arena_tier.text = card.arenaTier.name.toLowerCase().capitalize()
    }

    private fun configureRecycleView() {
        with(card_recycler_view) {
            layoutManager = LinearLayoutManager(this@CardActivity, LinearLayoutManager.HORIZONTAL, true)
            adapter = CardAdapter(cardVersions, onCardClick)
            setHasFixedSize(true)
            LinearSnapHelper().attachToRecyclerView(this)
            var listener: ViewTreeObserver.OnPreDrawListener? = null
            listener = ViewTreeObserver.OnPreDrawListener {
                card_recycler_view.viewTreeObserver.removeOnPreDrawListener(listener)
                ActivityCompat.startPostponedEnterTransition(this@CardActivity)
                getCardPatches()
                true
            }
            viewTreeObserver.addOnPreDrawListener(listener)
        }
    }

    private fun getCardPatches() {
        PublicInteractor.getPatches {
            val cardPatches = it.filter {
                it.changes.filter { it.shortName == card.shortName }.isNotEmpty()
            }.sortedBy { it.date }.reversed()
            Timber.d(cardPatches.toString())
            if (cardPatches.isNotEmpty()) {
                cardPatches.forEach {
                    val cardPatchName = "${card.shortName}_${it.uuidDate}"
                    val cardBasicInfo = CardBasicInfo(cardPatchName, card.set.name, card.attr.name)
                    cardVersions.add(Pair(cardBasicInfo, getString(R.string.card_patch_pre, it.desc)))
                }
                with(card_recycler_view) {
                    adapter.notifyDataSetChanged()
                    postDelayed({
                        smoothScrollBy(width * -1 / 3, 0)
                    }, DateUtils.SECOND_IN_MILLIS / 2)
                }
            }

        }
    }

    private fun updateFavoriteButton() {
        val drawableRes = R.drawable.ic_favorite_checked.takeIf { favorite } ?: R.drawable.ic_favorite_unchecked
        card_favorite_btn.setImageResource(drawableRes)
    }

    private fun onFavoriteClick() {
        if (App.hasUserLogged()) {
            PrivateInteractor.setUserCardFavorite(card, !favorite) {
                favorite = !favorite
                val stringRes = R.string.action_favorited.takeIf { favorite } ?: R.string.action_unfavorited
                toast(getString(stringRes, card.name))
                updateFavoriteButton()
                setResult(Activity.RESULT_OK, Intent())
                MetricsManager.trackAction(if (favorite)
                    MetricAction.ACTION_CARD_DETAILS_FAVORITE() else MetricAction.ACTION_CARD_DETAILS_UNFAVORITE())
            }
        } else {
            showErrorUserNotLogged()
        }
    }

    class CardAdapter(val items: List<Pair<CardBasicInfo, String>>, val onCardClick: () -> Unit) : RecyclerView.Adapter<CardViewHolder>() {
        override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): CardViewHolder {
            return CardViewHolder(parent?.inflate(R.layout.itemlist_card_full))
        }

        override fun onBindViewHolder(holder: CardViewHolder?, position: Int) {
            val pair = items[position]
            val isFirst = position == 0
            val isLast = position == items.size - 1
            val hasPatchVersion = itemCount > 1
            holder?.bind(pair.first, pair.second, isFirst, isLast, hasPatchVersion, onCardClick)
        }

        override fun getItemCount(): Int = items.size

    }

    class CardViewHolder(view: View?) : RecyclerView.ViewHolder(view) {

        fun bind(cardBasicInfo: CardBasicInfo, cardPatchDesc: String, isFirst: Boolean,
                 isLast: Boolean, hasPatchVersion: Boolean, onCardClick: () -> Unit) {
            with(itemView) {
                with(card_patch_full_image) {
                    setImageBitmap(Card.getCardImageBitmap(context, cardBasicInfo.set,
                            cardBasicInfo.attr, cardBasicInfo.shortName))
                    ViewCompat.setTransitionName(this, context.getString(R.string.card_transition_name).takeIf { isFirst } ?: "")
                    setPadding(if (isLast) 0 else resources.getDimensionPixelSize(R.dimen.huge_margin), 0, 0, 0)
                }
                card_patch_desc.text = cardPatchDesc
                card_patch_desc_shadow.text = cardPatchDesc
                card_patch_desc.visibility = View.VISIBLE.takeIf { hasPatchVersion } ?: View.GONE
                card_patch_desc_shadow.visibility = View.VISIBLE.takeIf { hasPatchVersion } ?: View.GONE
                card_patch_arrow.visibility = View.GONE.takeIf { isLast } ?: View.VISIBLE
                setOnClickListener { onCardClick() }
            }
        }

    }

}
