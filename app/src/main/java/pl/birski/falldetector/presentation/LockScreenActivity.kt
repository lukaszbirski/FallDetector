package pl.birski.falldetector.presentation

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.CountDownTimer
import android.view.WindowManager
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import dagger.hilt.android.AndroidEntryPoint
import pl.birski.falldetector.databinding.ActivityLockScreenBinding
import pl.birski.falldetector.presentation.viewmodel.LockScreenViewModel

@AndroidEntryPoint
class LockScreenActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLockScreenBinding

    private val viewModel: LockScreenViewModel by viewModels()

    private lateinit var timer: CountDownTimer

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        showWhenLockedAndTurnScreenOn()

        binding = ActivityLockScreenBinding.inflate(layoutInflater)

        startTimer()

        supportActionBar?.hide()

        binding.counterFragmentButton.setOnClickListener {
            timer.cancel()
            onTimerFinished()
            Intent(this, MainActivity::class.java).also {
                startActivity(it)
            }
        }

        setContentView(binding.root)
    }

    private fun showWhenLockedAndTurnScreenOn() {
        window.addFlags(
            WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON or
                WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD or
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
        )

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setTurnScreenOn(true)
            setShowWhenLocked(true)
        }
    }

    override fun onResume() {
        super.onResume()
        initTimer()
    }

    private fun startTimer() {

        timer = object : CountDownTimer(viewModel.getSecondsRemaining() * 1000, 1000) {

            override fun onFinish() = onTimerFinished().also { viewModel.sendMessages() }

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
