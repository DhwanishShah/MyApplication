package com.example.myapplication

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.appbar.MaterialToolbar

class AppNotificationsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)

        setContentView(R.layout.activity_app_notifications)

        val pkg = intent.getStringExtra("pkg") ?: return
        // **THE FIX**: Get the correct app name directly from the intent.
        val appName = intent.getStringExtra("app_name") ?: pkg

        val toolbar = findViewById<MaterialToolbar>(R.id.toolbar)
        val recycler = findViewById<RecyclerView>(R.id.recyclerAppNoti)

        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolbar.setNavigationOnClickListener { onBackPressed() }
        
        // Set the title using the correct name.
        title = appName

        recycler.layoutManager = LinearLayoutManager(this)
        val adapter = NotificationAdapter(emptyList()) { notification ->
            try {
                val launchIntent = packageManager.getLaunchIntentForPackage(notification.pkg)
                if (launchIntent != null) {
                    startActivity(launchIntent)
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
                val appIcon = it.getBlob(it.getColumnIndexOrThrow("app_icon"))

                list.add(
                    NotificationModel(
                        id = id,
                        pkg = pkg,
                        title = title,
                        text = text,
                        time = time,
                        image = image,
                        pendingIntent = null,
                        appIcon = appIcon
                    )
                )
            }
        }

        adapter.updateList(list.sortedByDescending { it.time })
    }
}
