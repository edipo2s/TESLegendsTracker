package com.ediposouza.teslesgendstracker.ui.cards

import com.ediposouza.teslesgendstracker.data.CardAttribute
import com.ediposouza.teslesgendstracker.data.CardRarity
import com.ediposouza.teslesgendstracker.data.CardSet
import com.ediposouza.teslesgendstracker.data.DeckClass

/**
 * Created by EdipoSouza on 11/5/16.
 */
data class CmdFilterSet(val set: CardSet?)

data class CmdFilterClass(val cls: DeckClass?)

data class CmdFilterSearch(val search: String?)

data class CmdFilterAttr(val attr: CardAttribute?)

data class CmdFilterAttrs(val attrs: List<CardAttribute>)

data class CmdFilterRarity(val rarity: CardRarity?)

data class CmdFilterMagicka(val magicka: Int)

data class CmdInputSearch(val search: String?)