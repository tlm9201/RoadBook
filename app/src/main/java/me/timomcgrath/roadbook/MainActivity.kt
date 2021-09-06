package me.timomcgrath.roadbook

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.NavHostFragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import me.timomcgrath.roadbook.fragments.DriveFragment
import me.timomcgrath.roadbook.utils.LocationUtils.hasLocationPermission
import me.timomcgrath.roadbook.utils.LocationUtils.requestLocationPermission

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.fragmentContainerView) as NavHostFragment
        val navController = navHostFragment.navController

        val bottomNavView = findViewById<BottomNavigationView>(R.id.bottomNavigationView)

        bottomNavView.setOnItemSelectedListener {
            when (it.itemId) {
                R.id.logFragment -> {
                    navController.navigate(R.id.logFragment)
                }

                R.id.homeFragment -> {
                    navController.navigate(R.id.homeFragment)
                }

                R.id.infoFragment -> {
                    navController.navigate(R.id.infoFragment)
                }

                else -> {
                }
            }
            true
        }
    }

    private fun startNewDrive() {
        val frag = supportFragmentManager.findFragmentByTag(DriveFragment.TAG)
            ?: DriveFragment()

        supportFragmentManager
            .beginTransaction()
            .setCustomAnimations(R.anim.fade_in, R.anim.fade_out, R.anim.fade_in, R.anim.fade_out)
            .replace(R.id.fragmentContainerView, frag)
            .setReorderingAllowed(true)
            .addToBackStack("home")
            .commit()
    }

    fun startDrive(view: View) {
        if (hasLocationPermission(this)) {
            //user already has location permission so start a new drive
            startNewDrive()
        } else {
            requestLocationPermission(whyLocationAccessUI(), this)
        }
    }

    @SuppressLint("MissingSuperCall")
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        when (requestCode) {
            LOCATION_PERMISSION_CODE -> {
                // If request is cancelled, the result arrays are empty.
                if ((grantResults.isNotEmpty() &&
                            grantResults[0] == PackageManager.PERMISSION_GRANTED)
                ) {
                    startNewDrive()
                } else {
                    showPermissionDeniedUI()
                }
                return
            }
            else -> {
                // Ignore all other requests.
            }
        }
    }

    private fun whyLocationAccessUI(): AlertDialog {
        val alertDialog: AlertDialog = this.let {
            val builder = AlertDialog.Builder(it)
            builder.apply {
                setPositiveButton(R.string.ok
                ) { _, _ ->
                    requestPermissions(
                        arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                        LOCATION_PERMISSION_CODE
                    )
                }
                setNegativeButton(R.string.cancel
                ) { dialog, _ ->
                    dialog.dismiss()
                }
                setMessage(R.string.location_permission_dialog_message)
            }
            builder.create()
        }

        return alertDialog
    }

    private fun showPermissionDeniedUI() {
        val alertDialog: AlertDialog = this.let {
            val builder = AlertDialog.Builder(it)
            builder.apply {
                setPositiveButton(R.string.ok
                ) { dialog, _ ->
                    dialog.dismiss()
                }
                setMessage(R.string.permission_denied_message)
            }
            builder.create()
        }

        alertDialog.show()
    }
}
private const val LOCATION_PERMISSION_CODE = 1
private const val TAG = "MainActivity"