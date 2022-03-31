package pl.birski.falldetector.data

class Filter {

    fun lowPassFilter(input: FloatArray, output: FloatArray?, alpha: Float): FloatArray {
        if (output == null) return input
        for (i in input.indices) {
            output[i] = output[i] + alpha * (input[i] - output[i])
        }
        return output
    }

    fun highPassFilter(
        input: FloatArray,
        output: FloatArray,
        gravity: FloatArray,
        alpha: Float
    ): List<FloatArray> {

        for (i in gravity.indices) {
            gravity[i] = alpha * gravity[i] + (1 - alpha) * input[i]
            output[i] = input[i] - gravity[i]
        }

        var resultList = mutableListOf<FloatArray>()
        resultList.add(gravity)
        resultList.add(output)
        return resultList
    }

    fun calculateAlpha(cutOffFrequency: Double, frequency: Double): Float {
        val dt = 1F.div(frequency)
        val period = 1F.div(cutOffFrequency)
        return dt.div(dt + period).toFloat()
    }
}
