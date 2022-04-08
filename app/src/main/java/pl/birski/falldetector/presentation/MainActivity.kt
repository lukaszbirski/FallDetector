package pl.birski.falldetector.presentation

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.setupWithNavController
import dagger.hilt.android.AndroidEntryPoint
import pl.birski.falldetector.BuildConfig
import pl.birski.falldetector.R
import pl.birski.falldetector.databinding.ActivityMainBinding
import pl.birski.falldetector.other.Constants
import pl.birski.falldetector.presentation.listener.PassDataInterface
import pl.birski.falldetector.presentation.viewmodel.MainViewModel
import timber.log.Timber
import timber.log.Timber.DebugTree

@AndroidEntryPoint
class MainActivity : AppCompatActivity(), PassDataInterface {

    private var _binding: ActivityMainBinding? = null
    private val binding get() = _binding!!

    private val viewModel: MainViewModel by viewModels()

    private var isFallDetected = false

    private var mMessageReceiver = object : BroadcastReceiver() {

        override fun onReceive(context: Context?, intent: Intent) {

            intent.action.let {
                if (!isFallDetected) {
                    Toast.makeText(
                        context,
                        context?.getString(R.string.fall_detected_toast_text),
                        Toast.LENGTH_LONG
                    ).show()
                    isFallDetected = true
                    navigateToCounterFragment()
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityMainBinding.inflate(layoutInflater)

        if (BuildConfig.DEBUG) {
            Timber.plant(DebugTree())
        }

        registerBroadcastReceiver()

        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.main_nav_host_fragment) as NavHostFragment

        binding.doctorBottomNav.setupWithNavController(navHostFragment.navController)

        navHostFragment.findNavController()
            .addOnDestinationChangedListener { _, destination, _ ->
                when (destination.id) {
                    R.id.settingsFragment, R.id.graphFragment ->
                        binding.doctorBottomNav.visibility = View.VISIBLE
                    else -> binding.doctorBottomNav.visibility = View.GONE
                }
            }

        setContentView(binding.root)
    }

    override fun onStop() {
        super.onStop()
        unregisterBroadcastReceiver()
    }

    override fun onDataReceived(data: Boolean) {
        isFallDetected = data
    }

    private fun unregisterBroadcastReceiver() {
        unregisterReceiver(mMessageReceiver)
    }

    private fun registerBroadcastReceiver() {
        IntentFilter(Constants.CUSTOM_FALL_DETECTED_RECEIVER).also {
            registerReceiver(mMessageReceiver, it)
        }
    }

    private fun navigateToCounterFragment() {
        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.main_nav_host_fragment) as NavHostFragment
        val navController = navHostFragment.navController
        navController.navigate(R.id.action_graphFragment_to_counterFragment)
    }
}
