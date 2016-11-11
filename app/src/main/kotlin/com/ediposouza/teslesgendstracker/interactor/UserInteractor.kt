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
class UserInteractor() : BaseInteractor() {

    val NODE_USERS = "users"
    val CHILD_NAME: String = "name"
    val CHILD_PHOTO: String = "photoUrl"
    val CHILD_FAVORITE = "favorite"
    val CHILD_QTD = "qtd"

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
            child(CHILD_NAME).setValue(user?.displayName ?: "")
            child(CHILD_PHOTO).setValue(user?.photoUrl.toString())
        }
    }

    fun setUserCardQtd(card: Card, qtd: Long, onComplete: () -> Unit) {
        userDBCardsReference(card.cls)?.apply {
            child(card.shortName).child(CHILD_QTD).setValue(qtd).addOnCompleteListener {
                onComplete.invoke()
            }
        }
    }

    fun setUserCardFavorite(card: Card, favorite: Boolean, onComplete: () -> Unit) {
        userDBCardsReference(card.cls)?.apply {
            child(card.shortName).child(CHILD_FAVORITE).apply {
                if (favorite) {
                    setValue(true).addOnCompleteListener { onComplete.invoke() }
                } else {
                    removeValue().addOnCompleteListener { onComplete.invoke() }
                }
            }
        }
    }

    fun getUserCollection(cls: Attribute, onSuccess: (Map<String, Long>) -> Unit) {
        userDBCardsReference(cls)?.addListenerForSingleValueEvent(object : ValueEventListener {

                    @Suppress("UNCHECKED_CAST")
                    override fun onDataChange(ds: DataSnapshot) {
                        val collection = ds.children.filter { it.hasChild(CHILD_QTD) }.map({
                            it.key to it.child(CHILD_QTD).value as Long
                        }).toMap()
                        Timber.d(collection.toString())
                        onSuccess.invoke(collection)
                    }

                    override fun onCancelled(de: DatabaseError) {
                        Timber.d("Fail: " + de.message)
                    }

                })
    }

    fun getUserFavorites(cls: Attribute, onSuccess: (List<String>) -> Unit) {
        userDBCardsReference(cls)?.addListenerForSingleValueEvent(object : ValueEventListener {

            @Suppress("UNCHECKED_CAST")
            override fun onDataChange(ds: DataSnapshot) {
                val favorites = ds.children.filter { (it.child(CHILD_FAVORITE)?.value ?: false) as Boolean }
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