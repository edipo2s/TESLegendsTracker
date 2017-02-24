package com.ediposouza.teslesgendstracker.ui.util

import android.app.PendingIntent
import android.content.Intent
import android.media.RingtoneManager
import android.os.Bundle
import android.support.v4.app.NotificationCompat
import android.support.v4.app.NotificationManagerCompat
import android.support.v4.content.ContextCompat
import android.text.TextUtils
import android.util.Log
import com.ediposouza.teslesgendstracker.R
import com.ediposouza.teslesgendstracker.ui.DashActivity
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

/**
 * Created by ediposouza on 6/23/16.
 */
class PushNotificationService : FirebaseMessagingService() {

    private val TAG = "FirebasePushService"

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)
        val msgData = remoteMessage.data ?: return
        Log.d(TAG, "From: " + remoteMessage.from)

        val bundleData = Bundle().apply {
            msgData.keys.forEach {
                putString(it, msgData[it])
            }
        }

        remoteMessage.notification?.apply {
            Log.d(TAG, "Firebase Notification Message: " + body)
            showNotification(getString(R.string.app_name), body ?: "", bundleData)
            return
        }
    }

    private fun showNotification(title: String, message: String, extras: Bundle?) {
        if (TextUtils.isEmpty(title) && TextUtils.isEmpty(message)) {
            return
        }

        val intent = Intent(this, DashActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            putExtras(extras)
        }

        val notification = NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.ic_legend)
                .setContentTitle(title)
                .setContentText(message)
                .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
                .setContentIntent(PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_ONE_SHOT))
                .setAutoCancel(true)
                .setColor(ContextCompat.getColor(this, R.color.colorPrimary))
                .setStyle(NotificationCompat.BigTextStyle()
                        .bigText(message))
                .build()

        NotificationManagerCompat.from(this).notify(0, notification)
    }

}
