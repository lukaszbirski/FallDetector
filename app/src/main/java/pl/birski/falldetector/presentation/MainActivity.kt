package pl.birski.falldetector.presentation

import android.content.BroadcastReceiver
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
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
import pl.birski.falldetector.other.Constants.PERMISSION_REQUEST_CODE
import pl.birski.falldetector.other.PermissionUtil
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

    private lateinit var binding: ActivityMainBinding

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

                    Intent(context, LockScreenActivity::class.java).also {
                        it.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                        it.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
                        it.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                        it.addFlags(Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED)
                        startActivity(it)
                    }
                }
            }
        }
    }

    // TODO in future most likely will need to move permission code into different fragment
    private var requestSinglePermission = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) {
        it.entries.forEachIndexed() { index, _ ->
            PermissionUtil.returnPermissionsArray()[index]
        }
        checkPermissions()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)

        if (BuildConfig.DEBUG) {
            Timber.plant(DebugTree())
        }

        registerBroadcastReceiver()

        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.main_nav_host_fragment) as NavHostFragment

        binding.bottomNav.setOnItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.graphFragment -> navigateToFragment(GraphFragment())
                R.id.settingsFragment -> navigateToFragment(SettingsFragment())
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
            R.string.navigation_drawer_open_text,
            R.string.navigation_drawer_closed_text
        )

        binding.navView.setNavigationItemSelectedListener(this)

        binding.drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        // TODO in future most likely will need to move permission code into different fragment
        requestSinglePermission.launch(
            PermissionUtil.returnPermissionsArray()
        )

        setContentView(binding.root)
    }

    override fun onBackPressed() {
        if (binding.drawerLayout.isDrawerOpen(GravityCompat.START)) {
            binding.drawerLayout.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterBroadcastReceiver()
    }

    override fun onDataReceived(data: Boolean) {
        isFallDetected = data
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {

        when (item.itemId) {
            R.id.graphFragment -> navigateToFragment(GraphFragment())
            R.id.settingsFragment -> navigateToFragment(SettingsFragment())
        }

        binding.drawerLayout.closeDrawer(GravityCompat.START)
        return true
    }

    // TODO in future most likely will need to move permission code into different fragment
    private fun checkPermissions() {
        if (!PermissionUtil.hasMessagesPermission(this))
            setDialog()
    }

    // TODO in future most likely will need to move permission code into different fragment
    private fun setDialog() {
        this.let {
            val builder = AlertDialog.Builder(it)
            builder.apply {
                setTitle(R.string.permission_dialog_title_text)
                setCancelable(false)
                setMessage(R.string.permission_dialog_message_text)
                setPositiveButton(
                    R.string.permission_dialog_dismiss_text,
                    DialogInterface.OnClickListener { dialog, id ->
                        askForPermissions()
                    }
                )
            }

            builder.create()
            builder.show()
        }
    }

    // TODO in future most likely will need to move permission code into different fragment
    private fun askForPermissions() = ActivityCompat.requestPermissions(
        this, PermissionUtil.returnPermissionsArray(),
        PERMISSION_REQUEST_CODE
    )

    private fun unregisterBroadcastReceiver() {
        unregisterReceiver(mMessageReceiver)
    }

    private fun registerBroadcastReceiver() {
        IntentFilter(Constants.CUSTOM_FALL_DETECTED_RECEIVER).also {
            registerReceiver(mMessageReceiver, it)
        }
    }

    private fun navigateToFragment(fragment: Fragment) =
        supportFragmentManager.beginTransaction().apply {
            replace(R.id.main_nav_host_fragment, fragment)
            commit()
        }
}
