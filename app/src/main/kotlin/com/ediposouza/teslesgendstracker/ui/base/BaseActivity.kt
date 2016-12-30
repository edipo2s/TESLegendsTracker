package com.ediposouza.teslesgendstracker.ui.base

import android.content.Intent
import android.graphics.Rect
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.support.annotation.StringRes
import android.support.design.widget.Snackbar
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import android.text.format.DateUtils
import android.widget.ProgressBar
import com.ediposouza.teslesgendstracker.App
import com.ediposouza.teslesgendstracker.R
import com.ediposouza.teslesgendstracker.interactor.PrivateInteractor
import com.ediposouza.teslesgendstracker.interactor.PublicInteractor
import com.ediposouza.teslesgendstracker.util.ConfigManager
import com.ediposouza.teslesgendstracker.util.MetricAction
import com.ediposouza.teslesgendstracker.util.MetricsManager
import com.google.android.gms.auth.api.Auth
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.api.GoogleApiClient
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.jetbrains.anko.alert
import org.jetbrains.anko.contentView
import org.jetbrains.anko.toast
import timber.log.Timber

/**
 * Created by EdipoSouza on 10/30/16.
 */
open class BaseActivity : AppCompatActivity(), GoogleApiClient.OnConnectionFailedListener, FirebaseAuth.AuthStateListener {

    private val RC_SIGN_IN: Int = 235

    private var loading: AlertDialog? = null
    private var snackbar: Snackbar? = null
    private var googleApiClient: GoogleApiClient? = null
    private val firebaseAuth by lazy { FirebaseAuth.getInstance() }

    protected var canExit: Boolean = false
    protected val handler = Handler()
    protected var keyboardVisible = false
    protected var onKeyboardVisibilityChange: (() -> Unit)? = null
    protected val eventBus: EventBus by lazy { EventBus.getDefault() }

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build()
        googleApiClient = GoogleApiClient.Builder(this)
                .enableAutoManage(this, this)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build()
    }

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)
        setSupportActionBar(findViewById(R.id.toolbar) as Toolbar?)
        supportActionBar?.title = ""
        supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_arrow_back)
        contentView?.viewTreeObserver?.addOnGlobalLayoutListener({
            val r = Rect()
            contentView?.getWindowVisibleDisplayFrame(r)
            val screenHeight = contentView?.rootView?.height ?: 0
            // r.bottom is the position above soft keypad or device button. if keypad is shown, the r.bottom is smaller than that before.
            val keypadHeight = screenHeight - r.bottom
            keyboardVisible = keypadHeight > (screenHeight * 0.15) // 0.15 ratio is perhaps enough to determine keypad height.
            onKeyboardVisibilityChange?.invoke()
        })
    }

    override fun onStart() {
        super.onStart()
        firebaseAuth.addAuthStateListener(this)
        eventBus.register(this)
        ConfigManager.updateCaches {
            if (ConfigManager.isVersionUnsupported()) {
                alert(getString(R.string.app_version_unsupported)) {
                    okButton {
                        MetricsManager.trackAction(MetricAction.ACTION_VERSION_UNSUPPORTED())
                        startActivity(Intent(Intent.ACTION_VIEW)
                                .setData(Uri.parse(getString(R.string.playstore_url_format, packageName))))
                        System.exit(0)
                    }
                    setTheme(R.style.AppDialog)
                }.show()
            }
        }
    }

    override fun onStop() {
        super.onStop()
        eventBus.unregister(this)
        firebaseAuth.removeAuthStateListener(this)
    }

    override fun onDestroy() {
        MetricsManager.flush()
        super.onDestroy()
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

    override fun onConnectionFailed(p0: ConnectionResult) {
    }

    override fun onAuthStateChanged(firebaseAuth: FirebaseAuth) {
        val currentUser = firebaseAuth.currentUser
        if (currentUser != null) {
            Timber.d("onAuthStateChanged:signed_in:" + currentUser.uid)
            if (!App.hasUserLogged) {
                MetricsManager.trackSignIn(currentUser, true)
                App.hasUserLogged = true
            }
        } else {
            Timber.d("onAuthStateChanged:signed_out")
        }
    }

    protected fun showExitConfirm(@StringRes exitMsg: Int = R.string.app_exit_confirm) {
        canExit = true
        handler.postDelayed({ canExit = false }, DateUtils.SECOND_IN_MILLIS * 2)
        toast(exitMsg)
    }

    private fun firebaseAuthWithGoogle(acct: GoogleSignInAccount?) {
        Timber.d("firebaseAuthWithGoogle:" + acct?.id)
        showLoading()
        val credential = GoogleAuthProvider.getCredential(acct?.idToken, null)
        firebaseAuth.signInWithCredential(credential)
                .addOnCompleteListener(this) { task ->
                    Timber.d("signInWithCredential:onComplete:" + task.isSuccessful)
                    if (task.isSuccessful) {
                        val currentUser = firebaseAuth.currentUser
                        PublicInteractor().getUserInfo(currentUser?.uid ?: "", {
                            toast("SignUp with " + currentUser?.displayName)
                            MetricsManager.trackSignUp()
                        }, {
                            toast("SignIn with " + currentUser?.displayName)
                        })
                        PrivateInteractor().setUserInfo()
                        EventBus.getDefault().post(CmdLoginSuccess())
                    } else {
                        Timber.w("signInWithCredential", task.exception)
                        toast(getString(R.string.error_login))
                        MetricsManager.trackSignIn(null, false)
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
    fun onCmdShowSnackMsg(cmdShowSnackbarMsg: CmdShowSnackbarMsg) {
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
    fun onCmdShowLogin(showLogin: CmdShowLogin) {
        val signInIntent = Auth.GoogleSignInApi.getSignInIntent(googleApiClient)
        startActivityForResult(signInIntent, RC_SIGN_IN)
    }

}