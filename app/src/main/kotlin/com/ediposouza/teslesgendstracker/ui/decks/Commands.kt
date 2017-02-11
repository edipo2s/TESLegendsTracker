package com.ediposouza.teslesgendstracker.ui.decks

import com.ediposouza.teslesgendstracker.data.Card
import com.ediposouza.teslesgendstracker.data.CardAttribute
import com.ediposouza.teslesgendstracker.data.CardSlot

/**
 * Created by ediposouza on 06/12/16.
 */
data class CmdAddCard(

        val card: Card

)

data class CmdRemAttr(

        val attr: CardAttribute

)

data class CmdUpdateCardSlot(

        val cardSlot: CardSlot

)