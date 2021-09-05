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
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.io.File
import java.time.Instant
import java.time.format.DateTimeFormatter

class DriveDataUtils constructor(private var activity: Activity) {
    private var app = RoadBookApplication() //global variables

    // API keys
    private val ai: ApplicationInfo = activity.packageManager.getApplicationInfo(
        activity.packageName,
        PackageManager.GET_META_DATA
    )
    private val weatherApiKey = ai.metaData["weatherApiKey"].toString()
    private val radarApiKey = ai.metaData["radarApiKey"].toString()

    private var timeOfDay = ""
    private var units = app.getUnitsType() //metric or imperial; used in api calls
    private val queue = Volley.newRequestQueue(activity)
    private val driveDataFileName = "driveData.json"


    interface VolleyCallback {
        fun onSuccess(jsonObject: JSONObject)
    }

    private fun requestJsonObject(
        sendAuthorizationHeader: String?,
        method: Int,
        url: String,
        callback: VolleyCallback
    ) {
        val jsonObjectRequest = object : JsonObjectRequest(method, url, null,
            { response ->
                callback.onSuccess(response)
            },
            { error ->
                error.printStackTrace()
            }) {
            override fun getHeaders(): MutableMap<String, String> {
                val headers = HashMap<String, String>()
                if (sendAuthorizationHeader != null) {
                    Log.d(TAG, "Sending authorization header: $sendAuthorizationHeader")
                    headers["Authorization"] = "$sendAuthorizationHeader"
                    return headers
                }
                return headers
            }
        }

        queue.add(jsonObjectRequest)
    }

    fun getWeatherConditions(lat: Double, lon: Double, listener: WeatherConditionsListener) {
        requestJsonObject(
            null,
            Request.Method.GET,
            "https://api.openweathermap.org/data/2.5/weather?lat=${lat}&lon=${lon}&appid=${weatherApiKey}",
            object : VolleyCallback {
                override fun onSuccess(jsonObject: JSONObject) {
                    try {
                        val weatherInfo: JSONObject =
                            jsonObject.getJSONArray("weather").getJSONObject(0)
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
        requestJsonObject(
            null,
            Request.Method.GET,
            "https://api.openweathermap.org/data/2.5/weather?lat=$lat&lon=$lon&appid=${weatherApiKey}&units=$units",
            object : VolleyCallback {
                override fun onSuccess(jsonObject: JSONObject) {
                    try {
                        val sunrise: Long = jsonObject.getJSONObject("sys").getLong("sunrise")
                        val sunset: Long = jsonObject.getJSONObject("sys").getLong("sunset")
                        val currentTime: Long = jsonObject.getLong("dt")

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

    fun getRoadDistanceTravelled(
        origin: Location,
        destination: Location,
        listener: RoadDistanceListener
    ) {
        requestJsonObject(
            radarApiKey,
            Request.Method.GET,
            "https://api.radar.io/v1/route/distance?origin=${origin.latitude},${origin.longitude}&destination=${destination.latitude},${destination.longitude}&modes=car&units=$units",
            object : VolleyCallback {
                override fun onSuccess(jsonObject: JSONObject) {
                    try {
                        listener.onSuccess(
                            jsonObject.getJSONObject("routes").getJSONObject("car")
                                .getJSONObject("distance").getDouble("value")
                        )
                    } catch (e: JSONException) {
                        e.printStackTrace()
                    }
                }
            })
    }

    interface RoadDistanceListener {
        fun onSuccess(roadDistance: Double)

        // TODO: Implement onError
    }

    fun createDriveDataModel(
        originLocation: Location,
        destinationLocation: Location,
        timeOfDay: String,
        weatherConditions: String,
        distanceTravelled: Double,
        timeElapsed: Long
    ) {
        saveDriveData(
            DriveDataModel(
                DateTimeFormatter.ISO_INSTANT.format(Instant.now()),
                originLocation.latitude,
                originLocation.longitude,
                destinationLocation.latitude,
                destinationLocation.longitude,
                distanceTravelled,
                timeElapsed,
                timeOfDay,
                weatherConditions,
                units
            )
        )
    }

    private fun saveDriveData(driveDataModel: DriveDataModel) {
        val file = File(activity.filesDir, driveDataFileName)
        val gson = Gson()
        val driveDataJSONModel = gson.toJson(driveDataModel)

        if (file.createNewFile()) {
            file.writeText("[\n\t$driveDataJSONModel\n\n]")
        } else {
            val listOfLines: MutableList<String> = file.readLines() as MutableList<String> // List of lines in driveData.json
            // iterate through listOfLines
            for ((i, value) in listOfLines.withIndex()) {
                if (value.isEmpty()) {
                    // Change the blank line to driveDataJSONModel
                    listOfLines[i] = "\t$driveDataJSONModel\n"
                    // Add a comma to the previous line
                    listOfLines[i-1] = listOfLines[i-1].plus(",")
                    // Finally, join our new list and write it to driveData.json
                    file.writeText(listOfLines.joinToString("\n"))
                    Log.d(TAG, "Successfully saved drive data to ${activity.filesDir}/$driveDataFileName")
                    break
                }
            }
        }
    }

    fun getTotalDriveTime(): Long {
        var totalTime: Long = 0
        val file = File(activity.filesDir, driveDataFileName)
        val data = JSONArray(file.readText())

        for (i in 0 until data.length())
            totalTime +=  data.getJSONObject(i).getLong("timeElapsed")

        return totalTime
    }

    fun getTotalNighttimeDrivingTime(): Long {
        var totalTime: Long = 0
        val file = File(activity.filesDir, driveDataFileName)
        val data = JSONArray(file.readText())

        for (i in 0 until data.length()) {
            val obj = data.getJSONObject(i)

            if (obj.getString("timeOfDay").equals("Night")) {
                totalTime +=  obj.getLong("timeElapsed")
            }
        }

        return totalTime
    }
}

private const val TAG = "DriveDataUtils"