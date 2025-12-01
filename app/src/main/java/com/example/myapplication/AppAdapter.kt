package com.example.myapplication

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class AppAdapter(private val apps: List<App>, private val listener: (App) -> Unit) : RecyclerView.Adapter<AppAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.list_item_app, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val app = apps[position]
        holder.bind(app, listener)
    }

    override fun getItemCount() = apps.size

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val appIcon: ImageView = view.findViewById(R.id.app_icon)
        private val appName: TextView = view.findViewById(R.id.app_name)

        fun bind(app: App, listener: (App) -> Unit) {
            appName.text = app.appName
            appIcon.setImageDrawable(app.appIcon)
            itemView.setOnClickListener { listener(app) }
        }
    }
}
