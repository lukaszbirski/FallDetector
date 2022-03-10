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

    @Inject
    lateinit var stabilizer: Stabilizer

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        when (sensor?.type) {
            Sensor.TYPE_ACCELEROMETER ->
                Timber.d("Accuracy of the accelerometer is now equal to $accuracy")
            Sensor.TYPE_GYROSCOPE ->
                Timber.d("Accuracy of the gyroscope is now equal to $accuracy")
        }
    }

    override fun onSensorChanged(event: SensorEvent) {
        when (event.sensor.type) {
            Sensor.TYPE_ACCELEROMETER -> {
                val rawAcceleration = getAcceleration(event = event)
                val resampledAcceleration = stabilizer.stabilizeSignal(rawAcceleration)
                Timber.d("Current acceleration is equal to: $rawAcceleration")
                Timber.d("Resampled acceleration is equal to: $resampledAcceleration")
                acceleration.value = rawAcceleration
            }
            Sensor.TYPE_GYROSCOPE -> {
                val rawVelocity = getVelocity(event = event)
                Timber.d("Current angular velocity is equal to: $rawVelocity")
                angularVelocity.value = rawVelocity
            }
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
        (event.values[2].div(SensorManager.STANDARD_GRAVITY)).toDouble(),
        event.timestamp / 1000000
    )

    private fun getVelocity(event: SensorEvent) = AngularVelocity(
        event.values[0].toDouble(),
        event.values[1].toDouble(),
        event.values[2].toDouble(),
        event.timestamp / 1000000
    )
}
