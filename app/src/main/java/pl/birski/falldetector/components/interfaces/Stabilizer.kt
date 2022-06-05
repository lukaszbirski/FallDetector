package pl.birski.falldetector.components.interfaces

import pl.birski.falldetector.model.Acceleration

interface Stabilizer {

    fun stabilizeSignal(previousAcc: Acceleration): Acceleration
}
