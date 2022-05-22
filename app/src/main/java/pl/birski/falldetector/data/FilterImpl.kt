package pl.birski.falldetector.data

import pl.birski.falldetector.model.HighPassFilterData

class FilterImpl : Filter {

    override fun lowPassFilter(input: FloatArray, output: FloatArray?, alpha: Float): FloatArray {
        if (output == null) return input
        for (i in input.indices) {
            output[i] = alpha * output[i] + (1 - alpha) * input[i]
        }
        return output
    }

    override fun highPassFilter(
        input: FloatArray,
        hpfData: HighPassFilterData,
        alpha: Float
    ): HighPassFilterData {
        for (i in hpfData.gravity.indices) {
            hpfData.gravity[i] = alpha * hpfData.gravity[i] + (1 - alpha) * input[i]
            hpfData.acceleration[i] = input[i] - hpfData.gravity[i]
        }
        return hpfData
    }

    override fun calculateAlpha(cutOffFrequency: Double, frequency: Double): Float {
        val dt = 1F.div(frequency)
        val period = 1F.div(cutOffFrequency)
        return period.div(dt + period).toFloat()
    }
}
