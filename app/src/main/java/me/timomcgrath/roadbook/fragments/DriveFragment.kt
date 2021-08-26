package me.timomcgrath.roadbook.fragments

import android.app.Activity
import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.fragment_drive.*
import me.timomcgrath.roadbook.R
import me.timomcgrath.roadbook.utils.ChronometerUtils
import me.timomcgrath.roadbook.utils.DriveDataUtils

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
        driveDataUtils.startDrive()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        viewOfLayout = inflater.inflate(R.layout.fragment_drive, container, false)
        chronometerUtils = ChronometerUtils(viewOfLayout)

        chronometerUtils.createTimer(R.id.driveTimer, R.id.pauseBtn)
        chronometerUtils.startTimer()

        return viewOfLayout
    }

    private fun verifyDrive() {

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