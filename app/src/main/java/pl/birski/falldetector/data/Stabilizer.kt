package pl.birski.falldetector.data

import android.util.Log
import pl.birski.falldetector.other.Constants

class Stabilizer {

    val buffers: Buffers = Buffers(Constants.BUFFER_COUNT, Constants.N, 0, Double.NaN)

    var anteX: Double = Double.NaN
    var anteY: Double = Double.NaN
    var anteZ: Double = Double.NaN
    var anteTime: Long = 0
    var regular: Long = 0

    private fun linearRecalculation(
        before: Long,
        ante: Double,
        after: Long,
        post: Double,
        now: Long
    ): Double {
        return ante + (post - ante) * (now - before).toDouble() / (after - before).toDouble()
    }

    // Android sampling is irregular, thus the signal is (linearly) resampled at 50 Hz
    fun resample(postTime: Long, postX: Double, postY: Double, postZ: Double) {
        if (0L == anteTime) {
            regular = postTime + Constants.INTERVAL_MS
            return
        }
        while (regular < postTime) {
            val x = linearRecalculation(anteTime, anteX, postTime, postX, regular)
            val y = linearRecalculation(anteTime, anteY, postTime, postY, regular)
            val z = linearRecalculation(anteTime, anteZ, postTime, postZ, regular)

            Log.d("testuje", "resample: anteX $anteX")
            Log.d("testuje", "resample: postX $postX")
            Log.d("testuje", "resample: x $x")
            Log.d("testuje", "resample: --------------------------")
            // sending signal should be here somewhere
            buffers.position = (buffers.position + 1) % Constants.N
            regular += Constants.INTERVAL_MS
        }
    }

    inner class Buffers(count: Int, size: Int, var position: Int, value: Double)
}
