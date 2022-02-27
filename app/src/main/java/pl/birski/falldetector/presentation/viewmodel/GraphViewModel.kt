package pl.birski.falldetector.presentation.viewmodel

import android.app.Application
import android.content.Intent
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.jjoe64.graphview.series.DataPoint
import com.jjoe64.graphview.series.LineGraphSeries
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import pl.birski.falldetector.data.Accelerometer
import pl.birski.falldetector.service.TrackingService
import pl.birski.falldetector.service.enum.ServiceActions
import timber.log.Timber

@HiltViewModel
class GraphViewModel
@Inject
constructor(
    private val application: Application
) : ViewModel() {

    @Inject
    lateinit var accelerometer: Accelerometer

    private val graphIntervalCounter = 0
    private var dataSet: LineGraphSeries<DataPoint>? = null

    val pointsPlotted: MutableState<Double> = mutableStateOf(5.0)

    init {
        dataSet = LineGraphSeries(
            arrayOf(
                DataPoint(0.0, 0.0)
            )
        )
    }

    fun startService() = sendCommandToService(ServiceActions.START_OR_RESUME)
        .also { accelerometer.initiateSensor(application) }

    fun stopService() = sendCommandToService(ServiceActions.STOP)
        .also { accelerometer.stopMeasurement() }

    private fun sendCommandToService(action: ServiceActions) =
        Intent(application, TrackingService::class.java).also {
            it.action = action.name
            application.startService(it)
        }

    fun startMeasurements() {
        accelerometer.acceleration.value?.let {
            Timber.d("Measured value: ${accelerometer.acceleration.value}")
            pointsPlotted.value++
            dataSet?.appendData(
                DataPoint(
                    pointsPlotted.value, accelerometer.acceleration.value?.x!!
                ),
                true,
                pointsPlotted.value.toInt()
            )
            resetView()
        }
    }

    fun getValuesForGraph() = dataSet

    private fun resetView() {
        if (pointsPlotted.value > 100) {
            pointsPlotted.value = 0.0
            dataSet?.resetData(arrayOf(DataPoint(0.0, 0.0)))
        }
    }
}
