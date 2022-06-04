package pl.birski.falldetector.data

import pl.birski.falldetector.model.Acceleration

interface IFallDetectorTest {

    fun setMinMaxSW(minMaxSW: MutableList<Acceleration>)

    fun setImpactTimeOutForTests()

    fun setPostureDetectionSW(postureDetectionSW: MutableList<Acceleration>)
}
