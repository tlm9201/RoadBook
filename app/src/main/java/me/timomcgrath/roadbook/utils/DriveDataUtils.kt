package me.timomcgrath.roadbook.utils

import android.app.Activity
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.location.Location
import android.util.Log
import com.android.volley.Request
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.google.gson.Gson
import me.timomcgrath.roadbook.RoadBookApplication
import org.json.JSONException
import org.json.JSONObject
import java.time.Instant
import java.time.format.DateTimeFormatter

class DriveDataUtils constructor(private var activity: Activity) {
    private var app = RoadBookApplication() //global variables
    // Api keys
    private val ai: ApplicationInfo = activity.packageManager.getApplicationInfo(activity.packageName, PackageManager.GET_META_DATA)
    private val weatherApiKey = ai.metaData["weatherApiKey"].toString()
    private val radarApiKey = ai.metaData["radarApiKey"].toString()

    private var timeOfDay = ""
    private var units = app.getUnitsType() //metric or imperial; used in api calls
    private val queue = Volley.newRequestQueue(activity)

    interface VolleyCallback {
        fun onSuccess(jsonObject: JSONObject)
    }

    private fun getJsonObject(method: Int, url: String, callback: VolleyCallback) {
        val jsonObjectRequest = JsonObjectRequest(method, url, null,
            { response ->
                callback.onSuccess(response)
            },
            { error ->
                error.printStackTrace()
            })
        queue.add(jsonObjectRequest)
    }

    fun getWeatherConditions(lat: Double, lon: Double, listener: WeatherConditionsListener) {
        getJsonObject(Request.Method.GET, "https://api.openweathermap.org/data/2.5/weather?lat=${lat}&lon=${lon}&appid=${weatherApiKey}", object: VolleyCallback {
            override fun onSuccess(jsonObject: JSONObject) {
                try {
                    val weatherInfo: JSONObject = jsonObject.getJSONArray("weather").getJSONObject(0)
                    listener.onSuccess(weatherInfo.getString("main"))
                } catch (e: JSONException) {
                    e.printStackTrace()
                }
            }
        })
    }

    interface WeatherConditionsListener {
        fun onSuccess(weatherConditions: String)

        // TODO: Implement onError
    }


    fun getTimeOfDay(lat: Double, lon: Double, listener: TimeOfDayListener) {
        getJsonObject(Request.Method.GET, "https://api.openweathermap.org/data/2.5/weather?lat=$lat&lon=$lon&appid=${weatherApiKey}&units=$units", object: VolleyCallback {
            override fun onSuccess(jsonObject: JSONObject) {
                try {
                    val sunrise: Long = jsonObject.getJSONObject("sys").getLong("sunrise")
                    val sunset: Long = jsonObject.getJSONObject("sys").getLong("sunset")
                    val currentTime:Long = jsonObject.getLong("dt")
                    //debug
                    Log.d(TAG, sunrise.toString())
                    Log.d(TAG, sunset.toString())
                    Log.d(TAG, currentTime.toString())
                    timeOfDay = if (currentTime in sunrise..sunset) {
                        "Day"
                    } else {
                        "Night"
                    }
                    listener.onSuccess(timeOfDay)
                } catch (e: JSONException) {
                    e.printStackTrace()
                }
            }
        })
    }

    interface TimeOfDayListener {
        fun onSuccess(timeOfDay: String)

        // TODO: Implement onError
    }

    fun createDriveDataModel(originLocation: Location, destinationLocation: Location, timeOfDay: String, weatherConditions: String,  distanceTravelled: Int, timeElapsed: Long): DriveDataModel {
        return DriveDataModel(DateTimeFormatter.ISO_INSTANT.format(Instant.now()), originLocation, destinationLocation, distanceTravelled, timeElapsed, timeOfDay, weatherConditions)
    }

    fun saveDriveData(driveDataModel: DriveDataModel) {
        val driveDataFileName = "driveData.json"
        val files: Array<String> = activity.fileList()
        var gson = Gson()
        var driveDataModelString = gson.toJson(driveDataModel).toString()

        Log.d(TAG, driveDataModelString)
    }




//    fun getRoadDistanceTravelled(callback: ): JSONObject? {
//        var response: JSONObject? = null
//
//        val originLatitude: Double = locationAtStartOfDrive.latitude
//        val originLongitude: Double = locationAtStartOfDrive.longitude
//
//        val destinationLatitude: Double = locationAtEndOfDrive.latitude
//        val destinationLongitude: Double = locationAtEndOfDrive.longitude
//
//        val jsonObjectRequest = object: JsonObjectRequest(Method.GET,
//            "https://api.radar.io/v1/route/distance?origin=${originLatitude},${originLongitude}&destination=${destinationLatitude},${destinationLongitude}&modes=car&units=${units}", null,
//            { res ->
//                response = res
//            },
//            { err ->
//                err.message?.let { Log.d("DriveDataUtils", it) }
//            })
//        {
//            override fun getHeaders(): MutableMap<String, String> {
//                val headers = HashMap<String, String>()
//                headers["Authorization"] = "Basic $radarApiKey"
//                return headers
//            }
//        }
//        queue.add(jsonObjectRequest)
//        return response
//    }
}
private const val TAG="DriveDataUtils"