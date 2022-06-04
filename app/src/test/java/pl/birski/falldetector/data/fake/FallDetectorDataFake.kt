package pl.birski.falldetector.data.fake

import pl.birski.falldetector.model.Acceleration

class FallDetectorDataFake {
    val minMaxListWithoutDiffs = mutableListOf<Acceleration>(
        Acceleration(0.0, 0.0, 1.0, 1L),
        Acceleration(0.0, 0.0, 1.0, 1L),
        Acceleration(0.0, 0.0, 1.0, 1L),
        Acceleration(0.0, 0.0, 1.0, 1L),
        Acceleration(0.0, 0.0, 1.0, 1L),
        Acceleration(0.0, 0.0, 1.0, 1L)
    )
    val minMaxListWithDiffs = mutableListOf<Acceleration>(
        Acceleration(0.0, 0.0, 0.0, 1L),
        Acceleration(0.3, 0.3, 0.3, 1L),
        Acceleration(0.6, 0.6, 0.6, 1L),
        Acceleration(0.8, 0.5, 0.5, 1L),
        Acceleration(1.0, 1.0, 1.0, 1L),
        Acceleration(1.2, 1.2, 1.2, 1L)
    )
    val postureDetectionSWFakeHigh = mutableListOf<Acceleration>(
        Acceleration(0.1, 0.3, 0.6, 1L),
        Acceleration(0.1, 0.3, 0.6, 1L),
        Acceleration(0.1, 0.3, 0.6, 1L),
        Acceleration(0.1, 0.3, 0.6, 1L),
    )
    val postureDetectionSWFakeLow = mutableListOf<Acceleration>(
        Acceleration(0.1, 0.3, 0.5, 1L),
        Acceleration(0.1, 0.3, 0.5, 1L),
        Acceleration(0.1, 0.3, 0.5, 1L),
        Acceleration(0.1, 0.3, 0.5, 1L),
    )
}
