package pl.birski.falldetector.presentation.viewmodel

import android.app.Application
import android.content.Intent
import android.graphics.Color
import androidx.lifecycle.ViewModel
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.components.YAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.Job
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import pl.birski.falldetector.data.Accelerometer
import pl.birski.falldetector.model.Acceleration
import pl.birski.falldetector.service.TrackingService
import pl.birski.falldetector.service.enum.DataSet
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

    private var thread: Thread? = null
    var mChart: LineChart? = null

    private var plotData = true
    private var job: Job? = null
    private val GRAPH_UPDATE_SLEEP_TIME = 50L
    private val THREAD_SLEEP_TIME = 10L

    private suspend fun updateGraph() {
        stopGraphUpdates()
        job = MainScope().launch {
            while (true) {
                measureAcceleration()
                delay(GRAPH_UPDATE_SLEEP_TIME)
            }
        }
    }

    private fun runGraphUpdate() {
        MainScope().launch {
            updateGraph()
        }
    }

    private fun stopGraphUpdates() {
        job?.cancel()
        job = null
    }

    fun initChart() {
        // disable description text
        mChart!!.description.isEnabled = false

        // enable touch gestures
        mChart!!.setTouchEnabled(false)

        // enable scaling and dragging
        mChart!!.isDragEnabled = false
        mChart!!.setScaleEnabled(true)
        mChart!!.setDrawGridBackground(true)

        // if disabled, scaling can be done on x- and y-axis separately
        mChart!!.setPinchZoom(true)

        // set an alternative background color
        mChart!!.setBackgroundColor(Color.WHITE)
        val data = LineData()
        data.setValueTextColor(Color.WHITE)

        // add empty data
        mChart!!.data = data

        // get the legend (only possible after setting data)
        val l = mChart!!.legend

        // modify the legend ...
        l.form = Legend.LegendForm.LINE
        l.textColor = Color.BLACK
        val xl = mChart!!.xAxis
        xl.textColor = Color.WHITE
        xl.setDrawGridLines(true)
        xl.setAvoidFirstLastClipping(true)
        xl.isEnabled = true
        val leftAxis = mChart!!.axisLeft
        leftAxis.textColor = Color.BLACK
        leftAxis.setDrawGridLines(true)
        leftAxis.axisMaximum = 18f
        leftAxis.axisMinimum = -18f
        leftAxis.setDrawGridLines(true)
        val rightAxis = mChart!!.axisRight
        rightAxis.isEnabled = false
        mChart!!.setDrawBorders(true)
    }

    fun startService() = sendCommandToService(ServiceActions.START_OR_RESUME)
        .also {
            accelerometer.initiateSensor(application)
            runGraphUpdate()
        }

    fun stopService() = sendCommandToService(ServiceActions.STOP)
        .also {
            accelerometer.stopMeasurement()
            stopGraphUpdates()
        }

    private fun sendCommandToService(action: ServiceActions) =
        Intent(application, TrackingService::class.java).also {
            it.action = action.name
            application.startService(it)
        }

    private fun measureAcceleration() {
        accelerometer.acceleration.value?.let {
            Timber.d("Measured value: $it")
            if (plotData) {
                addEntry(it)
            }
            plotData = false
        }
    }

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

    private fun addEntry(acceleration: Acceleration) {
        val data = mChart?.data

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

            // let the chart know it's data has changed
            mChart!!.notifyDataSetChanged()

            // limit the number of visible entries
            mChart!!.setVisibleXRangeMaximum(150f)

            // move to the latest entry
            mChart!!.moveViewToX(data.entryCount.toFloat())
        }
    }

    fun feedMultiple() {
        if (thread != null) {
            thread!!.interrupt()
        }
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
        thread!!.start()
    }
}
