package pl.birski.falldetector.data

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import javax.inject.Inject
import pl.birski.falldetector.model.Acceleration
import pl.birski.falldetector.model.AngularVelocity
import timber.log.Timber

class Sensor @Inject constructor() : SensorEventListener {

    private lateinit var manager: SensorManager

    val acceleration: MutableState<Acceleration?> = mutableStateOf(null)
    val angularVelocity: MutableState<AngularVelocity?> = mutableStateOf(null)

    override fun onSensorChanged(event: SensorEvent) {
        when (event.sensor.type) {
            Sensor.TYPE_ACCELEROMETER -> {
                Timber.d(
                    "Current acceleration is equal to " +
                        "x: ${event?.values?.get(0)}, " +
                        "y: ${event?.values?.get(1)}, " +
                        "z: ${event?.values?.get(2)}"
                )
                acceleration.value = event?.let { getAcceleration(it) }
            }
            Sensor.TYPE_GYROSCOPE -> {
                Timber.d(
                    "Current angular velocity is equal to " +
                        "x: ${event?.values?.get(0)}, " +
                        "y: ${event?.values?.get(1)}, " +
                        "z: ${event?.values?.get(2)}"
                )
                angularVelocity.value = event?.let { getVelocity(it) }
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        if (Sensor.TYPE_ACCELEROMETER == sensor?.type) {
            Timber.d("Accuracy of the accelerometer is now equal to $accuracy")
        }
    }

    fun initiateSensor(context: Context) {
        manager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
        val sensor: Sensor? = manager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        val gyroscope: Sensor? = manager.getDefaultSensor(Sensor.TYPE_GYROSCOPE)
        sensor?.let {
            manager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_NORMAL)
        }
        gyroscope?.let {
            manager.registerListener(this, gyroscope, SensorManager.SENSOR_DELAY_NORMAL)
        }
    }

    fun stopMeasurement() {
        manager.unregisterListener(this)
    }

    private fun getAcceleration(event: SensorEvent) = Acceleration(
        (event.values[0].div(SensorManager.STANDARD_GRAVITY)).toDouble(),
        (event.values[1].div(SensorManager.STANDARD_GRAVITY)).toDouble(),
        (event.values[2].div(SensorManager.STANDARD_GRAVITY)).toDouble()
    )

    private fun getVelocity(event: SensorEvent) = AngularVelocity(
        event.values[0].toDouble(),
        event.values[1].toDouble(),
        event.values[2].toDouble()
    )
}
