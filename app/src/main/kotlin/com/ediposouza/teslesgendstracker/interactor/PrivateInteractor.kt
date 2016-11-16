package com.ediposouza.teslesgendstracker.interactor

import com.ediposouza.teslesgendstracker.data.Attribute
import com.ediposouza.teslesgendstracker.data.Card
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import timber.log.Timber

/**
 * Created by ediposouza on 01/11/16.
 */
class PrivateInteractor() : BaseInteractor() {

    private val NODE_USERS = "users"
    private val CARD_NAME_KEY: String = "name"
    private val CARD_PHOTO_KEY: String = "photoUrl"
    private val CARD_FAVORITE_KEY = "favorite"
    private val CARD_QTD_KEY = "qtd"

    private fun userDBReference(): DatabaseReference? {
        val uid = FirebaseAuth.getInstance().currentUser?.uid
        return if (uid != null) database.child(NODE_USERS).child(uid) else null
    }

    private fun userDBCardsReference(cls: Attribute): DatabaseReference? {
        return userDBReference()?.child(NODE_CARDS)?.child(NODE_CORE)?.child(cls.name.toLowerCase())
    }

    fun setUserInfo() {
        val user = FirebaseAuth.getInstance().currentUser
        userDBReference()?.apply {
            child(CARD_NAME_KEY).setValue(user?.displayName ?: "")
            child(CARD_PHOTO_KEY).setValue(user?.photoUrl.toString())
        }
    }

    fun setUserCardQtd(card: Card, qtd: Long, onComplete: () -> Unit) {
        userDBCardsReference(card.cls)?.apply {
            child(card.shortName).child(CARD_QTD_KEY).setValue(qtd).addOnCompleteListener {
                onComplete.invoke()
            }
        }
    }

    fun setUserCardFavorite(card: Card, favorite: Boolean, onComplete: () -> Unit) {
        userDBCardsReference(card.cls)?.apply {
            child(card.shortName).child(CARD_FAVORITE_KEY).apply {
                if (favorite) {
                    setValue(true).addOnCompleteListener { onComplete.invoke() }
                } else {
                    removeValue().addOnCompleteListener { onComplete.invoke() }
                }
            }
        }
    }

    fun getUserCollection(attr: Attribute, onSuccess: (Map<String, Long>) -> Unit) {
        userDBCardsReference(attr)?.addListenerForSingleValueEvent(object : ValueEventListener {

                    @Suppress("UNCHECKED_CAST")
                    override fun onDataChange(ds: DataSnapshot) {
                        val collection = ds.children.filter { it.hasChild(CARD_QTD_KEY) }.map({
                            it.key to it.child(CARD_QTD_KEY).value as Long
                        }).toMap()
                        Timber.d(collection.toString())
                        onSuccess.invoke(collection)
                    }

                    override fun onCancelled(de: DatabaseError) {
                        Timber.d("Fail: " + de.message)
                    }

                })
    }

    fun getUserFavorites(attr: Attribute, onSuccess: (List<String>) -> Unit) {
        userDBCardsReference(attr)?.addListenerForSingleValueEvent(object : ValueEventListener {

            @Suppress("UNCHECKED_CAST")
            override fun onDataChange(ds: DataSnapshot) {
                val favorites = ds.children.filter { (it.child(CARD_FAVORITE_KEY)?.value ?: false) as Boolean }
                        .map({ it.key })
                Timber.d(favorites.toString())
                onSuccess.invoke(favorites)
            }

            override fun onCancelled(de: DatabaseError) {
                Timber.d("Fail: " + de.message)
            }

        })
    }

}