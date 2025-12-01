package com.example.myapplication

import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.os.IBinder

class SaveService : Service() {

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        val pkg = intent?.getStringExtra("pkg") ?: ""
        val title = intent?.getStringExtra("title") ?: ""
        val text = intent?.getStringExtra("text") ?: ""
        val time = intent?.getLongExtra("time", 0) ?: 0
        val image = intent?.getByteArrayExtra("image")

        // The PendingIntent has been removed from the save method to fix the crash.
        NotificationDB.save(this, pkg, title, text, time, image)

        return START_NOT_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null
}