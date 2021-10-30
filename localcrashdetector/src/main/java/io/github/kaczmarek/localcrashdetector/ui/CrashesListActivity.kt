package io.github.kaczmarek.localcrashdetector.ui

import android.content.pm.ApplicationInfo
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import io.github.kaczmarek.localcrashdetector.R

class CrashesListActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_crashes_list)

        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        toolbar.subtitle = getString(
            R.string.activity_crash_list_description,
            getApplicationName()
        )
    }

    private fun getApplicationName(): String {
        val applicationInfo: ApplicationInfo = applicationInfo
        val stringId = applicationInfo.labelRes
        return if (stringId == 0)
            applicationInfo.nonLocalizedLabel.toString()
        else
            getString(stringId)
    }
}