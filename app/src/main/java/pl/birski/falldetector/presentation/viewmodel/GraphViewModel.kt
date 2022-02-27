package pl.birski.falldetector.presentation.viewmodel

import android.app.Application
import android.content.Intent
import android.graphics.Color
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.github.mikephil.charting.components.YAxis
import com.github.mikephil.charting.data.LineDataSet
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import pl.birski.falldetector.model.Acceleration
import pl.birski.falldetector.service.TrackingService
import pl.birski.falldetector.service.enum.DataSet
import pl.birski.falldetector.service.enum.ServiceActions

@HiltViewModel
class GraphViewModel
@Inject
constructor(
    private val application: Application
) : ViewModel() {

//    @Inject
//    lateinit var accelerometer: Accelerometer

    private var thread: Thread? = null
    var plotData = true

    private val _data: MutableLiveData<Acceleration> = MutableLiveData()
    val data: LiveData<Acceleration> get() = _data

//    init {
//        _data.postValue(getValues())
//    }

    fun startService() = sendCommandToService(ServiceActions.START_OR_RESUME)
//        .also { accelerometer.initiateSensor(application) }

    fun stopService() = sendCommandToService(ServiceActions.STOP)
        .also {
//            accelerometer.stopMeasurement()
            // startMeasurements()
        }

    private fun sendCommandToService(action: ServiceActions) =
        Intent(application, TrackingService::class.java).also {
            it.action = action.name
            application.startService(it)
        }

//    private fun startMeasurements() {
//        accelerometer.acceleration.value?.let {
//            Timber.d("Measured value: ${accelerometer.acceleration.value}")
//            _data.postValue(it)
//        }
//    }

//    fun getValues() = accelerometer.acceleration.value

    fun createSet(axis: DataSet) = LineDataSet(null, selectDescription(axis = axis))
        .also {
            it.axisDependency = YAxis.AxisDependency.LEFT
            it.lineWidth = 1f
            it.isHighlightEnabled = false
            it.setDrawValues(false)
            it.setDrawCircles(false)
            it.mode = LineDataSet.Mode.CUBIC_BEZIER
            it.cubicIntensity = 0.2f
            it.color = selectLineColor(axis = axis)
        }

    private fun selectDescription(axis: DataSet) = when (axis) {
        DataSet.X_AXIS -> "X-axis acceleration"
        DataSet.Y_AXIS -> "Y-axis acceleration"
        DataSet.Z_AXIS -> "Z-axis acceleration"
    }

    private fun selectLineColor(axis: DataSet) = when (axis) {
        DataSet.X_AXIS -> Color.BLUE
        DataSet.Y_AXIS -> Color.GREEN
        DataSet.Z_AXIS -> Color.RED
    }

    fun feedMultiple() {
        if (thread != null) {
            thread!!.interrupt()
        }
        thread = Thread {
            while (true) {
                plotData = true
                try {
                    Thread.sleep(10)
                } catch (e: InterruptedException) {
                    e.printStackTrace()
                }
            }
        }
        thread!!.start()
    }
}
