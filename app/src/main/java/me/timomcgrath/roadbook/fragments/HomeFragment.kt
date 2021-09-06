package me.timomcgrath.roadbook.fragments

import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import androidx.fragment.app.Fragment
import me.timomcgrath.roadbook.R
import me.timomcgrath.roadbook.utils.DriveDataUtils
import java.util.concurrent.TimeUnit

class HomeFragment : Fragment() {
    private lateinit var viewOfLayout: View
    private lateinit var timerText: TextView
    private lateinit var totalDrivingProgressText: TextView
    private lateinit var nighttimeDrivingProgressText: TextView
    private lateinit var totalDrivingProgressBar: ProgressBar
    private lateinit var nighttimeDrivingProgressBar: ProgressBar
    private lateinit var driveDataUtils: DriveDataUtils
    private lateinit var activity: Activity

    private var totalDriveTime: Long = 0
    private var nighttimeDriveTime: Long = 0

    override fun onAttach(context: Context) {
        super.onAttach(context)

        if (context is Activity) {
            activity = context
            driveDataUtils = DriveDataUtils(activity)
        } else {
            throw RuntimeException("HomeFragment must be created from an activity context")
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        viewOfLayout = inflater.inflate(R.layout.fragment_home, container, false)
        totalDriveTime = driveDataUtils.getTotalDriveTime()
        nighttimeDriveTime = driveDataUtils.getTotalNighttimeDrivingTime()

        timerText = viewOfLayout.findViewById(R.id.timerText)

        totalDrivingProgressText = viewOfLayout.findViewById(R.id.totalDrivingProgress)
        nighttimeDrivingProgressText = viewOfLayout.findViewById(R.id.nighttimeDrivingProgress)

        totalDrivingProgressBar = viewOfLayout.findViewById(R.id.totalProgressBar)
        nighttimeDrivingProgressBar = viewOfLayout.findViewById(R.id.nighttimeProgressBar)

        timerText.text = driveDataUtils.formatMillis("%d hours %d mins", totalDriveTime)

        totalDrivingProgressText.text = driveDataUtils.formatMillis("%02d:%02d", totalDriveTime)
        totalDrivingProgressBar.progress = (totalDriveTime.toInt())


        nighttimeDrivingProgressText.text = driveDataUtils.formatMillis("%02d:%02d", nighttimeDriveTime)
        nighttimeDrivingProgressBar.progress = (nighttimeDriveTime.toInt())

        return viewOfLayout
    }

}
private const val TAG="HomeFragment"