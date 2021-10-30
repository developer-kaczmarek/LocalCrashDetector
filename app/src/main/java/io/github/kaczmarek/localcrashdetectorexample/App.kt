package io.github.kaczmarek.localcrashdetectorexample

import android.app.Application
import io.github.kaczmarek.localcrashdetector.LocalCrashDetector

class App: Application() {

    override fun onCreate() {
        super.onCreate()

        LocalCrashDetector.init(this)
    }
}