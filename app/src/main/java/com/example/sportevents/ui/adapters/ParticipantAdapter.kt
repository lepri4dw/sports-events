package com.example.sportevents.ui.adapters

import android.content.res.ColorStateList
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.sportevents.data.models.EventRegistration
import com.example.sportevents.databinding.ItemParticipantBinding
import java.text.SimpleDateFormat
import java.util.Locale

class ParticipantAdapter(
    private val isOrganizer: Boolean,
    private val onApproveClick: (EventRegistration) -> Unit,
    private val onRejectClick: (EventRegistration) -> Unit,
    private val onCancelClick: (EventRegistration) -> Unit
) : ListAdapter<EventRegistration, ParticipantAdapter.ParticipantViewHolder>(DIFF_CALLBACK) {

    companion object {
        private val DIFF_CALLBACK = object : DiffUtil.ItemCallback<EventRegistration>() {
            override fun areItemsTheSame(oldItem: EventRegistration, newItem: EventRegistration): Boolean {
                return oldItem.id == newItem.id
            }

            override fun areContentsTheSame(oldItem: EventRegistration, newItem: EventRegistration): Boolean {
                return oldItem.status == newItem.status &&
                       oldItem.notesByUser == newItem.notesByUser
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ParticipantViewHolder {
        val binding = ItemParticipantBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ParticipantViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ParticipantViewHolder, position: Int) {
        val registration = getItem(position)
        holder.bind(registration)
    }

    inner class ParticipantViewHolder(
        private val binding: ItemParticipantBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(registration: EventRegistration) {
            // Устанавливаем имя участника
            binding.textViewParticipantName.text = registration.user.display_name
            
            // Устанавливаем дату регистрации
            val dateFormat = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault())
            val apiDateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
            val registrationDate = apiDateFormat.parse(registration.registrationDatetime)
            val formattedDate = dateFormat.format(registrationDate!!)
            binding.textViewRegistrationDate.text = "Зарегистрирован: $formattedDate"
            
            // Устанавливаем статус
            binding.chipStatus.text = getStatusText(registration.status)
            binding.chipStatus.chipBackgroundColor = ColorStateList.valueOf(getStatusColor(registration.status))
            
            // Устанавливаем заметки пользователя, если они есть
            if (!registration.notesByUser.isNullOrBlank()) {
                binding.textViewParticipantNotes.text = registration.notesByUser
                binding.textViewParticipantNotes.visibility = View.VISIBLE
            } else {
                binding.textViewParticipantNotes.visibility = View.GONE
            }
            
            // Настраиваем кнопки действий
            setupActionButtons(registration)
        }
        
        private fun setupActionButtons(registration: EventRegistration) {
            // Сначала скрываем все кнопки
            binding.buttonApprove.visibility = View.GONE
            binding.buttonReject.visibility = View.GONE
            binding.buttonCancel.visibility = View.GONE
            
            // Если пользователь организатор, показываем кнопки подтверждения/отклонения
            if (isOrganizer) {
                if (registration.status == "PENDING") {
                    binding.buttonApprove.visibility = View.VISIBLE
                    binding.buttonReject.visibility = View.VISIBLE
                    
                    binding.buttonApprove.setOnClickListener {
                        onApproveClick(registration)
                    }
                    
                    binding.buttonReject.setOnClickListener {
                        onRejectClick(registration)
                    }
                }
            } else {
                // Если пользователь не организатор, но это его регистрация
                // и она в статусе PENDING или APPROVED, показываем кнопку отмены
                if (registration.status == "PENDING" || registration.status == "APPROVED") {
                    binding.buttonCancel.visibility = View.VISIBLE
                    binding.buttonCancel.setOnClickListener {
                        onCancelClick(registration)
                    }
                }
            }
        }
        
        private fun getStatusText(status: String): String {
            return when (status) {
                "PENDING" -> "Ожидает подтверждения"
                "APPROVED" -> "Подтверждено"
                "REJECTED" -> "Отклонено"
                "CANCELLED" -> "Отменено"
                else -> status
            }
        }
        
        private fun getStatusColor(status: String): Int {
            return when (status) {
                "PENDING" -> Color.parseColor("#FFA000") // Янтарный
                "APPROVED" -> Color.parseColor("#00C853") // Зеленый
                "REJECTED" -> Color.parseColor("#D50000") // Красный
                "CANCELLED" -> Color.parseColor("#757575") // Серый
                else -> Color.parseColor("#9E9E9E") // Серый
            }
        }
    }
} 