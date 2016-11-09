package com.ediposouza.teslesgendstracker.interactor

import com.ediposouza.teslesgendstracker.data.Attribute
import com.ediposouza.teslesgendstracker.data.Card
import com.ediposouza.teslesgendstracker.data.UserData
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
    val NODE_COLLECTION = "collection"

    private fun getUserDBReference(): DatabaseReference? {
        val uid = FirebaseAuth.getInstance().currentUser?.uid
        return if (uid != null) database.child(NODE_USERS).child(uid) else null
    }

    fun setUserInfo() {
        val user = FirebaseAuth.getInstance().currentUser
        val userData = UserData(user?.displayName ?: "", user?.photoUrl.toString())
        database.child(NODE_USERS).child(user?.uid).setValue(userData)
    }

    fun setUserCardQtd(cls: Attribute, card: Card, qtd: Long, onComplete: () -> Unit) {
        val user = FirebaseAuth.getInstance().currentUser
        val node_cls = cls.name.toLowerCase()
        database.child(NODE_USERS).child(user?.uid).child(NODE_COLLECTION).child(NODE_CORE)
                .child(node_cls).child(card.shortName).setValue(qtd).addOnCompleteListener {
            onComplete.invoke()
        }
    }

    fun getUserCollection(cls: Attribute, onSuccess: (HashMap<String, Long>) -> Unit) {
        val node_cls = cls.name.toLowerCase()
        getUserDBReference()?.child(NODE_COLLECTION)?.child(NODE_CORE)?.child(node_cls)
                ?.addValueEventListener(object : ValueEventListener {

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