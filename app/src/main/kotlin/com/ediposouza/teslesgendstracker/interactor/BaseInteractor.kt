package com.ediposouza.teslesgendstracker.interactor

import com.google.firebase.database.FirebaseDatabase

/**
 * Created by ediposouza on 01/11/16.
 */
open class BaseInteractor() {

    val NODE_CARDS = "cards"
    val NODE_CORE = "core"
    val CARD_COST_KEY = "cost"

    val database by lazy { FirebaseDatabase.getInstance().reference }

}