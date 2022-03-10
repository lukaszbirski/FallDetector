package pl.birski.falldetector.data

import android.util.Log
import pl.birski.falldetector.model.Acceleration
import pl.birski.falldetector.other.Constants

class Stabilizer {

    private val buffers: Buffers = Buffers(Constants.BUFFER_COUNT, Constants.N, 0, Double.NaN)

    private var currentAcc: Acceleration = Acceleration(Double.NaN, Double.NaN, Double.NaN, 0)
    private var regular: Long = 0

    fun stabilizeSignal(previousAcc: Acceleration) {
        synchronized(buffers) {
            resample(previousAcc = previousAcc)
        }
        currentAcc = previousAcc
    }

    // Android sampling is irregular, hence signal is (linearly) resampled at 50 Hz
    private fun resample(previousAcc: Acceleration) {
        if (0L == currentAcc.timeStamp) {
            regular = previousAcc.timeStamp + Constants.INTERVAL_MILISEC
            return
        }
        while (regular < previousAcc.timeStamp) {
            val x = linearRecalculation(
                currentAcc.timeStamp,
                currentAcc.x,
                previousAcc.timeStamp,
                previousAcc.x,
                regular
            )
            val y = linearRecalculation(
                currentAcc.timeStamp,
                currentAcc.y,
                previousAcc.timeStamp,
                previousAcc.y,
                regular
            )
            val z = linearRecalculation(
                currentAcc.timeStamp,
                currentAcc.z,
                previousAcc.timeStamp,
                previousAcc.z,
                regular
            )

            Log.d("testuje", "resample: anteX ${currentAcc.x}")
            Log.d("testuje", "resample: postX ${previousAcc.x}")
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
