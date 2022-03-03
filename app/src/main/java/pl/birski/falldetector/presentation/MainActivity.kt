package pl.birski.falldetector.presentation

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import dagger.hilt.android.AndroidEntryPoint
import pl.birski.falldetector.BuildConfig
import pl.birski.falldetector.R
import pl.birski.falldetector.presentation.viewmodel.MainViewModel
import timber.log.Timber
import timber.log.Timber.DebugTree

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        if (BuildConfig.DEBUG) {
            Timber.plant(DebugTree())
        }
    }
}
