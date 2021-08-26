package me.timomcgrath.roadbook

import android.Manifest
import android.annotation.SuppressLint
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.add
import androidx.fragment.app.commit
import androidx.fragment.app.replace
import androidx.navigation.fragment.NavHostFragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import me.timomcgrath.roadbook.fragments.DriveFragment
import me.timomcgrath.roadbook.utils.LocationUtils.hasLocationPermission
import me.timomcgrath.roadbook.utils.LocationUtils.requestLocationPermission

class MainActivity : AppCompatActivity() {

    private val LOCATION_PERMISSION_CODE = 1;

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val navHostFragment = supportFragmentManager.findFragmentById(R.id.fragmentContainerView) as NavHostFragment
        val navController = navHostFragment.navController

        val bottomNavView = findViewById<BottomNavigationView>(R.id.bottomNavigationView)

        bottomNavView.setOnItemSelectedListener {
            when (it.itemId) {
                R.id.logFragment -> {
                    Log.d(TAG, "log selected")
                    navController.navigate(R.id.logFragment)
                }

                R.id.homeFragment -> {
                    Log.d(TAG, "home fragment")
                    navController.navigate(R.id.homeFragment)
                }

                R.id.infoFragment -> {
                    Log.d(TAG, "info fragment")
                    navController.navigate(R.id.infoFragment)
                }

                else -> {}
            }
            true
        }
    }

    private fun startNewDrive() {
        supportFragmentManager.commit {
            replace<DriveFragment>(R.id.fragmentContainerView)
            setReorderingAllowed(true)
            addToBackStack("home")
        }
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
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
           when (requestCode) {
               LOCATION_PERMISSION_CODE -> {
                   // If request is cancelled, the result arrays are empty.
                   if ((grantResults.isNotEmpty() &&
                               grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
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

    private fun whyLocationAccessUI(): AlertDialog? {
        val alertDialog: AlertDialog? = this?.let {
            val builder = AlertDialog.Builder(it)
            builder.apply {
                setPositiveButton(R.string.ok,
                    DialogInterface.OnClickListener { dialog, id ->
                        requestPermissions(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), LOCATION_PERMISSION_CODE)
                    })
                setNegativeButton(R.string.cancel,
                    DialogInterface.OnClickListener { dialog, id ->
                        dialog.dismiss()
                    })
                setMessage(R.string.location_permission_dialog_message)
            }
            builder.create()
        }

        return alertDialog
    }

    private fun showPermissionDeniedUI() {
        val alertDialog: AlertDialog? = this?.let {
            val builder = AlertDialog.Builder(it)
            builder.apply {
                setPositiveButton(R.string.ok,
                    DialogInterface.OnClickListener { dialog, id ->
                        dialog.dismiss()
                    })
                setMessage(R.string.permission_denied_message)
            }
            builder.create()
        }

        alertDialog?.show()
    }
}

private const val TAG="MainActivity"