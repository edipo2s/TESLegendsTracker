package com.ediposouza.teslesgendstracker.data

import org.threeten.bp.LocalDate
import org.threeten.bp.LocalDateTime

data class Deck(

        val id: String,
        val name: String,
        val owner: String,
        val private: Boolean,
        val type: DeckType,
        val cls: Class,
        val cost: Int,
        val createdAt: LocalDate,
        val updatedAt: LocalDateTime,
        val patch: String,
        val likes: List<String>,
        val views: Int,
        val cards: Map<String, Int>,
        val updates: List<DeckUpdate>,
        val comments: List<DeckComment>

)

data class DeckUpdate(

        val date: LocalDateTime,
        val changes: Map<String, Int>

)

data class DeckComment(

        val id: String,
        val owner: String,
        val comment: String,
        val date: LocalDateTime

)

enum class DeckType() {

    AGGRO,
    ARENA,
    COMBO,
    CONTROL,
    MIDRANGE,
    OTHER

}