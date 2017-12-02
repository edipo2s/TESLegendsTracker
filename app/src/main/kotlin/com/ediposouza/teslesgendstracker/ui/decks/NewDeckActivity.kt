package com.ediposouza.teslesgendstracker.ui.decks

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.text.format.DateUtils
import android.util.TypedValue
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.ArrayAdapter
import com.ediposouza.teslesgendstracker.App
import com.ediposouza.teslesgendstracker.DECK_NAME_MIN_SIZE
import com.ediposouza.teslesgendstracker.R
import com.ediposouza.teslesgendstracker.data.*
import com.ediposouza.teslesgendstracker.interactor.PrivateInteractor
import com.ediposouza.teslesgendstracker.interactor.PublicInteractor
import com.ediposouza.teslesgendstracker.ui.base.BaseFilterActivity
import com.ediposouza.teslesgendstracker.ui.base.CmdShowCardsByAttr
import com.ediposouza.teslesgendstracker.ui.base.CmdShowSnackbarMsg
import com.ediposouza.teslesgendstracker.ui.cards.CmdFilterClass
import com.ediposouza.teslesgendstracker.ui.cards.CmdFilterMagicka
import com.ediposouza.teslesgendstracker.ui.cards.CmdFilterRarity
import com.ediposouza.teslesgendstracker.util.MetricAction
import com.ediposouza.teslesgendstracker.util.MetricScreen
import com.ediposouza.teslesgendstracker.util.MetricsManager
import kotlinx.android.synthetic.main.activity_new_deck.*
import kotlinx.android.synthetic.main.dialog_new_deck.view.*
import org.greenrobot.eventbus.Subscribe
import org.jetbrains.anko.alert
import org.jetbrains.anko.cancelButton
import org.jetbrains.anko.intentFor
import org.jetbrains.anko.toast
import java.util.*

class NewDeckActivity : BaseFilterActivity() {

    companion object {

        val DECK_EXTRA = "deckExtra"
        val DECK_PRIVATE_EXTRA = "privateExtra"

    }

    private val ANIM_DURATION = 250L
    private val DECK_MIN_CARDS_QTD = 50
    private val EXIT_CONFIRM_MIN_CARDS = 3
    private val KEY_DECK_CARDS = "deckCardsKey"

    private val deckToEdit: Deck? by lazy { intent.getParcelableExtra<Deck>(DECK_EXTRA) ?: Deck.DUMMY }

    private val attrFilterClick: (CardAttribute) -> Unit = {
        eventBus.post(CmdShowCardsByAttr(it))
        new_deck_attr_filter.selectAttr(it, true)
        new_deck_attr_filter.lastAttrSelected = it
        updateDualFilter()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_new_deck)
        setResult(Activity.RESULT_CANCELED, Intent())
    }

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        new_deck_toolbar_title.text = deckToEdit?.name.takeIf { deckToEdit?.uuid?.isNotEmpty() ?: false } ?: getString(R.string.new_deck_title)
        new_deck_cardlist.editMode = true
        if (deckToEdit?.uuid?.isNotEmpty() == true) {
            new_deck_cardlist.showDeck(deckToEdit)
        }
        configDeckFilters()
        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                    .replace(R.id.new_deck_fragment_cards, NewDeckCardsListFragment().apply {
                        arguments = Bundle().apply {
                            putParcelable(NewDeckCardsListFragment.EXTRA_DECK, deckToEdit)
                        }
                    })
                    .commit()
        }
        handler.postDelayed({
            eventBus.post(CmdShowCardsByAttr(deckToEdit?.cls?.attr1?.takeIf { deckToEdit != Deck.DUMMY }
                    ?: CardAttribute.STRENGTH))
        }, DateUtils.SECOND_IN_MILLIS)
        MetricsManager.trackScreen(MetricScreen.SCREEN_NEW_DECKS())
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.apply {
            val cardsArrayList = ArrayList<CardSlot>(new_deck_cardlist?.getCards() ?: listOf())
            putParcelableArrayList(KEY_DECK_CARDS, cardsArrayList)
        }
        super.onSaveInstanceState(outState)
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle?) {
        super.onRestoreInstanceState(savedInstanceState)
        savedInstanceState?.apply {
            val deckCardSlots = getParcelableArrayList<CardSlot>(KEY_DECK_CARDS)
            new_deck_cardlist?.addCards(deckCardSlots)
            handler.postDelayed({
                deckCardSlots.forEach {
                    eventBus.post(CmdUpdateCardSlot(it))
                    new_deck_attr_filter.lockAttrs(it.card.dualAttr1, it.card.dualAttr2, false)
                }
            }, DateUtils.SECOND_IN_MILLIS / 2)
            updateDualFilter()
        }
    }

    override fun onBackPressed() {
        if (canExit || new_deck_cardlist.getCards().size < EXIT_CONFIRM_MIN_CARDS) {
            super.onBackPressed()
        } else {
            showExitConfirm(R.string.deck_exit_confirm)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_sets, menu)
        menuInflater.inflate(R.menu.menu_done, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item?.itemId) {
            android.R.id.home -> {
                ActivityCompat.finishAfterTransition(this)
                return true
            }
            R.id.menu_done -> {
                if (!App.hasUserLogged()) {
                    showErrorUserNotLogged()
                    return false
                }
                if (new_deck_cardlist.getCards().sumBy { it.qtd } >= DECK_MIN_CARDS_QTD) {
                    showDeckInfoDialog()
                } else {
                    eventBus.post(CmdShowSnackbarMsg(CmdShowSnackbarMsg.TYPE_ERROR, R.string.new_deck_save_error_incomplete)
                            .withAction(android.R.string.ok, {}))
                }
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun configDeckFilters() {
        with(new_deck_attr_filter) {
            filterClick = attrFilterClick
            onAttrLock = { attr1: CardAttribute, attr2: CardAttribute ->
                val deckCls = DeckClass.getClasses(listOf(attr1, attr2)).first()
                new_deck_class_cover.setImageResource(deckCls.imageRes)
                if (deckToEdit == null) {
                    new_deck_toolbar_title.text = getString(R.string.new_deck_class_title, deckCls.name.toLowerCase().capitalize())
                }
                val outValue = TypedValue()
                resources.getValue(R.dimen.deck_class_cover_alpha, outValue, true)
                new_deck_class_cover.animate().alpha(outValue.float).setDuration(ANIM_DURATION).start()
            }
            onAttrUnlock = {
                if (deckToEdit == null) {
                    new_deck_toolbar_title.text = getString(R.string.new_deck_title)
                }
                new_deck_class_cover.animate().alpha(0f).setDuration(ANIM_DURATION).start()
            }
            deckToEdit?.let {
                lockAttrs(it.cls.attr1, it.cls.attr2)
                attrFilterClick.invoke(it.cls.attr1.takeIf { deckToEdit != Deck.DUMMY } ?: CardAttribute.STRENGTH)
            }
        }
        cards_filter_rarity.filterClick = { eventBus.post(CmdFilterRarity(it)) }
        cards_filter_magicka.filterClick = { eventBus.post(CmdFilterMagicka(it)) }
    }

    private fun showDeckInfoDialog() {
        val view = View.inflate(this, R.layout.dialog_new_deck, null)
        val deckTypes = DeckType.values().filter { it != DeckType.ARENA }.map { it.name.toLowerCase().capitalize() }
        view.new_deck_dialog_type_spinner.adapter = ArrayAdapter<String>(this,
                android.R.layout.simple_spinner_dropdown_item, deckTypes)
        val allPatches = mutableListOf<Patch>()
        PublicInteractor.getPatches { deckPatches ->
            allPatches.addAll(deckPatches)
            val deckPatchesDesc = deckPatches.map { it.desc }.reversed()
            view.new_deck_dialog_patch_spinner.adapter = ArrayAdapter<String>(this,
                    android.R.layout.simple_spinner_dropdown_item, deckPatchesDesc)
            if (deckToEdit?.patch?.isNotEmpty() == true) {
                view.new_deck_dialog_patch_spinner.setSelection(deckPatchesDesc.indexOf(deckToEdit!!.patch))
            }
        }
        if (deckToEdit?.uuid?.isNotEmpty() == true) {
            view.new_deck_dialog_title.text = getString(R.string.new_deck_update_dialog_title)
            view.new_deck_dialog_name.setText(deckToEdit!!.name)
            val currentDeckTypeName = deckToEdit!!.type.name.toLowerCase().capitalize()
            view.new_deck_dialog_type_spinner.setSelection(deckTypes.indexOf(currentDeckTypeName))
            view.new_deck_dialog_public.isChecked = !deckToEdit!!.private
        }
        alert {
            customView = view
            val confirmText = R.string.new_deck_save_dialog_update.takeIf { deckToEdit?.uuid?.isNotEmpty() ?: false } ?:
                    R.string.new_deck_save_dialog_save
            positiveButton(confirmText) { saveUpdateDeck(view, allPatches) }
            cancelButton { }
        }.show()
    }

    private fun saveUpdateDeck(view: View, deckPatches: List<Patch>) {
        val deckName = view.new_deck_dialog_name.text.toString()
        val deckCls = DeckClass.getClasses(listOf(new_deck_attr_filter.lockAttr1 ?: CardAttribute.NEUTRAL,
                new_deck_attr_filter.lockAttr2 ?: CardAttribute.NEUTRAL)).first()
        val deckTypeText = view.new_deck_dialog_type_spinner.selectedItem as String
        val deckTypeSelected = DeckType.valueOf(deckTypeText.toUpperCase())
        val deckPatchDesc = view.new_deck_dialog_patch_spinner.selectedItem as String
        val deckPatchSelected = deckPatches.find { it.desc == deckPatchDesc } ?: deckPatches.last()
        val deckCards = new_deck_cardlist.getCards().map { it.card.shortName to it.qtd }.toMap()
        val deckSoulCost = new_deck_cardlist.getSoulCost()
        val deckPrivate = !view.new_deck_dialog_public.isChecked
        if (deckName.length < DECK_NAME_MIN_SIZE) {
            eventBus.post(CmdShowSnackbarMsg(CmdShowSnackbarMsg.TYPE_ERROR, R.string.new_match_dialog_start_error_name))
            return
        }
        if (deckToEdit?.uuid?.isNotEmpty() ?: false) {
            val deck = deckToEdit!!.update(deckName, deckPrivate, deckTypeSelected, deckCls,
                    deckSoulCost, deckPatchSelected.uuidDate, deckCards)
            PrivateInteractor.updateDeckCards(deck, deckToEdit!!.cards, deckSoulCost) {
                PrivateInteractor.updateDeck(deck, deckToEdit!!.private) {
                    toast(if (deckPrivate) R.string.new_deck_updated_as_private else R.string.new_deck_updated_as_public)
                    val data = intentFor<NewDeckActivity>(DECK_PRIVATE_EXTRA to deckPrivate)
                    setResult(Activity.RESULT_OK, data)
                    ActivityCompat.finishAfterTransition(this@NewDeckActivity)
                    MetricsManager.trackAction(MetricAction.ACTION_DECK_UPDATE(deckTypeText, deckPatchDesc, deckPrivate))
                }
            }
        } else {
            PrivateInteractor.saveDeck(deckName, deckCls, deckTypeSelected, deckSoulCost,
                    deckPatchSelected.uuidDate, deckCards, deckPrivate) {
                toast(if (deckPrivate) R.string.new_deck_save_as_private else R.string.new_deck_save_as_public)
                val data = intentFor<NewDeckActivity>(DECK_PRIVATE_EXTRA to deckPrivate)
                setResult(Activity.RESULT_OK, data)
                ActivityCompat.finishAfterTransition(this)
                MetricsManager.trackAction(MetricAction.ACTION_NEW_DECK_SAVE(deckTypeText, deckPatchDesc, deckPrivate))
            }
        }
    }

    private fun updateDualFilter() {
        if (new_deck_attr_filter.lastAttrSelected == CardAttribute.DUAL) {
            val cls = DeckClass.getClasses(listOf(new_deck_attr_filter.lockAttr1 ?: CardAttribute.NEUTRAL,
                    new_deck_attr_filter.lockAttr2 ?: CardAttribute.NEUTRAL)).filter { it != DeckClass.NEUTRAL }
            eventBus.post(CmdFilterClass(cls.firstOrNull()))
        }
    }

    @Subscribe
    @Suppress("unused")
    fun onCmdCardAdd(cmdCardAdd: CmdAddCard) {
        new_deck_cardlist.addCard(cmdCardAdd.card)
        new_deck_attr_filter.lockAttrs(cmdCardAdd.card.dualAttr1, cmdCardAdd.card.dualAttr2)
        updateDualFilter()
    }

    @Subscribe
    @Suppress("unused")
    fun onCmdRemAttr(cmdRemAttr: CmdRemAttr) {
        new_deck_attr_filter.unlockAttr(cmdRemAttr.attr)
        updateDualFilter()
    }

}