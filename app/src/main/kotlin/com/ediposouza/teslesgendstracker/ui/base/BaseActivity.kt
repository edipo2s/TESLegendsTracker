package com.ediposouza.teslesgendstracker.ui.base

import android.content.Intent
import android.graphics.Rect
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.preference.PreferenceManager
import android.support.annotation.StringRes
import android.support.design.widget.CoordinatorLayout
import android.support.design.widget.Snackbar
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import android.text.format.DateUtils
import android.view.View
import android.widget.ProgressBar
import com.ediposouza.teslesgendstracker.App
import com.ediposouza.teslesgendstracker.R
import com.ediposouza.teslesgendstracker.interactor.PrivateInteractor
import com.ediposouza.teslesgendstracker.interactor.PublicInteractor
import com.ediposouza.teslesgendstracker.util.*
import com.facebook.AccessToken
import com.facebook.CallbackManager
import com.facebook.FacebookCallback
import com.facebook.FacebookException
import com.facebook.login.LoginResult
import com.google.android.gms.auth.api.Auth
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FacebookAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.android.synthetic.main.include_login_button.view.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.jetbrains.anko.contentView
import org.jetbrains.anko.find
import org.jetbrains.anko.toast
import timber.log.Timber

/**
 * Created by EdipoSouza on 10/30/16.
 */
open class BaseActivity : AppCompatActivity(), GoogleApiClient.OnConnectionFailedListener, FirebaseAuth.AuthStateListener {

    private val KEY_CURRENT_PROVIDER: String = "currentProviderKey"
    private val RC_SIGN_IN: Int = 235

    private var loading: AlertDialog? = null
    private var snackbar: Snackbar? = null
    private var googleApiClient: GoogleApiClient? = null
    private val firebaseAuth by lazy { FirebaseAuth.getInstance() }

    protected var canExit = false
    protected var currentMethod = MetricsConstants.PARAM_SIGN_METHOD_VALUE_GOOGLE
        set(value) {
            field = value
            PreferenceManager.getDefaultSharedPreferences(applicationContext).edit()
                    .putString(KEY_CURRENT_PROVIDER, value)
                    .apply()
        }
    protected var keyboardVisible = false
    protected var snackbarNeedMargin = true
    protected var onKeyboardVisibilityChange: (() -> Unit)? = null
    protected val handler = Handler()
    protected val eventBus: EventBus by lazy { EventBus.getDefault() }
    protected val callbackManager: CallbackManager by lazy { CallbackManager.Factory.create() }

    val keyboardChangeListener = {
        val r = Rect()
        contentView?.getWindowVisibleDisplayFrame(r)
        val screenHeight = contentView?.rootView?.height ?: 0
        // r.bottom is the position above soft keypad or device button. if keypad is shown, the r.bottom is smaller than that before.
        val keypadHeight = screenHeight - r.bottom
        val newKeyboardVisible = keypadHeight > (screenHeight * 0.15) // 0.15 ratio is perhaps enough to determine keypad height.
        if (keyboardVisible != newKeyboardVisible) {
            keyboardVisible = newKeyboardVisible
            onKeyboardVisibilityChange?.invoke()
        }
    }

    val onSignCompleteListener: (Task<AuthResult>) -> Unit = { task ->
        Timber.d("signWithCredential:onComplete:" + task.isSuccessful)
        if (task.isSuccessful) {
            val currentUser = firebaseAuth.currentUser
            PublicInteractor.getUserInfo(currentUser?.uid ?: "") {
                if (it.name.isEmpty()) {
                    toast("SignUp with " + currentUser?.displayName)
                    MetricsManager.trackSignUp(currentMethod)
                } else {
                    toast("SignIn with " + currentUser?.displayName)
                }
                PrivateInteractor.setUserInfo()
            }
            eventBus.post(CmdLoginSuccess())
        } else {
            Timber.w("signWithCredential", task.exception)
            toast(getString(R.string.error_login))
            MetricsManager.trackSignIn(null, false, currentMethod)
        }
        hideLoading()
    }

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        googleApiClient = GoogleApiClient.Builder(this)
                .enableAutoManage(this, this)
                .addApi(Auth.GOOGLE_SIGN_IN_API, GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                        .requestIdToken(getString(R.string.default_web_client_id))
                        .requestEmail()
                        .build())
                .build()
        currentMethod = PreferenceManager.getDefaultSharedPreferences(applicationContext)
                .getString(KEY_CURRENT_PROVIDER, MetricsConstants.PARAM_SIGN_METHOD_VALUE_GOOGLE)
    }

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)
        setSupportActionBar(findViewById(R.id.toolbar) as Toolbar?)
        supportActionBar?.title = ""
        supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_arrow_back)
        contentView?.viewTreeObserver?.addOnGlobalLayoutListener(keyboardChangeListener)
    }

    override fun onStart() {
        super.onStart()
        firebaseAuth.addAuthStateListener(this)
        eventBus.register(this)
        ConfigManager.updateCaches {
            if (ConfigManager.isVersionUnsupported()) {
                alertThemed(R.string.app_version_unsupported, theme = R.style.AppDialog) {
                    okButton {
                        MetricsManager.trackAction(MetricAction.ACTION_VERSION_UNSUPPORTED())
                        startActivity(Intent(Intent.ACTION_VIEW)
                                .setData(Uri.parse(getString(R.string.playstore_url_format, packageName))))
                        System.exit(0)
                    }
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
        contentView?.viewTreeObserver?.removeOnGlobalLayoutListener(keyboardChangeListener)
        MetricsManager.flush()
        super.onDestroy()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == RC_SIGN_IN) {
            val result = Auth.GoogleSignInApi.getSignInResultFromIntent(data)
            if (result?.isSuccess ?: false) {
                val account = result.signInAccount
                firebaseAuthWithGoogle(account)
            } else {
                hideLoading()
            }
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    override fun onConnectionFailed(p0: ConnectionResult) {
        googleApiClient?.apply {
            if (isConnected) {
                clearDefaultAccountAndReconnect()
            }
        }
    }

    override fun onAuthStateChanged(firebaseAuth: FirebaseAuth) {
        val currentUser = firebaseAuth.currentUser
        if (currentUser != null) {
            Timber.d("onAuthStateChanged:signed_in:" + currentUser.uid)
            if (!App.hasUserAlreadyLogged) {
                MetricsManager.trackSignIn(currentUser, true, currentMethod)
                App.hasUserAlreadyLogged = true
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

    protected fun showErrorUserNotLogged() {
        eventBus.post(CmdShowSnackbarMsg(CmdShowSnackbarMsg.TYPE_ERROR, R.string.error_auth)
                .withAction(R.string.action_login, { showLogin() }))
    }

    protected fun showLogin() {
        val view = View.inflate(this@BaseActivity, R.layout.dialog_login, null)
        val loginDialog = AlertDialog.Builder(this)
                .setView(view)
                .create()
        view.signin_buttons.visibility = View.VISIBLE
        view.signin_google_button.setOnClickListener {
            FirebaseAuth.getInstance().signOut()
            eventBus.post(CmdShowLogin())
            loginDialog.dismiss()
        }
        with(view.signin_facebook_button) {
            setReadPermissions("email")
            setOnClickListener {
                FirebaseAuth.getInstance().signOut()
                loginDialog.dismiss()
            }
            registerCallback(callbackManager, object : FacebookCallback<LoginResult?> {
                override fun onSuccess(result: LoginResult?) {
                    eventBus.post(result?.getAccessToken())
                }

                override fun onError(error: FacebookException?) {
                    Timber.d(error)
                }

                override fun onCancel() {
                }

            })
        }
        loginDialog.show()
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

    private fun firebaseAuthWithGoogle(acct: GoogleSignInAccount?) {
        Timber.d("firebaseAuthWithGoogle:" + acct?.id)
        showLoading()
        currentMethod = MetricsConstants.PARAM_SIGN_METHOD_VALUE_GOOGLE
        firebaseAuth.signInWithCredential(GoogleAuthProvider.getCredential(acct?.idToken, null))
                .addOnCompleteListener(this, onSignCompleteListener)
    }

    @Subscribe
    fun firebaseAuthWithFacebook(accessToken: AccessToken) {
        Timber.d("firebaseAuthWithFacebook:" + accessToken.userId)
        showLoading()
        currentMethod = MetricsConstants.PARAM_SIGN_METHOD_VALUE_FACEBOOK
        firebaseAuth.signInWithCredential(FacebookAuthProvider.getCredential(accessToken.getToken()))
                .addOnCompleteListener(this, onSignCompleteListener)
    }

    @Subscribe
    @SuppressWarnings("unused", "ResourceType")
    fun onCmdShowSnackMsg(cmdShowSnackbarMsg: CmdShowSnackbarMsg) {
        snackbar?.dismiss()
        val msgRes = cmdShowSnackbarMsg.msgRes
        val msg = if (msgRes > 0) getString(msgRes) else cmdShowSnackbarMsg.msg
        snackbar = Snackbar.make(find<View>(R.id.coordinatorLayout), msg, cmdShowSnackbarMsg.duration)
        if (cmdShowSnackbarMsg.action != null) {
            val actionTextRes = cmdShowSnackbarMsg.actionTextRes
            val actionText = if (actionTextRes > 0) getString(actionTextRes) else cmdShowSnackbarMsg.actionText
            snackbar?.setAction(actionText, { cmdShowSnackbarMsg.action?.invoke() })
        }
        if (snackbarNeedMargin) {
            val snackbarLP = snackbar?.view?.layoutParams as CoordinatorLayout.LayoutParams
            snackbarLP.bottomMargin = resources.getDimensionPixelSize(R.dimen.navigation_bar_height)
            snackbar?.view?.layoutParams = snackbarLP
        }
        snackbar?.show()
    }

    @Subscribe
    @Suppress("unused", "UNUSED_PARAMETER")
    fun onCmdShowLogin(showLogin: CmdShowLogin) {
        googleApiClient?.apply {
            if (isConnected) {
                clearDefaultAccountAndReconnect()
            }
        }
        val signInIntent = Auth.GoogleSignInApi.getSignInIntent(googleApiClient)
        startActivityForResult(signInIntent, RC_SIGN_IN)
    }

}