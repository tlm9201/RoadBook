package me.timomcgrath.roadbook.utils

import android.Manifest
import android.app.Activity
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.location.Location
import android.util.Log
import androidx.core.app.ActivityCompat
import com.android.volley.Request
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import me.timomcgrath.roadbook.RoadBookApplication
import org.json.JSONObject

class DriveDataUtils constructor(private var activity: Activity) {
    private var app = RoadBookApplication() //global variables
    // Api keys
    private val ai: ApplicationInfo = activity.packageManager.getApplicationInfo(activity.packageName, PackageManager.GET_META_DATA)
    private val weatherApiKey = ai.metaData["weatherApiKey"].toString()
    private val radarApiKey = ai.metaData["radarApiKey"].toString()

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var locationAtStartOfDrive: Location? = null
    private var locationAtEndOfDrive: Location? = null
    private var currentLocation: Location? = null
    private var currentWeather: JSONObject? = null
    private var units = app.getUnitsType() //metric or imperial; used in api calls
    private val queue = Volley.newRequestQueue(activity)
    private val weatherApiRequestUrl = { lat: Double, lon: Double -> "api.openweathermap.org/data/2.5/weather?lat=$lat&lon=$lon&appid=$weatherApiKey" }

    private fun initLocationServices() {
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(activity)
    }

    fun startDrive() {
        initLocationServices()

        if (ActivityCompat.checkSelfPermission(
                activity,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED || ActivityCompat.checkSelfPermission(
                activity,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        fusedLocationClient.lastLocation
            .addOnSuccessListener { location : Location? ->
                locationAtStartOfDrive = location
                currentLocation = location
            }
    }

    fun endDrive() {
        if (ActivityCompat.checkSelfPermission(
                activity,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED || ActivityCompat.checkSelfPermission(
                activity,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        fusedLocationClient.lastLocation
            .addOnSuccessListener { location : Location? ->
                locationAtEndOfDrive = location
                currentLocation = location
            }
    }

    fun getLocation(): Location? {
        var loc: Location? = null
        if (ActivityCompat.checkSelfPermission(
                activity,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED || ActivityCompat.checkSelfPermission(
                activity,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return null
        }
        fusedLocationClient.lastLocation
            .addOnSuccessListener { location : Location? ->
                loc = location
            }
        currentLocation = loc
        return loc
    }

    private fun makeWeatherApiGETRequest(lat: Double, lon: Double): JSONObject? {
        var response: JSONObject? = null

        val jsonObjectRequest = JsonObjectRequest(Request.Method.GET,
            weatherApiRequestUrl(lat, lon), null,
            { res ->
                response = res
            },
            { err ->
                //response is null
            }
        )
        queue.add(jsonObjectRequest)
        return response
    }

    fun getWeatherConditions(): String? {
        if (currentLocation != null) {
            return if (currentWeather != null) {
                currentWeather!!.getJSONArray("weather").getJSONObject(0).getString("main")
            } else {
                currentWeather = makeWeatherApiGETRequest(currentLocation!!.latitude, currentLocation!!.longitude)
                currentWeather!!.getJSONArray("weather").getJSONObject(0).getString("main")
            }
        }
        // TODO: Handle unsafe api call
        return null
    }

    fun getTimeOfDay(): String {
        var sunsetTime: Long?
        var sunriseTime: Long?
        var currentTime: Long = System.currentTimeMillis()

        if (currentLocation != null) {
            if (currentWeather != null) {
                sunsetTime = currentWeather!!.getJSONObject("sys").getLong("sunset")
                sunriseTime = currentWeather!!.getJSONObject("sys").getLong("sunrise")
            } else {
                currentWeather = makeWeatherApiGETRequest(currentLocation!!.latitude, currentLocation!!.longitude)
                sunsetTime = currentWeather!!.getJSONObject("sys").getLong("sunset")
                sunriseTime = currentWeather!!.getJSONObject("sys").getLong("sunrise")
            }
        } else {
            //should never occur
            return "Error: Current location is null"
        }

        return if (currentTime >= sunriseTime && currentTime <= sunsetTime) {
            "Day"
        } else {
            "Night"
        }
    }

    fun getRoadDistanceTravelled(): JSONObject? {
        if (locationAtStartOfDrive != null && locationAtEndOfDrive != null) {
            var response: JSONObject? = null
            val originLatitude: Double = locationAtStartOfDrive!!.latitude
            val originLongitude: Double = locationAtStartOfDrive!!.longitude

            val destinationLatitude: Double = locationAtEndOfDrive!!.latitude
            val destinationLongitude: Double = locationAtEndOfDrive!!.longitude

            val jsonObjectRequest = object: JsonObjectRequest(Request.Method.GET,
                "https://api.radar.io/v1/route/distance?origin=${originLatitude},${originLongitude}&destination=${destinationLatitude},${destinationLongitude}&modes=car&units=${units}", null,
                { res ->
                    response = res
                },
                { err ->
                    err.message?.let { Log.d("DriveDataUtils", it) }
                    // TODO: Handle error response
                })
            {
                override fun getHeaders(): MutableMap<String, String> {
                    val headers = HashMap<String, String>()
                    headers["Authorization"] = "Basic $radarApiKey"
                    return headers
                }
            }
            queue.add(jsonObjectRequest)
            return response
        }
        return null
    }
}
private const val TAG="DriveDataUtils"