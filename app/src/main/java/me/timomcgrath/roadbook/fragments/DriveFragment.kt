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
                { location: Location ->
                    originLocation = location
                })
        } catch (e: SecurityException) {
            e.printStackTrace()
        }

        return viewOfLayout
    }

    @SuppressLint("MissingPermission")
    private fun finishDrive() {
        var callbackCounter = 0
        Log.d(TAG, originLocation.toString())
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
                                Log.d(TAG, "Time of Day: $timeOfDay")
                                timeOfDayG = timeOfDay
                                callbackCounter++
                                if (callbackCounter == 3) {
                                    Log.d(
                                        TAG,
                                        "Variable callbackCounter has reached: $callbackCounter. Trying to save drive data..."
                                    )
                                    driveDataUtils.createDriveDataModel(
                                        originLocation,
                                        location,
                                        timeOfDayG,
                                        weatherConditionsG,
                                        roadDistanceG,
                                        chronometerUtils.getElapsedDriveTime()
                                    )
                                }
                                Log.d(TAG, callbackCounter.toString())
                            }
                        })

                    // Weather conditions listener callback
                    driveDataUtils.getWeatherConditions(
                        lat,
                        lon,
                        object : DriveDataUtils.WeatherConditionsListener {
                            override fun onSuccess(weatherConditions: String) {
                                Log.d(TAG, "Weather conditions: $weatherConditions")
                                weatherConditionsG = weatherConditions
                                callbackCounter++
                                if (callbackCounter == 3) {
                                    Log.d(
                                        TAG,
                                        "Variable callbackCounter has reached: $callbackCounter. Trying to save drive data..."
                                    )
                                    driveDataUtils.createDriveDataModel(
                                        originLocation,
                                        location,
                                        timeOfDayG,
                                        weatherConditionsG,
                                        roadDistanceG,
                                        chronometerUtils.getElapsedDriveTime()
                                    )
                                }
                                Log.d(TAG, callbackCounter.toString())

                            }
                        })

                    // Road distance listener callback
                    driveDataUtils.getRoadDistanceTravelled(
                        originLocation,
                        location,
                        object : DriveDataUtils.RoadDistanceListener {
                            override fun onSuccess(roadDistance: Double) {
                                Log.d(TAG, "Road distance travelled: $roadDistance")
                                roadDistanceG = roadDistance
                                callbackCounter++
                                if (callbackCounter == 3) {
                                    Log.d(
                                        TAG,
                                        "Variable callbackCounter has reached: $callbackCounter. Trying to save drive data..."
                                    )
                                    driveDataUtils.createDriveDataModel(
                                        originLocation,
                                        location,
                                        timeOfDayG,
                                        weatherConditionsG,
                                        roadDistanceG,
                                        chronometerUtils.getElapsedDriveTime()
                                    )
                                }
                                Log.d(TAG, callbackCounter.toString())
                            }
                        })


                }

            })
    }

    companion object {
        const val TAG = "DriveFragment"
    }
}
