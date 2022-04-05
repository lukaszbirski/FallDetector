package pl.birski.falldetector.presentation.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.NavHostFragment
import dagger.hilt.android.AndroidEntryPoint
import pl.birski.falldetector.R
import pl.birski.falldetector.databinding.FragmentCounterBinding

@AndroidEntryPoint
class CounterFragment : Fragment() {

    private var _binding: FragmentCounterBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentCounterBinding.inflate(inflater, container, false)

        binding.counterFragmentButton.setOnClickListener {
            NavHostFragment.findNavController(this)
                .navigate(R.id.action_counterFragment_to_graphFragment)
        }

        return binding.root
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}
