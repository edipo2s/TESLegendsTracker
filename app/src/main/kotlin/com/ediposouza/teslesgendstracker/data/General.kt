package com.ediposouza.teslesgendstracker.data

/**
 * Created by EdipoSouza on 10/31/16.
 */
data class CardSlot(

        val card: Card,
        val qtd: Int

) : Comparable<CardSlot> {

    override fun compareTo(other: CardSlot): Int = card.compareTo(other.card)

}

data class Patch(

        val uidDate: String,
        val desc: String

)