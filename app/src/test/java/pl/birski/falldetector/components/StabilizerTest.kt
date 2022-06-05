package pl.birski.falldetector.components

import org.junit.Before
import org.junit.Test
import org.junit.jupiter.api.Assertions.assertEquals
import pl.birski.falldetector.components.implementations.StabilizerImpl
import pl.birski.falldetector.components.interfaces.Stabilizer

class StabilizerTest {

    // system in test
    private lateinit var stabilizer: Stabilizer

    @Before
    fun setup() {
        stabilizer = StabilizerImpl()
    }

    @Test
    fun `check if linearRecalculation returns correct value`() {

        val method = stabilizer.javaClass.getDeclaredMethod(
            "linearRecalculation",
            Long::class.java,
            Double::class.java,
            Long::class.java,
            Double::class.java,
            Long::class.java
        )

        method.isAccessible = true

        val parameters = arrayOfNulls<Any>(5)

        // parameters
        parameters[0] = 4 // timeAfter
        parameters[1] = 0 // valueAfter
        parameters[2] = 0 // timePrevious
        parameters[3] = 100 //  valuePrevious
        parameters[4] = 3 // currentTime

        val result = method.invoke(stabilizer, *parameters) as Double

        assertEquals(25.00, result, 0.05)
    }
}
