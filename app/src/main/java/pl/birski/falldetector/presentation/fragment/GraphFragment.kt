package pl.birski.falldetector.presentation.fragment

import android.content.Context.SENSOR_SERVICE
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.github.mikephil.charting.data.Entry
import dagger.hilt.android.AndroidEntryPoint
import pl.birski.falldetector.databinding.FragmentGraphBinding
import pl.birski.falldetector.presentation.viewmodel.GraphViewModel
import pl.birski.falldetector.service.enum.DataSet

@AndroidEntryPoint
class GraphFragment : Fragment(), SensorEventListener {

    private var _binding: FragmentGraphBinding? = null
    private val binding get() = _binding!!

    private val viewModel: GraphViewModel by viewModels()

    private var mSensorManager: SensorManager? = null
    private var mAccelerometer: Sensor? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentGraphBinding.inflate(inflater, container, false)

        mSensorManager = requireActivity().getSystemService(SENSOR_SERVICE) as SensorManager
        mAccelerometer = mSensorManager!!.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

        if (mAccelerometer != null) {
            mSensorManager!!.registerListener(this, mAccelerometer, SensorManager.SENSOR_DELAY_GAME)
        }

        binding.start.setOnClickListener {
            viewModel.startService()
        }

        binding.stop.setOnClickListener {
            viewModel.stopService()
        }

        viewModel.mChart = binding.chart

        viewModel.apply {
            initChart()
            feedMultiple()
        }

        viewModel.initChart()

        viewModel.feedMultiple()

        return binding.root
    }



    override fun onPause() {
        super.onPause()
        mSensorManager!!.unregisterListener(this)
    }

    override fun onDestroy() {
        mSensorManager!!.unregisterListener(this@GraphFragment)
        super.onDestroy()
    }

    override fun onSensorChanged(p0: SensorEvent) {
        if (viewModel.plotData) {
            viewModel.addEntry(p0)
            Log.d("testuje", "onSensorChanged: ${p0.values[0]}, ${p0.values[1]}, ${p0.values[2]}")
            viewModel.plotData = false
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
