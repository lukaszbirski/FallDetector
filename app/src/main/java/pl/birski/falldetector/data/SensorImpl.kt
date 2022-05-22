package pl.birski.falldetector.data

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.widget.Toast
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import javax.inject.Inject
import pl.birski.falldetector.R
import pl.birski.falldetector.model.Acceleration
import pl.birski.falldetector.model.AngularVelocity
import pl.birski.falldetector.other.Constants
import pl.birski.falldetector.other.PrefUtil
import timber.log.Timber

class SensorImpl @Inject constructor(
    private val fallDetector: FallDetector,
    private val stabilizer: Stabilizer,
    private val prefUtil: PrefUtil
) : pl.birski.falldetector.data.Sensor, SensorEventListener {

    private lateinit var manager: SensorManager

    private val acceleration: MutableState<Acceleration?> = mutableStateOf(null)
    private val angularVelocity: MutableState<AngularVelocity?> = mutableStateOf(null)

    private var rawAcceleration = Acceleration(0.0, 0.0, 0.0, 0)
    private var rawVelocity = AngularVelocity(0.0, 0.0, 0.0, 0)

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

                rawAcceleration = createAcceleration(event = event)
                acceleration.value = rawAcceleration
                Timber.d("Raw acceleration is equal to: $rawAcceleration")

                val resampledSignal = stabilizer.stabilizeSignal(rawAcceleration, rawVelocity)
                Timber.d("Resampled signal is equal to: $resampledSignal")

                // Core of detecting fall is here
                fallDetector.detectFall(resampledSignal)
            }
            Sensor.TYPE_GYROSCOPE -> {
                rawVelocity = createVelocity(event = event)
                angularVelocity.value = rawVelocity
                Timber.d("Current angular velocity is equal to: $rawVelocity")
            }
        }
    }

    override fun initiateSensor(context: Context) {
        manager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
        val sensor: Sensor? = manager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        val gyroscope: Sensor? = manager.getDefaultSensor(Sensor.TYPE_GYROSCOPE)
        sensor?.let {
            manager.registerListener(this, sensor, Constants.INTERVAL_MILISEC * 1000)
        } ?: Toast.makeText(
            context,
            context.getText(R.string.accelerometer_not_supported_toast_text),
            Toast.LENGTH_LONG
        ).show()
        gyroscope.takeIf { prefUtil.isGyroscopeEnabled() }?.let {
            manager.registerListener(this, gyroscope, Constants.INTERVAL_MILISEC * 1000)
        } ?: Toast.makeText(
            context,
            context.getText(R.string.gyroscope_not_supported_toast_text),
            Toast.LENGTH_LONG
        ).show()
    }

    override fun stopMeasurement() {
        manager.unregisterListener(this).also {
            resetAngularVelocity()
        }
    }

    override fun getMutableAcceleration() = acceleration

    override fun getMutableVelocity() = angularVelocity

    override fun createAcceleration(event: SensorEvent) = Acceleration(
        event.values[0].div(SensorManager.STANDARD_GRAVITY).toDouble(),
        event.values[1].div(SensorManager.STANDARD_GRAVITY).toDouble(),
        event.values[2].div(SensorManager.STANDARD_GRAVITY).toDouble(),
        event.timestamp / 1000000
    )

    override fun createVelocity(event: SensorEvent) = AngularVelocity(
        event.values[0].toDouble(),
        event.values[1].toDouble(),
        event.values[2].toDouble(),
        event.timestamp / 1000000
    )

    private fun resetAngularVelocity() {
        angularVelocity.takeIf { !prefUtil.isGyroscopeEnabled() }
            ?.let { it.value = null }
    }
}
