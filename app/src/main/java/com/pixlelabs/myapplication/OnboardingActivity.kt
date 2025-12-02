package com.pixlelabs.myapplication

import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity

class OnboardingActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // If permission is already granted, skip onboarding and go to the main app
        if (isNotificationListenerEnabled()) {
            startMainActivity()
            return
        }

        setContentView(R.layout.activity_onboarding)

        val grantButton = findViewById<Button>(R.id.grant_permission_button)
        grantButton.setOnClickListener {
            startActivity(Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS))
        }
    }

    override fun onResume() {
        super.onResume()
        // When the user returns from the settings screen, check the permission again.
        if (isNotificationListenerEnabled()) {
            startMainActivity()
        }
    }

    private fun isNotificationListenerEnabled(): Boolean {
        val enabledListeners = Settings.Secure.getString(contentResolver, "enabled_notification_listeners") ?: ""
        return enabledListeners.contains(packageName)
    }

    private fun startMainActivity() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish() // Finish this activity so the user can't navigate back to it
    }
}
