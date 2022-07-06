package pl.birski.falldetector.components

import android.content.Context
import android.os.Build
import androidx.test.core.app.ApplicationProvider
import org.junit.Before
import org.junit.Test
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.internal.DoNotInstrument
import pl.birski.falldetector.components.fake.FallDetectorDataFake
import pl.birski.falldetector.components.implementations.FallDetectorImpl
import pl.birski.falldetector.components.implementations.FilterImpl
import pl.birski.falldetector.components.interfaces.Filter
import pl.birski.falldetector.model.Acceleration
import pl.birski.falldetector.other.PrefUtil
import pl.birski.falldetector.other.PrefUtilImpl

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [Build.VERSION_CODES.P])
@DoNotInstrument
class FallDetectorTest {

    private val fakeData = FallDetectorDataFake()

    // system in test
    private lateinit var fallDetector: FallDetectorImpl

    private lateinit var filter: Filter
    private lateinit var prefUtil: PrefUtil

    private val context: Context = ApplicationProvider.getApplicationContext()

    @Before
    fun setup() {
        filter = FilterImpl()
        prefUtil = PrefUtilImpl(context)
        fallDetector = FallDetectorImpl(context, filter, prefUtil)
    }

    @Test
    fun `check if returns true when SV is lower than threshold`() {
        // parameters
        val sum = 1.6 // sum
        val threshold = 1.4 // threshold
        val result = fallDetector.isSumVectorGreaterThanThreshold(sum, threshold)

        assertEquals(true, result)
    }

    @Test
    fun `check if returns false when SV is lower than threshold`() {
        // parameters
        val sum = 1.4 // sum
        val threshold = 1.6 // threshold
        val result = fallDetector.isSumVectorGreaterThanThreshold(sum, threshold)

        assertEquals(false, result)
    }

    @Test
    fun `check if returns false when SV is equal threshold`() {
        // parameters
        val sum = 1.4 // sum
        val threshold = 1.4 // threshold
        val result = fallDetector.isSumVectorGreaterThanThreshold(sum, threshold)

        assertEquals(false, result)
    }

    @Test
    fun `check if calculateSumVector returns correct value`() {
        // parameters
        val x = 8.0 // x
        val y = 1.5 // y
        val z = 1.0 // z
        val result = fallDetector.calculateSumVector(x, y, z)

        assertEquals(8.201, result, 0.001)
    }

    @Test
    fun `check if expireTimeOut returns given value minus one`() {
        val value = 10
        val result = fallDetector.expireTimeOut(value)

        assertEquals(9, result)
    }

    @Test
    fun `check if expireTimeOut returns -1 when -1 is given`() {
        val value = -1
        val result = fallDetector.expireTimeOut(value)

        assertEquals(-1, result)
    }

    @Test
    fun `check if calculateVerticalAcceleration returns correct value`() {
        // parameters
        val svTOT = 1.1 // svTOT
        val svD = 0.1 // svD
        val result = fallDetector.calculateVerticalAcceleration(svTOT, svD)

        assertEquals(0.1, result, 0.001)
    }

    @Test
    fun `check if returns false when vertical acceleration is greater than threshold`() {
        // parameters
        val svTotal = 2.4 // svTotal
        val svDynamic = 1.3 // svDynamic

        val result = fallDetector.isVerticalAccelerationGreaterThanThreshold(svTotal, svDynamic)

        assertEquals(true, result)
    }

    @Test
    fun `check if returns false when vertical acceleration is lower than threshold`() {
        // parameters
        val svTotal = 1.0 // svTotal
        val svDynamic = 0.0 // svDynamic

        val result = fallDetector.isVerticalAccelerationGreaterThanThreshold(svTotal, svDynamic)

        assertEquals(false, result)
    }

    @Test
    fun `check if detects start of fall when acceleration is greater than threshold`() {
        val acceleration = Acceleration(0.0, 0.0, 1.0, 1L)

        fallDetector.detectStartOfFall(acceleration)

        val result = fallDetector.fallingTimeOut

        assertEquals(-1, result)
    }

    @Test
    fun `check if detects start of fall when acceleration is lower than threshold`() {
        val acceleration = Acceleration(0.20, 0.20, 0.40, 1L)

        fallDetector.detectStartOfFall(acceleration)

        val result = fallDetector.fallingTimeOut

        assertNotEquals(-1, result)
    }

    @Test
    fun `check if impact will be detected when only SV Dynamic is greater than threshold`() {
        // parameters
        val hpfAcceleration = Acceleration(0.2, 0.2, 2.5, 1L)
        val acceleration = Acceleration(0.0, 0.0, 0.0, 1L)
        val minMaxSW = fakeData.minMaxListWithoutDiffs

        fallDetector.detectImpact(hpfAcceleration, acceleration, minMaxSW)

        val result = fallDetector.impactTimeOut

        assertNotEquals(-1, result)
    }

    @Test
    fun `check if impact will be detected when only SV Total is greater than threshold`() {
        // parameters
        val hpfAcceleration = Acceleration(0.0, 0.0, 0.0, 1L)
        val acceleration = Acceleration(0.2, 0.2, 2.0, 1L)
        val minMaxSW = fakeData.minMaxListWithoutDiffs

        fallDetector.detectImpact(hpfAcceleration, acceleration, minMaxSW)

        val result = fallDetector.impactTimeOut

        assertNotEquals(-1, result)
    }

    @Test
    fun `check if impact will be detected when only vertical acc is greater than threshold`() {
        // parameters
        val hpfAcceleration = Acceleration(0.0, 0.0, 1.0, 1L)
        val acceleration = Acceleration(0.2, 0.2, 2.3, 1L)
        val minMaxSW = fakeData.minMaxListWithoutDiffs

        fallDetector.detectImpact(hpfAcceleration, acceleration, minMaxSW)

        val result = fallDetector.impactTimeOut

        assertNotEquals(-1, result)
    }

    @Test
    fun `check if impact will be detected when only min max SV is greater than threshold`() {
        // parameters
        val hpfAcceleration = Acceleration(0.0, 0.0, 0.0, 1L)
        val acceleration = Acceleration(0.0, 0.0, 0.0, 1L)
        val minMaxSW = fakeData.minMaxListWithDiffs

        fallDetector.detectImpact(hpfAcceleration, acceleration, minMaxSW)

        val result = fallDetector.impactTimeOut

        assertNotEquals(-1, result)
    }

    @Test
    fun `check if impact will not be detected when non value is greater than threshold`() {
        // parameters
        val hpfAcceleration = Acceleration(0.0, 0.0, 0.0, 1L)
        val acceleration = Acceleration(0.0, 0.0, 0.0, 1L)
        val minMaxSW = fakeData.minMaxListWithoutDiffs

        fallDetector.detectImpact(hpfAcceleration, acceleration, minMaxSW)

        val result = fallDetector.impactTimeOut

        assertEquals(-1, result)
    }

    @Test
    fun `check if return true when isMinMaxSumVector is greater than threshold`() {
        val minMaxSW = fakeData.minMaxListWithDiffs

        val result = fallDetector.isMinMaxSVGreaterThanThreshold(minMaxSW)

        assertEquals(true, result)
    }

    @Test
    fun `check if return false when isMinMaxSumVector is lower than threshold`() {
        val minMaxSW = fakeData.minMaxListWithoutDiffs

        val result = fallDetector.isMinMaxSVGreaterThanThreshold(minMaxSW)

        assertEquals(false, result)
    }

    @Test
    fun `check if detects posture when average vertical acceleration is greater than threshold`() {
        // parameters
        val impactTimeOut = 0
        val postureSW = fakeData.postureDetectionSWFakeLow

        fallDetector.detectPosture(impactTimeOut, postureSW)

        val result = fallDetector.isLyingPostureDetected

        assertEquals(true, result)
    }

    @Test
    fun `check if detects posture when average vertical acceleration is lower than threshold`() {
        // parameters
        val impactTimeOut = 0
        val postureSW = fakeData.postureDetectionSWFakeHigh

        fallDetector.detectPosture(impactTimeOut, postureSW)

        val result = fallDetector.isLyingPostureDetected

        assertEquals(false, result)
    }

    @Test
    fun `check if numericalIntegrationTrapezoidalRule returns correct value`() {
        val accelerations = arrayListOf(
            Acceleration(0.0, 0.0, 0.0, 0),
            Acceleration(0.0, 0.0, 45.0, 20),
            Acceleration(0.0, 0.0, 90.0, 40),
            Acceleration(0.0, 0.0, 135.0, 60),
            Acceleration(0.0, 0.0, 180.0, 80),
            Acceleration(0.0, 0.0, 225.0, 100)
        )

        val result = fallDetector.numericalIntegrationTrapezoidalRule(accelerations)
        assertEquals(110.325, result, 0.001)
    }

    @Test
    fun `check if detects that velocity is greater than threshold`() {
        // falling time out is greater than -1
        fallDetector.fallingTimeOut = 10

        // svTotal will be  >= 1
        val acceleration = Acceleration(1.0, 1.0, 1.0, 1L)
        // integrated velocity will be greater than threshold
        val list = fakeData.postureDetectionSWFakeLow

        fallDetector.detectVelocity(acceleration, list)

        val result = fallDetector.isVelocityGreaterThanThreshold
        assertEquals(true, result)
    }

    @Test
    fun `check if not detects that velocity is greater than threshold when fallingTimeOut is -1`() {
        // falling time out is -1, should return false
        fallDetector.fallingTimeOut = -1

        // svTotal will be  >= 1
        val acceleration = Acceleration(1.0, 1.0, 1.0, 1L)
        // integrated velocity will be greater than threshold
        val list = fakeData.postureDetectionSWFakeLow

        fallDetector.detectVelocity(acceleration, list)

        val result = fallDetector.isVelocityGreaterThanThreshold
        assertEquals(false, result)
    }

    @Test
    fun `check if not detects that velocity is greater than threshold when svTotal is below 1`() {
        // falling time out is greater than -1
        fallDetector.fallingTimeOut = 10

        // svTotal will be < 1
        val acceleration = Acceleration(0.2, 0.2, 0.2, 1L)
        // integrated velocity will be greater than threshold
        val list = fakeData.postureDetectionSWFakeLow

        fallDetector.detectVelocity(acceleration, list)

        val result = fallDetector.isVelocityGreaterThanThreshold
        assertEquals(false, result)
    }

    @Test
    fun `check if not detects velocity greater than threshold when velocity is below threshold`() {
        // falling time out is greater than -1
        fallDetector.fallingTimeOut = 10

        // svTotal will be  >= 1
        val acceleration = Acceleration(1.0, 1.0, 1.0, 1L)
        // integrated velocity will be below threshold
        val list = fakeData.velocityDetectionSWFake

        fallDetector.detectVelocity(acceleration, list)

        val result = fallDetector.isVelocityGreaterThanThreshold
        assertEquals(false, result)
    }
}
