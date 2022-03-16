package pl.birski.falldetector.data

class Filter {

    fun lowPassFilter(input: FloatArray, output: FloatArray?, alpha: Float): FloatArray {
        if (output == null) return input
        for (i in input.indices) {
            output[i] = output[i] + alpha * (input[i] - output[i])
        }
        return output
    }
}
