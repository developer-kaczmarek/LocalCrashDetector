package io.github.kaczmarek.localcrashdetector.ui.crashes_list

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

data class CrashItem(
    val time: String,
    val info: String
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

    private fun readCrashReason(file: File): String {
        val reader = BufferedReader(FileReader(file))
        val firstLine = reader.readLine()
        reader.close()
        return firstLine
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
                        val info = readCrashReason(it)
                        CrashItem(time, info)
                    } ?: emptyList()
                )
            }
        }
        return crashes
    }
}