package pl.birski.falldetector.data

import pl.birski.falldetector.model.Acceleration
import pl.birski.falldetector.model.AngularVelocity

data class SensorData(val acceleration: Acceleration, val velocity: AngularVelocity)
