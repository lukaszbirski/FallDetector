package pl.birski.falldetector.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.NotificationManager.IMPORTANCE_LOW
import android.app.PendingIntent
import android.app.PendingIntent.FLAG_UPDATE_CURRENT
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.MutableLiveData
import pl.birski.falldetector.R
import pl.birski.falldetector.presentation.MainActivity
import pl.birski.falldetector.service.enum.ServiceActions
import timber.log.Timber

class TrackingService : LifecycleService() {

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        intent?.let {
            when(it.action) {
                ServiceActions.START_OR_RESUME.name -> {
                    Timber.d("Started od resumed service")
                    startForegroundService()
                }
                ServiceActions.STOP.name -> {
                    Timber.d("Stopped service")
                }
                else -> {}
            }
        }
        return super.onStartCommand(intent, flags, startId)
    }

    companion object {
        internal fun initiate(context: Context, action: ServiceActions) {
            val intent = Intent(context, TrackingService::class.java).also {
                it.action = action.name
            }
            context.startService(intent)
        }

        internal fun stop(context: Context, action: ServiceActions) {
            val intent = Intent(context, TrackingService::class.java).also {
                it.action = action.name
            }
            context.stopService(intent)
        }

        val isTracking = MutableLiveData<Boolean>()

        private const val NOTIFICATION_CHANNEL_ID = "tracking_channel"
        private const val NOTIFICATION_CHANNEL_NAME = "Tracking"
        private const val NOTIFICATION_ID = 1
    }

    private fun createNotificationChannel(notificationManager: NotificationManager) {
        val channel = NotificationChannel(
            NOTIFICATION_CHANNEL_ID,
            NOTIFICATION_CHANNEL_NAME,
            IMPORTANCE_LOW
        )
        notificationManager.createNotificationChannel(channel)
    }

    private fun startForegroundService() {
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        createNotificationChannel(notificationManager)

        val notificationBuilder = NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
            .setAutoCancel(false)
            .setOngoing(true)
            .setSmallIcon(R.drawable.ic_person_falling_down_24)
            .setContentTitle(this.applicationContext.getString(R.string.notification_title))
            .setContentIntent(getMainActivityPendingIntent())

        startForeground(NOTIFICATION_ID, notificationBuilder.build())
    }

    private fun getMainActivityPendingIntent() = PendingIntent.getActivity(
        this,
        0,
        Intent(this, MainActivity::class.java).also {
            it.action = ServiceActions.SHOW.name
        },
        FLAG_UPDATE_CURRENT
    )
}