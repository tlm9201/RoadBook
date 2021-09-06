package me.timomcgrath.roadbook.utils

import android.app.Activity
import android.content.Context
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.util.Log
import androidx.core.content.res.ResourcesCompat
import me.timomcgrath.roadbook.R
import java.io.OutputStream
import java.text.DateFormat
import java.util.*
import kotlin.math.roundToInt

class PdfGeneratorUtils {
    // constants
    private val document = PdfDocument()
    private val pageWidth = 595
    private val pageHeight = 842

    private var dataMargin = 171F

    fun createPdf(outputStream: OutputStream, activity: Activity) {
        val pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, 1).create()
        var currentPage = document.startPage(pageInfo)
        var spacingCount = 0
        var spacing = 0
        val driveDataUtils = DriveDataUtils(activity)

        val boldPaint = Paint()
        boldPaint.color = Color.WHITE
        boldPaint.style = Paint.Style.FILL
        currentPage.canvas.drawPaint(boldPaint)

        val normalPaint = Paint()
        normalPaint.style = Paint.Style.FILL
        normalPaint.color = Color.BLACK


        boldPaint.typeface = Typeface.DEFAULT_BOLD
        boldPaint.color = Color.BLACK

        initColumnTitles(currentPage, boldPaint)
        initPageHeaders(currentPage, activity)

        //start
        val driveDataList: List<DriveDataModel> = driveDataUtils.getDriveDataList()

        for (value in driveDataList) {
            var currentPageNum = 1
            spacingCount++
            val date = value.dateTimestamp.substring(0, 10)
            var distance = "${Math.round(((value.distanceTravelled / 5280) * 10)/10)} mi"
            if (value.distanceTravelled < 5280) {
                distance = "${value.distanceTravelled.roundToInt()} ft"
            }
            spacing = spacingCount*20

            // will this row fit?
            if (spacing+101 < pageHeight-86) {
                currentPage.canvas.drawText(date, 74F, dataMargin+spacing, normalPaint)
                currentPage.canvas.drawText(distance, 154F, dataMargin+spacing, normalPaint)
                currentPage.canvas.drawText(driveDataUtils.formatMillis("%d hrs %d mins", value.timeElapsed), 238F, dataMargin+spacing, normalPaint)
                currentPage.canvas.drawText(value.weatherConditions, 360F, dataMargin+spacing, normalPaint)
                currentPage.canvas.drawText(value.timeOfDay, 454F, dataMargin+spacing, normalPaint)
            } else {
                document.finishPage(currentPage)
                currentPageNum++
                val newPageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, currentPageNum).create()
                dataMargin = 101F
                currentPage = document.startPage(newPageInfo)
                initColumnTitles(currentPage, boldPaint)
                spacingCount = 1
                spacing = spacingCount*20

                currentPage.canvas.drawText(date, 74F, dataMargin+spacing, normalPaint)
                currentPage.canvas.drawText(distance, 154F, dataMargin+spacing, normalPaint)
                currentPage.canvas.drawText(driveDataUtils.formatMillis("%d hrs %d mins", value.timeElapsed), 238F, dataMargin+spacing, normalPaint)
                currentPage.canvas.drawText(value.weatherConditions, 360F, dataMargin+spacing, normalPaint)
                currentPage.canvas.drawText(value.timeOfDay, 454F, dataMargin+spacing, normalPaint)
            }

        }

        initEnding(activity, currentPage, driveDataUtils, normalPaint, spacing)

        document.finishPage(currentPage)
        document.writeTo(outputStream)
        document.close()
        Log.d(TAG, "Successfully generated pdf file using $outputStream")
    }

    private fun initColumnTitles(page: PdfDocument.Page, paint: Paint) {
        page.canvas.drawText("Date", 74F, dataMargin, paint)
        page.canvas.drawText("Distance", 154F, dataMargin, paint)
        page.canvas.drawText("Time Elapsed", 238F, dataMargin, paint)
        page.canvas.drawText("Weather", 360F, dataMargin, paint)
        page.canvas.drawText("Time of Day", 454F, dataMargin, paint)
    }

    private fun initPageHeaders(page: PdfDocument.Page, context: Context) {
        val boldFont = ResourcesCompat.getFont(context, R.font.helvetica_neue_bd)
        val font = ResourcesCompat.getFont(context, R.font.helvetica_neue)
        val thinFont = ResourcesCompat.getFont(context, R.font.helvetica_neue_thin)
        val paint = Paint()

        paint.color = Color.BLACK
        paint.style = Paint.Style.FILL

        paint.textSize = 36F
        paint.typeface = boldFont
        page.canvas.drawText("RoadBook", 74F, 101F, paint)

        paint.textSize = 14F
        paint.typeface = font
        page.canvas.drawText("Track, log, and save automated driving statistics", 74F, 121F, paint)

        paint.textSize = 10F
        paint.typeface = thinFont
        page.canvas.drawText("Log generated on ${DateFormat.getDateInstance().format(Date())}", 74F, 135F, paint)
    }

    private fun initEnding(context: Context, page: PdfDocument.Page, driveDataUtils: DriveDataUtils, paint: Paint, spacing: Int) {
        val totalDriveTime = driveDataUtils.formatMillisWithSeconds("%d hours, %d minutes, %d seconds", driveDataUtils.getTotalDriveTime())
        val totalNightDriveTime = driveDataUtils.formatMillisWithSeconds("%d hours, %d minutes, %d seconds", driveDataUtils.getTotalNighttimeDrivingTime())
        paint.textSize = 16F
        paint.typeface = ResourcesCompat.getFont(context, R.font.helvetica_neue)
        page.canvas.drawText("Total driving time:", 74F, spacing+dataMargin+50F, paint)
        page.canvas.drawText("Total night driving time:", 74F, spacing+dataMargin+100F, paint)

        paint.typeface = ResourcesCompat.getFont(context, R.font.helvetica_neue_thin)
        page.canvas.drawText(totalDriveTime, 74F, spacing+dataMargin+75F, paint)
        page.canvas.drawText(totalNightDriveTime, 74F, spacing+dataMargin+125F, paint)
    }
}
private const val TAG="PdfGeneratorUtils"