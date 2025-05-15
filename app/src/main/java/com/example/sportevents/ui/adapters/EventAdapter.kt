package com.example.sportevents.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.sportevents.data.models.Event
import com.example.sportevents.databinding.ItemEventBinding
import java.text.SimpleDateFormat
import java.util.Locale

class EventAdapter(private val onEventClick: (eventId: Int) -> Unit) : 
    ListAdapter<Event, EventAdapter.EventViewHolder>(EventDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EventViewHolder {
        val binding = ItemEventBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return EventViewHolder(binding)
    }

    override fun onBindViewHolder(holder: EventViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class EventViewHolder(private val binding: ItemEventBinding) : 
        RecyclerView.ViewHolder(binding.root) {
        
        init {
            binding.root.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onEventClick(getItem(position).id)
                }
            }
        }
        
        fun bind(event: Event) {
            binding.textViewTitle.text = event.title
            binding.textViewSportType.text = event.sport_type.name
            binding.textViewEventType.text = event.event_type.name
            
            val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
            val displayFormat = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault())
            
            val startDate = dateFormat.parse(event.start_datetime)
            binding.textViewStartDate.text = displayFormat.format(startDate!!)
            
            val locationText = event.location?.name ?: event.custom_location_text ?: "No location"
            binding.textViewLocation.text = locationText
            
            val participantsText = if (event.max_participants != null) {
                "${event.current_participants_count}/${event.max_participants}"
            } else {
                "${event.current_participants_count}"
            }
            binding.textViewParticipants.text = participantsText
            
            binding.textViewOrganizer.text = "By: ${event.organizer.display_name}"
        }
    }
}

class EventDiffCallback : DiffUtil.ItemCallback<Event>() {
    override fun areItemsTheSame(oldItem: Event, newItem: Event): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: Event, newItem: Event): Boolean {
        return oldItem == newItem
    }
} 