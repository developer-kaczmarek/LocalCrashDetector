package io.github.kaczmarek.localcrashdetector.ui.crashes_list

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import io.github.kaczmarek.localcrashdetector.R

class CrashListRvAdapter :
    ListAdapter<CrashItem, CrashListRvAdapter.CrashViewHolder>(DiffCallback()) {
    var listener: OnClickCrashItemListener? = null

    private class DiffCallback : DiffUtil.ItemCallback<CrashItem>() {
        override fun areContentsTheSame(oldItem: CrashItem, newItem: CrashItem): Boolean =
            oldItem == newItem

        override fun areItemsTheSame(oldItem: CrashItem, newItem: CrashItem): Boolean =
            oldItem.time == newItem.time
    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): CrashViewHolder =
        CrashViewHolder(
            LayoutInflater
                .from(viewGroup.context)
                .inflate(R.layout.rv_crash_info_item, viewGroup, false)
        )

    override fun onBindViewHolder(viewHolder: CrashViewHolder, position: Int) {
        viewHolder.bind(getItem(position))
    }

    fun update(list: List<CrashItem>) {
        this.submitList(ArrayList(list))
    }

    inner class CrashViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val tvTime: TextView = view.findViewById(R.id.tv_crash_time)
        private val tvInfo: TextView = view.findViewById(R.id.tv_crash_info)
        private val llContent: LinearLayout = view.findViewById(R.id.ll_content)

        init {
            llContent.setOnClickListener {
                val position = adapterPosition
                if (position == RecyclerView.NO_POSITION) return@setOnClickListener
                listener?.onClick(getItem(position))
            }
        }

        fun bind(item: CrashItem) {
            tvTime.text = item.time
            tvInfo.text = item.previewInfo
        }
    }
}

interface OnClickCrashItemListener {

    fun onClick(item: CrashItem)
}