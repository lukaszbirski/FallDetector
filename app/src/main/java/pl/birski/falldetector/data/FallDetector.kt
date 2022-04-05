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

    private var minMaxSW: MutableList<Acceleration> = mutableListOf()
    private var postureDetectionSW: MutableList<Acceleration> = mutableListOf()
    private var impactTimeOut: Int = -1

    fun detectFall(sensorData: SensorData) {
        measureFall(sensorData)
    }

    private fun addAccelerationToWindow(
        acceleration: Acceleration,
        windowSize: Int,
        window: MutableList<Acceleration>
    ) {
        if (window.size >= windowSize) window.removeAt(0)
        window.add(acceleration)
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

        addAccelerationToWindow(
            acceleration = sensorData.acceleration,
            windowSize = Constants.MIN_MAX_SW_SIZE.toInt(),
            window = minMaxSW
        )

        addAccelerationToWindow(
            acceleration = lpfAcceleration,
            windowSize = Constants.POSTURE_DETECTION_SW_SIZE.toInt(),
            window = postureDetectionSW
        )

        if (postureDetectionSW.size >= Constants.POSTURE_DETECTION_SW_SIZE)
            useImpactPostureAlgorithm(
                hpfAcceleration = hpfAcceleration,
                acceleration = sensorData.acceleration
            )
    }

    private fun detectPosture() {

        // posture must be detected 2 sec after the impact
        // it is marked as impactTimeOut == 0
        if (impactTimeOut == 0) {
            sendBroadcast()
            // The posture was detected 2 s
            // after the impact from the LP filtered vertical signal, based on the
            // average acceleration in a 0.4 s time interval, with a signal value of
            // 0.5g or lower considered to be a lying posture.
        }
    }

    private fun useImpactPostureAlgorithm(
        hpfAcceleration: Acceleration,
        acceleration: Acceleration
    ) {
        // first use detectImpact()
        // if impact was observed wait 2 sec
        // detect posture
        impactTimeOut = expireTimeOut(impactTimeOut)
        detectImpact(hpfAcceleration = hpfAcceleration, acceleration = acceleration)
        detectPosture()
    }

    private fun detectImpact(
        hpfAcceleration: Acceleration,
        acceleration: Acceleration
    ) {
        val svTotal = calculateSumVector(acceleration.x, acceleration.y, acceleration.z)
        val svDynamic = calculateSumVector(hpfAcceleration.x, hpfAcceleration.y, hpfAcceleration.z)

        if (isMinMaxSumVectorGreaterThanThreshold() ||
            isVerticalAccelerationGreaterThanThreshold(svTotal, svDynamic) ||
            isSumVectorGreaterThanThreshold(svTotal, SV_TOT_THRESHOLD) ||
            isSumVectorGreaterThanThreshold(svDynamic, SV_D_THRESHOLD)
        ) {
            Timber.d("Impact was detected!")
            // impact was detected, set impact time out
            impactTimeOut = Constants.IMPACT_TIME_SPAN
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
            DataSet.X_AXIS -> minMaxSW.map { it.x }.maxOf { it }
            DataSet.Y_AXIS -> minMaxSW.map { it.y }.maxOf { it }
            DataSet.Z_AXIS -> minMaxSW.map { it.z }.maxOf { it }
        }
    }

    private fun getMinValue(dataSet: DataSet): Double {
        return when (dataSet) {
            DataSet.X_AXIS -> minMaxSW.map { it.x }.minOf { it }
            DataSet.Y_AXIS -> minMaxSW.map { it.y }.minOf { it }
            DataSet.Z_AXIS -> minMaxSW.map { it.z }.minOf { it }
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
