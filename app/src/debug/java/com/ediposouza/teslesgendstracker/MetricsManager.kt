package com.ediposouza.teslesgendstracker.ui.utils

import android.content.Context
import com.ediposouza.teslesgendstracker.data.Card
import com.google.firebase.auth.FirebaseUser

/**
 * Created by ediposouza on 08/12/16.
 */
@SuppressWarnings("unused")
class MetricsManager() : MetricsManagerConstants() {

    companion object {

        private var static: MetricsManager? = null

        fun getInstance(): MetricsManager {
            if (static == null) {
                static = MetricsManager()
            }
            return static!!
        }

    }

    fun initialize(@SuppressWarnings("unused") context: Context) {
    }

    fun trackScreen(@ScreenParam screen: String) {
    }

    fun trackSignUp() {
    }

    fun trackSignIn(user: FirebaseUser?, success: Boolean) {
    }

    fun trackSearch(searchTerm: String) {
    }

    fun trackCardView(card: Card) {
    }

}