package pl.birski.falldetector.components

import pl.birski.falldetector.model.Acceleration
import pl.birski.falldetector.other.Constants

class Stabilizer {

    private var timeStamp: Long = 0

    private var currentAcceleration = Acceleration(0.0, 0.0, 0.0, timeStamp)
    private var resampledAcceleration = Acceleration(0.0, 0.0, 0.0, timeStamp)

    fun stabilizeSignal(
        previousAcc: Acceleration
    ): Acceleration {
        synchronized(Any()) {
            resample(previousAcc = previousAcc)
        }
        currentAcceleration = previousAcc

        return resampledAcceleration
    }

    // Android sampling is irregular, hence signal is (linearly) resampled at 50 Hz
    private fun resample(previousAcc: Acceleration) {
        if (0L == currentAcceleration.timeStamp) {
            timeStamp = previousAcc.timeStamp + Constants.INTERVAL_MILISEC
            return
        }
        while (timeStamp < previousAcc.timeStamp) {
            val accX = linearRecalculation(
                timeAfter = currentAcceleration.timeStamp,
                valueAfter = currentAcceleration.x,
                timePrevious = previousAcc.timeStamp,
                valuePrevious = previousAcc.x,
                currentTime = timeStamp
            )
            val accY = linearRecalculation(
                timeAfter = currentAcceleration.timeStamp,
                valueAfter = currentAcceleration.y,
                timePrevious = previousAcc.timeStamp,
                valuePrevious = previousAcc.y,
                currentTime = timeStamp
            )
            val accZ = linearRecalculation(
                timeAfter = currentAcceleration.timeStamp,
                valueAfter = currentAcceleration.z,
                timePrevious = previousAcc.timeStamp,
                valuePrevious = previousAcc.z,
                currentTime = timeStamp
            )

            resampledAcceleration = resampledAcceleration.copy(
                timeStamp = timeStamp,
                x = accX,
                y = accY,
                z = accZ
            )

            timeStamp += Constants.INTERVAL_MILISEC
        }
    }

    private fun linearRecalculation(
        timeAfter: Long,
        valueAfter: Double,
        timePrevious: Long,
        valuePrevious: Double,
        currentTime: Long
    ): Double {
        return valueAfter + (valuePrevious - valueAfter) * (currentTime - timeAfter).toDouble() / (timePrevious - timeAfter).toDouble()
    }
}
