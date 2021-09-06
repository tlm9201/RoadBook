package me.timomcgrath.roadbook.fragments

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.*
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import com.github.barteksc.pdfviewer.PDFView
import me.timomcgrath.roadbook.R
import me.timomcgrath.roadbook.utils.PdfGeneratorUtils
import java.io.File
import java.io.FileOutputStream

class LogFragment : Fragment() {
    private lateinit var viewOfLayout: View
    private lateinit var activity: Activity

    // Check if context is of activity type, then set pdfGeneratorUtils with activity context
    override fun onAttach(context: Context) {
        super.onAttach(context)

        if (context is Activity) {
            activity = context
        } else {
            throw RuntimeException("LogFragment must be created from an activity context")
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        setHasOptionsMenu(true)
        viewOfLayout = inflater.inflate(R.layout.fragment_log, container, false)
        getPdf()

        return viewOfLayout
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        menu.clear()
        inflater.inflate(R.menu.share_menu, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.share -> sharePdf()
        }
        return super.onOptionsItemSelected(item)
    }

    private fun sharePdf() {
        val file = File("${activity.filesDir}/$FILENAME")
        val sendIntent: Intent = Intent().apply {
            type = "application/pdf"
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_STREAM, FileProvider.getUriForFile(activity, "me.timomcgrath.roadbook.provider", file))
            putExtra(Intent.EXTRA_SUBJECT, "Sharing file...")
            putExtra(Intent.EXTRA_TEXT, "Sharing file...")
        }
        startActivity(Intent.createChooser(sendIntent, "Share file"))
    }


    private fun getPdf() {
        val pdfView = viewOfLayout.findViewById<PDFView>(R.id.pdfView)
        val file = File("${activity.filesDir}/$FILENAME")
        PdfGeneratorUtils().createPdf(FileOutputStream(file), activity)
        pdfView.fromFile(file).spacing(10).load()
    }
}
private const val FILENAME="driveData.pdf"