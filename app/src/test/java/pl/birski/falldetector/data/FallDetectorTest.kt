package pl.birski.falldetector.data

import android.content.Context
import android.os.Build
import androidx.test.core.app.ApplicationProvider
import java.lang.reflect.Field
import org.junit.Before
import org.junit.Test
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.internal.DoNotInstrument
import pl.birski.falldetector.data.fake.FallDetectorDataFake
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

    private val context: Context = ApplicationProvider.getApplicationContext<Context>()

    @Before
    fun setup() {
        filter = FilterImpl()
        prefUtil = PrefUtilImpl(context)
        fallDetector = FallDetectorImpl(context, filter, prefUtil)
    }

    @Test
    fun `check if returns true when SV is lower than threshold`() {

        val method = fallDetector.javaClass.getDeclaredMethod(
            "isSumVectorGreaterThanThreshold",
            Double::class.java,
            Double::class.java
        )
        method.isAccessible = true
        val parameters = arrayOfNulls<Any>(2)

        // parameters
        parameters[0] = 1.6 // sum
        parameters[1] = 1.4 // threshold

        assertEquals(true, method.invoke(fallDetector, *parameters))
    }

    @Test
    fun `check if returns false when SV is lower than threshold`() {

        val method = fallDetector.javaClass.getDeclaredMethod(
            "isSumVectorGreaterThanThreshold",
            Double::class.java,
            Double::class.java
        )
        method.isAccessible = true
        val parameters = arrayOfNulls<Any>(2)

        // parameters
        parameters[0] = 1.4 // sum
        parameters[1] = 1.6 // threshold

        assertEquals(false, method.invoke(fallDetector, *parameters))
    }

    @Test
    fun `check if returns false when SV is equal threshold`() {

        val method = fallDetector.javaClass.getDeclaredMethod(
            "isSumVectorGreaterThanThreshold",
            Double::class.java,
            Double::class.java
        )
        method.isAccessible = true
        val parameters = arrayOfNulls<Any>(2)

        // parameters
        parameters[0] = 1.4 // sum
        parameters[1] = 1.4 // threshold

        assertEquals(false, method.invoke(fallDetector, *parameters))
    }

    @Test
    fun `check if calculateSumVector returns correct value`() {

        val method = fallDetector.javaClass.getDeclaredMethod(
            "calculateSumVector",
            Double::class.java,
            Double::class.java,
            Double::class.java
        )
        method.isAccessible = true
        val parameters = arrayOfNulls<Any>(3)

        // parameters
        parameters[0] = 8.0 // x
        parameters[1] = 1.5 // y
        parameters[2] = 1.0 // z

        val result = method.invoke(fallDetector, *parameters) as Double

        assertEquals(8.201, result, 0.001)
    }

    @Test
    fun `check if expireTimeOut returns given value minus one`() {

        val method = fallDetector.javaClass.getDeclaredMethod(
            "expireTimeOut",
            Int::class.java,
        )
        method.isAccessible = true
        val parameters = arrayOfNulls<Any>(1)

        // parameters
        parameters[0] = 10

        val result = method.invoke(fallDetector, *parameters) as Int

        assertEquals(9, result)
    }

    @Test
    fun `check if expireTimeOut returns -1 when -1 is given`() {

        val method = fallDetector.javaClass.getDeclaredMethod(
            "expireTimeOut",
            Int::class.java,
        )
        method.isAccessible = true
        val parameters = arrayOfNulls<Any>(1)

        // parameters
        parameters[0] = -1

        val result = method.invoke(fallDetector, *parameters) as Int

        assertEquals(-1, result)
    }

    @Test
    fun `check if calculateVerticalAcceleration returns correct value`() {

        val method = fallDetector.javaClass.getDeclaredMethod(
            "calculateVerticalAcceleration",
            Double::class.java,
            Double::class.java
        )
        method.isAccessible = true
        val parameters = arrayOfNulls<Any>(2)

        // parameters
        parameters[0] = 1.1 // svTOT
        parameters[1] = 0.1 // svD

        val result = method.invoke(fallDetector, *parameters) as Double

        assertEquals(0.1, result, 0.001)
    }

    @Test
    fun `check if returns false when vertical acceleration is greater than threshold`() {

        val method = fallDetector.javaClass.getDeclaredMethod(
            "isVerticalAccelerationGreaterThanThreshold",
            Double::class.java,
            Double::class.java
        )
        method.isAccessible = true
        val parameters = arrayOfNulls<Any>(2)

        // parameters
        parameters[0] = 2.4 // svTotal
        parameters[1] = 1.3 // svDynamic

        val result = method.invoke(fallDetector, *parameters) as Boolean

        assertEquals(true, result)
    }

    @Test
    fun `check if returns false when vertical acceleration is lower than threshold`() {

        val method = fallDetector.javaClass.getDeclaredMethod(
            "isVerticalAccelerationGreaterThanThreshold",
            Double::class.java,
            Double::class.java
        )
        method.isAccessible = true
        val parameters = arrayOfNulls<Any>(2)

        // parameters
        parameters[0] = 1.0 // svTotal
        parameters[1] = 0.0 // svDynamic

        val result = method.invoke(fallDetector, *parameters) as Boolean

        assertEquals(false, result)
    }

    @Test
    fun `check if detects start of fall when acceleration is greater than threshold`() {
        val method = fallDetector.javaClass.getDeclaredMethod(
            "detectStartOfFall",
            Acceleration::class.java
        )
        method.isAccessible = true
        val parameters = arrayOfNulls<Any>(1)

        val acceleration = Acceleration(0.0, 0.0, 1.0, 1L)

        // parameter
        parameters[0] = acceleration // acceleration

        method.invoke(fallDetector, *parameters)

        val field: Field = FallDetectorImpl::class.java.getDeclaredField("fallingTimeOut")
        field.isAccessible = true

        val result = field.get(fallDetector) as Int

        assertEquals(-1, result)
    }

    @Test
    fun `check if detects start of fall when acceleration is lower than threshold`() {
        val method = fallDetector.javaClass.getDeclaredMethod(
            "detectStartOfFall",
            Acceleration::class.java
        )
        method.isAccessible = true
        val parameters = arrayOfNulls<Any>(1)

        // parameter
        parameters[0] = Acceleration(0.20, 0.20, 0.40, 1L) // acceleration

        method.invoke(fallDetector, *parameters)

        val field: Field = FallDetectorImpl::class.java.getDeclaredField("fallingTimeOut")
        field.isAccessible = true

        val result = field.get(fallDetector) as Int

        assertNotEquals(-1, result)
    }

    @Test
    fun `check if impact will be detected when only SV Dynamic is greater than threshold`() {

        // for test need to set this sliding windows
        fallDetector.setMinMaxSW(fakeData.minMaxListWithoutDiffs)

        val method = fallDetector.javaClass.getDeclaredMethod(
            "detectImpact",
            Acceleration::class.java,
            Acceleration::class.java
        )
        method.isAccessible = true
        val parameters = arrayOfNulls<Any>(2)

        // parameter
        parameters[0] = Acceleration(0.2, 0.2, 2.5, 1L) // hpfAcceleration - for SV Dynamic
        parameters[1] = Acceleration(0.0, 0.0, 0.0, 1L) // acceleration

        method.invoke(fallDetector, *parameters)

        val field: Field = FallDetectorImpl::class.java.getDeclaredField("impactTimeOut")
        field.isAccessible = true

        val result = field.get(fallDetector) as Int

        assertNotEquals(-1, result)
    }

    @Test
    fun `check if impact will be detected when only SV Total is greater than threshold`() {

        // for test need to set this sliding windows
        fallDetector.setMinMaxSW(fakeData.minMaxListWithoutDiffs)

        val method = fallDetector.javaClass.getDeclaredMethod(
            "detectImpact",
            Acceleration::class.java,
            Acceleration::class.java
        )
        method.isAccessible = true
        val parameters = arrayOfNulls<Any>(2)

        // parameter
        parameters[0] = Acceleration(0.0, 0.0, 0.0, 1L) // hpfAcceleration
        parameters[1] = Acceleration(0.2, 0.2, 2.0, 1L) // acceleration - for SV Total

        method.invoke(fallDetector, *parameters)

        val field: Field = FallDetectorImpl::class.java.getDeclaredField("impactTimeOut")
        field.isAccessible = true

        val result = field.get(fallDetector) as Int

        assertNotEquals(-1, result)
    }

    @Test
    fun `check if impact will be detected when only vertical acc is greater than threshold`() {

        // for test need to set this sliding windows
        fallDetector.setMinMaxSW(fakeData.minMaxListWithoutDiffs)

        val method = fallDetector.javaClass.getDeclaredMethod(
            "detectImpact",
            Acceleration::class.java,
            Acceleration::class.java
        )
        method.isAccessible = true
        val parameters = arrayOfNulls<Any>(2)

        // parameter
        parameters[0] = Acceleration(0.0, 0.0, 1.0, 1L) // hpfAcceleration - for SV Dynamic
        parameters[1] = Acceleration(0.2, 0.2, 2.3, 1L) // acceleration - for SV Total

        method.invoke(fallDetector, *parameters)

        val field: Field = FallDetectorImpl::class.java.getDeclaredField("impactTimeOut")
        field.isAccessible = true

        val result = field.get(fallDetector) as Int

        assertNotEquals(-1, result)
    }

    @Test
    fun `check if impact will be detected when only min max SV is greater than threshold`() {

        // for test need to set this sliding windows
        fallDetector.setMinMaxSW(fakeData.minMaxListWithDiffs)

        val method = fallDetector.javaClass.getDeclaredMethod(
            "detectImpact",
            Acceleration::class.java,
            Acceleration::class.java
        )
        method.isAccessible = true
        val parameters = arrayOfNulls<Any>(2)

        // parameter
        parameters[0] = Acceleration(0.0, 0.0, 0.0, 1L) // hpfAcceleration
        parameters[1] = Acceleration(0.0, 0.0, 0.0, 1L) // acceleration

        method.invoke(fallDetector, *parameters)

        val field: Field = FallDetectorImpl::class.java.getDeclaredField("impactTimeOut")
        field.isAccessible = true

        val result = field.get(fallDetector) as Int

        assertNotEquals(-1, result)
    }

    @Test
    fun `check if impact will not be detected when non value is greater than threshold`() {

        // for test need to set this sliding windows
        fallDetector.setMinMaxSW(fakeData.minMaxListWithoutDiffs)

        val method = fallDetector.javaClass.getDeclaredMethod(
            "detectImpact",
            Acceleration::class.java,
            Acceleration::class.java
        )
        method.isAccessible = true
        val parameters = arrayOfNulls<Any>(2)

        // parameter
        parameters[0] = Acceleration(0.0, 0.0, 0.0, 1L) // hpfAcceleration
        parameters[1] = Acceleration(0.0, 0.0, 0.0, 1L) // acceleration

        method.invoke(fallDetector, *parameters)

        val field: Field = FallDetectorImpl::class.java.getDeclaredField("impactTimeOut")
        field.isAccessible = true

        val result = field.get(fallDetector) as Int

        assertEquals(-1, result)
    }

    @Test
    fun `check if return true when isMinMaxSumVector is greater than threshold`() {

        // for test need to set this sliding windows
        fallDetector.setMinMaxSW(fakeData.minMaxListWithDiffs)

        val method = fallDetector.javaClass.getDeclaredMethod(
            "isMinMaxSumVectorGreaterThanThreshold"
        )
        method.isAccessible = true

        method.invoke(fallDetector)

        assertEquals(true, method.invoke(fallDetector))
    }

    @Test
    fun `check if return false when isMinMaxSumVector is lower than threshold`() {

        // for test need to set this sliding windows
        fallDetector.setMinMaxSW(fakeData.minMaxListWithoutDiffs)

        val method = fallDetector.javaClass.getDeclaredMethod(
            "isMinMaxSumVectorGreaterThanThreshold"
        )
        method.isAccessible = true

        method.invoke(fallDetector)

        assertEquals(false, method.invoke(fallDetector))
    }

    @Test
    fun `check if detects posture when average vertical acceleration is greater than threshold`() {

        val method = fallDetector.javaClass.getDeclaredMethod(
            "detectPosture",
            Int::class.java,
            List::class.java
        )
        method.isAccessible = true

        val parameters = arrayOfNulls<Any>(2)

        // parameter
        parameters[0] = 0
        parameters[1] = fakeData.postureDetectionSWFakeHigh

        method.invoke(fallDetector, *parameters)

        val field: Field = FallDetectorImpl::class.java.getDeclaredField("isLyingPostureDetected")
        field.isAccessible = true

        val result = field.get(fallDetector) as Boolean

        assertEquals(true, result)
    }

    @Test
    fun `check if detects posture when average vertical acceleration is lower than threshold`() {

        val method = fallDetector.javaClass.getDeclaredMethod(
            "detectPosture",
            Int::class.java,
            List::class.java
        )
        method.isAccessible = true

        val parameters = arrayOfNulls<Any>(2)

        // parameter
        parameters[0] = 0
        parameters[1] = fakeData.postureDetectionSWFakeLow

        method.invoke(fallDetector, *parameters)

        val field: Field = FallDetectorImpl::class.java.getDeclaredField("isLyingPostureDetected")
        field.isAccessible = true

        val result = field.get(fallDetector) as Boolean

        assertEquals(false, result)
    }
}
