package io.github.kaczmarek.localcrashdetector.ui.crashes_list

import android.content.pm.ApplicationInfo
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.FrameLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetBehavior
import io.github.kaczmarek.localcrashdetector.R
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import android.content.Intent
import androidx.core.content.FileProvider
import java.io.File

class CrashesListActivity : AppCompatActivity(), OnClickCrashItemListener {

    private lateinit var vm: CrashesListViewModel
    private lateinit var tvFullCrashReason: TextView
    private lateinit var tvDeviceDetails: TextView
    private lateinit var btnShareFile: Button
    private lateinit var bottomSheetBehavior: BottomSheetBehavior<FrameLayout>
    private val bottomSheetCallback = object : BottomSheetBehavior.BottomSheetCallback() {

        override fun onStateChanged(bottomSheet: View, newState: Int) {}

        override fun onSlide(bottomSheet: View, slideOffset: Float) {}
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_crashes_list)
        vm = ViewModelProvider(this)[CrashesListViewModel::class.java]

        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        val rvCrashesList = findViewById<RecyclerView>(R.id.rv_crashes_list)
        val flCrashDetailsBottomSheet =
            findViewById<FrameLayout>(R.id.fl_crash_details_bottom_sheet)
        tvFullCrashReason = findViewById(R.id.tv_details_full_info)
        tvDeviceDetails = findViewById(R.id.tv_details_device_info)
        btnShareFile = findViewById(R.id.btn_share_file)
        bottomSheetBehavior = BottomSheetBehavior.from(flCrashDetailsBottomSheet)

        toolbar.subtitle = getString(
            R.string.activity_crash_list_description,
            getApplicationName()
        )
        val rvAdapter = CrashListRvAdapter().apply {
            listener = this@CrashesListActivity
        }
        rvCrashesList.adapter = rvAdapter

        bottomSheetBehavior.addBottomSheetCallback(bottomSheetCallback)
        bottomSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                vm.uiState.collect { result ->
                    when (result) {
                        is Result.Loading -> Log.i(TAG, "Loading")

                        is Result.Success -> rvAdapter.update(result.data)

                        is Error -> Log.i(TAG, "Error = ${result.message}")

                        is Result.Empty -> Log.i(TAG, "Empty")
                    }
                }
            }
        }
    }

    override fun onDestroy() {
        bottomSheetBehavior.removeBottomSheetCallback(bottomSheetCallback)
        super.onDestroy()
    }

    override fun onClick(item: CrashItem) {
        showDetails(item.fullInfo, item.device, item.path)
    }

    private fun showDetails(crashReason: String, device: DeviceDetails, path: String) {
        tvFullCrashReason.text = crashReason
        val deviceDetails = getString(
            R.string.activity_crash_device_details,
            device.manufacturer,
            device.model,
            device.android,
            device.apiLevel,
            device.board
        )
        tvDeviceDetails.text = deviceDetails
        btnShareFile.setOnClickListener {
            val sharingIntent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                putExtra(
                    Intent.EXTRA_STREAM,
                    FileProvider.getUriForFile(
                        this@CrashesListActivity,
                        AUTHORITIES,
                        File(path)
                    )
                )
                putExtra(Intent.EXTRA_TEXT, deviceDetails)
            }
            startActivity(Intent.createChooser(sharingIntent, null))
        }
        bottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
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
        const val AUTHORITIES = "io.github.kaczmarek.localcrashdetector.provider"
    }
}