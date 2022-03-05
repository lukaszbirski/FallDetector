package pl.birski.falldetector.data

import pl.birski.falldetector.model.Acceleration

class Normalizer {

    private var previousX = 0.0
    private var previousY = 0.0
    private var previousZ = 0.0

    fun normalize(acceleration: Acceleration): Acceleration {

        val normalized = Acceleration(
            acceleration.x - previousX,
            acceleration.y - previousY,
            acceleration.z - previousZ,
            acceleration.timeStamp
        )
        previousX = acceleration.x
        previousY = acceleration.y
        previousZ = acceleration.z
        return normalized
    }
}
