package pl.birski.falldetector.data

import android.util.Log
import pl.birski.falldetector.model.Acceleration
import pl.birski.falldetector.other.Constants

class Stabilizer {

    private val buffers: Buffers = Buffers(Constants.BUFFER_COUNT, Constants.N, 0, Double.NaN)

    private var nextAcceleration: Acceleration = Acceleration(Double.NaN, Double.NaN, Double.NaN, 0)
    private var regular: Long = 0

    fun stabilizeSignal(acceleration: Acceleration) {
        synchronized(buffers) {
            resample(acceleration = acceleration)
        }
        nextAcceleration = acceleration
    }

    // Android sampling is irregular, hence signal is (linearly) resampled at 50 Hz
    private fun resample(acceleration: Acceleration) {
        if (0L == nextAcceleration.timeStamp) {
            regular = acceleration.timeStamp + Constants.INTERVAL_MILISEC
            return
        }
        while (regular < acceleration.timeStamp) {
            val x = linearRecalculation(
                nextAcceleration.timeStamp,
                nextAcceleration.x,
                acceleration.timeStamp,
                acceleration.x,
                regular
            )
            val y = linearRecalculation(
                nextAcceleration.timeStamp,
                nextAcceleration.y,
                acceleration.timeStamp,
                acceleration.x,
                regular
            )
            val z = linearRecalculation(
                nextAcceleration.timeStamp,
                nextAcceleration.z,
                acceleration.timeStamp,
                acceleration.x,
                regular
            )

            Log.d("testuje", "resample: anteX ${nextAcceleration.x}")
            Log.d("testuje", "resample: postX ${acceleration.x}")
            Log.d("testuje", "resample: x $x")
            Log.d("testuje", "resample: --------------------------")
            // TODO sending signal should be here somewhere
            buffers.position = (buffers.position + 1) % Constants.N
            regular += Constants.INTERVAL_MILISEC
        }
    }

    private fun linearRecalculation(
        before: Long,
        ante: Double,
        after: Long,
        post: Double,
        now: Long
    ): Double {
        return ante + (post - ante) * (now - before).toDouble() / (after - before).toDouble()
    }

    inner class Buffers(count: Int, size: Int, var position: Int, value: Double)
}
