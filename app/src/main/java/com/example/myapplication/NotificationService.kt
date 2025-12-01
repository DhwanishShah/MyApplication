package com.example.myapplication

import android.app.Notification
import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import java.io.ByteArrayOutputStream

class NotificationService : NotificationListenerService() {

    override fun onNotificationPosted(sbn: StatusBarNotification) {
        val packageName = sbn.packageName
        val notification = sbn.notification
        val extras = notification.extras

        val title = extras.getString(Notification.EXTRA_TITLE) ?: "No Title"
        var text = extras.getCharSequence(Notification.EXTRA_TEXT)?.toString() ?: "No Message"

        // For messaging apps, get the full conversation from the lines
        val lines = extras.getCharSequenceArray(Notification.EXTRA_TEXT_LINES)
        if (lines != null && lines.isNotEmpty()) {
            text = lines.joinToString("\n")
        }

        val time = System.currentTimeMillis()

        var image: ByteArray? = null
        if (extras.containsKey(Notification.EXTRA_PICTURE)) {
            val bmp = extras.get(Notification.EXTRA_PICTURE) as? Bitmap
            if (bmp != null) {
                val stream = ByteArrayOutputStream()
                bmp.compress(Bitmap.CompressFormat.PNG, 100, stream)
                image = stream.toByteArray()
            }
        }

        val intent = Intent(this, SaveService::class.java)
        intent.putExtra("pkg", packageName)
        intent.putExtra("title", title)
        intent.putExtra("text", text)
        intent.putExtra("time", time)
        intent.putExtra("image", image)
        startService(intent)
    }
}
