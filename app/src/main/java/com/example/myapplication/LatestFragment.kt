package com.example.myapplication

import android.app.PendingIntent
import android.os.Bundle
import android.os.Parcel
import android.view.View
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class LatestFragment : Fragment(R.layout.fragment_latest) {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: NotificationAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        recyclerView = view.findViewById(R.id.rvLatest)
        adapter = NotificationAdapter { notification ->
            notification.pendingIntent?.send()
        }

        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = adapter

        loadNotifications()
    }

    private fun loadNotifications() {
        val cursor = NotificationDB.getAll(requireContext())
        val notifications = mutableListOf<NotificationModel>()

        cursor.use {
            while (it.moveToNext()) {
                val id = it.getInt(it.getColumnIndexOrThrow("id"))
                val pkg = it.getString(it.getColumnIndexOrThrow("pkg"))
                val title = it.getString(it.getColumnIndexOrThrow("title"))
                val text = it.getString(it.getColumnIndexOrThrow("text"))
                val time = it.getLong(it.getColumnIndexOrThrow("time"))
                val image = it.getBlob(it.getColumnIndexOrThrow("image"))
                val pendingIntentBytes = it.getBlob(it.getColumnIndexOrThrow("pending_intent"))

                var pendingIntent: PendingIntent? = null
                if (pendingIntentBytes != null) {
                    val parcel = Parcel.obtain()
                    parcel.unmarshall(pendingIntentBytes, 0, pendingIntentBytes.size)
                    parcel.setDataPosition(0)
                    pendingIntent = PendingIntent.readPendingIntentOrNullFromParcel(parcel)
                    parcel.recycle()
                }

                notifications.add(NotificationModel(id, pkg, title, text, time, image, pendingIntent))
            }
        }

        adapter.updateList(notifications)
    }
}
