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
    private val KEY_CARD_NAME: String = "name"
    private val KEY_CARD_PHOTO: String = "photoUrl"
    private val KEY_CARD_FAVORITE = "favorite"
    private val KEY_CARD_QTD = "qtd"

    private fun dbUser(): DatabaseReference? {
        return if (userID.isNotEmpty()) database.child(NODE_USERS).child(userID) else null
    }

    private fun dbUserCards(cls: Attribute): DatabaseReference? {
        return dbUser()?.child(NODE_CARDS)?.child(NODE_CORE)?.child(cls.name.toLowerCase())
    }

    fun setUserInfo() {
        val user = FirebaseAuth.getInstance().currentUser
        dbUser()?.apply {
            child(KEY_CARD_NAME).setValue(user?.displayName ?: "")
            child(KEY_CARD_PHOTO).setValue(user?.photoUrl.toString())
        }
    }

    fun setUserCardQtd(card: Card, qtd: Long, onComplete: () -> Unit) {
        dbUserCards(card.cls)?.apply {
            child(card.shortName).child(KEY_CARD_QTD).setValue(qtd).addOnCompleteListener {
                onComplete.invoke()
            }
        }
    }

    fun setUserCardFavorite(card: Card, favorite: Boolean, onComplete: () -> Unit) {
        dbUserCards(card.cls)?.apply {
            child(card.shortName).child(KEY_CARD_FAVORITE).apply {
                if (favorite) {
                    setValue(true).addOnCompleteListener { onComplete.invoke() }
                } else {
                    removeValue().addOnCompleteListener { onComplete.invoke() }
                }
            }
        }
    }

    fun getUserCollection(attr: Attribute, onSuccess: (Map<String, Long>) -> Unit) {
        dbUserCards(attr)?.addListenerForSingleValueEvent(object : ValueEventListener {

                    @Suppress("UNCHECKED_CAST")
                    override fun onDataChange(ds: DataSnapshot) {
                        val collection = ds.children.filter { it.hasChild(KEY_CARD_QTD) }.map({
                            it.key to it.child(KEY_CARD_QTD).value as Long
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
        dbUserCards(attr)?.addListenerForSingleValueEvent(object : ValueEventListener {

            @Suppress("UNCHECKED_CAST")
            override fun onDataChange(ds: DataSnapshot) {
                val favorites = ds.children.filter { (it.child(KEY_CARD_FAVORITE)?.value ?: false) as Boolean }
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