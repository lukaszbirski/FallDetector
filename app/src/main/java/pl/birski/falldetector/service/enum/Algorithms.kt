package pl.birski.falldetector.service.enum

enum class Algorithms(val number: String) {
    IMPACT_POSTURE("1"),
    START_IMPACT_POSTURE("2"),
    START_VELOCITY_IMPACT_POSTURE("3");

    companion object {
        fun getByValue(number: String) = values().find { it.number == number }
    }
}
