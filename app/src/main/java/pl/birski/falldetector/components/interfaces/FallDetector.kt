package pl.birski.falldetector.components.interfaces

import pl.birski.falldetector.model.Acceleration

interface FallDetector {

    fun detectFall(acceleration: Acceleration)
}
