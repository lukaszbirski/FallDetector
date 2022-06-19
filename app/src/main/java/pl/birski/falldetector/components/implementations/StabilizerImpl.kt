package pl.birski.falldetector.components.implementations

import pl.birski.falldetector.components.interfaces.Stabilizer
import pl.birski.falldetector.model.Acceleration
import pl.birski.falldetector.other.Constants

class StabilizerImpl : Stabilizer {

    private var timeStamp: Long = 0
    private var previousAcceleration = Acceleration()
    internal var resampledAcceleration = Acceleration()

    override fun stabilizeSignal(
        currentAcceleration: Acceleration
    ): Acceleration {
        resample(currentAcceleration, previousAcceleration)
        previousAcceleration = currentAcceleration

        return resampledAcceleration
    }

    // Android sampling is irregular, hence signal is (linearly) resampled
    internal fun resample(
        currentAcceleration: Acceleration,
        previousAcceleration: Acceleration
    ) {
        if (0L == previousAcceleration.timeStamp) {
            timeStamp = currentAcceleration.timeStamp + Constants.INTERVAL_MILISEC
            return
        }
        while (timeStamp < currentAcceleration.timeStamp) {
            val accX = linearRecalculation(
                timeAfter = previousAcceleration.timeStamp,
                valueAfter = previousAcceleration.x,
                timePrevious = currentAcceleration.timeStamp,
                valuePrevious = currentAcceleration.x,
                currentTime = timeStamp
            )
            val accY = linearRecalculation(
                timeAfter = previousAcceleration.timeStamp,
                valueAfter = previousAcceleration.y,
                timePrevious = currentAcceleration.timeStamp,
                valuePrevious = currentAcceleration.y,
                currentTime = timeStamp
            )
            val accZ = linearRecalculation(
                timeAfter = previousAcceleration.timeStamp,
                valueAfter = previousAcceleration.z,
                timePrevious = currentAcceleration.timeStamp,
                valuePrevious = currentAcceleration.z,
                currentTime = timeStamp
            )

            resampledAcceleration = Acceleration(accX, accY, accZ, timeStamp)

            timeStamp += Constants.INTERVAL_MILISEC
        }
    }

    internal fun linearRecalculation(
        timeAfter: Long,
        valueAfter: Double,
        timePrevious: Long,
        valuePrevious: Double,
        currentTime: Long
    ): Double {
        val currentAfterTime = (currentTime - timeAfter).toDouble()
        val previousAfterTime = (timePrevious - timeAfter).toDouble()
        return valueAfter + (valuePrevious - valueAfter) * currentAfterTime / previousAfterTime
    }
}
