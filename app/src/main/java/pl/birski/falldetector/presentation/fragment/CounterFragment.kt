package pl.birski.falldetector.presentation.fragment

import android.os.Bundle
import android.os.CountDownTimer
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

    private lateinit var timer: CountDownTimer

    private var timerLengthSeconds = 120L
    private var secondsRemaining = 120L

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

            NavHostFragment.findNavController(this)
                .navigate(R.id.action_counterFragment_to_graphFragment)
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

        timer = object : CountDownTimer(secondsRemaining * 1000, 1000) {

            override fun onFinish() = onTimerFinished()

            override fun onTick(millisUntilFinished: Long) {
                secondsRemaining = millisUntilFinished / 1000
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
        val minutesUntilFinished = secondsRemaining / 60
        val secondsInMinuteUntilFinished = secondsRemaining - minutesUntilFinished * 60
        val secondsStr = secondsInMinuteUntilFinished.toString()
        binding.countdownTextView.text =
            "$minutesUntilFinished:${if (secondsStr.length == 2) secondsStr else "0 $secondsStr"}"
        binding.progressCountDown.progress = (timerLengthSeconds - secondsRemaining).toInt()
    }

    private fun setTimerLength() {
        binding.progressCountDown.max = timerLengthSeconds.toInt()
    }
}
