package pl.birski.falldetector.other

object Constants {

    const val NOTIFICATION_CHANNEL_ID = "tracking_channel"
    const val NOTIFICATION_CHANNEL_NAME = "Tracking"
    const val NOTIFICATION_ID = 1

    const val INTERVAL_MILISEC = 20 // frequency is set to 50 Hz
    const val SLIDING_WINDOW_TIME_SEC = 0.1F // SW size is 2 sec
    const val SLIDING_WINDOW_SIZE = SLIDING_WINDOW_TIME_SEC * 1000 / INTERVAL_MILISEC

    const val CUSTOM_FALL_DETECTED_RECEIVER = "pl.birski.falldetector.CUSTOM_INTENT"
}
