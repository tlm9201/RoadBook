package me.timomcgrath.roadbook.utils

import android.os.SystemClock
import android.view.View
import android.widget.Chronometer
import android.widget.ToggleButton
import kotlin.math.abs

class ChronometerUtils constructor(var view: View) {
    private lateinit var driveTimer: Chronometer
    private var running: Boolean = false
    private var elapsedTime: Long = 0

    fun createTimer(chronometerId: Int, pauseBtnId: Int) {
        driveTimer = view.findViewById(chronometerId)
        val pauseBtn = view.findViewById<ToggleButton>(pauseBtnId)
        pauseBtn.setOnCheckedChangeListener { _, isChecked ->
            if (!isChecked)
                pauseTimer()
            else
                startTimer()
        }
    }

    fun startTimer() {
        if (!running) {
            driveTimer.base = (SystemClock.elapsedRealtime() + elapsedTime)
            driveTimer.start()
            running = true
        }
    }

    fun pauseTimer() {
        if (running) {
            elapsedTime = driveTimer.base - SystemClock.elapsedRealtime()
            driveTimer.stop()
            running = false
        }
    }

    fun getElapsedDriveTime(): Long {
        return abs(driveTimer.base - SystemClock.elapsedRealtime())
    }
}