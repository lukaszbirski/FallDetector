package pl.birski.falldetector.presentation

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import dagger.hilt.android.AndroidEntryPoint
import pl.birski.falldetector.BuildConfig
import pl.birski.falldetector.R
import pl.birski.falldetector.other.Constants
import pl.birski.falldetector.presentation.viewmodel.MainViewModel
import timber.log.Timber
import timber.log.Timber.DebugTree

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private val viewModel: MainViewModel by viewModels()

    private var mMessageReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent) {
            intent.action.let {
                Toast.makeText(context, "FALL DETECTED!", Toast.LENGTH_LONG).show()
                viewModel.stopService()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        IntentFilter(Constants.CUSTOM_FALL_DETECTED_RECEIVER).also {
            registerReceiver(mMessageReceiver, it)
        }

        if (BuildConfig.DEBUG) {
            Timber.plant(DebugTree())
        }
    }

    override fun onStop() {
        super.onStop()
        unregisterReceiver(mMessageReceiver)
    }
}
