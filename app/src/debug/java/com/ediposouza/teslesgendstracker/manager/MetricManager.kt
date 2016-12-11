package com.ediposouza.teslesgendstracker.manager

import android.content.Context
import com.ediposouza.teslesgendstracker.MetricAction
import com.ediposouza.teslesgendstracker.MetricConstants
import com.ediposouza.teslesgendstracker.MetricScreen
import com.ediposouza.teslesgendstracker.data.Card
import com.google.firebase.auth.FirebaseUser
import timber.log.Timber

/**
 * Created by ediposouza on 08/12/16.
 */
@SuppressWarnings("unused")
class MetricManager() : MetricConstants() {

    companion object {

        private var static: MetricManager? = null

        fun getInstance(): MetricManager {
            if (static == null) {
                static = MetricManager()
            }
            return static!!
        }

    }

    fun initialize(context: Context) {
    }

    fun trackAction(action: MetricAction, vararg params: String) {
        Timber.d(if (params.isEmpty()) action.name else "${action.name} with params: ${params.toSet()}")
    }

    fun trackScreen(screen: MetricScreen) {
        Timber.d(screen.name)
    }

    fun trackSignUp() {
        Timber.d("SignUp")
    }

    fun trackSignIn(user: FirebaseUser?, success: Boolean) {
        Timber.d("SignIn success: $success")
    }

    fun trackSearch(searchTerm: String) {
        Timber.d("Search: $searchTerm")
    }

    fun trackCardView(card: Card) {
        Timber.d("Card view: $card")
    }

}