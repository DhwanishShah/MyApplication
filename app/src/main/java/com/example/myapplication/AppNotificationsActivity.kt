package com.example.myapplication

import android.app.PendingIntent
import android.os.Bundle
import android.os.Parcel
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class AppNotificationsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_app_notifications)

        val pkg = intent.getStringExtra("pkg") ?: return
        val recycler = findViewById<RecyclerView>(R.id.recyclerAppNoti)

        recycler.layoutManager = LinearLayoutManager(this)
        val adapter = NotificationAdapter { notification ->
            try {
                notification.pendingIntent?.send()
            } catch (e: PendingIntent.CanceledException) {
                e.printStackTrace()
            }
        }
        recycler.adapter = adapter

        val cursor = NotificationDB.getByPackage(this, pkg)
        val list = mutableListOf<NotificationModel>()

        cursor.use {
            while (it.moveToNext()) {
                val id = it.getInt(it.getColumnIndexOrThrow("id"))
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

                list.add(
                    NotificationModel(
                        id,
                        pkg,
                        title,
                        text,
                        time,
                        image,
                        pendingIntent
                    )
                )
            }
        }

        adapter.updateList(list.sortedByDescending { it.time })
    }
}
