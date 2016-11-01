package com.ediposouza.teslesgendstracker.interactor

import com.ediposouza.teslesgendstracker.data.Attribute
import com.ediposouza.teslesgendstracker.data.FirebaseCard
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import timber.log.Timber

/**
 * Created by ediposouza on 01/11/16.
 */
class CardInteractor() {

    val NODE_CARDS = "cards"
    val NODE_CORE = "core"

    val mDatabase by lazy { FirebaseDatabase.getInstance().reference }

    fun getCards(cls: Attribute) {
        mDatabase.child(NODE_CARDS).child(NODE_CORE).child(cls.name.toLowerCase()).orderByKey()
                .addValueEventListener(object : ValueEventListener {

                    override fun onDataChange(ds: DataSnapshot) {
                        val cards = ds.value
                        Timber.d(cards?.toString())
                        ds.children.forEach {
                            val card = it.getValue(FirebaseCard::class.java).toCard(cls)
                            Timber.d(card.toString())
                        }
                    }

                    override fun onCancelled(de: DatabaseError) {
                        Timber.d("Fail: " + de.message)
                    }

                })
    }

}