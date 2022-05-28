package pl.birski.falldetector.data

import android.content.Context
import android.os.Build
import androidx.test.core.app.ApplicationProvider
import org.junit.Before
import org.junit.Test
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.internal.DoNotInstrument
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
    fun checkIfIsSumVectorGreaterThanThresholdReturnTrueWhenSumIsGreaterThanThreshold() {

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
    fun checkIfIsSumVectorGreaterThanThresholdReturnTrueWhenSumIsLowerThanThreshold() {

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
    fun checkIfIsSumVectorGreaterThanThresholdReturnTrueWhenSumIsEqualThreshold() {

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
}
