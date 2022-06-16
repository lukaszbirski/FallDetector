package pl.birski.falldetector.components

import org.junit.Before
import org.junit.Test
import org.junit.jupiter.api.Assertions.assertEquals
import pl.birski.falldetector.components.implementations.StabilizerImpl
import pl.birski.falldetector.model.Acceleration

class StabilizerTest {

    // system in test
    private lateinit var stabilizer: StabilizerImpl

    @Before
    fun setup() {
        stabilizer = StabilizerImpl()
    }

    @Test
    fun `check if linearRecalculation returns correct value`() {
        // parameters
        val timeAfter = 4L // timeAfter
        val valueAfter = 0.0 // valueAfter
        val timePrevious = 0L // timePrevious
        val valuePrevious = 100.0 //  valuePrevious
        val currentTime = 3L // currentTime

        val result = stabilizer.linearRecalculation(
            timeAfter = timeAfter,
            valueAfter = valueAfter,
            timePrevious = timePrevious,
            valuePrevious = valuePrevious,
            currentTime = currentTime
        )

        assertEquals(25.00, result, 0.05)
    }

    @Test
    fun `check if resample returns correct value`() {
        // parameters
        val currentAcceleration = Acceleration(0.0, 0.0, 0.0, 50) // currentAcceleration
        val previousAcceleration = Acceleration(2.0, 2.0, 2.0, 30) // previousAcceleration

        val result = stabilizer.resample(currentAcceleration, previousAcceleration)

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
