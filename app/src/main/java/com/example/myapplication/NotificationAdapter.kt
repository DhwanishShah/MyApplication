package com.example.myapplication

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.graphics.BitmapFactory
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.card.MaterialCardView
import java.text.SimpleDateFormat
import java.util.*

class NotificationAdapter(
    private var notifications: List<NotificationModel>,
    private val listener: (NotificationModel) -> Unit,
    private val selectionListener: (Int) -> Unit
) : RecyclerView.Adapter<NotificationAdapter.ViewHolder>() {

    private val selectedItems = mutableSetOf<Int>()
    var isSelectionMode = false

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.notification_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = notifications[position]
        holder.bind(item)
        holder.itemView.animation = AnimationUtils.loadAnimation(holder.itemView.context, R.anim.fade_in)
    }

    override fun getItemCount() = notifications.size

    fun getSelectedItems(): List<NotificationModel> {
        return selectedItems.map { notifications[it] }
    }

    fun clearSelection() {
        selectedItems.clear()
        notifyDataSetChanged()
        isSelectionMode = false
    }

    fun updateList(list: List<NotificationModel>) {
        notifications = list
        notifyDataSetChanged()
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
}
