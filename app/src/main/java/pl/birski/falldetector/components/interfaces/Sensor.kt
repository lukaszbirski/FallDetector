package pl.birski.falldetector.components.interfaces

import android.content.Context
import android.hardware.SensorEvent
import androidx.compose.runtime.MutableState
import pl.birski.falldetector.model.Acceleration
import pl.birski.falldetector.model.AngularVelocity

interface Sensor {

    fun initiateSensor(context: Context)

    fun stopMeasurement()

    fun createAcceleration(event: SensorEvent): Acceleration

    fun createVelocity(event: SensorEvent): AngularVelocity

    fun getMutableAcceleration(): MutableState<Acceleration?>

    fun getMutableVelocity(): MutableState<AngularVelocity?>
}
