package com.ediposouza.teslesgendstracker.interactor

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

/**
 * Created by ediposouza on 01/11/16.
 */
open class BaseInteractor() {

    protected val NODE_CARDS = "cards"
    protected val NODE_CORE = "core"

    protected val NODE_DECKS = "decks"
    protected val NODE_PUBLIC = "public"

    protected val KEY_CARD_COST = "cost"
    protected val KEY_DECK_CLASS = "cls"
    protected val KEY_DECK_UPDATE_AT = "updatedAt"

    protected val database by lazy { FirebaseDatabase.getInstance().reference }

    protected val dbDecks by lazy { database.child(NODE_DECKS) }

    protected val userID  by lazy { FirebaseAuth.getInstance().currentUser?.uid ?: "" }

}