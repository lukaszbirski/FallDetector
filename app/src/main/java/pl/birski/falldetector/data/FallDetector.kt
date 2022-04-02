package pl.birski.falldetector.data

import android.content.Context
import android.content.Intent
import javax.inject.Inject
import kotlin.math.sqrt
import pl.birski.falldetector.model.Acceleration
import pl.birski.falldetector.model.HighPassFilterData
import pl.birski.falldetector.model.SensorData
import pl.birski.falldetector.other.Constants
import pl.birski.falldetector.service.enum.DataSet
import timber.log.Timber

class FallDetector @Inject constructor(
    private val context: Context,
    private val filter: Filter
) {

    // signal frequency is 50Hz and cut-off frequency is 0.25 Hz
    private val ALPHA = filter.calculateAlpha(0.25, 50.0)

    private val INTERVAL_MILISEC = 20 // frequency is set to 50 Hz
    private val SLIDING_WINDOW_TIME_SEC = 0.1F // SW size is 2 sec
    private val SLIDING_WINDOW_SIZE = SLIDING_WINDOW_TIME_SEC * 1000 / INTERVAL_MILISEC

    private val IMPACT_TIME_SPAN = 2000 / INTERVAL_MILISEC // after impact need to wait 2 sec

    private val G_CONST = 1.0
    private val SV_TOT_THRESHOLD = 2.0
    private val SV_D_THRESHOLD = 1.7
    private val SV_MAX_MIN_THRESHOLD = 2.0
    private val VERTICAL_ACC_THRESHOLD = 1.5

    private var lpfData = floatArrayOf(0.0f, 0.0f, 0.0f)
    private var hpfData = HighPassFilterData(
        floatArrayOf(0.0f, 0.0f, 0.0f),
        floatArrayOf(0.0f, 0.0f, 0.0f)
    )

    private var slidingWindow: MutableList<Acceleration> = mutableListOf()
    private var impactTimeOut: Int = -1

    fun detectFall(sensorData: SensorData) {
        addAccelerationValueToWindow(sensorData)
        measureFall(sensorData)
    }

    private fun addAccelerationValueToWindow(sensorData: SensorData) {
        if (slidingWindow.size >= SLIDING_WINDOW_SIZE) slidingWindow.removeAt(0)
        slidingWindow.add(sensorData.acceleration)
    }

    private fun measureFall(sensorData: SensorData) {

        val accelerationFloatArray = floatArrayOf(
            sensorData.acceleration.x.toFloat(),
            sensorData.acceleration.y.toFloat(),
            sensorData.acceleration.z.toFloat()
        )

        lpfData = filter.lowPassFilter(accelerationFloatArray, lpfData, ALPHA)
        hpfData = filter.highPassFilter(accelerationFloatArray, hpfData, ALPHA)

        val lpfAcceleration = getAcceleration(sensorData.acceleration, lpfData)
        val hpfAcceleration = getAcceleration(sensorData.acceleration, hpfData.acceleration)

        useImpactPostureAlgorithm(lpfAcceleration, hpfAcceleration)
    }

    private fun detectPosture(lpfAcceleration: Acceleration) {

        // posture must be detected 2 sec after the impact
        // it is marked as impactTimeOut == 0
        if (impactTimeOut == 0) {
            // The posture was detected 2 s
            // after the impact from the LP filtered vertical signal, based on the
            // average acceleration in a 0.4 s time interval, with a signal value of
            // 0.5g or lower considered to be a lying posture.
        }
    }

    private fun useImpactPostureAlgorithm(
        lpfAcceleration: Acceleration,
        hpfAcceleration: Acceleration
    ) {
        // first use detectImpact()
        // if impact was observed wait 2 sec
        // detect posture
        impactTimeOut = expireTimeOut(impactTimeOut)
        detectImpact(lpfAcceleration, hpfAcceleration)
        detectPosture(lpfAcceleration)
    }

    private fun detectImpact(lpfAcceleration: Acceleration, hpfAcceleration: Acceleration) {
        val svTotal = calculateSumVector(lpfAcceleration.x, lpfAcceleration.y, lpfAcceleration.z)
        val svDynamic = calculateSumVector(hpfAcceleration.x, hpfAcceleration.y, hpfAcceleration.z)

        if (isMinMaxSumVectorGreaterThanThreshold() ||
            isVerticalAccelerationGreaterThanThreshold(svTotal, svDynamic) ||
            isSumVectorGreaterThanThreshold(svTotal, SV_TOT_THRESHOLD) ||
            isSumVectorGreaterThanThreshold(svDynamic, SV_D_THRESHOLD)
        ) {
            Timber.d("Impact was detected!")
            // impact was detected, set impact time out
            impactTimeOut = IMPACT_TIME_SPAN
        }
    }

    private fun expireTimeOut(timeOut: Int) = if (timeOut > -1) timeOut - 1 else -1

    private fun isSumVectorGreaterThanThreshold(sumVector: Double, threshold: Double) =
        sumVector > threshold

    private fun isMinMaxSumVectorGreaterThanThreshold(): Boolean {
        val xMinMax = getMaxValue(DataSet.X_AXIS) - getMinValue(DataSet.X_AXIS)
        val yMinMax = getMaxValue(DataSet.Y_AXIS) - getMinValue(DataSet.Y_AXIS)
        val zMinMax = getMaxValue(DataSet.Z_AXIS) - getMinValue(DataSet.Z_AXIS)
        return calculateSumVector(xMinMax, yMinMax, zMinMax) > SV_MAX_MIN_THRESHOLD
    }

    private fun isVerticalAccelerationGreaterThanThreshold(
        svTotal: Double,
        svDynamic: Double
    ) = calculateVerticalAcceleration(svTotal, svDynamic) > VERTICAL_ACC_THRESHOLD

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
