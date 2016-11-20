package com.ediposouza.teslesgendstracker.ui.base

import android.content.Intent
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.widget.ProgressBar
import com.ediposouza.teslesgendstracker.R
import com.ediposouza.teslesgendstracker.interactor.PrivateInteractor
import com.google.android.gms.auth.api.Auth
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.api.GoogleApiClient
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import icepick.Icepick
import kotlinx.android.synthetic.main.activity_dash.*
import kotlinx.android.synthetic.main.dialog_signin.view.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.jetbrains.anko.toast
import timber.log.Timber

/**
 * Created by EdipoSouza on 10/30/16.
 */
open class BaseActivity : AppCompatActivity(), GoogleApiClient.OnConnectionFailedListener, FirebaseAuth.AuthStateListener {

    protected val eventBus by lazy { EventBus.getDefault() }

    private val RC_SIGN_IN: Int = 235

    private var loading: AlertDialog? = null
    private var snackbar: Snackbar? = null
    private var googleApiClient: GoogleApiClient? = null
    private val firebaseAuth by lazy { FirebaseAuth.getInstance() }

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Icepick.restoreInstanceState(this, savedInstanceState)

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build()
        googleApiClient = GoogleApiClient.Builder(this)
                .enableAutoManage(this, this)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build()
    }

    override fun onConnectionFailed(p0: ConnectionResult) {
    }

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)
        setSupportActionBar(dash_toolbar)
        supportActionBar?.title = ""
    }

    public override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        Icepick.saveInstanceState(this, outState)
    }

    override fun onStart() {
        super.onStart()
        firebaseAuth.addAuthStateListener(this)
    }

    override fun onResume() {
        super.onResume()
        eventBus.register(this)
    }

    override fun onPause() {
        super.onPause()
        eventBus.unregister(this)
    }

    override fun onStop() {
        super.onStop()
        firebaseAuth.removeAuthStateListener(this)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == RC_SIGN_IN) {
            val result = Auth.GoogleSignInApi.getSignInResultFromIntent(data)
            if (result.isSuccess) {
                val account = result.signInAccount
                firebaseAuthWithGoogle(account)
            } else {
                hideLoading()
            }
        }
    }

    override fun onAuthStateChanged(firebaseAuth: FirebaseAuth) {
        val user = firebaseAuth.currentUser
        if (user != null) {
            Timber.d("onAuthStateChanged:signed_in:" + user.getUid())
        } else {
            Timber.d("onAuthStateChanged:signed_out")
        }
    }

    fun firebaseAuthWithGoogle(acct: GoogleSignInAccount?) {
        Timber.d("firebaseAuthWithGoogle:" + acct?.id)
        showLoading()
        val credential = GoogleAuthProvider.getCredential(acct?.idToken, null)
        firebaseAuth.signInWithCredential(credential)
                .addOnCompleteListener(this) { task ->
                    Timber.d("signInWithCredential:onComplete:" + task.isSuccessful)
                    if (task.isSuccessful) {
                        toast("Logged with " + firebaseAuth.currentUser?.displayName)
                        PrivateInteractor().setUserInfo()
                    } else {
                        Timber.w("signInWithCredential", task.exception)
                        toast("Authentication failed.")
                    }
                    hideLoading()
                }
    }

    private fun showLoading() {
        val progressBar = ProgressBar(this)
        progressBar.isIndeterminate = true
        val largeMargin = resources.getDimensionPixelSize(R.dimen.large_margin)
        progressBar.setPadding(0, largeMargin, 0, largeMargin)
        loading = AlertDialog.Builder(this)
                .setView(progressBar)
                .show()
    }

    private fun hideLoading() {
        loading?.dismiss()
    }

    @SuppressWarnings("ResourceType")
    @Subscribe
    fun showSnackMsg(cmdShowSnackbarMsg: CmdShowSnackbarMsg) {
        snackbar?.dismiss()
        val msgRes = cmdShowSnackbarMsg.msgRes
        val msg = if (msgRes > 0) getString(msgRes) else cmdShowSnackbarMsg.msg
        snackbar = Snackbar.make(findViewById(R.id.coordinatorLayout), msg, cmdShowSnackbarMsg.duration)
        if (cmdShowSnackbarMsg.action != null) {
            val actionTextRes = cmdShowSnackbarMsg.actionTextRes
            val actionText = if (actionTextRes > 0) getString(actionTextRes) else cmdShowSnackbarMsg.actionText
            snackbar?.setAction(actionText, { cmdShowSnackbarMsg.action?.invoke() })
        }
        snackbar?.show()
    }

    @Subscribe
    fun showLogin(showLogin: CmdShowLogin) {
        val loginView = View.inflate(this, R.layout.dialog_signin, null)
        val alertDialog = AlertDialog.Builder(this)
                .setView(loginView)
                .setCancelable(true)
                .show()
        loginView.login_signin_google.setOnClickListener {
            initGoogleLogin()
            alertDialog.dismiss()
        }
        loginView.login_signin_twitter.setOnClickListener {
            initTwitterLogin()
            alertDialog.dismiss()
        }
    }

    private fun initGoogleLogin() {
        val signInIntent = Auth.GoogleSignInApi.getSignInIntent(googleApiClient)
        startActivityForResult(signInIntent, RC_SIGN_IN)
    }

    private fun initTwitterLogin() {

    }

}