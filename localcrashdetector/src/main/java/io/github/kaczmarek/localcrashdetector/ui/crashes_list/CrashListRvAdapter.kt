package io.github.kaczmarek.localcrashdetector.ui.crashes_list

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import io.github.kaczmarek.localcrashdetector.R

class CrashListRvAdapter(private val crashes: List<CrashItem>) :
    RecyclerView.Adapter<CrashListRvAdapter.CrashViewHolder>() {

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): CrashViewHolder =
        CrashViewHolder(
            LayoutInflater
                .from(viewGroup.context)
                .inflate(R.layout.rv_crash_info_item, viewGroup, false)
        )

    override fun onBindViewHolder(viewHolder: CrashViewHolder, position: Int) {
        viewHolder.bind(crashes[position])
    }

    override fun getItemCount() = crashes.size

    class CrashViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val tvTime: TextView = view.findViewById(R.id.tv_crash_time)
        private val tvInfo: TextView = view.findViewById(R.id.tv_crash_info)
        private val llContent: LinearLayout = view.findViewById(R.id.ll_content)

        init {
            llContent.setOnClickListener {

            }
        }

        fun bind(item: CrashItem) {
            tvTime.text = item.time
            tvInfo.text = item.info
        }
    }
}