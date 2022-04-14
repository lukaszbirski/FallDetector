package pl.birski.falldetector.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.NotificationManager.IMPORTANCE_LOW
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.lifecycle.LifecycleService
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import pl.birski.falldetector.other.Constants
import pl.birski.falldetector.other.Constants.NOTIFICATION_ID
import pl.birski.falldetector.service.enum.ServiceActions
import timber.log.Timber

@AndroidEntryPoint
class TrackingService : LifecycleService() {

    @Inject
    lateinit var notificationBuilder: NotificationCompat.Builder

//    @Inject
//    lateinit var pendingIntent: PendingIntent

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        intent?.let {
            when (it.action) {
                ServiceActions.START_OR_RESUME.name -> {
                    Timber.d("Started or resumed service")
                    startForegroundService()
                }
                ServiceActions.STOP.name -> {
                    Timber.d("Stopped service")
                    stopForegroundService(intent)
                }
                else -> {}
            }
        }
        return super.onStartCommand(intent, flags, startId)
    }

    private fun createNotificationChannel(notificationManager: NotificationManager) {
        val channel = NotificationChannel(
            Constants.NOTIFICATION_CHANNEL_ID,
            Constants.NOTIFICATION_CHANNEL_NAME,
            IMPORTANCE_LOW
        )
        notificationManager.createNotificationChannel(channel)
    }

    private fun startForegroundService() {
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE)
            as NotificationManager
        createNotificationChannel(notificationManager)
        startForeground(NOTIFICATION_ID, notificationBuilder.build())
    }

    // TODO needs to be corrected
    private fun stopForegroundService(intent: Intent?) {
        this.stopService(intent)
    }
}
