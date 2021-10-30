package io.github.kaczmarek.localcrashdetector.ui.crashes_list

import android.content.pm.ApplicationInfo
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.RecyclerView
import io.github.kaczmarek.localcrashdetector.R
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class CrashesListActivity : AppCompatActivity() {

    private lateinit var vm: CrashesListViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_crashes_list)

        vm = ViewModelProvider(this)[CrashesListViewModel::class.java]

        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        val rvCrashesList = findViewById<RecyclerView>(R.id.rv_crashes_list)
        toolbar.subtitle = getString(
            R.string.activity_crash_list_description,
            getApplicationName()
        )

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                vm.uiState.collect { result ->
                    when (result) {
                        is Result.Loading -> Log.i(TAG, "Loading")

                        is Result.Success -> rvCrashesList.adapter = CrashListRvAdapter(result.data)

                        is Error -> Log.i(TAG, "Error = ${result.message}")

                        is Result.Empty -> Log.i(TAG, "Empty")
                    }
                }
            }
        }
    }

    private fun getApplicationName(): String {
        val applicationInfo: ApplicationInfo = applicationInfo
        val stringId = applicationInfo.labelRes
        return if (stringId == 0)
            applicationInfo.nonLocalizedLabel.toString()
        else
            getString(stringId)
    }

    companion object {
        const val TAG = "CrashesListActivity"
    }
}