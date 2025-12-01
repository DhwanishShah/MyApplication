package com.example.myapplication

import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        if (!isNotificationListenerEnabled()) {
            startActivity(Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS))
        }

        if (savedInstanceState == null) {
            loadFragment(LatestFragment())
        }

        val bottom = findViewById<BottomNavigationView>(R.id.bottomNav)

        bottom.setOnItemSelectedListener {
            when (it.itemId) {
                R.id.menu_latest -> loadFragment(LatestFragment())
                R.id.menu_apps -> loadFragment(AppsFragment())
            }
            true
        }
    }

    private fun loadFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.frameContainer, fragment)
            .commit()
    }

    private fun isNotificationListenerEnabled(): Boolean {
        val enabledListeners = Settings.Secure.getString(
            contentResolver,
            "enabled_notification_listeners"
        ) ?: ""
        return enabledListeners.contains(packageName)
    }
}
