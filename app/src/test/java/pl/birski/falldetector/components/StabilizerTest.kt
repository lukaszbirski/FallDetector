package pl.birski.falldetector.components

import org.junit.Before
import org.junit.Test
import org.junit.jupiter.api.Assertions.assertEquals
import pl.birski.falldetector.components.implementations.StabilizerImpl
import pl.birski.falldetector.components.interfaces.Stabilizer
import pl.birski.falldetector.model.Acceleration

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

    @Test
    fun `check if resample returns correct value`() {

        val method = stabilizer.javaClass.getDeclaredMethod(
            "resample",
            Acceleration::class.java,
            Acceleration::class.java
        )

        method.isAccessible = true

        val parameters = arrayOfNulls<Any>(2)

        // parameters
        parameters[0] = Acceleration(0.0, 0.0, 0.0, 50) // currentAcceleration
        parameters[1] = Acceleration(2.0, 2.0, 2.0, 30) // previousAcceleration

        val result = method.invoke(stabilizer, *parameters) as Acceleration

        assertEquals(Acceleration(1.0, 1.0, 1.0, 40), result)
    }

    @Test
    fun `check if stabilizeSignal returns correct value`() {

        // first runs are to set system, it should start to work properly after 3 iterations
        stabilizer.stabilizeSignal(Acceleration(2.0, 2.0, 2.0, 18))
        stabilizer.stabilizeSignal(Acceleration(1.0, 1.0, 1.0, 37))

        var result = stabilizer.stabilizeSignal(Acceleration(1.0, 1.0, 1.0, 56))

        assertEquals(Acceleration(1.0, 1.0, 1.0, 38), result)

        result = stabilizer.stabilizeSignal(Acceleration(0.9, 0.9, 0.9, 76))

        assertEquals(Acceleration(0.99, 0.99, 0.99, 58), result)

        result = stabilizer.stabilizeSignal(Acceleration(1.5, 1.5, 1.5, 95))

        assertEquals(
            Acceleration(0.9631578947368421, 0.9631578947368421, 0.9631578947368421, 78),
            result
        )

        result = stabilizer.stabilizeSignal(Acceleration(1.0, 1.0, 1.0, 114))

        assertEquals(
            Acceleration(1.4210526315789473, 1.4210526315789473, 1.4210526315789473, 98),
            result
        )
    }
}
