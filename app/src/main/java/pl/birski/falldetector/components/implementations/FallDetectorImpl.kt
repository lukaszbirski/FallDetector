package pl.birski.falldetector.components.implementations

import android.content.Context
import android.content.Intent
import android.hardware.SensorManager
import javax.inject.Inject
import kotlin.math.sqrt
import pl.birski.falldetector.components.interfaces.FallDetector
import pl.birski.falldetector.components.interfaces.Filter
import pl.birski.falldetector.model.Acceleration
import pl.birski.falldetector.model.HighPassFilterData
import pl.birski.falldetector.other.Constants
import pl.birski.falldetector.other.PrefUtil
import pl.birski.falldetector.service.enum.Algorithms
import pl.birski.falldetector.service.enum.DataSet
import timber.log.Timber

class FallDetectorImpl @Inject constructor(
    private val context: Context,
    private val filter: Filter,
    private val prefUtil: PrefUtil
) : FallDetector {

    private val ALPHA = filter.calculateAlpha(
        Constants.FREQUENCY_CUT_OFF,
        Constants.FREQUENCY_MEASUREMENT
    )

    private val G_CONST = 1.0
    private val SV_TOT_THRESHOLD = 2.0
    private val SV_D_THRESHOLD = 1.7
    private val SV_MAX_MIN_THRESHOLD = 2.0
    private val VERTICAL_ACC_THRESHOLD = 1.5
    private val LYING_POSTURE_VERTICAL_THRESHOLD = 0.5
    private val SV_TOTAL_FALLING_THRESHOLD = 0.6
    private val VELOCITY_THRESHOLD = 0.6

    private var lowPassFilterData = floatArrayOf(0.0f, 0.0f, 0.0f)
    private var highPassFilterData = HighPassFilterData(
        floatArrayOf(0.0f, 0.0f, 0.0f),
        floatArrayOf(0.0f, 0.0f, 0.0f)
    )

    private var minMaxSW: MutableList<Acceleration> = mutableListOf()
    private var postureDetectionSW: MutableList<Acceleration> = mutableListOf()
    private var fallSW: MutableList<Acceleration> = mutableListOf()

    internal var impactTimeOut: Int = -1
    internal var fallingTimeOut: Int = -1
    internal var isVelocityGreaterThanThreshold = false
    internal var isLyingPostureDetected = false

    override fun detectFall(acceleration: Acceleration) {
        measureFall(acceleration)
    }

    private fun addAccelerationToWindow(
        acceleration: Acceleration,
        windowSize: Int,
        window: MutableList<Acceleration>
    ) {
        if (window.size >= windowSize) window.removeAt(0)
        window.add(acceleration)
    }

    private fun measureFall(acceleration: Acceleration) {
        val accelerationFloatArray = floatArrayOf(
            acceleration.x.toFloat(),
            acceleration.y.toFloat(),
            acceleration.z.toFloat()
        )

        lowPassFilterData = filter.lowPassFilter(
            input = accelerationFloatArray,
            output = lowPassFilterData,
            alpha = ALPHA
        )
        highPassFilterData = filter.highPassFilter(
            input = accelerationFloatArray,
            hpfData = highPassFilterData,
            alpha = ALPHA
        )

        val lpfAcceleration = getAcceleration(
            rawAcceleration = acceleration,
            acceleration = lowPassFilterData
        )
        val hpfAcceleration = getAcceleration(
            rawAcceleration = acceleration,
            acceleration = highPassFilterData.acceleration
        )

        addAccelerationToWindow(
            acceleration = acceleration,
            windowSize = Constants.MIN_MAX_SW_SIZE.toInt(),
            window = minMaxSW
        )

        addAccelerationToWindow(
            acceleration = lpfAcceleration,
            windowSize = Constants.POSTURE_DETECTION_SW_SIZE.toInt(),
            window = postureDetectionSW
        )

        if (postureDetectionSW.size >= Constants.POSTURE_DETECTION_SW_SIZE) {
            when (prefUtil.getDetectionAlgorithm()) {
                Algorithms.FIRST -> useFirstAlgorithm(hpfAcceleration, acceleration)
                Algorithms.SECOND -> useSecondAlgorithm(hpfAcceleration, acceleration)
                Algorithms.THIRD -> useThirdAlgorithm(hpfAcceleration, acceleration)
            }
        }
    }

    internal fun detectPosture(impactTimeOut: Int, postureDetectionSW: MutableList<Acceleration>) {
        // posture must be detected 2 sec after the impact
        // it is marked as impactTimeOut == 0
        if (impactTimeOut == 0) {
            isLyingPostureDetected = false

            val sum = postureDetectionSW.sumOf { it.y }
            val count = postureDetectionSW.size.toDouble()

            // impact occurred if vertical signal (y axis in Android coordinate system),
            // based on the average acceleration
            // in a 0.4 s time interval is 0.5G or lower
            if ((sum / count) <= LYING_POSTURE_VERTICAL_THRESHOLD) {
                Timber.d("4. FallDetector: Detected lying position!")
                isLyingPostureDetected = true
                sendBroadcast()
            }
        }
    }

    private fun useFirstAlgorithm(
        hpfAcceleration: Acceleration,
        acceleration: Acceleration
    ) {
        // first use detectImpact()
        // if impact was observed wait 2 sec
        // detect posture
        impactTimeOut = expireTimeOut(impactTimeOut)
        detectImpact(
            hpfAcceleration = hpfAcceleration,
            acceleration = acceleration,
            minMaxSW = minMaxSW
        )
        detectPosture(impactTimeOut, postureDetectionSW)
    }

    private fun useSecondAlgorithm(
        hpfAcceleration: Acceleration,
        acceleration: Acceleration
    ) {
        // first detected the start of the fall
        // if SVTOT is lower than the threshold of 0.6g
        // detect impact within a 1 sec frame
        // if impact was observed wait 2 sec
        // detect posture
        impactTimeOut = expireTimeOut(impactTimeOut)
        fallingTimeOut = expireTimeOut(fallingTimeOut)
        detectStartOfFall(acceleration)
        detectImpact(
            hpfAcceleration = hpfAcceleration,
            acceleration = acceleration,
            minMaxSW = minMaxSW
        )
        detectPosture(impactTimeOut, postureDetectionSW)
    }

    private fun useThirdAlgorithm(
        hpfAcceleration: Acceleration,
        acceleration: Acceleration
    ) {
        // first detected the start of the fall
        // if SVTOT is lower than the threshold of 0.6g
        // check if velocity exceeds the threshold
        // detect impact within a 1 sec frame
        // if impact was observed wait 2 sec
        // detect posture
        impactTimeOut = expireTimeOut(impactTimeOut)
        fallingTimeOut = expireTimeOut(fallingTimeOut)
        detectStartOfFall(acceleration)
        detectVelocity(acceleration, fallSW)
        detectImpact(
            hpfAcceleration = hpfAcceleration,
            acceleration = acceleration,
            minMaxSW = minMaxSW
        )
        detectPosture(impactTimeOut, postureDetectionSW)
    }

    internal fun detectStartOfFall(acceleration: Acceleration) {
        val svTotal = calculateSumVector(acceleration.x, acceleration.y, acceleration.z)
        if (svTotal <= SV_TOTAL_FALLING_THRESHOLD) {
            Timber.d("1. FallDetector: Start of the fall was detected!")
            fallingTimeOut = Constants.FALLING_TIME_SPAN
        }
    }

    internal fun detectImpact(
        hpfAcceleration: Acceleration,
        acceleration: Acceleration,
        minMaxSW: MutableList<Acceleration>
    ) {
        val svTotal = calculateSumVector(acceleration.x, acceleration.y, acceleration.z)
        val svDynamic = calculateSumVector(hpfAcceleration.x, hpfAcceleration.y, hpfAcceleration.z)

        // impact should be detected when using ImpactPosture algorithm
        // or when start of the fall was detected
        // or when start of the fall was detected and isVelocityGreaterThanThreshold == true
        if (isFirstAlgorithm() ||
            isDetectingImpactForSecondAlgorithm() ||
            isDetectingImpactForThirdAlgorithm()
        ) {
            Timber.d("Starts to detect impact")
            if (isMinMaxSVGreaterThanThresholdForImpactPostureAlgorithm(minMaxSW) ||
                isVerticalAccelerationGreaterThanThreshold(svTotal, svDynamic) ||
                isSumVectorGreaterThanThreshold(svTotal, SV_TOT_THRESHOLD) ||
                isSumVectorGreaterThanThreshold(svDynamic, SV_D_THRESHOLD)
            ) {
                Timber.d("3. FallDetector: Impact was detected!")
                // impact was detected, set impact time out
                impactTimeOut = Constants.IMPACT_TIME_SPAN
            } else {
                clearDetections()
            }
        }
    }

    private fun isFirstAlgorithm() =
        prefUtil.getDetectionAlgorithm() == Algorithms.FIRST

    private fun isDetectingImpactForSecondAlgorithm() =
        prefUtil.getDetectionAlgorithm() == Algorithms.SECOND && isFalling()

    private fun isDetectingImpactForThirdAlgorithm() =
        prefUtil.getDetectionAlgorithm() == Algorithms.THIRD &&
            isFalling() && isVelocityGreaterThanThreshold

    private fun isFalling() = fallingTimeOut > -1

    internal fun detectVelocity(acceleration: Acceleration, velocitySW: MutableList<Acceleration>) {
        // velocity is calculated by integrating the area of SVTOT
        // from the beginning of the fall until the impact, where SVTOT < 1g
        val svTotal = calculateSumVector(acceleration.x, acceleration.y, acceleration.z)

        val isFalling = isFalling() && svTotal >= 1

        if (isFalling) {
            addAccelerationToWindow(
                acceleration = acceleration,
                windowSize = Int.MAX_VALUE,
                window = velocitySW
            )

            val result = numericalIntegrationTrapezoidalRule(velocitySW)

            if (result > VELOCITY_THRESHOLD) {
                Timber.d("2. FallDetector: Velocity is greater than the threshold")
                isVelocityGreaterThanThreshold = true
            }
        }
    }

    internal fun numericalIntegrationTrapezoidalRule(accelerations: List<Acceleration>): Double {
        // all accelerations need to be converted to m/s
        val svFirst = calculateSumVector(
            convertToMetersPerSeconds(accelerations.first().x),
            convertToMetersPerSeconds(accelerations.first().y),
            convertToMetersPerSeconds(accelerations.first().z)
        )
        val svLast = calculateSumVector(
            convertToMetersPerSeconds(accelerations.last().x),
            convertToMetersPerSeconds(accelerations.last().y),
            convertToMetersPerSeconds(accelerations.last().z)
        )

        val dx = Constants.INTERVAL_MILISEC.toDouble() * 0.001 // need to be converted to sec
        var total = 0.0

        for (i in 1 until accelerations.size - 1) {
            total += calculateSumVector(
                convertToMetersPerSeconds(accelerations[i].x),
                convertToMetersPerSeconds(accelerations[i].y),
                convertToMetersPerSeconds(accelerations[i].z)
            )
        }

        total += (svFirst + svLast) / 2
        total *= dx

        return total
    }

    internal fun expireTimeOut(timeOut: Int) = if (timeOut > -1) timeOut - 1 else -1

    internal fun isSumVectorGreaterThanThreshold(sumVector: Double, threshold: Double) =
        sumVector > threshold

    internal fun isMinMaxSVGreaterThanThreshold(minMaxSW: MutableList<Acceleration>): Boolean {
        val xMinMax = getMaxValue(DataSet.X_AXIS, minMaxSW) - getMinValue(DataSet.X_AXIS, minMaxSW)
        val yMinMax = getMaxValue(DataSet.Y_AXIS, minMaxSW) - getMinValue(DataSet.Y_AXIS, minMaxSW)
        val zMinMax = getMaxValue(DataSet.Z_AXIS, minMaxSW) - getMinValue(DataSet.Z_AXIS, minMaxSW)
        return calculateSumVector(xMinMax, yMinMax, zMinMax) > SV_MAX_MIN_THRESHOLD
    }

    private fun isMinMaxSVGreaterThanThresholdForImpactPostureAlgorithm(
        minMaxSW: MutableList<Acceleration>
    ) = isMinMaxSVGreaterThanThreshold(minMaxSW) && isFirstAlgorithm()

    internal fun isVerticalAccelerationGreaterThanThreshold(
        svTotal: Double,
        svDynamic: Double
    ) = calculateVerticalAcceleration(svTotal, svDynamic) > VERTICAL_ACC_THRESHOLD

    internal fun calculateSumVector(x: Double, y: Double, z: Double) =
        sqrt(x * x + y * y + z * z)

    internal fun calculateVerticalAcceleration(svTOT: Double, svD: Double) =
        (svTOT * svTOT - svD * svD - G_CONST * G_CONST) / (2.0 * G_CONST)

    private fun getMaxValue(dataSet: DataSet, minMaxSW: MutableList<Acceleration>): Double {
        return when (dataSet) {
            DataSet.X_AXIS -> minMaxSW.map { it.x }.maxOf { it }
            DataSet.Y_AXIS -> minMaxSW.map { it.y }.maxOf { it }
            DataSet.Z_AXIS -> minMaxSW.map { it.z }.maxOf { it }
        }
    }

    private fun getMinValue(dataSet: DataSet, minMaxSW: MutableList<Acceleration>): Double {
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

    private fun clearDetections() {
        isVelocityGreaterThanThreshold = false
        isLyingPostureDetected = false
        fallSW = mutableListOf()
    }

    private fun sendBroadcast() = Intent(Constants.CUSTOM_FALL_DETECTED_RECEIVER).also {
        context.sendBroadcast(it)
    }

    private fun convertToMetersPerSeconds(value: Double) = value * SensorManager.STANDARD_GRAVITY
}
