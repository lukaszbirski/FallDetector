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
import pl.birski.falldetector.other.Constants
import timber.log.Timber

class Accelerometer @Inject constructor() : SensorEventListener {

    private lateinit var manager: SensorManager

    val acceleration: MutableState<Acceleration?> = mutableStateOf(null)

    @Inject
    lateinit var stabilizer: Stabilizer

    val buffers: Buffers = Buffers(Constants.BUFFER_COUNT, Constants.N, 0, Double.NaN)

    private var anteX: Double = Double.NaN
    private var anteY: Double = Double.NaN
    private var anteZ: Double = Double.NaN
    private var anteTime: Long = 0
    private var regular: Long = 0

    // Android sampling is irregular, thus the signal is (linearly) resampled at 50 Hz
    private fun resample(postTime: Long, postX: Double, postY: Double, postZ: Double) {
        if (0L == anteTime) {
            regular = postTime + Constants.INTERVAL_MS
            return
        }
        while (regular < postTime) {
            val x = stabilizer.linearRecalculation(anteTime, anteX, postTime, postX, regular)
            val y = stabilizer.linearRecalculation(anteTime, anteY, postTime, postY, regular)
            val z = stabilizer.linearRecalculation(anteTime, anteZ, postTime, postZ, regular)
            android.util.Log.d("testuje", "resample: x: $x, y: $y, z: $z")
            // sending signal should be here somewhere
            buffers.position = (buffers.position + 1) % Constants.N
            regular += Constants.INTERVAL_MS
        }
    }

    private fun protect(postTime: Long, postX: Double, postY: Double, postZ: Double) {
        synchronized(buffers) {
            resample(postTime, postX, postY, postZ)
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        if (Sensor.TYPE_ACCELEROMETER == sensor?.type) {
            Timber.d("Accuracy of the accelerometer is now equal to $accuracy")
        }
    }

    fun initiateSensor(context: Context) {
        manager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
        val accelerometer: Sensor = manager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        manager.registerListener(this, accelerometer, Constants.INTERVAL_MS * 1000)
    }

    override fun onSensorChanged(event: SensorEvent) {
        if (Sensor.TYPE_ACCELEROMETER == event.sensor.type) {
            val postTime: Long = event.timestamp / 1000000
            val postX = event.values[0].toDouble() / SensorManager.STANDARD_GRAVITY
            val postY = event.values[1].toDouble() / SensorManager.STANDARD_GRAVITY
            val postZ = event.values[2].toDouble() / SensorManager.STANDARD_GRAVITY
            protect(postTime, postX, postY, postZ)
            anteTime = postTime
            anteX = postX
            anteY = postY
            anteZ = postZ
            android.util.Log.d("testuje", "onSensorChanged: x: $postX, y: $postY")
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
}
