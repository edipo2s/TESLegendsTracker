package com.ediposouza.teslesgendstracker.ui.decks

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Rect
import android.os.Build
import android.os.Bundle
import android.support.design.widget.BottomSheetBehavior
import android.support.design.widget.CollapsingToolbarLayout
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v7.widget.CardView
import android.support.v7.widget.DividerItemDecoration
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.text.format.DateUtils
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.RelativeLayout
import com.bumptech.glide.Glide
import com.ediposouza.teslesgendstracker.App
import com.ediposouza.teslesgendstracker.R
import com.ediposouza.teslesgendstracker.TIME_PATTERN
import com.ediposouza.teslesgendstracker.data.Deck
import com.ediposouza.teslesgendstracker.data.DeckComment
import com.ediposouza.teslesgendstracker.interactor.PrivateInteractor
import com.ediposouza.teslesgendstracker.interactor.PublicInteractor
import com.ediposouza.teslesgendstracker.ui.base.BaseActivity
import com.ediposouza.teslesgendstracker.ui.base.CmdShowSnackbarMsg
import com.ediposouza.teslesgendstracker.ui.util.CircleTransform
import com.ediposouza.teslesgendstracker.ui.util.KeyboardUtil
import com.ediposouza.teslesgendstracker.util.*
import com.google.firebase.auth.FirebaseAuth
import io.fabric.sdk.android.services.common.CommonUtils
import jp.wasabeef.recyclerview.animators.SlideInLeftAnimator
import kotlinx.android.synthetic.main.activity_deck.*
import kotlinx.android.synthetic.main.include_deck_info.*
import kotlinx.android.synthetic.main.itemlist_deck_comment.view.*
import org.jetbrains.anko.*
import org.threeten.bp.format.DateTimeFormatter
import timber.log.Timber
import java.text.NumberFormat
import java.util.*

class DeckActivity : BaseActivity() {

    companion object {

        private val EXTRA_DECK = "deckExtra"
        private val EXTRA_FAVORITE = "favoriteExtra"
        private val EXTRA_LIKE = "likeExtra"
        private val EXTRA_OWNED = "ownedExtra"

        fun newIntent(context: Context, deck: Deck, favorite: Boolean, like: Boolean, owned: Boolean): Intent {
            return context.intentFor<DeckActivity>(EXTRA_DECK to deck, EXTRA_FAVORITE to favorite,
                    EXTRA_LIKE to like, EXTRA_OWNED to owned)
        }

    }

    private val publicInteractor by lazy { PublicInteractor() }
    private val privateInteractor by lazy { PrivateInteractor() }
    private val keyboardUtil by lazy { KeyboardUtil(this, contentView) }
    private val deckOwned by lazy { intent.getBooleanExtra(EXTRA_OWNED, false) }
    private val deck by lazy { intent.getParcelableExtra<Deck>(EXTRA_DECK) }
    private val numberInstance: NumberFormat by lazy { NumberFormat.getNumberInstance() }
    private val commentsSheetBehavior: BottomSheetBehavior<CardView> by lazy { BottomSheetBehavior.from(deck_bottom_sheet) }

    private var favorite: Boolean = false
    private var like: Boolean = false
    private var menuLike: MenuItem? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_deck)
        val statusBarHeight = resources.getDimensionPixelSize(R.dimen.status_bar_height)
        if (Build.VERSION.SDK_INT == Build.VERSION_CODES.KITKAT) {
            val coverLP = deck_class_cover.layoutParams as RelativeLayout.LayoutParams
            coverLP.height = coverLP.height - statusBarHeight
            deck_class_cover.layoutParams = coverLP
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            val layoutParams = toolbar.layoutParams as CollapsingToolbarLayout.LayoutParams
            layoutParams.topMargin = statusBarHeight
            toolbar.layoutParams = layoutParams
        }

        favorite = intent.getBooleanExtra(EXTRA_FAVORITE, false)
        like = intent.getBooleanExtra(EXTRA_LIKE, false)
        configViews()
        updateFavoriteItem()
        loadDeckInfo()
    }

    private fun configViews() {
        if (deckOwned) {
            deck_fab_favorite.hide()
            deck_details_likes.visibility = View.GONE
            deck_details_views.visibility = View.GONE
            commentsSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN
        }
        deck_fab_favorite.setOnClickListener {
            if (App.hasUserLogged()) {
                privateInteractor.setUserDeckFavorite(deck, !favorite) {
                    favorite = !favorite
                    val stringRes = if (favorite) R.string.action_favorited else R.string.action_unfavorited
                    toast(getString(stringRes, deck.name))
                    updateFavoriteItem()
                    MetricsManager.trackAction(if (favorite)
                        MetricAction.ACTION_DECK_DETAILS_FAVORITE() else MetricAction.ACTION_DECK_DETAILS_UNFAVORITE())
                }
            } else {
                showErrorUserNotLogged()
            }
        }
        commentsSheetBehavior.setBottomSheetCallback(object : BottomSheetBehavior.BottomSheetCallback() {
            override fun onSlide(bottomSheet: View, slideOffset: Float) {
            }

            override fun onStateChanged(bottomSheet: View, newState: Int) {
                when (newState) {
                    BottomSheetBehavior.STATE_EXPANDED ->
                        MetricsManager.trackAction(MetricAction.ACTION_DECK_COMMENTS_EXPAND())
                    BottomSheetBehavior.STATE_COLLAPSED ->
                        MetricsManager.trackAction(MetricAction.ACTION_DECK_COMMENTS_COLLAPSE())
                }
            }

        })
        deck_bottom_sheet.setOnClickListener { commentsSheetBehavior.toggleExpanded() }
        deck_comment_send?.setOnClickListener {
            if (App.hasUserLogged()) {
                if (deck_comment_new.text.toString().length < 4) {
                    eventBus.post(CmdShowSnackbarMsg(CmdShowSnackbarMsg.TYPE_ERROR, R.string.deck_comment_size_error)
                            .withAction(android.R.string.ok, {}))
                } else {
                    PrivateInteractor().addDeckComment(deck, deck_comment_new.text.toString()) {
                        deck_comment_new.setText("")
                        addComment(it)
                        MetricsManager.trackAction(MetricAction.ACTION_DECK_COMMENTS_SEND())
                    }
                }
            } else {
                showErrorUserNotLogged()
            }
        }
    }

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        onKeyboardVisibilityChange = {
            deck_comment_recycle_view.requestLayout()
        }
        if (!App.hasUserLogged()) {
            deck_comment_new?.isEnabled = false
            deck_comment_send?.isEnabled = false
            deck_comment_new?.hint = getText(R.string.deck_comment_new_hint_anonymous)
        }
        loadDeckRemoteInfo()
        if (savedInstanceState != null) {
            deck_comment_new.postDelayed({
                CommonUtils.hideKeyboard(this, deck_comment_new)
            }, DateUtils.SECOND_IN_MILLIS / 2)
        }
        if (ConfigManager.isShowDeckAds()) {
            deck_ads_view.visibility = View.VISIBLE
            deck_ads_view.load()
        }
        setResult(Activity.RESULT_OK, Intent())
        MetricsManager.trackScreen(MetricScreen.SCREEN_DECK_DETAILS())
    }

    override fun onBackPressed() {
        if (commentsSheetBehavior.state == BottomSheetBehavior.STATE_EXPANDED) {
            commentsSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
        } else {
            super.onBackPressed()
        }
    }

    override fun onStart() {
        super.onStart()
        keyboardUtil.enable()
    }

    override fun onStop() {
        super.onStop()
        keyboardUtil.disable()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(if (deckOwned) R.menu.menu_delete else R.menu.menu_like, menu)
        menuLike = menu?.findItem(R.id.menu_like)
        updateLikeItem()
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item?.itemId) {
            android.R.id.home -> {
                ActivityCompat.finishAfterTransition(this)
                return true
            }
            R.id.menu_like -> {
                if (!App.hasUserLogged()) {
                    showErrorUserNotLogged()
                    return false
                }
                privateInteractor.setUserDeckLike(deck, !like) {
                    like = !like
                    updateLikeItem()
                    val deckLikes = Integer.parseInt(deck_details_likes.text.toString())
                    deck_details_likes.text = numberInstance.format(deckLikes + if (like) 1 else -1)
                    MetricsManager.trackAction(if (like)
                        MetricAction.ACTION_DECK_DETAILS_LIKE() else MetricAction.ACTION_DECK_DETAILS_UNLIKE())
                }
                return true
            }
            R.id.menu_delete -> {
                alert(R.string.confirm_message) {
                    negativeButton(android.R.string.no, {})
                    positiveButton(android.R.string.yes, {
                        privateInteractor.deleteDeck(deck, deck.private) {
                            toast(R.string.deck_deleted)
                            ActivityCompat.finishAfterTransition(this@DeckActivity)
                            MetricsManager.trackAction(MetricAction.ACTION_DECK_DETAILS_DELETE())
                        }
                    })
                    setTheme(R.style.AppDialog)
                }.show()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun updateLikeItem() {
        val icon = if (like) R.drawable.ic_like_checked else R.drawable.ic_like_unchecked
        menuLike?.icon = ContextCompat.getDrawable(this, icon)
        menuLike?.title = getString(if (like) R.string.menu_unlike else R.string.menu_like)
    }

    private fun updateFavoriteItem() {
        val icon = if (favorite) R.drawable.ic_favorite_checked else R.drawable.ic_favorite_unchecked
        deck_fab_favorite.setImageDrawable(ContextCompat.getDrawable(this, icon))
        val contentDescription = if (favorite) R.string.menu_unfavorite else R.string.menu_favorite
        deck_fab_favorite.contentDescription = getString(contentDescription)
    }

    private fun loadDeckInfo() {
        deck_name.text = deck.name
        deck_class_cover.setImageResource(deck.cls.imageRes)
        deck_class_attr1.setImageResource(deck.cls.attr1.imageRes)
        deck_class_attr2.setImageResource(deck.cls.attr2.imageRes)
        deck_details_type.text = deck.type.name.toLowerCase().capitalize()
        deck_details_views.text = numberInstance.format(deck.views)
        deck_details_likes.text = numberInstance.format(deck.likes.size)
        deck_details_soul_cost.text = numberInstance.format(deck.cost)
        deck_details_create_at.text = deck.createdAt.toLocalDate().toString()
        val updateDate = deck.updatedAt.toLocalDate()
        val updateTime = deck.updatedAt.toLocalTime().format(DateTimeFormatter.ofPattern(TIME_PATTERN))
        deck_details_update_at.text = getString(R.string.deck_details_last_update_format, updateDate, updateTime)
        deck_details_cardlist.showDeck(deck, false)
        configDeckComments()
    }

    private fun configDeckComments() {
        with(deck_comment_recycle_view) {
            adapter = DeckCommentAdapter(deck.comments, publicInteractor) {
                privateInteractor.remDeckComment(deck, it) {
                    remComment(it)
                }
            }
            layoutManager = object : LinearLayoutManager(this@DeckActivity) {
                override fun setMeasuredDimension(childrenBounds: Rect, wSpec: Int, hSpec: Int) {
                    val maxHeight = resources.displayMetrics.heightPixels * getVisiblePercent()
                    with(childrenBounds) {
                        set(left, top, right, if (bottom < maxHeight) bottom else maxHeight.toInt())
                    }
                    super.setMeasuredDimension(childrenBounds, wSpec, hSpec)
                }

                private fun getVisiblePercent(): Float = if (keyboardVisible) 0.2f else 0.6f

            }
            itemAnimator = SlideInLeftAnimator()
            addItemDecoration(DividerItemDecoration(this@DeckActivity, DividerItemDecoration.VERTICAL))
        }
        deck_comment_qtd.text = numberInstance.format(deck.comments.size)
    }

    private fun loadDeckRemoteInfo() {
        doAsync {
            calculateMissingSoul(deck, privateInteractor)
            if (!deckOwned) {
                publicInteractor.incDeckView(deck) {
                    deck_details_views.text = it.toString()
                }
            }
            publicInteractor.getPatches {
                val patch = it.find { it.uuidDate == deck.patch }
                runOnUiThread {
                    deck_details_patch.text = patch?.desc ?: ""
                }
            }
            publicInteractor.getUserInfo(deck.owner) {
                val ownerUser = it
                runOnUiThread {
                    deck_details_create_by.text = ownerUser.name
                    Glide.with(this@DeckActivity)
                            .load(ownerUser.photoUrl)
                            .transform(CircleTransform(this@DeckActivity))
                            .into(deck_details_create_by_photo)
                }
            }
        }
    }

    fun calculateMissingSoul(deck: Deck, privateInteractor: PrivateInteractor) {
        with(deck_details_soul_missing) {
            visibility = View.INVISIBLE
            deck_details_soul_missing_loading.visibility = View.VISIBLE
            privateInteractor.getDeckMissingCards(deck, { deck_details_soul_missing_loading.visibility = View.VISIBLE }) {
                deck_details_soul_missing_loading.visibility = View.GONE
                val missingSoul = it.map { it.qtd * it.rarity.soulCost }.sum()
                Timber.d("Missing %d", missingSoul)
                text = NumberFormat.getNumberInstance().format(missingSoul)
                visibility = View.VISIBLE
                deck_details_cardlist.showMissingCards(it)
            }
        }
    }

    private fun addComment(it: DeckComment) {
        (deck_comment_recycle_view.adapter as DeckCommentAdapter).add(it)
        deck_comment_recycle_view.scrollToPosition(0)
        deck_comment_qtd.text = deck_comment_recycle_view.adapter.itemCount.toString()
        deck_comment_recycle_view.requestLayout()
    }

    private fun remComment(commentId: String) {
        (deck_comment_recycle_view.adapter as DeckCommentAdapter).rem(commentId)
        deck_comment_qtd.text = deck_comment_recycle_view.adapter.itemCount.toString()
        deck_comment_recycle_view.requestLayout()
    }

    class DeckCommentAdapter(val items: List<DeckComment>, val publicInteractor: PublicInteractor,
                             val onRemComment: (commentId: String) -> Unit) : RecyclerView.Adapter<DeckCommentViewHolder>() {

        init {
            sortDeckComments()
        }

        override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): DeckCommentViewHolder {
            return DeckCommentViewHolder(parent?.inflate(R.layout.itemlist_deck_comment), onRemComment)
        }

        override fun onBindViewHolder(holder: DeckCommentViewHolder?, position: Int) {
            holder?.bind(items[position], publicInteractor)
        }

        override fun getItemCount() = items.size

        fun add(deckComment: DeckComment) {
            (items as ArrayList).add(0, deckComment)
            notifyItemInserted(0)
        }

        fun rem(commentId: String) {
            val deckComment = items.find { it.uuid == commentId }
            val deckCommentIndex = items.indexOf(deckComment)
            (items as ArrayList).remove(deckComment)
            notifyItemRemoved(deckCommentIndex)
        }

        private fun sortDeckComments() {
            Collections.sort(items, { dc1, dc2 -> dc2.date.compareTo(dc1.date) })
        }

    }

    class DeckCommentViewHolder(view: View?, val onRemComment: (commentId: String) -> Unit) : RecyclerView.ViewHolder(view) {

        fun bind(comment: DeckComment, publicInteractor: PublicInteractor) {
            val timeFormatter = DateTimeFormatter.ofPattern(TIME_PATTERN)
            itemView.deck_comment_msg.text = comment.comment
            itemView.deck_comment_date.text = itemView.context.getString(R.string.deck_comment_date_format,
                    comment.date.toLocalDate(), comment.date.toLocalTime().format(timeFormatter))
            doAsync {
                publicInteractor.getUserInfo(comment.owner) {
                    val ownerUser = it
                    itemView.post {
                        itemView.deck_comment_owner.text = ownerUser.name
                        with(itemView.deck_comment_delete) {
                            val owner = comment.owner == FirebaseAuth.getInstance().currentUser?.uid
                            visibility = if (owner) View.VISIBLE else View.GONE
                            setOnClickListener { onRemComment(comment.uuid) }
                        }
                        Glide.with(itemView.context)
                                .load(ownerUser.photoUrl)
                                .fallback(R.drawable.ic_user)
                                .transform(CircleTransform(itemView.context))
                                .into(itemView.deck_comment_owner_photo)
                    }
                }
            }
        }

    }

}