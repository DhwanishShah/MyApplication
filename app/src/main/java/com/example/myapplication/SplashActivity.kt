package com.example.myapplication

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.animation.AnimationUtils
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class SplashActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        val appIconView = findViewById<ImageView>(R.id.splash_app_icon)
        val appNameView = findViewById<TextView>(R.id.splash_app_name)
        val fadeInAnimation = AnimationUtils.loadAnimation(this, R.anim.fade_in_splash)
        
        // Animate both the icon and the name
        appIconView.startAnimation(fadeInAnimation)
        appNameView.startAnimation(fadeInAnimation)

        // Delay for a short period and then start the main activity
        Handler(Looper.getMainLooper()).postDelayed({
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish() // Finish this activity so the user can't navigate back to it
        }, 2000) // 2-second delay
    }
}
