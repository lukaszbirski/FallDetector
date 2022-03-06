package pl.birski.falldetector.data

import pl.birski.falldetector.other.Constants

class Stabilizer {

    val buffers: Buffers = Buffers(Constants.BUFFER_COUNT, Constants.N, 0, Double.NaN)

    var anteX: Double = Double.NaN
    var anteY: Double = Double.NaN
    var anteZ: Double = Double.NaN
    var anteTime: Long = 0
    var regular: Long = 0

    fun linearRecalculation(
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
