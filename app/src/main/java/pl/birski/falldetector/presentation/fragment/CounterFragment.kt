package pl.birski.falldetector.presentation.fragment

import android.os.Bundle
import android.os.CountDownTimer
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import dagger.hilt.android.AndroidEntryPoint
import pl.birski.falldetector.R
import pl.birski.falldetector.databinding.FragmentCounterBinding
import pl.birski.falldetector.presentation.viewmodel.CounterViewModel

@AndroidEntryPoint
class CounterFragment : Fragment() {

    private var _binding: FragmentCounterBinding? = null
    private val binding get() = _binding!!

    private val viewModel: CounterViewModel by viewModels()

    private lateinit var timer: CountDownTimer

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentCounterBinding.inflate(inflater, container, false)

        startTimer()

        binding.counterFragmentButton.setOnClickListener {
            timer.cancel()
            onTimerFinished()

            parentFragmentManager.beginTransaction().apply {
                replace(R.id.main_nav_host_fragment, GraphFragment())
                    .commit()
            }
        }

        return binding.root
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

    override fun onResume() {
        super.onResume()
        initTimer()
    }

    private fun startTimer() {

        timer = object : CountDownTimer(viewModel.getSecondsRemaining() * 1000, 1000) {

            override fun onFinish() = onTimerFinished()

            override fun onTick(millisUntilFinished: Long) {
                viewModel.updateSecondsRemaining(millisUntilFinished)
                updateCountdownView()
            }
        }.start()
    }

    private fun initTimer() {
        setTimerLength()
        updateCountdownView()
    }

    private fun onTimerFinished() {
        binding.progressCountDown.progress = 0
        updateCountdownView()
    }

    private fun updateCountdownView() {
        binding.countdownTextView.text = viewModel.getTextForCountdownTextView()
        binding.progressCountDown.progress = viewModel.countProgress()
    }

    private fun setTimerLength() {
        binding.progressCountDown.max = viewModel.getTimerLengthSeconds().toInt()
    }
}
