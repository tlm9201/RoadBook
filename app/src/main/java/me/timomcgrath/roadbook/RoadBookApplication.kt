package me.timomcgrath.roadbook

import android.app.Application
import android.util.Log
import me.timomcgrath.roadbook.exceptions.InvalidUnitsException
import java.lang.Exception

class RoadBookApplication : Application() {

    private var unitsType = "imperial"

    fun getUnitsType(): String {
        return unitsType
    }

    fun setUnitsType(units: String) {
        try {
            validateUnits(units)
        } catch (e: InvalidUnitsException){
            e.message?.let { Log.d(TAG, it) }
            return
        } catch (e: Exception) {
            e.message?.let { Log.d(TAG, it) }
            return
        }
        unitsType = units
    }

    private fun validateUnits(units: String) {
        if(!units.equals("imperial", true) || !units.equals("metric", true)) {
            throw InvalidUnitsException("$units is not a valid unit type. Valid unit types are: imperial / metric")
        }
    }
}
private const val TAG="RoadBookApplication"