package pl.birski.falldetector.data

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import timber.log.Timber
import javax.inject.Inject

class FallDetector @Inject constructor() : SensorEventListener {

    private lateinit var manager: SensorManager

    override fun onSensorChanged(event: SensorEvent?) {
        Timber.d("Current acceleration is equal to x: ${event?.values?.get(0)}, y: ${event?.values?.get(1)}, z: ${event?.values?.get(2)}")
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        if (Sensor.TYPE_ACCELEROMETER == sensor?.type) {
            Timber.d("Accuracy of the accelerometer is now equal to $accuracy")
        }
    }

    fun initiateSensor(context: Context) {
        manager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
        val accelerometer: Sensor = manager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        manager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL)
    }

    fun stopMeasurement() {
        manager.unregisterListener(this)
    }

}