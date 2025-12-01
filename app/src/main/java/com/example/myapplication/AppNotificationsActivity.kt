package com.example.myapplication

import android.content.Intent
import android.os.Bundle
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
        
        // 1. Pass an empty list to the adapter's constructor
        // 2. Update the click listener to open the app, which is more reliable
        val adapter = NotificationAdapter(emptyList()) { notification ->
            try {
                val intent = packageManager.getLaunchIntentForPackage(notification.pkg)
                if (intent != null) {
                    startActivity(intent)
                }
            } catch (e: Exception) {
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
                
                // 3. Get the app icon data from the cursor
                val appIcon = it.getBlob(it.getColumnIndexOrThrow("app_icon"))

                // 4. Instantiate the correct NotificationModel, removing the broken PendingIntent
                list.add(
                    NotificationModel(
                        id = id,
                        pkg = pkg,
                        title = title,
                        text = text,
                        time = time,
                        image = image,
                        pendingIntent = null, // This is no longer used but kept for constructor shape
                        appIcon = appIcon
                    )
                )
            }
        }

        adapter.updateList(list.sortedByDescending { it.time })
    }
}
