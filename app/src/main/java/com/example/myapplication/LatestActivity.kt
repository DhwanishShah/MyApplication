package com.example.myapplication

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class LatestActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: NotificationAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_latest)

        recyclerView = findViewById(R.id.recyclerLatest)

        // 1. Pass an empty list to the adapter's constructor
        // 2. Update the click listener to open the app, which is more reliable
        adapter = NotificationAdapter(emptyList()) { notification ->
            try {
                val intent = packageManager.getLaunchIntentForPackage(notification.pkg)
                if (intent != null) {
                    startActivity(intent)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter
    }

    override fun onResume() {
        super.onResume()
        loadNotifications()
    }

    private fun loadNotifications() {
        val cursor = NotificationDB.getAll(this)
        val notifications = mutableListOf<NotificationModel>()

        cursor.use {
            while (it.moveToNext()) {
                val id = it.getInt(it.getColumnIndexOrThrow("id"))
                val pkg = it.getString(it.getColumnIndexOrThrow("pkg"))
                val title = it.getString(it.getColumnIndexOrThrow("title"))
                val text = it.getString(it.getColumnIndexOrThrow("text"))
                val time = it.getLong(it.getColumnIndexOrThrow("time"))
                val image = it.getBlob(it.getColumnIndexOrThrow("image"))
                
                // 3. Get the app icon data from the cursor
                val appIcon = it.getBlob(it.getColumnIndexOrThrow("app_icon"))

                // 4. Instantiate the correct NotificationModel
                notifications.add(
                    NotificationModel(
                        id = id,
                        pkg = pkg,
                        title = title,
                        text = text,
                        time = time,
                        image = image,
                        pendingIntent = null, // This is no longer used
                        appIcon = appIcon
                    )
                )
            }
        }

        adapter.updateList(notifications)
    }
}
