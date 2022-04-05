package pl.birski.falldetector.data

import pl.birski.falldetector.model.HighPassFilterData

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
        hpfData: HighPassFilterData,
        alpha: Float
    ): HighPassFilterData {
        for (i in hpfData.gravity.indices) {
            hpfData.gravity[i] = (1 - alpha) * hpfData.gravity[i] + alpha * input[i]
            hpfData.acceleration[i] = input[i] - hpfData.gravity[i]
        }
        return hpfData
    }

    fun calculateAlpha(cutOffFrequency: Double, frequency: Double): Float {
        val dt = 1F.div(frequency)
        val period = 1F.div(cutOffFrequency)
        return dt.div(dt + period).toFloat()
    }
}
