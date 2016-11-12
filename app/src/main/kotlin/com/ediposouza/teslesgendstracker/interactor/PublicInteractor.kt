package com.ediposouza.teslesgendstracker.interactor

import com.ediposouza.teslesgendstracker.data.Attribute
import com.ediposouza.teslesgendstracker.data.Card
import com.ediposouza.teslesgendstracker.data.Class
import com.ediposouza.teslesgendstracker.data.Deck
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import timber.log.Timber

/**
 * Created by ediposouza on 01/11/16.
 */
class PublicInteractor() : BaseInteractor() {

    private val NODE_DECKS = "decks"
    private val UPDATE_AT_KEY = "updatedAt"

    fun getCards(attr: Attribute, onSuccess: (List<Card>) -> Unit) {
        val node_attr = attr.name.toLowerCase()
        database.child(NODE_CARDS).child(NODE_CORE).child(node_attr).orderByChild(CARD_COST_KEY)
                .addListenerForSingleValueEvent(object : ValueEventListener {

                    override fun onDataChange(ds: DataSnapshot) {
                        val cards = ds.children.mapTo(arrayListOf<Card>()) {
                            it.getValue(CardParser::class.java).toCard(it.key, attr)
                        }
                        Timber.d(cards.toString())
                        onSuccess.invoke(cards)
                    }

                    override fun onCancelled(de: DatabaseError) {
                        Timber.d("Fail: " + de.message)
                    }

                })
    }

    fun getDecks(cls: Class, onSuccess: (List<Deck>) -> Unit) {
        val node_cls = cls.name.toLowerCase()
        database.child(NODE_DECKS).child(node_cls).orderByChild(UPDATE_AT_KEY)
                .addListenerForSingleValueEvent(object : ValueEventListener {

                    override fun onDataChange(ds: DataSnapshot) {
                        val decks = ds.children.mapTo(arrayListOf<Deck>()) {
                            it.getValue(DeckParser::class.java).toCard(it.key, cls)
                        }
                        Timber.d(decks.toString())
                        onSuccess.invoke(decks)
                    }

                    override fun onCancelled(de: DatabaseError) {
                        Timber.d("Fail: " + de.message)
                    }

                })
    }

}

