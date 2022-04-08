package pl.birski.falldetector.other

object Constants {

    const val NOTIFICATION_CHANNEL_ID = "tracking_channel"
    const val NOTIFICATION_CHANNEL_NAME = "Tracking"
    const val NOTIFICATION_ID = 1

    const val CUSTOM_FALL_DETECTED_RECEIVER = "pl.birski.falldetector.CUSTOM_INTENT"

    const val INTERVAL_MILISEC = 20 // frequency is set to 50 Hz

    const val MIN_MAX_SLIDING_WINDOW_TIME_SEC = 0.1F // SW size for SVminmax is 0.1 s
    const val MIN_MAX_SW_SIZE = MIN_MAX_SLIDING_WINDOW_TIME_SEC * 1000 / INTERVAL_MILISEC

    const val POSTURE_DETECTION_SW_TIME_SEC = 0.4F // SW size for posture detection is 0.4 s
    const val POSTURE_DETECTION_SW_SIZE = POSTURE_DETECTION_SW_TIME_SEC * 1000 / INTERVAL_MILISEC

    const val IMPACT_TIME_SPAN = 2000 / INTERVAL_MILISEC // after impact need to wait 2 sec
    const val FALLING_TIME_SPAN = 1000 / INTERVAL_MILISEC // impact is measured within 1 sec frame
}
