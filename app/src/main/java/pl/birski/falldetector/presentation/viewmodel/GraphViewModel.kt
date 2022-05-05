package pl.birski.falldetector.presentation.viewmodel

import android.app.Activity
import android.app.Application
import android.content.Intent
import android.graphics.Color
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.mikephil.charting.components.YAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import pl.birski.falldetector.R
import pl.birski.falldetector.data.LocationTracker
import pl.birski.falldetector.data.Normalizer
import pl.birski.falldetector.data.SensorImpl
import pl.birski.falldetector.model.Acceleration
import pl.birski.falldetector.model.AngularVelocity
import pl.birski.falldetector.other.PrefUtil
import pl.birski.falldetector.service.TrackingService
import pl.birski.falldetector.service.enum.DataSet
import pl.birski.falldetector.service.enum.ServiceActions
import timber.log.Timber

@HiltViewModel
class GraphViewModel
@Inject
constructor(
    private val application: Application,
    private val normalizer: Normalizer,
    private val locationTracker: LocationTracker,
    private val prefUtil: PrefUtil
) : ViewModel() {

    @Inject
    lateinit var sensor: SensorImpl

    private val _lineData = MutableLiveData<LineData?>()
    val lineData: LiveData<LineData?> get() = _lineData

    private val _velocity = MutableLiveData<AngularVelocity?>()
    val velocity: LiveData<AngularVelocity?> get() = _velocity

    private var plotData = true
    private var job: Job? = null
    private var thread: Thread? = null

    private val GRAPH_UPDATE_SLEEP_TIME = 50L
    private val THREAD_SLEEP_TIME = 10L

    private var isNormalized = false

    private suspend fun updateGraph(lineData: LineData?) {
        stopGraphUpdates()
        job = viewModelScope.launch {
            while (true) {
                measureAcceleration(lineData = lineData)
                delay(GRAPH_UPDATE_SLEEP_TIME)
            }
        }
    }

    private fun runGraphUpdate(lineData: LineData?) {
        viewModelScope.launch {
            updateGraph(lineData = lineData)
        }
    }

    private fun stopGraphUpdates() {
        job?.cancel()
        job = null
    }

    fun startService(lineData: LineData?) = sendCommandToService(ServiceActions.START_OR_RESUME)
        .also {
            sensor.initiateSensor(application)
            runGraphUpdate(lineData = lineData)
        }

    fun stopService() = sendCommandToService(ServiceActions.STOP)
        .also {
            sensor.stopMeasurement()
            stopGraphUpdates()
        }

    private fun sendCommandToService(action: ServiceActions) =
        Intent(application, TrackingService::class.java).also {
            it.action = action.name
            application.startService(it)
        }

    private fun measureAcceleration(lineData: LineData?) {
        sensor.acceleration.value?.let {
            Timber.d("Measured acceleration value is: $it")
            if (plotData) {
                addEntry(
                    acceleration = if (isNormalized) normalizer.normalize(it) else it,
                    lineData = lineData
                )
            }
            plotData = false
        }
        sensor.angularVelocity.value?.let {
            Timber.d("Measured angular velocity is: $it")
            _velocity.postValue(it)
        }
    }

    private fun createSet(axis: DataSet) = LineDataSet(null, selectDescription(axis = axis))
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
        DataSet.X_AXIS ->
            application.getString(R.string.graph_fragment_x_axis_acc_text)
        DataSet.Y_AXIS ->
            application.getString(R.string.graph_fragment_y_axis_acc_text)
        DataSet.Z_AXIS ->
            application.getString(R.string.graph_fragment_z_axis_acc_text)
    }

    private fun selectLineColor(axis: DataSet) = when (axis) {
        DataSet.X_AXIS -> Color.BLUE
        DataSet.Y_AXIS -> Color.GREEN
        DataSet.Z_AXIS -> Color.RED
    }

    private fun createEntry(
        acceleration: Acceleration,
        measurement: ILineDataSet,
        dataSet: DataSet
    ) = Entry(
        measurement.entryCount.toFloat(),
        selectValue(acceleration, dataSet).toFloat()
    )

    private fun selectValue(acceleration: Acceleration, dataSet: DataSet) = when (dataSet) {
        DataSet.X_AXIS -> acceleration.x ?: 0.0
        DataSet.Y_AXIS -> acceleration.y ?: 0.0
        DataSet.Z_AXIS -> acceleration.z ?: 0.0
    }

    private fun addEntry(acceleration: Acceleration, lineData: LineData?) {
        val data = lineData

        data?.let {

            val xMeasurement =
                data.getDataSetByIndex(0) ?: createSet(DataSet.X_AXIS).also { data.addDataSet(it) }

            val yMeasurement =
                data.getDataSetByIndex(1) ?: createSet(DataSet.Y_AXIS).also { data.addDataSet(it) }

            val zMeasurement =
                data.getDataSetByIndex(2) ?: createSet(DataSet.Z_AXIS).also { data.addDataSet(it) }

            data.addEntry(createEntry(acceleration, xMeasurement, DataSet.X_AXIS), 0)
            data.addEntry(createEntry(acceleration, yMeasurement, DataSet.Y_AXIS), 1)
            data.addEntry(createEntry(acceleration, zMeasurement, DataSet.Z_AXIS), 2)

            data.notifyDataChanged()
            _lineData.postValue(data)
        }
    }

    fun feedMultiple() {
        thread?.interrupt()
        thread = Thread {
            while (true) {
                plotData = true
                try {
                    Thread.sleep(THREAD_SLEEP_TIME)
                } catch (e: InterruptedException) {
                    e.printStackTrace()
                }
            }
        }
        thread?.start()
    }

    fun formatVelocityValue(dataSet: DataSet, value: Double): String = when (dataSet) {
        DataSet.X_AXIS -> {
            application.getString(
                R.string.graph_fragment_angular_velocity_x_value_string, round(3, value)
            )
        }
        DataSet.Y_AXIS -> {
            application.getString(
                R.string.graph_fragment_angular_velocity_y_value_string, round(3, value)
            )
        }
        DataSet.Z_AXIS -> {
            application.getString(
                R.string.graph_fragment_angular_velocity_z_value_string, round(3, value)
            )
        }
    }

    fun enableLocationService(activity: Activity) {
        if (!locationTracker.locationEnabled())
            locationTracker.showSettingsAlert(activity)
    }

    fun isGyroscopeEnabled() = prefUtil.isGyroscopeEnabled()

    private fun round(decimals: Int = 2, number: Number) = "%.${decimals}f".format(number).trim()
}
