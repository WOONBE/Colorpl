package com.presentation.component.adapter.schedule

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncDifferConfig
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.colorpl.presentation.databinding.ItemCalendarBinding
import com.domain.model.CalendarItem
import com.presentation.base.BaseDiffUtil

class CalendarAdapter(private val onItemClick: (CalendarItem) -> Unit) :
    ListAdapter<CalendarItem, ViewHolder>(AsyncDifferConfig.Builder(BaseDiffUtil<CalendarItem>()).build()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = ItemCalendarBinding.inflate(inflater, parent, false)
        return CalendarViewHolder(binding, onItemClick)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        when (holder) {
            is CalendarViewHolder -> {
                holder.bind(getItem(position))
            }
        }
    }
}