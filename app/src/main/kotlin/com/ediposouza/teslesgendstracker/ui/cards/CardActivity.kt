package com.ediposouza.teslesgendstracker.ui.cards

import android.Manifest
import android.app.Activity
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.res.AssetFileDescriptor
import android.media.AudioManager
import android.media.MediaPlayer
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.provider.MediaStore
import android.provider.Settings
import android.support.design.widget.BottomSheetBehavior
import android.support.v4.app.ActivityCompat
import android.support.v4.app.ActivityOptionsCompat
import android.support.v4.view.ViewCompat
import android.support.v7.widget.*
import android.text.format.DateUtils
import android.transition.Transition
import android.view.ContextThemeWrapper
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.widget.Toast
import com.ediposouza.teslesgendstracker.App
import com.ediposouza.teslesgendstracker.R
import com.ediposouza.teslesgendstracker.SEASON_UUID_PATTERN
import com.ediposouza.teslesgendstracker.data.Card
import com.ediposouza.teslesgendstracker.data.CardBasicInfo
import com.ediposouza.teslesgendstracker.data.CardSet
import com.ediposouza.teslesgendstracker.data.CardType
import com.ediposouza.teslesgendstracker.interactor.PrivateInteractor
import com.ediposouza.teslesgendstracker.interactor.PublicInteractor
import com.ediposouza.teslesgendstracker.ui.base.BaseActivity
import com.ediposouza.teslesgendstracker.ui.base.CmdShowSnackbarMsg
import com.ediposouza.teslesgendstracker.util.*
import com.google.firebase.storage.FirebaseStorage
import kotlinx.android.synthetic.main.activity_card.*
import kotlinx.android.synthetic.main.include_card_info.*
import kotlinx.android.synthetic.main.itemlist_card_full.view.*
import kotlinx.android.synthetic.main.itemlist_card_min.view.*
import org.jetbrains.anko.intentFor
import org.jetbrains.anko.toast
import org.threeten.bp.YearMonth
import org.threeten.bp.format.DateTimeFormatter
import org.threeten.bp.format.TextStyle
import timber.log.Timber
import java.io.File
import java.util.*

class CardActivity : BaseActivity() {

    companion object {

        private val EXTRA_CARD = "cardExtra"
        private val EXTRA_PREVIOUS_CARD = "previousCardExtra"
        private val EXTRA_FROM_SPOILER = "fromSpoilerExtra"

        fun newIntent(context: Context, card: Card, previousCard: Card? = null, fromSpoiler: Boolean = false): Intent {
            return context.intentFor<CardActivity>(EXTRA_CARD to card, EXTRA_FROM_SPOILER to fromSpoiler).apply {
                previousCard?.let {
                    putExtra(EXTRA_PREVIOUS_CARD, previousCard)
                }
            }
        }

    }

    private val RC_WRITE_STORAGE_PERMISSION = 125
    private val RC_WRITE_SETTINGS_PERMISSION = 126
    private val PERMISSION_WRITE_STORAGE = Manifest.permission.WRITE_EXTERNAL_STORAGE

    private val card: Card by lazy { intent.getParcelableExtra<Card>(EXTRA_CARD) ?: Card.DUMMY }
    private val previousCard: Card by lazy { intent.getParcelableExtra<Card>(EXTRA_PREVIOUS_CARD) ?: Card.DUMMY }
    private val fromSpoiler: Boolean by lazy { intent.getBooleanExtra(EXTRA_FROM_SPOILER, false) }
    private val cardInfoSheetBehavior: BottomSheetBehavior<CardView> by lazy { BottomSheetBehavior.from(card_bottom_sheet) }
    private val cardVersions by lazy {
        val cardBasicInfo = CardBasicInfo(card.shortName, card.set.toString(), card.attr.name, card.isToken())
        mutableListOf(Pair(cardBasicInfo, getString(R.string.card_patch_current)))
    }

    private var favorite: Boolean = false
    private var userCardQtd = 0
    private val onCardClick = {
        if (cardInfoSheetBehavior.state == BottomSheetBehavior.STATE_EXPANDED) {
            cardInfoSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
        }
        finishAndAnimateBack()
        MetricsManager.trackAction(MetricAction.ACTION_CARD_DETAILS_CLOSE_TAP())
    }

    private val ringtoneDir by lazy { Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_RINGTONES) }
    private val currentVersion by lazy { getCurrentVersion() }
    private val playAsRingtoneFile by lazy { File(ringtoneDir, "${card.name}_play.mp3") }
    private val attackAsRingtoneFile by lazy { File(ringtoneDir, "${card.name}_attack.mp3") }
    private val extraAsRingtoneFile by lazy { File(ringtoneDir, "${card.name}_extra.mp3") }
    private var playSoundBytes: ByteArray? = null
    private var attackSoundBytes: ByteArray? = null
    private var extraSoundBytes: ByteArray? = null

    private lateinit var tmpRingtoneFile: File

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
                try {
                    getCardSounds()
                    getCardFullArt()
                } catch (e: Exception) {
                    Timber.e(e)
                }
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
        if (App.hasUserLogged() && !card.isToken() && !fromSpoiler) {
            showUserCardQtd()
        }
        card_favorite_btn.visibility = View.VISIBLE.takeUnless { card.isToken() } ?: View.GONE
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

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        if (requestCode == RC_WRITE_STORAGE_PERMISSION) {
            if (hasPermission(PERMISSION_WRITE_STORAGE)) {
                setAsRingtoneClick(tmpRingtoneFile)
            } else {
                if (ActivityCompat.shouldShowRequestPermissionRationale(this, PERMISSION_WRITE_STORAGE)) {
                    eventBus.post(CmdShowSnackbarMsg(CmdShowSnackbarMsg.TYPE_INFO, R.string.card_full_permission_write_storage)
                            .withAction(android.R.string.ok, { requestWriteStoragePermission() }))
                } else {
                    eventBus.post(CmdShowSnackbarMsg(CmdShowSnackbarMsg.TYPE_INFO, R.string.card_full_permission_write_storage_denied))
                }
            }
        }
        if (requestCode == RC_WRITE_SETTINGS_PERMISSION) {
            if (Settings.System.canWrite(this)) {
                setAsRingtoneClick(tmpRingtoneFile)
            } else {
                eventBus.post(CmdShowSnackbarMsg(CmdShowSnackbarMsg.TYPE_INFO, R.string.card_full_permission_write_settings_denied))
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    private fun requestWriteStoragePermission() {
        requestPermissions(arrayOf(PERMISSION_WRITE_STORAGE), RC_WRITE_STORAGE_PERMISSION)
    }

    private fun requestWriteSettingsPermission() {
        startActivityForResult(Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS).apply {
            data = Uri.parse("package:${packageName}")
        }, RC_WRITE_SETTINGS_PERMISSION)
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
        card_set.text = card.set.let { set ->
            set.title.takeIf { set != CardSet.UNKNOWN } ?: set.unknownSetTitle
        }
        if (card.season.isNotEmpty()) {
            val yearMonth = YearMonth.parse(card.season, DateTimeFormatter.ofPattern(SEASON_UUID_PATTERN))
            val month = yearMonth.month.getDisplayName(TextStyle.FULL, Locale.getDefault())
            card_reward.text = "$month/${yearMonth.year}"
            card_reward_label.visibility = View.VISIBLE
        }
        card_race.text = card.race.name.toLowerCase().capitalize().replace("_", " ")
        card_race_label.visibility = View.VISIBLE.takeIf { card.type == CardType.CREATURE } ?: View.GONE
        card_race.visibility = View.VISIBLE.takeIf { card.type == CardType.CREATURE } ?: View.GONE
        card_race_desc.text = card.race.desc
        card_race_desc.visibility = View.GONE.takeIf { card.race.desc.isEmpty() } ?: View.VISIBLE
        card_arena_tier.text = card.arenaTier.name.toLowerCase().capitalize()
        configureTokens()
        configureShoutLevels()
    }

    private fun configureTokens() {
        if (card.canGenerateTokens() || card.isToken()) {
            card_tokens_label.setText(R.string.card_creators_label.takeIf { card.isToken() } ?: R.string.card_tokens_label)
            card_tokens_label.visibility = View.VISIBLE
            with(card_tokens_rv) {
                val relatedCards = mutableListOf<Card>()
                visibility = View.VISIBLE
                layoutManager = LinearLayoutManager(this@CardActivity, LinearLayoutManager.HORIZONTAL, false)
                adapter = CardRelatedAdapter(relatedCards) { view, relatedCard ->
                    Timber.d("${relatedCard.shortName} - ${previousCard.shortName}")
                    if (relatedCard == previousCard) {
                        ActivityCompat.finishAfterTransition(this@CardActivity)
                    } else {
                        val intent = CardActivity.newIntent(this@CardActivity, relatedCard, card)
                        ActivityCompat.startActivity(this@CardActivity, intent,
                                ActivityOptionsCompat.makeSceneTransitionAnimation(this@CardActivity,
                                        view, getString(R.string.card_transition_name)).toBundle())
                    }
                }
                setHasFixedSize(true)
                if (card.isToken()) {
                    PublicInteractor.getCards(null) { allCards ->
                        relatedCards.addAll(allCards.filter { card.creators.contains(it.shortName) })
                        adapter.notifyDataSetChanged()
                    }
                } else {
                    PublicInteractor.getTokens(null) { allTokens ->
                        relatedCards.addAll(allTokens.filter { card.tokens.contains(it.shortName) })
                        adapter.notifyDataSetChanged()
                    }
                }
            }
        }
    }

    private fun configureShoutLevels() {
        if (card.shout > 1) {
            card_levels_label.visibility = View.VISIBLE
            with(card_levels_rv) {
                visibility = View.VISIBLE
                layoutManager = LinearLayoutManager(this@CardActivity, LinearLayoutManager.HORIZONTAL, false)
                val shoutCards = (2..card.shout).asSequence().map { card to it }.toList()
                adapter = CardLevelsAdapter(shoutCards) { view, level ->
                    val intent = intentFor<CardLevelActivity>(CardLevelActivity.EXTRA_CARD to card,
                            CardLevelActivity.EXTRA_CARD_LEVEL to level)
                    ActivityCompat.startActivity(this@CardActivity, intent,
                            ActivityOptionsCompat.makeSceneTransitionAnimation(this@CardActivity,
                                    view, getString(R.string.card_full_transition_name)).toBundle())
                }
                setHasFixedSize(true)
            }
        }
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
                    val cardBasicInfo = CardBasicInfo(cardPatchName, card.set.name, card.attr.name, card.isToken())
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
        val ringtonePopupMenu = PopupMenu(ContextThemeWrapper(this, R.style.AppDialog), card_sound_ringtone).apply {
            inflate(R.menu.menu_ringtone)
            setOnMenuItemClickListener { item ->
                setAsRingtoneClick(when (item.itemId) {
                    R.id.menu_ringtone_play -> playAsRingtoneFile
                    R.id.menu_ringtone_attack -> attackAsRingtoneFile
                    else -> extraAsRingtoneFile
                })
                true
            }
        }
        card_sound_ringtone.setOnClickListener { ringtonePopupMenu.show() }
        FirebaseStorage.getInstance().reference.apply {
            with(card_sound_play) {
                val playSoundPath = card.playSoundPath()
                if (card.hasLocalPlaySound(resources)) {
                    showSoundButton(this)
                    ringtonePopupMenu.menu.findItem(R.id.menu_ringtone_play).isVisible = true
                    setOnClickListener {
                        playSound(afd = assets.openFd(playSoundPath))
                        MetricsManager.trackAction(MetricAction.ACTION_CARD_START_SOUND_PLAY(card))
                    }
                }
                child("v$currentVersion/$playSoundPath").downloadUrl.addOnSuccessListener { result ->
                    showSoundButton(this)
                    ringtonePopupMenu.menu.findItem(R.id.menu_ringtone_play).isVisible = true
                    setOnClickListener {
                        playSound(result)
                        MetricsManager.trackAction(MetricAction.ACTION_CARD_START_SOUND_PLAY(card))
                    }
                }
            }
            with(card_sound_attack) {
                val attackSoundPath = card.attackSoundPath()
                if (card.hasLocalAttackSound(resources)) {
                    showSoundButton(this)
                    ringtonePopupMenu.menu.findItem(R.id.menu_ringtone_attack).isVisible = true
                    setOnClickListener {
                        MetricsManager.trackAction(MetricAction.ACTION_CARD_START_SOUND_ATTACK(card))
                        playSound(afd = getAssets().openFd(attackSoundPath))
                    }
                }
                child("v$currentVersion/$attackSoundPath").downloadUrl.addOnSuccessListener { result ->
                    showSoundButton(this)
                    ringtonePopupMenu.menu.findItem(R.id.menu_ringtone_attack).isVisible = true
                    setOnClickListener {
                        MetricsManager.trackAction(MetricAction.ACTION_CARD_START_SOUND_ATTACK(card))
                        playSound(result)
                    }
                }
            }
            with(card_sound_extra_label) {
                val extraSoundPath = card.extraSoundPath()
                if (card.hasLocalExtraSound(resources)) {
                    showSoundButton(this)
                    ringtonePopupMenu.menu.findItem(R.id.menu_ringtone_extra).isVisible = true
                    setOnClickListener {
                        MetricsManager.trackAction(MetricAction.ACTION_CARD_START_SOUND_EXTRA(card))
                        playSound(afd = getAssets().openFd(extraSoundPath))
                    }
                }
                child("v$currentVersion/$extraSoundPath").downloadUrl.addOnSuccessListener { result ->
                    showSoundButton(this)
                    ringtonePopupMenu.menu.findItem(R.id.menu_ringtone_extra).isVisible = true
                    setOnClickListener {
                        MetricsManager.trackAction(MetricAction.ACTION_CARD_START_SOUND_EXTRA(card))
                        playSound(result)
                    }
                }
            }
        }
    }

    private fun setAsRingtoneClick(ringtoneFile: File) {
        tmpRingtoneFile = ringtoneFile
        if (!hasPermission(PERMISSION_WRITE_STORAGE)) {
            requestWriteStoragePermission()
            return
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.System.canWrite(this)) {
            requestWriteSettingsPermission()
            return
        }
        card_progress_bar.visibility = View.VISIBLE
        FirebaseStorage.getInstance().reference.apply {
            val playSoundPath = card.playSoundPath()
            if (card.hasLocalPlaySound(resources)) {
                playSoundBytes = assets.open(playSoundPath).readBytes()
            }
            child("v$currentVersion/$playSoundPath").getBytes(1024 * 1024).addOnSuccessListener { bytes ->
                playSoundBytes = bytes
            }
            val attackSoundPath = card.attackSoundPath()
            if (card.hasLocalAttackSound(resources)) {
                attackSoundBytes = assets.open(attackSoundPath).readBytes()
            }
            child("v$currentVersion/$attackSoundPath").getBytes(1024 * 1024).addOnSuccessListener { bytes ->
                attackSoundBytes = bytes
            }
            val extraSoundPath = card.extraSoundPath()
            if (card.hasLocalExtraSound(resources)) {
                extraSoundBytes = assets.open(extraSoundPath).readBytes()
            }
            child("v$currentVersion/$extraSoundPath").getBytes(1024 * 1024).addOnSuccessListener { bytes ->
                extraSoundBytes = bytes
            }
        }
        Handler().postDelayed({
            when (ringtoneFile) {
                playAsRingtoneFile -> {
                    playSoundBytes?.saveToFile(playAsRingtoneFile)
                    setAsRingtone(playAsRingtoneFile, Card.SOUND_TYPE_PLAY)
                }
                attackAsRingtoneFile -> {
                    attackSoundBytes?.saveToFile(attackAsRingtoneFile)
                    setAsRingtone(attackAsRingtoneFile, Card.SOUND_TYPE_ATTACK)
                }
                else -> {
                    extraSoundBytes?.saveToFile(extraAsRingtoneFile)
                    setAsRingtone(extraAsRingtoneFile, Card.SOUND_TYPE_EXTRA)
                }
            }
        }, DateUtils.SECOND_IN_MILLIS * 2)
    }

    private fun setAsRingtone(ringtoneFile: File, soundType: String) {
        if (!ringtoneFile.exists()) {
            return
        }
        val content = ContentValues().apply {
            put(MediaStore.MediaColumns.DATA, ringtoneFile.getAbsolutePath());
            put(MediaStore.MediaColumns.TITLE, ringtoneFile.name);
            put(MediaStore.MediaColumns.SIZE, 215454);
            put(MediaStore.MediaColumns.MIME_TYPE, "audio/*");
            put(MediaStore.Audio.Media.DURATION, 230);
            put(MediaStore.Audio.Media.IS_RINGTONE, true);
            put(MediaStore.Audio.Media.IS_NOTIFICATION, false);
            put(MediaStore.Audio.Media.IS_ALARM, false);
            put(MediaStore.Audio.Media.IS_MUSIC, false);
        }
        val uri = MediaStore.Audio.Media.getContentUriForPath(ringtoneFile.getAbsolutePath());

        getContentResolver().delete(uri, "${MediaStore.MediaColumns.DATA}=\"${ringtoneFile.getAbsolutePath()}\"", null);
        RingtoneManager.setActualDefaultRingtoneUri(getApplicationContext(),
                RingtoneManager.TYPE_RINGTONE, getContentResolver().insert(uri, content));

        card_progress_bar.visibility = View.GONE
        Toast.makeText(this, R.string.card_full_sound_set, Toast.LENGTH_SHORT).show()
        MetricsManager.trackAction(MetricAction.ACTION_CARD_SOUND_SET_RINGTONE(card, soundType))
    }

    private fun showSoundButton(button: View) {
        button.visibility = View.VISIBLE
        card_sounds_label.visibility = View.VISIBLE
        card_sound_ringtone.visibility = View.VISIBLE
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
                    loadFromCard(cardBasicInfo.set, cardBasicInfo.attr, cardBasicInfo.shortName, cardBasicInfo.isToken)
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

    class CardLevelsAdapter(val items: List<Pair<Card, Int>>, val onCardClick: (View, Int) -> Unit) : RecyclerView.Adapter<CardLevelsViewHolder>() {
        override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): CardLevelsViewHolder {
            return CardLevelsViewHolder(parent?.inflate(R.layout.itemlist_card_min))
        }

        override fun onBindViewHolder(holder: CardLevelsViewHolder?, position: Int) {
            holder?.bind(items[position], onCardClick)
        }

        override fun getItemCount(): Int = items.size

    }

    class CardLevelsViewHolder(view: View?) : RecyclerView.ViewHolder(view) {

        fun bind(cardLevel: Pair<Card, Int>, onCardClick: (View, Int) -> Unit) {
            with(itemView) {
                card_min_image.loadFromCard(cardLevel.first, cardLevel.second)
                setOnClickListener { onCardClick(card_min_image, cardLevel.second) }
            }
        }

    }

    class CardRelatedAdapter(val items: List<Card>, val onCardClick: (View, Card) -> Unit) : RecyclerView.Adapter<CardRelatedViewHolder>() {
        override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): CardRelatedViewHolder {
            return CardRelatedViewHolder(parent?.inflate(R.layout.itemlist_card_min))
        }

        override fun onBindViewHolder(holder: CardRelatedViewHolder?, position: Int) {
            holder?.bind(items[position], onCardClick)
        }

        override fun getItemCount(): Int = items.size

    }

    class CardRelatedViewHolder(view: View?) : RecyclerView.ViewHolder(view) {

        fun bind(card: Card, onCardClick: (View, Card) -> Unit) {
            with(itemView) {
                card_min_image.loadFromCard(card)
                setOnClickListener { onCardClick(card_min_image, card) }
            }
        }

    }

}
