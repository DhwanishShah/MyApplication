package com.example.myapplication

import android.app.PendingIntent

data class NotificationModel(
    val id: Int,
    val pkg: String,
    val title: String,
    val text: String,
    val time: Long,
    val image: ByteArray? = null, // Keep this for image data
    @Transient var pendingIntent: PendingIntent? = null
)
