package com.example.myapplication

import android.app.PendingIntent
import android.graphics.BitmapFactory
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import java.text.SimpleDateFormat
import java.util.*

class NotificationAdapter(private val listener: (NotificationModel) -> Unit) : RecyclerView.Adapter<NotificationAdapter.ViewHolder>() {

    private var notifications = listOf<NotificationModel>()

    fun updateList(list: List<NotificationModel>) {
        notifications = list
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.list_item_notification, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int = notifications.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = notifications[position]
        holder.bind(item, listener)
    }

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val title: TextView = view.findViewById(R.id.notification_title)
        private val body: TextView = view.findViewById(R.id.notification_text)
        private val time: TextView = view.findViewById(R.id.timeText)
        private val image: ImageView = view.findViewById(R.id.notification_image)

        fun bind(notification: NotificationModel, listener: (NotificationModel) -> Unit) {
            title.text = notification.title
            body.text = notification.text
            time.text = SimpleDateFormat("hh:mm a, dd MMM yyyy", Locale.getDefault())
                .format(Date(notification.time))

            if (notification.image != null) {
                val bitmap = BitmapFactory.decodeByteArray(notification.image, 0, notification.image.size)
                image.setImageBitmap(bitmap)
                image.visibility = View.VISIBLE
            } else {
                image.visibility = View.GONE
            }

            itemView.setOnClickListener { listener(notification) }
        }
    }
}
