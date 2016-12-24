package com.ediposouza.teslesgendstracker.ui

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.support.design.widget.BottomSheetBehavior
import android.support.design.widget.CollapsingToolbarLayout
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.view.Menu
import android.view.MenuItem
import android.view.View
import com.bumptech.glide.Glide
import com.ediposouza.teslesgendstracker.MetricScreen
import com.ediposouza.teslesgendstracker.R
import com.ediposouza.teslesgendstracker.TIME_PATTERN
import com.ediposouza.teslesgendstracker.data.Deck
import com.ediposouza.teslesgendstracker.interactor.PrivateInteractor
import com.ediposouza.teslesgendstracker.interactor.PublicInteractor
import com.ediposouza.teslesgendstracker.manager.MetricsManager
import com.ediposouza.teslesgendstracker.toogleExpanded
import com.ediposouza.teslesgendstracker.ui.base.BaseActivity
import com.ediposouza.teslesgendstracker.ui.utils.CircleTransform
import kotlinx.android.synthetic.main.activity_deck.*
import kotlinx.android.synthetic.main.include_deck_info.*
import org.jetbrains.anko.alert
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.intentFor
import org.jetbrains.anko.toast
import org.threeten.bp.format.DateTimeFormatter
import timber.log.Timber
import java.text.NumberFormat

class DeckActivity() : BaseActivity() {

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

    val deck by lazy { intent.getParcelableExtra<Deck>(EXTRA_DECK) }
    val deckOwned by lazy { intent.getBooleanExtra(EXTRA_OWNED, false) }
    val numberInstance by lazy { NumberFormat.getNumberInstance() }
    val privateInteractor by lazy { PrivateInteractor() }

    var favorite: Boolean = false
    var like: Boolean = false
    var menuLike: MenuItem? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_deck)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            val layoutParams = toolbar.layoutParams as CollapsingToolbarLayout.LayoutParams
            layoutParams.topMargin = resources.getDimensionPixelSize(R.dimen.status_bar_height)
            toolbar.layoutParams = layoutParams
        }

        favorite = intent.getBooleanExtra(EXTRA_FAVORITE, false)
        like = intent.getBooleanExtra(EXTRA_LIKE, false)
        val sheetBehavior = BottomSheetBehavior.from(deck_bottom_sheet)
        deck_fab_favorite.setOnClickListener {
            privateInteractor.setUserDeckFavorite(deck, !favorite) {
                favorite = !favorite
                updateFavoriteItem()
                setResult(Activity.RESULT_OK, Intent())
            }
        }
        deck_bottom_sheet.setOnClickListener { sheetBehavior.toogleExpanded() }
        updateFavoriteItem()
        loadDeckInfo()
        setResult(Activity.RESULT_CANCELED, Intent())
    }

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        doAsync {
            calculateMissingSoul(deck, privateInteractor)
            PublicInteractor().getPatches {
                val patch = it.find { it.uidDate == deck.patch }
                runOnUiThread {
                    deck_details_patch.text = patch?.desc ?: ""
                }
            }
            PublicInteractor().getUserInfo(deck.owner) {
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
        MetricsManager.trackScreen(MetricScreen.SCREEN_DECK_DETAILS())
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(if (deckOwned) R.menu.menu_deck_owned else R.menu.menu_deck, menu)
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
                privateInteractor.setUserDeckLike(deck, !like) {
                    like = !like
                    updateLikeItem()
                    setResult(Activity.RESULT_OK, Intent())
                    val deckLikes = Integer.parseInt(deck_details_likes.text.toString())
                    deck_details_likes.text = numberInstance.format(deckLikes + if (like) 1 else -1)
                }
                return true
            }
            R.id.menu_delete -> {
                alert(R.string.confirm_message) {
                    negativeButton(android.R.string.no, {})
                    positiveButton(android.R.string.yes, {
                        privateInteractor.deleteDeck(deck, deck.private) {
                            toast(R.string.deck_deleted)
                            setResult(Activity.RESULT_OK, Intent())
                            ActivityCompat.finishAfterTransition(this@DeckActivity)
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
        deck_comment_qtd.text = numberInstance.format(deck.comments.size)
        deck_details_type.text = deck.type.name.toLowerCase().capitalize()
        deck_details_views.text = numberInstance.format(deck.views)
        deck_details_likes.text = numberInstance.format(deck.likes.size)
        deck_details_soul_cost.text = numberInstance.format(deck.cost)
        deck_details_create_at.text = deck.createdAt.toLocalDate().toString()
        val updateTime = deck.updatedAt.toLocalTime().format(DateTimeFormatter.ofPattern(TIME_PATTERN))
        val updateText = "${deck.updatedAt.toLocalDate()} $updateTime"
        deck_details_update_at.text = updateText
        deck_details_cardlist.showDeck(deck)
    }

    fun calculateMissingSoul(deck: Deck, interactor: PrivateInteractor) {
        with(deck_details_soul_missing) {
            visibility = View.INVISIBLE
            deck_details_soul_missing_loading.visibility = View.VISIBLE
            interactor.getMissingCards(deck, { deck_details_soul_missing_loading.visibility = View.VISIBLE }) {
                deck_details_soul_missing_loading.visibility = View.GONE
                val missingSoul = it.map { it.qtd * it.rarity.soulCost }.sum()
                Timber.d("Missing %d", missingSoul)
                text = NumberFormat.getNumberInstance().format(missingSoul)
                visibility = View.VISIBLE
                deck_details_cardlist.showMissingCards(it)
            }
        }
    }

}
