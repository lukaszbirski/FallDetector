package pl.birski.falldetector.data

import pl.birski.falldetector.model.Acceleration
import pl.birski.falldetector.other.Constants

class Stabilizer {

    private val buffers: Buffers = Buffers(Constants.BUFFER_COUNT, Constants.N, 0, Double.NaN)

    private var timeStamp: Long = 0
    private var currentAcc = Acceleration(Double.NaN, Double.NaN, Double.NaN, 0)
    private var resampledAcceleration = Acceleration(Double.NaN, Double.NaN, Double.NaN, timeStamp)

    fun stabilizeSignal(previousAcc: Acceleration): Acceleration {
        synchronized(buffers) {
            resample(previousAcc = previousAcc)
        }
        currentAcc = previousAcc
        return resampledAcceleration
    }

    // Android sampling is irregular, hence signal is (linearly) resampled at 50 Hz
    private fun resample(previousAcc: Acceleration) {
        if (0L == currentAcc.timeStamp) {
            timeStamp = previousAcc.timeStamp + Constants.INTERVAL_MILISEC
            return
        }
        while (timeStamp < previousAcc.timeStamp) {
            val x = linearRecalculation(
                timeAfter = currentAcc.timeStamp,
                valueAfter = currentAcc.x,
                timePrevious = previousAcc.timeStamp,
                valuePrevious = previousAcc.x,
                currentTime = timeStamp
            )
            val y = linearRecalculation(
                timeAfter = currentAcc.timeStamp,
                valueAfter = currentAcc.y,
                timePrevious = previousAcc.timeStamp,
                valuePrevious = previousAcc.y,
                currentTime = timeStamp
            )
            val z = linearRecalculation(
                timeAfter = currentAcc.timeStamp,
                valueAfter = currentAcc.z,
                timePrevious = previousAcc.timeStamp,
                valuePrevious = previousAcc.z,
                currentTime = timeStamp
            )

            resampledAcceleration = resampledAcceleration.copy(
                timeStamp = timeStamp,
                x = x,
                y = y,
                z = z
            )

            buffers.position = (buffers.position + 1) % Constants.N
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

    inner class Buffers(count: Int, size: Int, var position: Int, value: Double)
}
