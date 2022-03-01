package pl.birski.falldetector.presentation.fragment

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.data.LineData
import dagger.hilt.android.AndroidEntryPoint
import pl.birski.falldetector.databinding.FragmentGraphBinding
import pl.birski.falldetector.presentation.viewmodel.GraphViewModel

@AndroidEntryPoint
class GraphFragment : Fragment() {

    private var _binding: FragmentGraphBinding? = null
    private val binding get() = _binding!!

    private val viewModel: GraphViewModel by viewModels()

    private val VISIBLE_X_RANGE_MAX = 150F
    private val MAX_Y_AXIS_VALUE = 18F
    private val MIN_Y_AXIS_VALUE = -18F

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentGraphBinding.inflate(inflater, container, false)

        binding.chart.apply {
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

        binding.start.setOnClickListener {
            viewModel.startService(binding.chart.lineData)
        }

        binding.stop.setOnClickListener {
            viewModel.stopService()
        }

        viewModel.apply {
            feedMultiple()
            lineData.observe(viewLifecycleOwner) {
                binding.chart.notifyDataSetChanged()
                binding.chart.setVisibleXRangeMaximum(VISIBLE_X_RANGE_MAX)
                it?.entryCount?.toFloat()?.let { count -> binding.chart.moveViewToX(count) }
            }
        }

        return binding.root
    }
}
