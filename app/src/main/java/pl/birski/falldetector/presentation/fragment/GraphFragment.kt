package pl.birski.falldetector.presentation.fragment

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.data.LineData
import dagger.hilt.android.AndroidEntryPoint
import pl.birski.falldetector.databinding.FragmentGraphBinding
import pl.birski.falldetector.extensions.visibleOrGone
import pl.birski.falldetector.presentation.listener.PassDataInterface
import pl.birski.falldetector.presentation.viewmodel.GraphViewModel
import pl.birski.falldetector.service.enum.DataSet

@AndroidEntryPoint
class GraphFragment : Fragment() {

    private var _binding: FragmentGraphBinding? = null
    private val binding get() = _binding!!

    private val viewModel: GraphViewModel by viewModels()

    private val VISIBLE_X_RANGE_MAX = 150F
    private val MAX_Y_AXIS_VALUE = 1.5F
    private val MIN_Y_AXIS_VALUE = -1.5F

    private lateinit var passDataInterface: PassDataInterface

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentGraphBinding.inflate(inflater, container, false)

        passDataInterface = requireActivity() as PassDataInterface

        setChart(binding.chart)
        setInitialVelocities()

        binding.start.setOnClickListener {
            viewModel.startService(binding.chart.lineData)
            passDataInterface.onDataReceived(false)
        }

        binding.stop.setOnClickListener {
            viewModel.stopService()
            passDataInterface.onDataReceived(true)
        }

        viewModel.apply {
            enableLocationService(requireActivity())

            feedMultiple()

            binding.velocityLinearLayout.visibleOrGone(isGyroscopeEnabled())

            lineData.observe(viewLifecycleOwner) {
                binding.chart.notifyDataSetChanged()
                binding.chart.setVisibleXRangeMaximum(VISIBLE_X_RANGE_MAX)
                it?.entryCount?.toFloat()?.let { count -> binding.chart.moveViewToX(count) }
            }

            velocity.observe(viewLifecycleOwner) {
                it?.let {
                    binding.velocityXTextView.text = formatVelocityValue(DataSet.X_AXIS, it.x)
                    binding.velocityYTextView.text = formatVelocityValue(DataSet.Y_AXIS, it.y)
                    binding.velocityZTextView.text = formatVelocityValue(DataSet.Z_AXIS, it.z)
                }
            }
        }

        return binding.root
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

    private fun setChart(chart: LineChart) {
        chart.apply {
            // disable description text
            description.isEnabled = false

            // enable touch gestures
            setTouchEnabled(false)

            // enable scaling and dragging
            isDragEnabled = false
            setScaleEnabled(true)
            setDrawGridBackground(true)

            // if disabled, scaling can be done on x- and y-axis separately
            setPinchZoom(true)

            // set an alternative background color
            setBackgroundColor(Color.WHITE)
            val lineData = LineData()
            lineData.setValueTextColor(Color.WHITE)

            // add empty data
            data = lineData

            // get the legend (only possible after setting data)
            val l = legend

            // modify the legend ...
            l.form = Legend.LegendForm.LINE
            l.textColor = Color.BLACK
            val xl = xAxis
            xl.textColor = Color.WHITE
            xl.setDrawGridLines(true)
            xl.setAvoidFirstLastClipping(true)
            xl.isEnabled = true
            val leftAxis = axisLeft
            leftAxis.textColor = Color.BLACK
            leftAxis.setDrawGridLines(true)
            leftAxis.axisMaximum = MAX_Y_AXIS_VALUE
            leftAxis.axisMinimum = MIN_Y_AXIS_VALUE
            leftAxis.setDrawGridLines(true)
            val rightAxis = axisRight
            rightAxis.isEnabled = false
            setDrawBorders(true)
        }
    }

    private fun setInitialVelocities() {
        binding.velocityXTextView.text = viewModel.formatVelocityValue(DataSet.X_AXIS, 0.0)
        binding.velocityYTextView.text = viewModel.formatVelocityValue(DataSet.Y_AXIS, 0.0)
        binding.velocityZTextView.text = viewModel.formatVelocityValue(DataSet.Z_AXIS, 0.0)
    }
}
