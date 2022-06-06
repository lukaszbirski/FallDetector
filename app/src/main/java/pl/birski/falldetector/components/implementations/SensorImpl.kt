package pl.birski.falldetector.components.implementations

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Handler
import android.os.Looper
import android.widget.Toast
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import javax.inject.Inject
import pl.birski.falldetector.R
import pl.birski.falldetector.components.interfaces.FallDetector
import pl.birski.falldetector.components.interfaces.Stabilizer
import pl.birski.falldetector.model.Acceleration
import pl.birski.falldetector.other.Constants
import timber.log.Timber

class SensorImpl @Inject constructor(
    private val fallDetector: FallDetector,
    private val stabilizer: Stabilizer
) : pl.birski.falldetector.components.interfaces.Sensor, SensorEventListener {

    private lateinit var mainHandler: Handler
    private lateinit var manager: SensorManager

    private val acceleration: MutableState<Acceleration?> = mutableStateOf(null)
    private var rawAcceleration = Acceleration()

    private val stabilize = object : Runnable {
        override fun run() {

            val resampledSignal = stabilizer.stabilizeSignal(rawAcceleration)
            Timber.d("Resampled signal is equal to: $resampledSignal")

            // Core of detecting fall is here
            fallDetector.detectFall(resampledSignal)

            mainHandler.postDelayed(this, Constants.INTERVAL_MILISEC.toLong())
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        when (sensor?.type) {
            Sensor.TYPE_ACCELEROMETER -> Timber.d("Accuracy  is equal to $accuracy")
        }
    }

    override fun onSensorChanged(event: SensorEvent) {
        when (event.sensor.type) {
            Sensor.TYPE_ACCELEROMETER -> {
                rawAcceleration = createAcceleration(event = event)
                acceleration.value = rawAcceleration
                Timber.d("Raw acceleration is equal to: $rawAcceleration")
            }
        }
    }

    override fun initiateSensor(context: Context) {
        manager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
        val sensor: Sensor? = manager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        sensor?.let {
            manager.registerListener(this, sensor, Constants.INTERVAL_MILISEC * 1000)
            runStabilizer()
        } ?: Toast.makeText(
            context,
            context.getText(R.string.accelerometer_not_supported_toast_text),
            Toast.LENGTH_LONG
        ).show()
    }

    override fun stopMeasurement() {
        manager.unregisterListener(this)
        stopStabilizer()
    }

    override fun getMutableAcceleration() = acceleration

    override fun createAcceleration(event: SensorEvent) = Acceleration(
        event.values[0].div(SensorManager.STANDARD_GRAVITY).toDouble(),
        event.values[1].div(SensorManager.STANDARD_GRAVITY).toDouble(),
        event.values[2].div(SensorManager.STANDARD_GRAVITY).toDouble(),
        event.timestamp / 1000000
    )

    private fun runStabilizer() {
        mainHandler = Handler(Looper.getMainLooper())
        mainHandler.post(stabilize)
    }

    private fun stopStabilizer() {
        mainHandler.removeCallbacks(stabilize)
    }
}
