package com.example.myapplication

import android.app.PendingIntent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import java.io.ByteArrayInputStream
import java.io.ObjectInputStream

class LatestActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: NotificationAdapter
    private val notifications = mutableListOf<NotificationModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_latest)

        recyclerView = findViewById(R.id.recyclerLatest)
        adapter = NotificationAdapter { notification ->
            try {
                notification.pendingIntent?.send()
            } catch (e: PendingIntent.CanceledException) {
                e.printStackTrace()
            }
        }

        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter

        loadNotifications()
    }

    private fun loadNotifications() {
        val cursor = NotificationDB.getAll(this)
        notifications.clear()

        cursor.use {
            while (it.moveToNext()) {
                val pendingIntentBytes = it.getBlob(it.getColumnIndexOrThrow("pending_intent"))
                var pendingIntent: PendingIntent? = null
                if (pendingIntentBytes != null) {
                    try {
                        val bis = ByteArrayInputStream(pendingIntentBytes)
                        val ois = ObjectInputStream(bis)
                        pendingIntent = ois.readObject() as? PendingIntent
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }

                notifications.add(
                    NotificationModel(
                        it.getInt(it.getColumnIndexOrThrow("id")),
                        it.getString(it.getColumnIndexOrThrow("pkg")),
                        it.getString(it.getColumnIndexOrThrow("title")),
                        it.getString(it.getColumnIndexOrThrow("text")),
                        it.getLong(it.getColumnIndexOrThrow("time")),
                        it.getBlob(it.getColumnIndexOrThrow("image")),
                        pendingIntent
                    )
                )
            }
        }

        adapter.updateList(notifications.sortedByDescending { it.time })
    }
}
