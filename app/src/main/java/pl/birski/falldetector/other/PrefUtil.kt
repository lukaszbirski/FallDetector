package pl.birski.falldetector.other

import pl.birski.falldetector.service.enum.Algorithms

interface PrefUtil {

    fun getTimerLength(): Long

    fun getDetectionAlgorithm(): Algorithms

    fun isSendingMessageAllowed(): Boolean
}
