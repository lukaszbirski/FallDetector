package pl.birski.falldetector.data

class Stabilizer {

    fun linearRecalculation(before: Long, ante: Double, after: Long, post: Double, now: Long): Double {
        return ante + (post - ante) * (now - before).toDouble() / (after - before).toDouble()
    }
}
