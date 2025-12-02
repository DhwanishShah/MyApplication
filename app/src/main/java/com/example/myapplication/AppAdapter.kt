package com.example.myapplication

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.card.MaterialCardView

class AppAdapter(
    private var apps: List<App>,
    private val listener: (App) -> Unit,
    private val selectionListener: (Int) -> Unit
) : RecyclerView.Adapter<AppAdapter.ViewHolder>() {

    private val selectedItems = mutableSetOf<Int>()
    var isSelectionMode = false

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.list_item_app, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val app = apps[position]
        holder.bind(app)
    }

    override fun getItemCount() = apps.size

    fun getSelectedItems(): List<App> {
        return selectedItems.map { apps[it] }
    }

    fun clearSelection() {
        selectedItems.clear()
        notifyDataSetChanged()
        isSelectionMode = false
    }

    fun updateData(newApps: List<App>) {
        this.apps = newApps
        notifyDataSetChanged()
    }

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val appIcon: ImageView = view.findViewById(R.id.app_icon)
        private val appName: TextView = view.findViewById(R.id.app_name)
        private val cardView: MaterialCardView = itemView as MaterialCardView

        fun bind(app: App) {
            appName.text = app.appName
            appIcon.setImageDrawable(app.appIcon)
            cardView.isChecked = selectedItems.contains(adapterPosition)

            itemView.setOnClickListener {
                if (isSelectionMode) {
                    toggleSelection(adapterPosition)
                } else {
                    listener(app)
                }
            }

            itemView.setOnLongClickListener {
                if (!isSelectionMode) {
                    isSelectionMode = true
                    toggleSelection(adapterPosition)
                    true
                } else {
                    false
                }
            }
        }

        private fun toggleSelection(position: Int) {
            if (selectedItems.contains(position)) {
                selectedItems.remove(position)
            } else {
                selectedItems.add(position)
            }
            cardView.isChecked = selectedItems.contains(position)
            selectionListener(selectedItems.size)
        }
    }
}
