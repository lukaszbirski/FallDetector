package pl.birski.falldetector.components.implementations

import android.content.Context
import android.content.Intent
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
    // signal frequency is 50Hz and cut-off frequency is 0.25 Hz

    // TODO("need to move everything related to frequency to one place and consider changing value")
    private val ALPHA = filter.calculateAlpha(0.25, 50.0)

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

    private var impactTimeOut: Int = -1
    private var fallingTimeOut: Int = -1
    private var measureVelocity = false
    private var isVelocityGreaterThanThreshold = false
    private var isLyingPostureDetected = false

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

                Algorithms.FIRST -> useFirstAlgorithm(
                    hpfAcceleration = hpfAcceleration,
                    acceleration = acceleration
                )

                Algorithms.SECOND -> useSecondAlgorithm(
                    hpfAcceleration = hpfAcceleration,
                    acceleration = acceleration
                )

                Algorithms.THIRD -> useThirdAlgorithm(
                    hpfAcceleration = hpfAcceleration,
                    acceleration = acceleration
                )
            }
        }
    }

    private fun detectPosture(impactTimeOut: Int, postureDetectionSW: MutableList<Acceleration>) {
        // posture must be detected 2 sec after the impact
        // it is marked as impactTimeOut == 0
        if (impactTimeOut == 0) {

            isLyingPostureDetected = false

            val sum = postureDetectionSW.sumOf { it.z }
            val count = postureDetectionSW.size.toDouble()

            // impact occurred if vertical signal, based on the average acceleration
            // in a 0.4 s time interval is 0.5G or lower
            if ((sum / count) > LYING_POSTURE_VERTICAL_THRESHOLD) {
                Timber.d("Detected lying position!")
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
        detectPosture(
            impactTimeOut = impactTimeOut,
            postureDetectionSW = postureDetectionSW
        )
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
        detectStartOfFall(acceleration = acceleration)
        detectImpact(
            hpfAcceleration = hpfAcceleration,
            acceleration = acceleration,
            minMaxSW = minMaxSW
        )
        detectPosture(
            impactTimeOut = impactTimeOut,
            postureDetectionSW = postureDetectionSW
        )
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
        detectStartOfFall(acceleration = acceleration)
        detectVelocity(acceleration = acceleration)
        detectImpact(
            hpfAcceleration = hpfAcceleration,
            acceleration = acceleration,
            minMaxSW = minMaxSW
        )
        detectPosture(
            impactTimeOut = impactTimeOut,
            postureDetectionSW = postureDetectionSW
        )
    }

    private fun detectStartOfFall(acceleration: Acceleration) {
        val svTotal = calculateSumVector(acceleration.x, acceleration.y, acceleration.z)
        if (svTotal <= SV_TOTAL_FALLING_THRESHOLD) {
            Timber.d("Start of the fall was detected!")
            fallingTimeOut = Constants.FALLING_TIME_SPAN
        }
    }

    private fun detectImpact(
        hpfAcceleration: Acceleration,
        acceleration: Acceleration,
        minMaxSW: MutableList<Acceleration>
    ) {
        val svTotal = calculateSumVector(acceleration.x, acceleration.y, acceleration.z)
        val svDynamic = calculateSumVector(hpfAcceleration.x, hpfAcceleration.y, hpfAcceleration.z)

        // impact should be detected when using ImpactPosture algorithm
        // or when start of the fall was detected
        if (isFirstAlgorithm() ||
            isDetectingImpactForSecondAlgorithm() ||
            isDetectingImpactForThirdAlgorithm()
        ) {
            if (isMinMaxSumVectorGreaterThanThresholdForImpactPostureAlgorithm(minMaxSW) ||
                isVerticalAccelerationGreaterThanThreshold(svTotal, svDynamic) ||
                isSumVectorGreaterThanThreshold(svTotal, SV_TOT_THRESHOLD) ||
                isSumVectorGreaterThanThreshold(svDynamic, SV_D_THRESHOLD)
            ) {
                Timber.d("Impact was detected!")
                // impact was detected, set impact time out
                impactTimeOut = Constants.IMPACT_TIME_SPAN
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

    // TODO("need to improve fun and create test for this fun")
    private fun detectVelocity(acceleration: Acceleration) {
        // velocity is calculated by integrating the area of SVTOT
        // from the beginning of the fall, until the impact, where the
        // signal value is lower than 1g

        val svTotal = calculateSumVector(acceleration.x, acceleration.y, acceleration.z)

        if (fallingTimeOut > -1 && svTotal < 1.0) {
            addAccelerationToWindow(
                acceleration = acceleration,
                windowSize = Int.MAX_VALUE,
                window = fallSW
            )
            measureVelocity = true
            isVelocityGreaterThanThreshold = false
        } else if (measureVelocity) {

            val sumSVTOT = fallSW.sumOf { calculateSumVector(it.x, it.y, it.z) }
            val time = (fallSW.last().timeStamp - fallSW.first().timeStamp).div(1000.0F)

            fallSW = mutableListOf()
            measureVelocity = false

            if ((sumSVTOT * time) > VELOCITY_THRESHOLD) {
                isVelocityGreaterThanThreshold = true
            }
        }
    }

    private fun expireTimeOut(timeOut: Int) = if (timeOut > -1) timeOut - 1 else -1

    private fun isSumVectorGreaterThanThreshold(sumVector: Double, threshold: Double) =
        sumVector > threshold

    private fun isMinMaxSVGreaterThanThreshold(minMaxSW: MutableList<Acceleration>): Boolean {
        val xMinMax = getMaxValue(DataSet.X_AXIS, minMaxSW) - getMinValue(DataSet.X_AXIS, minMaxSW)
        val yMinMax = getMaxValue(DataSet.Y_AXIS, minMaxSW) - getMinValue(DataSet.Y_AXIS, minMaxSW)
        val zMinMax = getMaxValue(DataSet.Z_AXIS, minMaxSW) - getMinValue(DataSet.Z_AXIS, minMaxSW)
        return calculateSumVector(xMinMax, yMinMax, zMinMax) > SV_MAX_MIN_THRESHOLD
    }

    private fun isMinMaxSumVectorGreaterThanThresholdForImpactPostureAlgorithm(
        minMaxSW: MutableList<Acceleration>
    ) = isMinMaxSVGreaterThanThreshold(minMaxSW) && isFirstAlgorithm()

    private fun isVerticalAccelerationGreaterThanThreshold(
        svTotal: Double,
        svDynamic: Double
    ) = calculateVerticalAcceleration(svTotal, svDynamic) > VERTICAL_ACC_THRESHOLD

    private fun calculateSumVector(x: Double, y: Double, z: Double) =
        sqrt(x * x + y * y + z * z)

    private fun calculateVerticalAcceleration(svTOT: Double, svD: Double) =
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

    private fun sendBroadcast() = Intent(Constants.CUSTOM_FALL_DETECTED_RECEIVER).also {
        context.sendBroadcast(it)
    }
}
