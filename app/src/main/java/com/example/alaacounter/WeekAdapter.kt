package com.example.alaacounter

// WeekAdapter.kt

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class WeekAdapter(private val weekDataList: List<WeekData>) : RecyclerView.Adapter<WeekAdapter.ViewHolder>() {

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val weekNumberTextView: TextView = itemView.findViewById(R.id.weekNumberTextView)
        val greenButtonCountTextView: TextView = itemView.findViewById(R.id.greenButtonCountTextView)
        val resultTextView: TextView = itemView.findViewById(R.id.resultTextView)
        val itemLayout: LinearLayout = itemView.findViewById(R.id.itemLayout) // Added line
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.item_week, parent, false)
        return ViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val weekData = weekDataList[position]

        holder.weekNumberTextView.text = "Previous Week:"
        holder.greenButtonCountTextView.text = "Days pressed in this week: ${weekData.greenButtonCount}"
        holder.resultTextView.text = "Your result for the week is:  ${weekData.resultText}"

        // Set background color based on the result value
        val backgroundColorResId = if (weekData.greenButtonCount == 0) R.color.green_background else R.color.red_background
        holder.itemLayout.setBackgroundResource(backgroundColorResId)
    }

    override fun getItemCount(): Int {
        return weekDataList.size
    }
}

