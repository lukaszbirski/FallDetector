package pl.birski.falldetector.model

data class HighPassFilterData(val acceleration: FloatArray, val gravity: FloatArray) {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as HighPassFilterData

        if (!acceleration.contentEquals(other.acceleration)) return false
        if (!gravity.contentEquals(other.gravity)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = acceleration.contentHashCode()
        result = 31 * result + gravity.contentHashCode()
        return result
    }
}
