package io.github.kaczmarek.localcrashdetector.ui.crashes_list

import android.os.Build
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.github.kaczmarek.localcrashdetector.LocalCrashDetector
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.io.BufferedReader
import java.io.File
import java.io.FileReader
import java.lang.RuntimeException
import java.lang.StringBuilder

data class CrashItem(
    val time: String,
    val previewInfo: String,
    val fullInfo: String,
    val device: DeviceDetails,
    val path: String
)

data class DeviceDetails(
    val manufacturer: String,
    val model: String,
    val android: String,
    val apiLevel: String,
    val board: String
)

sealed class Result {
    object Loading : Result()
    object Empty : Result()
    data class Success(val data: List<CrashItem>) : Result()
    data class Error(val e: Exception) : Result()
}

class CrashesListViewModel : ViewModel() {

    private val _uiState = MutableStateFlow<Result>(Result.Loading)
    val uiState: StateFlow<Result> = _uiState

    init {
        viewModelScope.launch {
            try {
                val result = getCrashItems()
                _uiState.value = if (result.isEmpty()) {
                    Result.Empty
                } else {
                    Result.Success(result)
                }
            } catch (e: Exception) {
                _uiState.value = Result.Error(e)
            }
        }
    }

    private suspend fun getCrashItems(): List<CrashItem> {
        val crashes = arrayListOf<CrashItem>()
        coroutineScope {
            launch(Dispatchers.IO) {
                val directoryPath = LocalCrashDetector.defaultPath
                val directory = File(directoryPath)
                if (!directory.exists() || !directory.isDirectory) {
                    throw RuntimeException("The path provided doesn't exists : $directoryPath")
                }
                crashes.addAll(
                    directory.listFiles()?.map {
                        val time = it.name.removeSuffix(".txt")
                        val previewInfo = readPreviewCrashReason(it)
                        val fullInfo = readAllInfoCrashReason(it)
                        val deviceDetails = getDeviceDetails()

                        CrashItem(time, previewInfo, fullInfo, deviceDetails, it.absolutePath)
                    } ?: emptyList()
                )
            }
        }
        return crashes
    }

    private fun readPreviewCrashReason(file: File): String {
        val reader = BufferedReader(FileReader(file))
        val firstLine = reader.readLine()
        reader.close()
        return firstLine
    }

    private fun readAllInfoCrashReason(file: File?): String {
        val crash = StringBuilder()
        val reader = BufferedReader(FileReader(file))
        var line: String?
        while (reader.readLine().also { line = it } != null) {
            crash.append(line)
            crash.append('\n')
        }
        reader.close()
        return crash.toString()
    }

    private fun getDeviceDetails(): DeviceDetails =
        DeviceDetails(
            manufacturer = Build.MANUFACTURER,
            model = Build.MODEL,
            android = Build.VERSION.RELEASE,
            apiLevel = Build.VERSION.SDK_INT.toString(),
            board = Build.BOARD
        )
}