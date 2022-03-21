package pl.birski.falldetector.other

object Constants {

    const val NOTIFICATION_CHANNEL_ID = "tracking_channel"
    const val NOTIFICATION_CHANNEL_NAME = "Tracking"
    const val NOTIFICATION_ID = 1

    const val INTERVAL_MILISEC = 20 // frequency is set to 50 Hz
    const val SLIDING_WINDOW_TIME_SEC = 2 // SW size is 2 sec
    const val SLIDING_WINDOW_SIZE = SLIDING_WINDOW_TIME_SEC * 1000 / INTERVAL_MILISEC
    const val DURATION_S = 10
    const val N = DURATION_S * 1000 / INTERVAL_MILISEC

    const val BUFFER_COUNT: Int = 19

    const val CUSTOM_FALL_DETECTED_RECEIVER = "pl.birski.falldetectorCUSTOM_INTENT"
}
