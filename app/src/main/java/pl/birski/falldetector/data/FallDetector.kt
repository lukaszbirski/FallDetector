package pl.birski.falldetector.data

import pl.birski.falldetector.model.SensorData

interface FallDetector {

    fun detectFall(sensorData: SensorData)
}
