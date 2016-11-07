package com.ediposouza.teslesgendstracker.ui.widget

import com.ediposouza.teslesgendstracker.data.Attribute
import com.ediposouza.teslesgendstracker.data.CardRarity

/**
 * Created by EdipoSouza on 11/5/16.
 */
data class CmdShowCardsByAttr(val attr: Attribute)

data class CmdFilterSearch(val search: String?)

data class CmdFilterRarity(val rarity: CardRarity?)

data class CmdFilterMagika(val magika: Int)