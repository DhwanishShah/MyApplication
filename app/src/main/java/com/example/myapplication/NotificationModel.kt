package com.example.myapplication

import android.app.PendingIntent

data class NotificationModel(
    val id: Int,
    val pkg: String,
    val title: String,
    val text: String,
    val time: Long,
    val image: ByteArray? = null,
    @Transient var pendingIntent: PendingIntent? = null, // This is not saved in the database
    val appIcon: ByteArray? = null
)
