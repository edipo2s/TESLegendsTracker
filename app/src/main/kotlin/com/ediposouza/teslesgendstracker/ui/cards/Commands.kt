package com.ediposouza.teslesgendstracker.ui.cards

import com.ediposouza.teslesgendstracker.data.CardRarity
import com.ediposouza.teslesgendstracker.data.CardSet
import com.ediposouza.teslesgendstracker.data.Class

/**
 * Created by EdipoSouza on 11/5/16.
 */
data class CmdFilterSet(val set: CardSet?)

data class CmdFilterClass(val cls: Class?)

data class CmdFilterSearch(val search: String?)

data class CmdFilterRarity(val rarity: CardRarity?)

data class CmdFilterMagika(val magika: Int)