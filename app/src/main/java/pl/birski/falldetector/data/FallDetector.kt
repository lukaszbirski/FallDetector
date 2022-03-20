package pl.birski.falldetector.data

import android.content.Context
import android.widget.Toast
import javax.inject.Inject
import kotlin.math.abs
import pl.birski.falldetector.model.Acceleration
import pl.birski.falldetector.model.SensorData
import pl.birski.falldetector.other.Constants
import timber.log.Timber

class FallDetector @Inject constructor(
    private val context: Context
) {

    private val CSV_THRESHOLD = 23.0
    private val CAV_THRESHOLD = 18.0
    private val CCA_THRESHOLD = 65.5

    private var slidingWindow: MutableList<Acceleration> = mutableListOf()

    fun detectFall(sensorData: SensorData) {
        addAccelerationValueToWindow(sensorData)
        measureFall()
    }

    private fun addAccelerationValueToWindow(sensorData: SensorData) {
        if (slidingWindow.size >= Constants.SLIDING_WINDOW_SIZE) slidingWindow.removeAt(0)
        slidingWindow.add(sensorData.acceleration)
    }

    private fun measureFall() {
        // measurement starts when sliding window is full and middle value is being checked
        if (slidingWindow.size >= Constants.SLIDING_WINDOW_SIZE) {
            slidingWindow.getOrNull(getWindowMiddleValueIndex())?.let {
                isSumVectorGreaterThanCSThreshold(it)
            }
        }
    }

    private fun isSumVectorGreaterThanCSThreshold(acceleration: Acceleration) {
        val calculatedSV = calculateSumVector(acceleration.x, acceleration.y, acceleration.z)
        if (calculatedSV > CSV_THRESHOLD) {
            Timber.d("SV value [$calculatedSV] is grater that CSV threshold!")
            isCurrentSumVectorEqualToMaxSumVector(calculatedSV)
        } else {
            // Fall not detected
            return
        }
    }

    private fun isCurrentSumVectorEqualToMaxSumVector(calculatedSV: Double) {
        if (getSumVectorMaxValue() == calculatedSV) {
            Timber.d("SV value [$calculatedSV] is current SW max!")
            // TODO
            showToast()
        } else {
            // Fall not detected
            return
        }
    }

    private fun calculateSumVector(x: Double, y: Double, z: Double) =
        performEuclidianNormalization(x, y, z)

    private fun getSumVectorMaxValue(): Double {
        return slidingWindow.map { calculateSumVector(it.x, it.y, it.z) }.maxOf { it }
    }

    private fun performEuclidianNormalization(x: Double, y: Double, z: Double) =
        abs(x) + abs(y) + abs(z)

    private fun showToast() {
        Toast.makeText(context, "FALL DETECTED!", Toast.LENGTH_LONG).show()
    }

    private fun getWindowMiddleValueIndex() = slidingWindow.size / 2
}
