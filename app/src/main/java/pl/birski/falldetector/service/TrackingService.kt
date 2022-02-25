package pl.birski.falldetector.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.NotificationManager.IMPORTANCE_LOW
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.lifecycle.LifecycleService
import dagger.hilt.android.AndroidEntryPoint
import pl.birski.falldetector.data.FallDetector
import pl.birski.falldetector.other.Constants
import pl.birski.falldetector.other.Constants.NOTIFICATION_ID
import pl.birski.falldetector.service.enum.ServiceActions
import timber.log.Timber
import javax.inject.Inject

@AndroidEntryPoint
class TrackingService : LifecycleService() {

    @Inject
    lateinit var fallDetector: FallDetector

    @Inject
    lateinit var notificationBuilder: NotificationCompat.Builder

    @Inject
    lateinit var pendingIntent: PendingIntent

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        intent?.let {
            when(it.action) {
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
        fallDetector.initiateSensor(this)
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        createNotificationChannel(notificationManager)
        startForeground(NOTIFICATION_ID, notificationBuilder.build())
    }

    private fun stopForegroundService(intent: Intent?) {
        fallDetector.stopMeasurement()
        this.stopService(intent)
    }

}
