package pl.birski.falldetector.other

object Constants {

    const val NOTIFICATION_CHANNEL_ID = "tracking_channel"
    const val NOTIFICATION_CHANNEL_NAME = "Tracking"
    const val NOTIFICATION_ID = 1

    const val CUSTOM_FALL_DETECTED_RECEIVER = "pl.birski.falldetector.CUSTOM_INTENT"

    const val INTERVAL_MILISEC = 20 // frequency is set to 50 Hz
    const val SLIDING_WINDOW_TIME_SEC = 0.4F // SW size is 0.4 sec
    const val SLIDING_WINDOW_SIZE = SLIDING_WINDOW_TIME_SEC * 1000 / INTERVAL_MILISEC

    const val SV_MIN_MAX_SLIDING_WINDOW_TIME_SEC = 0.1F // SW size for SVminmax is 0.4s
    const val SV_MIN_MAX_SW_SIZE = SV_MIN_MAX_SLIDING_WINDOW_TIME_SEC * 1000 / INTERVAL_MILISEC

    const val IMPACT_TIME_SPAN = 2000 / INTERVAL_MILISEC // after impact need to wait 2 sec
}
