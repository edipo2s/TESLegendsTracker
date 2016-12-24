package com.ediposouza.teslesgendstracker.ui.decks

import com.ediposouza.teslesgendstracker.data.Attribute
import com.ediposouza.teslesgendstracker.data.Card

/**
 * Created by ediposouza on 06/12/16.
 */
data class CmdAddCard(

        val card: Card

)

data class CmdRemAttr(

        val attr: Attribute

)