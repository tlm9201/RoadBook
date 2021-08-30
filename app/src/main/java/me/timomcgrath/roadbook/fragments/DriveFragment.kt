package me.timomcgrath.roadbook.fragments

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.location.Location
import android.location.LocationManager
import android.os.AsyncTask
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import me.timomcgrath.roadbook.R
import me.timomcgrath.roadbook.utils.ChronometerUtils
import me.timomcgrath.roadbook.utils.DriveDataUtils
import java.util.function.Consumer

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"


/**
 * A simple [Fragment] subclass.
 * Use the [DriveFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class DriveFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    private lateinit var chronometerUtils: ChronometerUtils
    private lateinit var viewOfLayout: View
    private lateinit var activity: Activity //context
    private lateinit var driveDataUtils: DriveDataUtils

    private val locationManager = requireContext().getSystemService(Context.LOCATION_SERVICE) as? LocationManager
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    @SuppressLint("MissingPermission")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
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
            locationManager?.getCurrentLocation(LocationManager.GPS_PROVIDER, null, AsyncTask.THREAD_POOL_EXECUTOR, Consumer { location: Location ->
                originLocation = location
            })
        } catch (e: SecurityException) {
            e.printStackTrace()
        }

        return viewOfLayout
    }

    @SuppressLint("MissingPermission")
    private fun finishDrive() {
        var destinationLocation: Location = Location("dummyprovider")

        locationManager?.getCurrentLocation(LocationManager.GPS_PROVIDER, null, AsyncTask.THREAD_POOL_EXECUTOR,
            { location: Location ->
                location.let {
                    destinationLocation = location
                    val lat: Double = location.latitude
                    val lon: Double = location.longitude
                    Log.d(TAG, "lat=$lat lon=$lon")

                    // Time of day listener callback
                    driveDataUtils.getTimeOfDay(lat, lon, object: DriveDataUtils.TimeOfDayListener {
                        override fun onSuccess(timeOfDay: String) {
                            Log.d(TAG, "Time of Day: $timeOfDay")
                            // TODO: Save data
                        }
                    })

                    // Weather conditions listener callback
                    driveDataUtils.getWeatherConditions(lat, lon, object: DriveDataUtils.WeatherConditionsListener {
                        override fun onSuccess(weatherConditions: String) {
                            Log.d(TAG, "Weather conditions: $weatherConditions")
                            // TODO: Save data
                        }
                    })

                }

            })
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment DriveFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            DriveFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}
private const val TAG="DriveFragment"