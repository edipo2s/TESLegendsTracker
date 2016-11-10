package com.ediposouza.teslesgendstracker.interactor

import com.ediposouza.teslesgendstracker.data.Attribute
import com.ediposouza.teslesgendstracker.data.Card
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import timber.log.Timber
import java.util.*

/**
 * Created by ediposouza on 01/11/16.
 */
class UserInteractor() : BaseInteractor() {

    val NODE_USERS = "users"
    val CHILD_NAME: String = "name"
    val CHILD_PHOTO: String = "photoUrl"
    val CHILD_COLLECTION = "collection"

    private fun userDBReference(): DatabaseReference? {
        val uid = FirebaseAuth.getInstance().currentUser?.uid
        return if (uid != null) database.child(NODE_USERS).child(uid) else null
    }

    fun setUserInfo() {
        val user = FirebaseAuth.getInstance().currentUser
        userDBReference()?.apply {
            child(CHILD_NAME).setValue(user?.displayName ?: "")
            child(CHILD_PHOTO).setValue(user?.photoUrl.toString())
        }
    }

    fun setUserCardQtd(cls: Attribute, card: Card, qtd: Long, onComplete: () -> Unit) {
        val node_cls = cls.name.toLowerCase()
        userDBReference()?.apply {
            child(CHILD_COLLECTION).child(NODE_CORE).child(node_cls)
                    .child(card.shortName).setValue(qtd).addOnCompleteListener { onComplete.invoke() }
        }
    }

    fun getUserCollection(cls: Attribute, onSuccess: (HashMap<String, Long>) -> Unit) {
        val node_cls = cls.name.toLowerCase()
        userDBReference()?.child(CHILD_COLLECTION)?.child(NODE_CORE)?.child(node_cls)
                ?.addListenerForSingleValueEvent(object : ValueEventListener {

                    @Suppress("UNCHECKED_CAST")
                    override fun onDataChange(ds: DataSnapshot) {
                        val collection = ds.value
                        val cards = if (collection != null) collection as HashMap<String, Long> else null
                        Timber.d(cards.toString())
                        onSuccess.invoke(cards ?: HashMap<String, Long>())
                    }

                    override fun onCancelled(de: DatabaseError) {
                        Timber.d("Fail: " + de.message)
                    }

                })
    }

}