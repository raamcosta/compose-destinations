package com.ramcosta.samples.playground

import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.TaskStackBuilder
import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.core.app.NotificationCompat
import androidx.core.net.toUri
import com.ramcosta.samples.playground.di.DependencyContainer
import com.ramcosta.samples.playground.ui.screens.destinations.ProfileScreenDestination
import com.ramcosta.samples.playground.ui.screens.profile.ValueClassArg

val LocalDIContainer = staticCompositionLocalOf<DependencyContainer> {
    error("No dependency container provided!")
}

@SuppressLint("InlinedApi")
class MainActivity : ComponentActivity() {

    private val diContainer = DependencyContainer()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            CompositionLocalProvider(LocalDIContainer provides diContainer) {
                PlaygroundApp(
                    testProfileDeepLink = {
                        displayNotification(
                            title = "Test deep link",
                            text = "Profile screen deep link",
                            notificationID = 100,
                            channel = "DEFAULT",
                            pendingIntent = getPendingIntent()
                        )
                    }
                )
            }
        }
    }

    private fun getPendingIntent(): PendingIntent {
        val deepLinkPrefix = "https://destinationssample.com/"
        val profileDeepLink =
            "$deepLinkPrefix${ProfileScreenDestination(id = 1337L, color = Color.Cyan, valueClass = ValueClassArg("qweqwe")).route}"

        return TaskStackBuilder.create(applicationContext).run {
            addNextIntentWithParentStack(
                Intent(
                    Intent.ACTION_VIEW,
                    profileDeepLink.toUri(),
                    applicationContext,
                    MainActivity::class.java
                )
            )
            getPendingIntent(0, PendingIntent.FLAG_IMMUTABLE)
        }
    }

    @SuppressLint("UnspecifiedImmutableFlag")
    @Suppress("SameParameterValue")
    private fun displayNotification(
        title: String,
        text: String,
        notificationID: Int,
        channel: String,
        pendingIntent: PendingIntent?
    ) {
        val notificationManager = applicationContext
            .getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val defaultChannel =
                NotificationChannel(
                    "DEFAULT",
                    "DEFAULT",
                    NotificationManager.IMPORTANCE_DEFAULT
                )
            defaultChannel.lockscreenVisibility = Notification.VISIBILITY_PUBLIC
            notificationManager.createNotificationChannel(defaultChannel)
        }
        val builder = NotificationCompat.Builder(
            applicationContext,
            channel
        )
        builder.setContentTitle(title)
        builder.setTicker(title)
        builder.setContentText(text)
        builder.setSmallIcon(R.drawable.ic_launcher_background)

        builder.setDefaults(NotificationCompat.DEFAULT_LIGHTS or NotificationCompat.DEFAULT_VIBRATE)
        val notificationIntent = Intent(applicationContext, MainActivity::class.java)
        builder.setContentIntent(
            pendingIntent ?: PendingIntent.getActivity(
                applicationContext,
                0,
                notificationIntent,
                0
            )
        )
        val notification = builder.build()

        //Dismiss the notification on tap
        notification.flags = notification.flags or Notification.FLAG_AUTO_CANCEL

        //Update the proper notification
        notificationManager.notify(notificationID, notification)
    }
}