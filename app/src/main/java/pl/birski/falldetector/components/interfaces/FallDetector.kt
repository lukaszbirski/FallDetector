package pl.birski.falldetector.components.interfaces

import pl.birski.falldetector.model.SensorData

interface FallDetector {

    fun detectFall(sensorData: SensorData)
}
