package pl.birski.falldetector.presentation.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import dagger.hilt.android.AndroidEntryPoint
import pl.birski.falldetector.databinding.FragmentGraphBinding
import pl.birski.falldetector.presentation.viewmodel.GraphViewModel

@AndroidEntryPoint
class GraphFragment : Fragment() {

    private var _binding: FragmentGraphBinding? = null
    private val binding get() = _binding!!

    private val viewModel: GraphViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentGraphBinding.inflate(inflater, container, false)

        viewModel.mChart = binding.chart

        binding.start.setOnClickListener {
            viewModel.startService()
        }

        binding.stop.setOnClickListener {
            viewModel.stopService()
        }

        viewModel.apply {
            initChart()
            feedMultiple()
        }

        return binding.root
    }
}
