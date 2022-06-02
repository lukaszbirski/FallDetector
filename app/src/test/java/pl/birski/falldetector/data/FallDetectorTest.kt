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
import pl.birski.falldetector.model.Acceleration
import pl.birski.falldetector.other.PrefUtil

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [Build.VERSION_CODES.P])
@DoNotInstrument
class FallDetectorTest {

    // system in test
    private lateinit var fallDetector: FallDetector

    private lateinit var filter: Filter
    private lateinit var prefUtil: PrefUtil

    private val context: Context = ApplicationProvider.getApplicationContext<Context>()

    @Before
    fun setup() {
        filter = FilterImpl()
        prefUtil = PrefUtil(context)
        fallDetector = FallDetectorImpl(context, filter, prefUtil)
    }

    @Test
    fun checkIfReturnsTrueWhenSumVectorIsGreaterThanThreshold() {

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
    fun checkIfReturnsFalseWhenSumVectorIsLowerThanThreshold() {

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
    fun checkIfReturnsFalseWhenSumVectorIsEqualThreshold() {

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
    fun checkIfCalculateSumVectorReturnsCorrectValue() {

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
    fun checkIfExpireTimeOutReturnsGivenValueMinusOne() {

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
    fun checkIfExpireTimeOutReturnsMinusOneWhenMinusOneGiven() {

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
    fun checkIfCalculateVerticalAccelerationReturnsCorrectValue() {

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
    fun checkIfReturnTrueWhenVerticalAccelerationIsGreaterThanThreshold() {

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
    fun checkIfReturnsFalseWhenVerticalAccelerationIsLowerThanThreshold() {

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
    fun checkIfNotDetectsFallWhenAccelerationIsGreaterThanThreshold() {
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
    fun checkIfDetectsFallAccelerationIsLowerThanThreshold() {
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
}
