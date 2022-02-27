package pl.birski.falldetector.presentation.viewmodel

import android.app.Application
import android.content.Intent
import android.graphics.Color
import android.hardware.SensorEvent
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
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
    var mChart: LineChart? = null
    var plotData = true

    private val _data: MutableLiveData<Acceleration> = MutableLiveData()
    val data: LiveData<Acceleration> get() = _data

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

    private fun createEntry(event: SensorEvent, measurement: ILineDataSet, number: Int) = Entry(
        measurement.entryCount.toFloat(),
        event.values[number]
    )

    fun addEntry(event: SensorEvent) {
        val data = mChart?.data

        data?.let {

            val xMeasurement =
                data.getDataSetByIndex(0) ?: createSet(DataSet.X_AXIS).also { data.addDataSet(it) }

            val yMeasurement =
                data.getDataSetByIndex(1) ?: createSet(DataSet.Y_AXIS).also { data.addDataSet(it) }

            val zMeasurement =
                data.getDataSetByIndex(2) ?: createSet(DataSet.Z_AXIS).also { data.addDataSet(it) }

            data.addEntry(createEntry(event, xMeasurement, 0), 0)
            data.addEntry(createEntry(event, yMeasurement, 1), 1)
            data.addEntry(createEntry(event, zMeasurement, 2), 2)

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
                    Thread.sleep(10)
                } catch (e: InterruptedException) {
                    e.printStackTrace()
                }
            }
        }
        thread!!.start()
    }
}
