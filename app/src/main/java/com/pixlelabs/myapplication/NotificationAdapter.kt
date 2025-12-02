package com.pixlelabs.myapplication

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.graphics.BitmapFactory
import android.widget.ImageView
import android.widget.TextView
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.card.MaterialCardView
import java.text.SimpleDateFormat
import java.util.*

class NotificationAdapter(
    private val listener: (NotificationModel) -> Unit,
    private val selectionListener: (Int) -> Unit
) : PagingDataAdapter<NotificationModel, NotificationAdapter.ViewHolder>(NotificationDiffCallback()) {

    private val selectedItems = mutableSetOf<Int>()
    var isSelectionMode = false

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.notification_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = getItem(position)
        if (item != null) {
            holder.bind(item)
        }
    }

    fun getSelectedItems(): List<NotificationModel> {
        val selectedPositions = selectedItems.toList()
        val selectedNotifications = mutableListOf<NotificationModel>()
        for (position in selectedPositions) {
            getItem(position)?.let { selectedNotifications.add(it) }
        }
        return selectedNotifications
    }

    fun clearSelection() {
        selectedItems.clear()
        notifyDataSetChanged()
        isSelectionMode = false
    }

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val title: TextView = view.findViewById(R.id.titleText)
        private val body: TextView = view.findViewById(R.id.bodyText)
        private val time: TextView = view.findViewById(R.id.timeText)
        private val appIcon: ImageView = view.findViewById(R.id.app_icon)
        private val cardView: MaterialCardView = itemView as MaterialCardView

        fun bind(notification: NotificationModel) {
            title.text = notification.title
            body.text = notification.text
            time.text = SimpleDateFormat("hh:mm a, dd MMM yyyy", Locale.getDefault())
                .format(Date(notification.time))

            if (notification.appIcon != null) {
                val bitmap = BitmapFactory.decodeByteArray(notification.appIcon, 0, notification.appIcon.size)
                appIcon.setImageBitmap(bitmap)
            } else {
                appIcon.setImageResource(R.mipmap.ic_launcher)
            }

            cardView.isChecked = selectedItems.contains(adapterPosition)

            itemView.setOnClickListener {
                if (isSelectionMode) {
                    toggleSelection(adapterPosition)
                } else {
                    listener(notification)
                }
            }

            itemView.setOnLongClickListener {
                if (!isSelectionMode) {
                    isSelectionMode = true
                    toggleSelection(adapterPosition)
                    true
                } else {
                    false
                }
            }
        }

        private fun toggleSelection(position: Int) {
            if (selectedItems.contains(position)) {
                selectedItems.remove(position)
            } else {
                selectedItems.add(position)
            }
            cardView.isChecked = selectedItems.contains(position)
            selectionListener(selectedItems.size)
        }
    }

    class NotificationDiffCallback : DiffUtil.ItemCallback<NotificationModel>() {
        override fun areItemsTheSame(oldItem: NotificationModel, newItem: NotificationModel): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: NotificationModel, newItem: NotificationModel): Boolean {
            return oldItem == newItem
        }
    }
}
