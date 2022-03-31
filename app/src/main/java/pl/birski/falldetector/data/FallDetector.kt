package pl.birski.falldetector.data

import android.content.Context
import android.content.Intent
import javax.inject.Inject
import kotlin.math.sqrt
import pl.birski.falldetector.model.Acceleration
import pl.birski.falldetector.model.SensorData
import pl.birski.falldetector.other.Constants
import pl.birski.falldetector.service.enum.DataSet
import timber.log.Timber

class FallDetector @Inject constructor(
    private val context: Context,
    private val filter: Filter
) {

    // signal frequency is 50Hz and cut-off frequency is 5 Hz
    private val ALPHA = filter.calculateAlpha(0.25, 50.0)
    private val G_CONST = 1.0

    private var lpfAcceleration = floatArrayOf(0.0f, 0.0f, 0.0f)
    private var hpfAcceleration = floatArrayOf(0.0f, 0.0f, 0.0f)
    private var gravity = floatArrayOf(0.0f, 0.0f, 0.0f)

    private val SV_TOT_FALLING = 0.6
    private val SV_TOT_THRESHOLD = 2.0

    private var slidingWindow: MutableList<Acceleration> = mutableListOf()

    fun detectFall(sensorData: SensorData) {
        addAccelerationValueToWindow(sensorData)
        measureFall(sensorData)
    }

    private fun addAccelerationValueToWindow(sensorData: SensorData) {
        if (slidingWindow.size >= Constants.SLIDING_WINDOW_SIZE) slidingWindow.removeAt(0)
        slidingWindow.add(sensorData.acceleration)
    }

    private fun measureFall(sensorData: SensorData) {

        val accelerationFloatArray = floatArrayOf(
            sensorData.acceleration.x.toFloat(),
            sensorData.acceleration.y.toFloat(),
            sensorData.acceleration.z.toFloat()
        )

        lpfAcceleration = filter.lowPassFilter(accelerationFloatArray, lpfAcceleration, ALPHA)

        val lpfAcceleration = getAcceleration(sensorData.acceleration, lpfAcceleration)
        val svTOT = calculateSumVector(lpfAcceleration.x, lpfAcceleration.y, lpfAcceleration.z)

        val hpfResult =
            filter.highPassFilter(accelerationFloatArray, hpfAcceleration, gravity, ALPHA)

        gravity = hpfResult[0]
        hpfAcceleration = hpfResult[1]

        val hpfAcceleration = getAcceleration(sensorData.acceleration, hpfAcceleration)
        val svD = calculateSumVector(hpfAcceleration.x, hpfAcceleration.y, hpfAcceleration.z)
        val z2 = calculateVerticalAcceleration(svTOT = svTOT, svD = svD)

        val xMinMax = getMaxValue(DataSet.X_AXIS) - getMinValue(DataSet.X_AXIS)
        val yMinMax = getMaxValue(DataSet.Y_AXIS) - getMinValue(DataSet.Y_AXIS)
        val zMinMax = getMaxValue(DataSet.Z_AXIS) - getMinValue(DataSet.Z_AXIS)
        val svMinMax = calculateSumVector(xMinMax, yMinMax, zMinMax)

        Timber.d("data SV total: $svTOT")
        Timber.d("data SV dynamic: $svD")
        Timber.d("data  Z2 : $z2")
        Timber.d("data svMinMax : $svMinMax")
        Timber.d("data ----------------------------------------")
    }
//
//    private fun isSumVectorGreaterThanCSThreshold(acceleration: Acceleration) {
//        val calculatedSV = calculateSumVector(acceleration.x, acceleration.y, acceleration.z)
//        if (calculatedSV > CSV_THRESHOLD) {
//            Timber.d("SV value [$calculatedSV] is grater that CSV threshold!")
//            sendBroadcast()
//            // isCurrentSumVectorEqualToMaxSumVector(calculatedSV)
//        } else {
//            // Fall not detected
//            return
//        }
//    }
//
//    private fun isCurrentSumVectorEqualToMaxSumVector(calculatedSV: Double) {
//        if (getSumVectorMaxValue() == calculatedSV) {
//            Timber.d("SV value [$calculatedSV] is current SW max!")
//            // TODO
//        } else {
//            // Fall not detected
//            return
//        }
//    }

    private fun calculateSumVector(x: Double, y: Double, z: Double) =
        sqrt(x * x + y * y + z * z)

    private fun calculateVerticalAcceleration(svTOT: Double, svD: Double) =
        (svTOT * svTOT - svD * svD - G_CONST * G_CONST) / (2.0 * G_CONST)

    private fun getMaxValue(dataSet: DataSet): Double {
        return when (dataSet) {
            DataSet.X_AXIS -> slidingWindow.map { it.x }.maxOf { it }
            DataSet.Y_AXIS -> slidingWindow.map { it.y }.maxOf { it }
            DataSet.Z_AXIS -> slidingWindow.map { it.z }.maxOf { it }
        }
    }

    private fun getMinValue(dataSet: DataSet): Double {
        return when (dataSet) {
            DataSet.X_AXIS -> slidingWindow.map { it.x }.minOf { it }
            DataSet.Y_AXIS -> slidingWindow.map { it.y }.minOf { it }
            DataSet.Z_AXIS -> slidingWindow.map { it.z }.minOf { it }
        }
    }

    private fun getAcceleration(rawAcceleration: Acceleration, acceleration: FloatArray) =
        Acceleration(
            acceleration[0].div(1).toDouble(),
            acceleration[1].div(1).toDouble(),
            acceleration[2].div(1).toDouble(),
            rawAcceleration.timeStamp
        )

    private fun sendBroadcast() = Intent(Constants.CUSTOM_FALL_DETECTED_RECEIVER).also {
        context.sendBroadcast(it)
    }
}
