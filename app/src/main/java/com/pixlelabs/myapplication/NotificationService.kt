package com.pixlelabs.myapplication

import android.app.Notification
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import java.io.ByteArrayOutputStream

class NotificationService : NotificationListenerService() {

    override fun onNotificationPosted(sbn: StatusBarNotification) {
        val packageName = sbn.packageName
        val prefs = getSharedPreferences("prefs", MODE_PRIVATE)

        // Only save if the app is enabled in settings.
        if (!prefs.getBoolean(packageName, true)) {
            return
        }

        val notification = sbn.notification
        val extras = notification.extras

        val title = extras.getString(Notification.EXTRA_TITLE) ?: "No Title"
        var text = extras.getCharSequence(Notification.EXTRA_TEXT)?.toString() ?: "No Message"

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

        // Capture app name and icon at the source.
        var appName: String? = null
        var appIcon: ByteArray? = null
        try {
            val pm = applicationContext.packageManager
            val appInfo = pm.getApplicationInfo(packageName, 0)
            appName = pm.getApplicationLabel(appInfo).toString()
            val iconDrawable = pm.getApplicationIcon(appInfo)
            appIcon = drawableToByteArray(iconDrawable)
        } catch (e: PackageManager.NameNotFoundException) {
            appName = packageName // Fallback to package name
        }

        val intent = Intent(this, SaveService::class.java)
        intent.putExtra("pkg", packageName)
        intent.putExtra("title", title)
        intent.putExtra("text", text)
        intent.putExtra("time", time)
        intent.putExtra("image", image)
        intent.putExtra("app_name", appName)
        intent.putExtra("app_icon", appIcon)
        startService(intent)
    }

    private fun drawableToByteArray(drawable: Drawable?): ByteArray? {
        if (drawable == null) return null

        val bitmap = if (drawable is BitmapDrawable) {
            drawable.bitmap
        } else if (drawable.intrinsicWidth <= 0 || drawable.intrinsicHeight <= 0) {
            null
        } else {
            Bitmap.createBitmap(drawable.intrinsicWidth, drawable.intrinsicHeight, Bitmap.Config.ARGB_8888).also {
                val canvas = Canvas(it)
                drawable.setBounds(0, 0, canvas.width, canvas.height)
                drawable.draw(canvas)
            }
        }

        return bitmap?.let {
            val stream = ByteArrayOutputStream()
            it.compress(Bitmap.CompressFormat.PNG, 100, stream)
            stream.toByteArray()
        }
    }
}
