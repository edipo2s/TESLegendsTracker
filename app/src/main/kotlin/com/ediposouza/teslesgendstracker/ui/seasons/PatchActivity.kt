package com.ediposouza.teslesgendstracker.ui.seasons

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.support.v4.app.ActivityCompat
import android.support.v4.app.ActivityOptionsCompat
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.transition.Fade
import android.transition.Transition
import android.transition.TransitionInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import com.ediposouza.teslesgendstracker.R
import com.ediposouza.teslesgendstracker.data.*
import com.ediposouza.teslesgendstracker.interactor.PublicInteractor
import com.ediposouza.teslesgendstracker.ui.base.BaseActivity
import com.ediposouza.teslesgendstracker.ui.cards.CardActivity
import com.ediposouza.teslesgendstracker.util.*
import jp.wasabeef.recyclerview.adapters.SlideInRightAnimationAdapter
import kotlinx.android.synthetic.main.activity_patch.*
import kotlinx.android.synthetic.main.itemlist_patch_cards.view.*
import org.jetbrains.anko.intentFor
import org.threeten.bp.format.DateTimeFormatter
import org.threeten.bp.format.FormatStyle

class PatchActivity : BaseActivity() {

    companion object {

        private val EXTRA_PATCH = "patchExtra"
        private val EXTRA_NEXT_PATCH_UUID = "nextPatchExtra"

        fun newIntent(context: Context, patch: Patch, patches: List<Patch>): Intent {
            return context.intentFor<PatchActivity>(EXTRA_PATCH to patch,
                    EXTRA_NEXT_PATCH_UUID to patches)
        }

    }

    private val transitionName: String by lazy { getString(R.string.card_transition_name) }
    private val selectedPatch: Patch by lazy { intent.getParcelableExtra<Patch>(EXTRA_PATCH) ?: Patch.DUMMY }
    private val patches: List<Patch> by lazy { intent.getParcelableArrayListExtra<Patch>(EXTRA_NEXT_PATCH_UUID) }

    private val onCardClick: (View, Card) -> Unit = { view, card ->
        ActivityCompat.startActivity(this, CardActivity.newIntent(this, card),
                ActivityOptionsCompat.makeSceneTransitionAnimation(this, view, transitionName).toBundle())
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_patch)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            setupEnterAnimation()
            setupExitAnimation()
        } else {
            initViews()
        }
    }

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)
        val date = selectedPatch.date.format(DateTimeFormatter.ofLocalizedDate(FormatStyle.SHORT))
        patch_date_name.text = "${selectedPatch.desc} - $date"
        configureRecycleView()
        patch_ads_view.load()
        MetricsManager.trackScreen(MetricScreen.SCREEN_PATCH_DETAILS())
        MetricsManager.trackPatchView(selectedPatch)
    }

    override fun onBackPressed() {
        patch_container_name.setBackgroundResource(R.drawable.xml_button_white)
        patch_date_name.text = selectedPatch.desc
        patch_ads_view.visibility = View.GONE
        patch_cards_recycler_view.visibility = View.GONE
        ActivityCompat.finishAfterTransition(this@PatchActivity)
    }

    private fun configureRecycleView() {
        with(patch_cards_recycler_view) {
            layoutManager = object : LinearLayoutManager(context) {
                override fun supportsPredictiveItemAnimations(): Boolean = false
            }
            val nextPatches = patches.filter { it.date.isAfter(selectedPatch.date) }
            adapter = SlideInRightAnimationAdapter(PatchAdapter(selectedPatch, nextPatches, onCardClick)).apply {
                setDuration(300)
                setFirstOnly(false)
            }
            setHasFixedSize(true)
        }
    }

    @SuppressLint("NewApi")
    private fun setupEnterAnimation() {
        val transition = TransitionInflater.from(this).inflateTransition(R.transition.changebounds_with_arcmotion)
        transition.duration = 300
        window.sharedElementEnterTransition = transition
        transition.addListener(object : Transition.TransitionListener {
            override fun onTransitionEnd(transition: Transition) {
                initViews()
            }

            override fun onTransitionResume(transition: Transition) {
            }

            override fun onTransitionPause(transition: Transition) {
            }

            override fun onTransitionCancel(transition: Transition) {
            }

            override fun onTransitionStart(transition: Transition) {
            }

        })
    }

    @SuppressLint("NewApi")
    private fun setupExitAnimation() {
        window.returnTransition = Fade().apply {
            duration = resources.getInteger(R.integer.anim_slide_duration).toLong()
            addListener(object : Transition.TransitionListener {
                override fun onTransitionStart(transition: Transition) {}

                override fun onTransitionEnd(transition: Transition) {}

                override fun onTransitionCancel(transition: Transition) {}

                override fun onTransitionPause(transition: Transition) {}

                override fun onTransitionResume(transition: Transition) {}
            })
        }
    }

    private fun initViews() {
        Handler(Looper.getMainLooper()).post({
            val animation = AnimationUtils.loadAnimation(this, android.R.anim.fade_in)
            animation.duration = 300
            patch_cards_recycler_view.startAnimation(animation)
            patch_cards_recycler_view.visibility = View.VISIBLE
            patch_container_name.setBackgroundResource(0)
            coordinatorLayout.setOnClickListener { onBackPressed() }
        })
    }

    class PatchAdapter(val patch: Patch, val nextPatches: List<Patch>,
                       val itemClick: (View, Card) -> Unit) : RecyclerView.Adapter<PatchViewHolder>() {

        val items: List<PatchChange> = patch.changes

        override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): PatchViewHolder {
            return PatchViewHolder(parent?.inflate(R.layout.itemlist_patch_cards))
        }

        override fun onBindViewHolder(holder: PatchViewHolder?, position: Int) {
            val patchChange = items[position]
            val nextPatchWithCard = nextPatches.filter {
                it.changes.filter { it.shortName == patchChange.shortName }.isNotEmpty()
            }.minBy { it.date }
            holder?.bind(patch.uuidDate, nextPatchWithCard?.uuidDate ?: "", patchChange, itemClick)
        }

        override fun getItemCount(): Int = items.size

    }

    class PatchViewHolder(view: View?) : RecyclerView.ViewHolder(view) {

        fun bind(patchUuid: String, nextPatchUuid: String, patchChange: PatchChange,
                 itemClick: (View, Card) -> Unit) {
            with(itemView) {
                patch_card_change.text = context.getString(R.string.patch_change, patchChange.change)
                patch_card_old_image.loadFromPatch(patchChange, patchUuid, false)
                patch_card_new_image.loadFromPatch(patchChange, nextPatchUuid, true)
                val set = CardSet.of(patchChange.set)
                val attr = CardAttribute.valueOf(patchChange.attr.toUpperCase())
                PublicInteractor.getCard(set, attr, patchChange.shortName) { card ->
                    patch_card_old_image.setOnClickListener { itemClick(patch_card_old_image, card) }
                    patch_card_new_image.setOnClickListener { itemClick(patch_card_new_image, card) }
                }
            }
        }

    }

}
