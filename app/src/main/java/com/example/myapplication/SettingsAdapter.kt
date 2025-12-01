package com.example.myapplication

import android.content.Context
import android.content.SharedPreferences
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.widget.SwitchCompat
import androidx.recyclerview.widget.RecyclerView

class SettingsAdapter(
    private val context: Context,
    private val apps: List<ApplicationInfo>
) : RecyclerView.Adapter<SettingsAdapter.ViewHolder>() {

    private val pm: PackageManager = context.packageManager
    private val prefs: SharedPreferences =
        context.getSharedPreferences("prefs", Context.MODE_PRIVATE)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.list_item_setting, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(apps[position])
    }

    override fun getItemCount(): Int = apps.size

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        private val appIcon: ImageView = itemView.findViewById(R.id.app_icon)
        private val appName: TextView = itemView.findViewById(R.id.app_name)
        private val appSwitch: SwitchCompat = itemView.findViewById(R.id.app_switch)

        fun bind(app: ApplicationInfo) {

            // Label + Icon
            appName.text = pm.getApplicationLabel(app).toString()
            appIcon.setImageDrawable(pm.getApplicationIcon(app))

            val key = app.packageName

            // ❗ Remove old listener to avoid wrong reuse state
            appSwitch.setOnCheckedChangeListener(null)

            // Load saved value
            val enabled = prefs.getBoolean(key, true)
            appSwitch.isChecked = enabled

            // Save new value
            appSwitch.setOnCheckedChangeListener { _, isChecked ->
                prefs.edit().putBoolean(key, isChecked).apply()
            }
        }
    }
}
