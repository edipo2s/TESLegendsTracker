package com.ediposouza.teslesgendstracker.interactor

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

/**
 * Created by ediposouza on 01/11/16.
 */
open class BaseInteractor() {

    protected val NODE_CARDS = "cards"
    protected val NODE_CARDS_CORE = "core"
    protected val NODE_USERS = "users"
    protected val NODE_USERS_INFO = "info"
    protected val NODE_DECKS = "decks"
    protected val NODE_DECKS_PUBLIC = "public"

    protected val KEY_CARD_COST = "cost"
    protected val KEY_USER_NAME = "name"
    protected val KEY_USER_PHOTO = "photoUrl"
    protected val KEY_DECK_CLASS = "cls"
    protected val KEY_DECK_UPDATE_AT = "updatedAt"

    protected val database by lazy { FirebaseDatabase.getInstance().reference }

    protected val dbDecks by lazy { database.child(NODE_DECKS) }

    protected val dbUsers by lazy { database.child(NODE_USERS) }

    protected val userID  by lazy { FirebaseAuth.getInstance().currentUser?.uid ?: "" }

}