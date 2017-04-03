package com.ediposouza.teslesgendstracker.ui.cards

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.res.AssetFileDescriptor
import android.media.AudioManager
import android.media.MediaPlayer
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.support.design.widget.BottomSheetBehavior
import android.support.v4.app.ActivityCompat
import android.support.v4.app.ActivityOptionsCompat
import android.support.v4.view.ViewCompat
import android.support.v7.widget.CardView
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.LinearSnapHelper
import android.support.v7.widget.RecyclerView
import android.text.format.DateUtils
import android.transition.Transition
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
import com.google.firebase.storage.FirebaseStorage
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

    private val card: Card by lazy { intent.getParcelableExtra<Card>(EXTRA_CARD) ?: Card.DUMMY }
    private val cardInfoSheetBehavior: BottomSheetBehavior<CardView> by lazy { BottomSheetBehavior.from(card_bottom_sheet) }
    private val cardVersions by lazy {
        val cardBasicInfo = CardBasicInfo(card.shortName, card.set.toString(), card.attr.name)
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
        with(card_recycler_view) {
            var listener: ViewTreeObserver.OnPreDrawListener? = null
            listener = ViewTreeObserver.OnPreDrawListener {
                viewTreeObserver.removeOnPreDrawListener(listener)
                ActivityCompat.startPostponedEnterTransition(this@CardActivity)
                getCardPatches()
                getCardSounds()
                getCardFullArt()
                true
            }
            viewTreeObserver.addOnPreDrawListener(listener)
        }
        setResult(Activity.RESULT_CANCELED, Intent())
    }

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            window.sharedElementEnterTransition?.apply {
                addListener(object : Transition.TransitionListener {
                    override fun onTransitionEnd(transition: Transition?) {
                        removeListener(this)
                        onTransitionEnds()
                    }

                    override fun onTransitionResume(transition: Transition?) {
                    }

                    override fun onTransitionPause(transition: Transition?) {
                    }

                    override fun onTransitionCancel(transition: Transition?) {
                        removeListener(this)
                    }

                    override fun onTransitionStart(transition: Transition?) {
                    }

                })
            }
        } else {
            onTransitionEnds()
        }
        MetricsManager.trackScreen(MetricScreen.SCREEN_CARD_DETAILS())
        MetricsManager.trackCardView(card)
    }

    private fun onTransitionEnds() {
        card_ads_view.load()
        card_favorite_btn.setOnClickListener { onFavoriteClick() }
        loadCardInfo()
        configBottomSheet()
        configQtdAndFavoriteInfo()
    }

    private fun configQtdAndFavoriteInfo() {
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

    private fun configBottomSheet() {
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
        card_race.text = card.race.name.toLowerCase().capitalize().replace("_", " ")
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

    private fun getCardFullArt() {
        card.getCardFullArtBitmap(this) { cardImage ->
            with(card_expand_btn) {
                if (visibility == View.VISIBLE && cardImage == null) {
                    return@getCardFullArtBitmap
                }
                visibility = View.VISIBLE.takeIf { cardImage != null } ?: View.GONE
                setOnClickListener {
                    visibility = View.GONE
                    card_expand_pb.visibility = View.VISIBLE
                    MetricsManager.trackAction(MetricAction.ACTION_CARD_FULL_ART(card))
                    visibility = View.VISIBLE
                    card_expand_pb.visibility = View.GONE
                    card_art_iv.setImageBitmap(cardImage)
                    val intent = intentFor<CardFullArtActivity>(CardFullArtActivity.EXTRA_CARD to card)
                    ActivityCompat.startActivity(this@CardActivity, intent,
                            ActivityOptionsCompat.makeSceneTransitionAnimation(this@CardActivity,
                                    card_art_iv, getString(R.string.card_full_transition_name)).toBundle())
                }
            }
        }
    }

    private fun getCardSounds() {
        FirebaseStorage.getInstance().reference.apply {
            with(card_sound_play) {
                if (card.hasLocalPlaySound(resources)) {
                    showSoundButton(this)
                    setOnClickListener {
                        MetricsManager.trackAction(MetricAction.ACTION_CARD_START_SOUND_PLAY(card))
                        playSound(afd = getAssets().openFd(card.playSoundPath()))
                    }
                }
                child(card.playSoundPath()).downloadUrl.addOnSuccessListener { result ->
                    showSoundButton(this)
                    setOnClickListener {
                        MetricsManager.trackAction(MetricAction.ACTION_CARD_START_SOUND_PLAY(card))
                        playSound(result)
                    }
                }
            }
            with(card_sound_attack) {
                if (card.hasLocalAttackSound(resources)) {
                    showSoundButton(this)
                    setOnClickListener {
                        MetricsManager.trackAction(MetricAction.ACTION_CARD_START_SOUND_ATTACK(card))
                        playSound(afd = getAssets().openFd(card.attackSoundPath()))
                    }
                }
                child(card.attackSoundPath()).downloadUrl.addOnSuccessListener { result ->
                    showSoundButton(this)
                    setOnClickListener {
                        MetricsManager.trackAction(MetricAction.ACTION_CARD_START_SOUND_ATTACK(card))
                        playSound(result)
                    }
                }
            }
            with(card_sound_extra_label) {
                if (card.hasLocalExtraSound(resources)) {
                    showSoundButton(this)
                    setOnClickListener {
                        MetricsManager.trackAction(MetricAction.ACTION_CARD_START_SOUND_EXTRA(card))
                        playSound(afd = getAssets().openFd(card.extraSoundPath()))
                    }
                }
                child(card.extraSoundPath()).downloadUrl.addOnSuccessListener { result ->
                    showSoundButton(this)
                    setOnClickListener {
                        MetricsManager.trackAction(MetricAction.ACTION_CARD_START_SOUND_EXTRA(card))
                        playSound(result)
                    }
                }
            }
        }
    }

    private fun showSoundButton(button: View) {
        card_sounds_label.visibility = View.VISIBLE
        button.visibility = View.VISIBLE
    }

    private fun playSound(uri: Uri? = null, afd: AssetFileDescriptor? = null) {
        try {
            MediaPlayer().apply {
                uri?.let {
                    setAudioStreamType(AudioManager.STREAM_MUSIC);
                    setDataSource(this@CardActivity, it);
                }
                afd?.let {
                    if (afd.declaredLength < 0) {
                        setDataSource(afd.fileDescriptor)
                    } else {
                        setDataSource(afd.fileDescriptor, afd.startOffset, afd.declaredLength)
                    }
                }
                prepare();
                start();
            }
        } catch (e: Exception) {
            Timber.e(e)
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
                    Card.loadCardImageInto(this, cardBasicInfo.set, cardBasicInfo.attr, cardBasicInfo.shortName)
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
