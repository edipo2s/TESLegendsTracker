package com.ediposouza.teslesgendstracker.ui.widget.filter

import com.ediposouza.teslesgendstracker.data.CardRarity

/**
 * Created by EdipoSouza on 11/5/16.
 */
data class CmdFilterSearch(val search: String?)

data class CmdFilterRarity(val rarity: CardRarity?)

data class CmdFilterMagika(val magika: Int)