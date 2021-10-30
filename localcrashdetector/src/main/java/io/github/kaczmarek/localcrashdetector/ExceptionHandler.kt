package io.github.kaczmarek.localcrashdetector

class ExceptionHandler : Thread.UncaughtExceptionHandler {
    private val exceptionHandler: Thread.UncaughtExceptionHandler? =
        Thread.getDefaultUncaughtExceptionHandler()

    override fun uncaughtException(thread: Thread, throwable: Throwable) {
        LocalCrashDetector.saveCrashReport(throwable)
        exceptionHandler?.uncaughtException(thread, throwable)
    }
}
