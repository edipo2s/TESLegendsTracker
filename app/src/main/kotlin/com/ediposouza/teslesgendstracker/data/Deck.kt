package com.ediposouza.teslesgendstracker.data

import org.threeten.bp.LocalDateTime

/**
 * Created by EdipoSouza on 10/31/16.
 */
data class Slot(

        val card: Card,
        val qtd: Long

)

enum class DeckType() {

    AGGRO,
    ARENA,
    COMBO,
    CONTROL,
    MIDRANGE,
    OTHER

}

data class DeckComment(

        val user: User,
        val comment: String,
        val rating: Int,
        val date: LocalDateTime

)

data class Deck(

        val name: String,
        val type: DeckType,
        val cls: Class,
        val cards: Array<Slot>,
        val cost: Int,
        val season: Int,
        val rating: Int,
        val comments: Array<DeckComment>

)