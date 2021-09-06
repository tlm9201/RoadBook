package me.timomcgrath.roadbook.fragments

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.location.Location
import android.location.LocationManager
import android.os.AsyncTask
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.FrameLayout
import android.widget.Toast
import androidx.fragment.app.Fragment
import me.timomcgrath.roadbook.R
import me.timomcgrath.roadbook.utils.ChronometerUtils
import me.timomcgrath.roadbook.utils.DriveDataUtils

class DriveFragment : Fragment() {
    private lateinit var chronometerUtils: ChronometerUtils
    private lateinit var viewOfLayout: View
    private lateinit var activity: Activity //context
    private lateinit var driveDataUtils: DriveDataUtils

    private val locationManager get() = requireContext().getSystemService(Context.LOCATION_SERVICE) as? LocationManager
    private var originLocation = Location("dummyprovider")

    // Check if context is of activity type, then set driveDataUtils with activity context
    override fun onAttach(context: Context) {
        super.onAttach(context)

        if (context is Activity) {
            activity = context
            driveDataUtils = DriveDataUtils(activity)
        } else {
            throw RuntimeException("DriveFragment must be created from an activity context")
        }
    }

    @SuppressLint("MissingPermission")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        viewOfLayout = inflater.inflate(R.layout.fragment_drive, container, false)
        chronometerUtils = ChronometerUtils(viewOfLayout)

        chronometerUtils.createTimer(R.id.driveTimer, R.id.pauseBtn)
        chronometerUtils.startTimer()

        val finishDriveBtn: Button = viewOfLayout.findViewById(R.id.finishDrive)
        finishDriveBtn.setOnClickListener {
            finishDrive()
        }

        // Get origin drive location
        try {
            locationManager?.getCurrentLocation(
                LocationManager.GPS_PROVIDER,
                null,
                AsyncTask.THREAD_POOL_EXECUTOR,
                { location: Location? ->
                    if (location != null) {
                        originLocation = location
                    }
                })
        } catch (e: SecurityException) {
            e.printStackTrace()
        }

        return viewOfLayout
    }

    @SuppressLint("MissingPermission")
    private fun finishDrive() {
        if (chronometerUtils.getElapsedDriveTime() < 10000) {
            Log.d(TAG, "elapsedTime is too short. Wait at least 10 seconds.")
            Toast.makeText(activity, "Wait 10 seconds.", Toast.LENGTH_SHORT).show()
            return
        }
        showLoading(true)
        var callbackCounter = 0
        chronometerUtils.pauseTimer() // Pause the timer so elapsedTime can update.

        locationManager?.getCurrentLocation(LocationManager.GPS_PROVIDER,
            null,
            AsyncTask.THREAD_POOL_EXECUTOR,
            { location: Location ->
                location.let {
                    val lat: Double = location.latitude
                    val lon: Double = location.longitude

                    var timeOfDayG = ""
                    var weatherConditionsG = ""
                    var roadDistanceG = 0.0

                    // Time of day listener callback
                    driveDataUtils.getTimeOfDay(
                        lat,
                        lon,
                        object : DriveDataUtils.TimeOfDayListener {
                            override fun onSuccess(timeOfDay: String) {
                                timeOfDayG = timeOfDay
                                callbackCounter++
                                if (callbackCounter == 3) {
                                    Log.d(TAG, "Variable callbackCounter has reached $callbackCounter. Trying to save drive data...")
                                    driveDataUtils.createDriveDataModel(
                                        originLocation,
                                        location,
                                        timeOfDayG,
                                        weatherConditionsG,
                                        roadDistanceG,
                                        chronometerUtils.getElapsedDriveTime()
                                    )
                                    showLoading(false)
                                    activity.onBackPressed()
                                }
                            }
                        })

                    // Weather conditions listener callback
                    driveDataUtils.getWeatherConditions(
                        lat,
                        lon,
                        object : DriveDataUtils.WeatherConditionsListener {
                            override fun onSuccess(weatherConditions: String) {
                                weatherConditionsG = weatherConditions
                                callbackCounter++
                                if (callbackCounter == 3) {
                                    Log.d(TAG, "Variable callbackCounter has reached $callbackCounter. Trying to save drive data...")
                                    driveDataUtils.createDriveDataModel(
                                        originLocation,
                                        location,
                                        timeOfDayG,
                                        weatherConditionsG,
                                        roadDistanceG,
                                        chronometerUtils.getElapsedDriveTime()
                                    )
                                    showLoading(false)
                                    activity.onBackPressed()
                                }
                            }
                        })

                    // Road distance listener callback
                    driveDataUtils.getRoadDistanceTravelled(
                        originLocation,
                        location,
                        object : DriveDataUtils.RoadDistanceListener {
                            override fun onSuccess(roadDistance: Double) {
                                roadDistanceG = roadDistance
                                callbackCounter++
                                if (callbackCounter == 3) {
                                    Log.d(TAG, "Variable callbackCounter has reached $callbackCounter. Trying to save drive data...")
                                    driveDataUtils.createDriveDataModel(
                                        originLocation,
                                        location,
                                        timeOfDayG,
                                        weatherConditionsG,
                                        roadDistanceG,
                                        chronometerUtils.getElapsedDriveTime()
                                    )
                                    showLoading(false)
                                    activity.onBackPressed()
                                }
                            }
                        })
                }
            })
    }

    fun showLoading(showLoading: Boolean) {
        activity.runOnUiThread {
            if (showLoading)
                viewOfLayout.findViewById<FrameLayout>(R.id.loading_frame).visibility = View.VISIBLE
            else
                viewOfLayout.findViewById<FrameLayout>(R.id.loading_frame).visibility = View.GONE
        }
    }

    companion object {
        const val TAG = "DriveFragment"
    }
}
