package pl.birski.falldetector.components

import pl.birski.falldetector.model.Acceleration
import pl.birski.falldetector.other.Constants

class Stabilizer {

    private var timeStamp: Long = 0
    private var currentAcceleration = Acceleration()

    fun stabilizeSignal(
        previousAcc: Acceleration
    ): Acceleration {
        val resampledAcceleration = resample(previousAcc = previousAcc)
        currentAcceleration = previousAcc

        return resampledAcceleration
    }

    // Android sampling is irregular, hence signal is (linearly) resampled
    private fun resample(previousAcc: Acceleration): Acceleration {

        var acceleration = Acceleration()

        if (0L == currentAcceleration.timeStamp) {
            timeStamp = previousAcc.timeStamp + Constants.INTERVAL_MILISEC
            return previousAcc
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

            acceleration = Acceleration(accX, accY, accZ, timeStamp)

            timeStamp += Constants.INTERVAL_MILISEC
        }

        return acceleration
    }

    private fun linearRecalculation(
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
