package io.github.kaczmarek.localcrashdetector

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.text.TextUtils
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import io.github.kaczmarek.localcrashdetector.ui.CrashesListActivity
import java.io.*
import java.lang.Exception
import java.text.SimpleDateFormat
import java.util.*

object LocalCrashDetector {
    private const val CRASH_REPORT_DIR = "crashes"
    private const val NOTIFICATION_ID = 379

    private var applicationContext: Context? = null
    private val context: Context
        get() {
            return applicationContext
                ?: throw IllegalStateException("Application context in LocalCrashDetector not initialized. Please call method init in your Application instance")
        }

    private val crashLogTime: String
        get() {
            val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
            return dateFormat.format(Date())
        }

    private val defaultPath: String
        get() {
            val defaultPath: String = context.getExternalFilesDir(null)?.absolutePath
                .toString() + File.separator + CRASH_REPORT_DIR
            val file = File(defaultPath)
            file.mkdirs()
            return defaultPath
        }

    fun init(context: Context) {
        applicationContext = context
        if (Thread.getDefaultUncaughtExceptionHandler() !is ExceptionHandler) {
            Thread.setDefaultUncaughtExceptionHandler(ExceptionHandler())
        }
    }

    fun saveCrashReport(throwable: Throwable) {
        val filename = "$crashLogTime.txt"
        writeToFile(filename, getStackTrace(throwable))
        showNotification(throwable.localizedMessage)
    }

    private fun writeToFile(filename: String, crashLog: String) {
        try {
            val bufferedWriter = BufferedWriter(
                FileWriter(defaultPath + File.separator + filename)
            )
            with(bufferedWriter) {
                write(crashLog)
                flush()
                close()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun showNotification(exceptionMessage: String?) {
        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                context.getString(R.string.local_crash_detector_channel_notify_id),
                context.getString(R.string.local_crash_detector_channel_notify_name),
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description =
                    context.getString(R.string.local_crash_detector_channel_notify_description)
            }

            notificationManager.createNotificationChannel(channel)
        }

        val notificationBuilder = NotificationCompat.Builder(
            context,
            context.getString(R.string.local_crash_detector_channel_notify_id)
        )

        val intent: Intent = Intent(applicationContext, CrashesListActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
            action = System.currentTimeMillis().toString()
        }

        val pendingIntent = PendingIntent.getActivity(context, 0, intent, 0)

        notificationBuilder
            .setSmallIcon(R.drawable.ic_warning)
            .setContentIntent(pendingIntent)
            .setContentTitle(context.getString(R.string.library_name))
            .setAutoCancel(true)
            .setColor(ContextCompat.getColor(context, R.color.red_bc0))
            .setContentText(
                if (TextUtils.isEmpty(exceptionMessage)) {
                    context.getString(R.string.local_crash_detector_notify_description)
                } else {
                    exceptionMessage
                }
            )

        notificationManager.notify(NOTIFICATION_ID, notificationBuilder.build())
    }

    private fun getStackTrace(e: Throwable): String {
        val result: Writer = StringWriter()
        val printWriter = PrintWriter(result)
        e.printStackTrace(printWriter)
        val crashLog = result.toString()
        printWriter.close()
        return crashLog
    }
}