package com.example.myapplication

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.graphics.BitmapFactory
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import java.text.SimpleDateFormat
import java.util.*

class NotificationAdapter(
    private var notifications: List<NotificationModel>,
    private val listener: (NotificationModel) -> Unit
) : RecyclerView.Adapter<NotificationAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.notification_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = notifications[position]
        holder.bind(item, listener)
        // Add the fade-in animation
        holder.itemView.animation = AnimationUtils.loadAnimation(holder.itemView.context, R.anim.fade_in)
    }

    override fun getItemCount() = notifications.size

    fun updateList(list: List<NotificationModel>) {
        notifications = list
        notifyDataSetChanged()
    }

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val title: TextView = view.findViewById(R.id.titleText)
        private val body: TextView = view.findViewById(R.id.bodyText)
        private val time: TextView = view.findViewById(R.id.timeText)
        private val appIcon: ImageView = view.findViewById(R.id.app_icon)

        fun bind(notification: NotificationModel, listener: (NotificationModel) -> Unit) {
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

            itemView.setOnClickListener { listener(notification) }
        }
    }
}
