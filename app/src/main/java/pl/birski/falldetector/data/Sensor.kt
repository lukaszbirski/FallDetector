package pl.birski.falldetector.data

import android.content.Context
import android.hardware.SensorEvent
import pl.birski.falldetector.model.Acceleration
import pl.birski.falldetector.model.AngularVelocity

interface Sensor {

    fun initiateSensor(context: Context)

    fun getAcceleration(event: SensorEvent): Acceleration

    fun getVelocity(event: SensorEvent): AngularVelocity
}
