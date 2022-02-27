package pl.birski.falldetector.presentation.fragment

import android.content.Context.SENSOR_SERVICE
import android.graphics.Color
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.components.YAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import dagger.hilt.android.AndroidEntryPoint
import pl.birski.falldetector.databinding.FragmentGraphBinding
import pl.birski.falldetector.presentation.viewmodel.GraphViewModel

@AndroidEntryPoint
class GraphFragment : Fragment(), SensorEventListener {

    private var _binding: FragmentGraphBinding? = null
    private val binding get() = _binding!!

    private val viewModel: GraphViewModel by viewModels()

    private var mSensorManager: SensorManager? = null
    private var mAccelerometer: Sensor? = null
    private val sensors: Sensor? = null
    private var mChart: LineChart? = null
    private var thread: Thread? = null
    private var plotData = true

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentGraphBinding.inflate(inflater, container, false)

        mSensorManager = requireActivity().getSystemService(SENSOR_SERVICE) as SensorManager
        mAccelerometer = mSensorManager!!.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION)
        val sensors = mSensorManager!!.getSensorList(Sensor.TYPE_ALL)

        if (mAccelerometer != null) {
            mSensorManager!!.registerListener(this, mAccelerometer, SensorManager.SENSOR_DELAY_GAME)
        }

        binding.start.setOnClickListener {
            viewModel.startService()
        }

        binding.stop.setOnClickListener {
            viewModel.stopService()
        }

        mChart = binding.chart

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
        leftAxis.axisMaximum = 20f
        leftAxis.axisMinimum = -20f
        leftAxis.setDrawGridLines(true)
        val rightAxis = mChart!!.axisRight
        rightAxis.isEnabled = false
        mChart!!.setDrawBorders(true)
        feedMultiple()

        return binding.root
    }

    private fun addEntry(event: SensorEvent) {
        val data = mChart!!.data
        if (data != null) {
            var setOne = data.getDataSetByIndex(0)

            if (setOne == null) {
                setOne = createSet()
                data.addDataSet(setOne)
            }

            data.addEntry(
                Entry(
                    setOne.entryCount.toFloat(),
                    event.values[0]
                ),
                0
            )
            data.notifyDataChanged()

            // let the chart know it's data has changed
            mChart!!.notifyDataSetChanged()

            // limit the number of visible entries
            mChart!!.setVisibleXRangeMaximum(150f)
            // mChart.setVisibleYRange(30, AxisDependency.LEFT);

            // move to the latest entry
            mChart!!.moveViewToX(data.entryCount.toFloat())
        }
    }

    private fun createSet(): LineDataSet {
        val set = LineDataSet(null, "X-axis acceleration")
        set.axisDependency = YAxis.AxisDependency.LEFT
        set.lineWidth = 1f
        set.color = Color.BLUE
        set.isHighlightEnabled = false
        set.setDrawValues(false)
        set.setDrawCircles(false)
        set.mode = LineDataSet.Mode.CUBIC_BEZIER
        set.cubicIntensity = 0.2f
        return set
    }

    private fun feedMultiple() {
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

    override fun onPause() {
        super.onPause()
        if (thread != null) {
            thread!!.interrupt()
        }
        mSensorManager!!.unregisterListener(this)
    }

    override fun onDestroy() {
        mSensorManager!!.unregisterListener(this@GraphFragment)
        thread!!.interrupt()
        super.onDestroy()
    }

    override fun onSensorChanged(p0: SensorEvent) {
        if (plotData) {
            addEntry(p0)
            plotData = false
        }
    }

    override fun onResume() {
        super.onResume()
        mSensorManager!!.registerListener(this, mAccelerometer, SensorManager.SENSOR_DELAY_GAME)
    }

    override fun onAccuracyChanged(p0: Sensor?, p1: Int) {
        // Do something here if sensor accuracy changes.
    }
}
