package pl.birski.falldetector.presentation

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController
import com.google.android.material.navigation.NavigationView
import dagger.hilt.android.AndroidEntryPoint
import pl.birski.falldetector.BuildConfig
import pl.birski.falldetector.R
import pl.birski.falldetector.databinding.ActivityMainBinding
import pl.birski.falldetector.other.Constants
import pl.birski.falldetector.presentation.fragment.CounterFragment
import pl.birski.falldetector.presentation.fragment.GraphFragment
import pl.birski.falldetector.presentation.fragment.SettingsFragment
import pl.birski.falldetector.presentation.listener.PassDataInterface
import pl.birski.falldetector.presentation.viewmodel.MainViewModel
import timber.log.Timber
import timber.log.Timber.DebugTree

@AndroidEntryPoint
class MainActivity :
    AppCompatActivity(),
    PassDataInterface,
    NavigationView.OnNavigationItemSelectedListener {

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
                    setCurrentFragment(CounterFragment())
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

        binding.bottomNav.setOnItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.graphFragment -> setCurrentFragment(GraphFragment())
                R.id.settingsFragment -> setCurrentFragment(SettingsFragment())
            }
            true
        }

        navHostFragment.findNavController()
            .addOnDestinationChangedListener { _, destination, _ ->
                when (destination.id) {
                    R.id.settingsFragment, R.id.graphFragment ->
                        binding.bottomNav.visibility = View.VISIBLE
                    else -> binding.bottomNav.visibility = View.GONE
                }
            }

        setSupportActionBar(binding.toolbar)

        val toggle = ActionBarDrawerToggle(
            this, binding.drawerLayout, binding.toolbar,
            R.string.accelerometer_not_supported_toast_text,
            R.string.gyroscope_not_supported_toast_text
        )

        binding.navView.setNavigationItemSelectedListener(this)

        binding.drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        setContentView(binding.root)
    }

    override fun onBackPressed() {
        if (binding.drawerLayout.isDrawerOpen(GravityCompat.START)) {
            binding.drawerLayout.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }

    override fun onStop() {
        super.onStop()
        unregisterBroadcastReceiver()
    }

    override fun onDataReceived(data: Boolean) {
        isFallDetected = data
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {

        when (item.itemId) {
            R.id.graphFragment -> setCurrentFragment(GraphFragment())
            R.id.settingsFragment -> setCurrentFragment(SettingsFragment())
        }

        binding.drawerLayout.closeDrawer(GravityCompat.START)
        return true
    }

    private fun unregisterBroadcastReceiver() {
        unregisterReceiver(mMessageReceiver)
    }

    private fun registerBroadcastReceiver() {
        IntentFilter(Constants.CUSTOM_FALL_DETECTED_RECEIVER).also {
            registerReceiver(mMessageReceiver, it)
        }
    }

    private fun setCurrentFragment(fragment: Fragment) =
        supportFragmentManager.beginTransaction().apply {
            replace(R.id.main_nav_host_fragment, fragment)
            commit()
        }
}
