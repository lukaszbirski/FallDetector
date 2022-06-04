package pl.birski.falldetector.components

import pl.birski.falldetector.model.Acceleration
import pl.birski.falldetector.model.AngularVelocity
import pl.birski.falldetector.model.SensorData
import pl.birski.falldetector.other.Constants

class Stabilizer {

    private var timeStamp: Long = 0

    private var currentAcceleration = Acceleration(0.0, 0.0, 0.0, timeStamp)
    private var resampledAcceleration = Acceleration(0.0, 0.0, 0.0, timeStamp)

    private var currentVelocity = AngularVelocity(0.0, 0.0, 0.0, 0)
    private var resampledVelocity = AngularVelocity(0.0, 0.0, 0.0, timeStamp)

    fun stabilizeSignal(
        previousAcc: Acceleration,
        previousVelocity: AngularVelocity
    ): SensorData {
        synchronized(Any()) {
            resample(previousAcc = previousAcc, previousVelocity = previousVelocity)
        }
        currentAcceleration = previousAcc
        currentVelocity = previousVelocity

        return SensorData(acceleration = resampledAcceleration, velocity = resampledVelocity)
    }

    // Android sampling is irregular, hence signal is (linearly) resampled at 50 Hz
    private fun resample(previousAcc: Acceleration, previousVelocity: AngularVelocity) {
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
            val velX = linearRecalculation(
                timeAfter = currentVelocity.timeStamp,
                valueAfter = currentVelocity.x,
                timePrevious = previousVelocity.timeStamp,
                valuePrevious = previousVelocity.x,
                currentTime = timeStamp
            )
            val velY = linearRecalculation(
                timeAfter = currentVelocity.timeStamp,
                valueAfter = currentVelocity.y,
                timePrevious = previousVelocity.timeStamp,
                valuePrevious = previousVelocity.y,
                currentTime = timeStamp
            )
            val velZ = linearRecalculation(
                timeAfter = currentVelocity.timeStamp,
                valueAfter = currentVelocity.z,
                timePrevious = previousVelocity.timeStamp,
                valuePrevious = previousVelocity.z,
                currentTime = timeStamp
            )

            resampledAcceleration = resampledAcceleration.copy(
                timeStamp = timeStamp,
                x = accX,
                y = accY,
                z = accZ
            )
            resampledVelocity = resampledVelocity.copy(
                timeStamp = timeStamp,
                x = velX,
                y = velY,
                z = velZ
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
