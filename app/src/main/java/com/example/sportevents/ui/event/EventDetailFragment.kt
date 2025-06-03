package com.example.sportevents.ui.event

import android.graphics.Color
import android.content.res.ColorStateList
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.example.sportevents.R
import com.example.sportevents.data.models.Event
import com.example.sportevents.databinding.FragmentEventDetailBinding
import com.example.sportevents.ui.auth.AuthViewModel
import com.example.sportevents.util.AuthManager
import com.example.sportevents.util.NetworkResult
import java.math.BigDecimal
import java.text.SimpleDateFormat
import java.util.Locale

class EventDetailFragment : Fragment() {

    private val TAG = "EventDetailFragment"
    private var _binding: FragmentEventDetailBinding? = null
    private val binding get() = _binding!!
    private lateinit var viewModel: EventDetailViewModel
    private lateinit var authViewModel: AuthViewModel
    private var eventId: Int = 0

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentEventDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = ViewModelProvider(this).get(EventDetailViewModel::class.java)
        authViewModel = ViewModelProvider(this).get(AuthViewModel::class.java)

        // Получаем ID мероприятия из аргументов
        eventId = arguments?.getInt("eventId") ?: 0
        
        viewModel.loadEvent(eventId)
        
        // Если пользователь авторизован, проверяем его регистрацию
        if (authViewModel.isLoggedIn()) {
            viewModel.loadUserRegistrations()
        }
        
        setupObservers()
        setupListeners()
    }

    private fun setupObservers() {
        viewModel.event.observe(viewLifecycleOwner) { result ->
            when (result) {
                is NetworkResult.Success -> {
                    binding.progressBar.visibility = View.GONE
                    
                    // Проверяем, является ли мероприятие приватным и пользователь не организатор
                    val currentUserId = AuthManager.getCurrentUser()?.id
                    if (!result.data.is_public && result.data.organizer.id != currentUserId) {
                        Toast.makeText(requireContext(), "Это приватное мероприятие, к которому у вас нет доступа", Toast.LENGTH_LONG).show()
                        // Возвращаемся назад
                        findNavController().popBackStack()
                        return@observe
                    }
                    
                    binding.contentLayout.visibility = View.VISIBLE
                    updateEventDetails(result.data)
                    updateActionButtons(result.data)
                }
                is NetworkResult.Error -> {
                    binding.progressBar.visibility = View.GONE
                    Toast.makeText(requireContext(), result.message, Toast.LENGTH_LONG).show()
                }
                is NetworkResult.Loading -> {
                    binding.progressBar.visibility = View.VISIBLE
                    binding.contentLayout.visibility = View.GONE
                }
            }
        }

        viewModel.registrationResult.observe(viewLifecycleOwner) { result ->
            when (result) {
                is NetworkResult.Success -> {
                    binding.buttonRegister.isEnabled = true
                    binding.progressBar.visibility = View.GONE
                    Toast.makeText(requireContext(), "Регистрация успешно завершена", Toast.LENGTH_SHORT).show()
                    viewModel.loadEvent(eventId) // Перезагружаем данные мероприятия
                }
                is NetworkResult.Error -> {
                    binding.buttonRegister.isEnabled = true
                    binding.progressBar.visibility = View.GONE
                    Toast.makeText(requireContext(), result.message, Toast.LENGTH_LONG).show()
                }
                is NetworkResult.Loading -> {
                    binding.buttonRegister.isEnabled = false
                    binding.progressBar.visibility = View.VISIBLE
                }
            }
        }
        
        viewModel.unregistrationResult.observe(viewLifecycleOwner) { result ->
            when (result) {
                is NetworkResult.Success -> {
                    binding.buttonCancelRegistration.isEnabled = true
                    binding.progressBar.visibility = View.GONE
                    Toast.makeText(requireContext(), "Вы успешно отменили регистрацию", Toast.LENGTH_SHORT).show()
                    
                    // После отмены регистрации перенаправляем на страницу профиля
                    findNavController().navigate(R.id.action_eventDetailFragment_to_navigation_notifications)
                }
                is NetworkResult.Error -> {
                    binding.buttonCancelRegistration.isEnabled = true
                    binding.progressBar.visibility = View.GONE
                    Toast.makeText(requireContext(), result.message, Toast.LENGTH_LONG).show()
                }
                is NetworkResult.Loading -> {
                    binding.buttonCancelRegistration.isEnabled = false
                    binding.progressBar.visibility = View.VISIBLE
                }
            }
        }
        
        viewModel.userRegistrations.observe(viewLifecycleOwner) { result ->
            if (result is NetworkResult.Success) {
                Log.d(TAG, "Получено ${result.data.size} регистраций пользователя")
                // Проверяем, нужно ли обновить видимость кнопок регистрации
                viewModel.event.value?.let { eventResult ->
                    if (eventResult is NetworkResult.Success) {
                        updateRegistrationVisibility(eventResult.data)
                    }
                }
            }
        }
    }

    private fun setupListeners() {
        binding.buttonRegister.setOnClickListener {
            if (authViewModel.isLoggedIn()) {
                val notes = binding.editTextNotes.text.toString().trim()
                viewModel.registerForEvent(eventId, notes)
            } else {
                findNavController().navigate(R.id.action_eventDetailFragment_to_loginFragment)
            }
        }
        
        binding.buttonCancelRegistration.setOnClickListener {
            viewModel.unregisterFromEvent(eventId)
        }
        
        binding.buttonEditEvent.setOnClickListener {
            val bundle = Bundle().apply {
                putInt("eventId", eventId)
            }
            findNavController().navigate(R.id.action_eventDetailFragment_to_editEventFragment, bundle)
        }
        
        binding.buttonParticipants.setOnClickListener {
            val bundle = Bundle().apply {
                putInt("eventId", eventId)
            }
            findNavController().navigate(R.id.action_eventDetailFragment_to_participantListFragment, bundle)
        }
    }

    private fun updateEventDetails(event: Event) {
        binding.textViewTitle.text = event.title
        binding.textViewSportType.text = event.sport_type.name
        binding.textViewEventType.text = event.event_type.name
        binding.textViewDescription.text = event.description
        binding.textViewOrganizer.text = getString(R.string.event_organizer_format, event.organizer.display_name)
        
        val startDateFormatted = formatDate(event.start_datetime)
        binding.textViewStartDate.text = getString(R.string.event_start_date, startDateFormatted)
        
        if (event.end_datetime != null) {
            val endDateFormatted = formatDate(event.end_datetime)
            binding.textViewEndDate.text = getString(R.string.event_end_date, endDateFormatted)
            binding.textViewEndDate.visibility = View.VISIBLE
        } else {
            binding.textViewEndDate.visibility = View.GONE
        }
        
        binding.textViewLocation.text = getString(R.string.event_location_format, event.getLocationDisplayText())
        
        val participantsText = if (event.max_participants != null) {
            "${event.current_participants_count}/${event.max_participants}"
        } else {
            "${event.current_participants_count}"
        }
        binding.textViewParticipants.text = getString(R.string.event_participants_format, participantsText)
        
        if (event.entry_fee != null && event.entry_fee > BigDecimal.ZERO) {
            binding.textViewEntryFee.text = getString(R.string.event_fee_format, event.entry_fee.toString())
            binding.textViewEntryFee.visibility = View.VISIBLE
        } else {
            binding.textViewEntryFee.visibility = View.GONE
        }
        
        if (!event.contact_email.isNullOrBlank()) {
            binding.textViewContactEmail.text = getString(R.string.event_email_format, event.contact_email)
            binding.textViewContactEmail.visibility = View.VISIBLE
        } else {
            binding.textViewContactEmail.visibility = View.GONE
        }
        
        if (!event.contact_phone.isNullOrBlank()) {
            binding.textViewContactPhone.text = getString(R.string.event_phone_format, event.contact_phone)
            binding.textViewContactPhone.visibility = View.VISIBLE
        } else {
            binding.textViewContactPhone.visibility = View.GONE
        }
        
        // Обновление статуса события
        val statusText = getStatusText(event.status)
        binding.statusChip.text = statusText
        binding.statusChip.chipBackgroundColor = ColorStateList.valueOf(getStatusColor(event.status))
        
        // Проверка возможности регистрации
        updateRegistrationStatus(event)
    }
    
    private fun updateActionButtons(event: Event) {
        val currentUserId = AuthManager.getCurrentUser()?.id
        val isOrganizer = event.organizer.id == currentUserId
        
        // Кнопка редактирования
        binding.buttonEditEvent.visibility = if (isOrganizer) View.VISIBLE else View.GONE
        
        // Кнопка участников
        binding.buttonParticipants.visibility = if (isOrganizer) View.VISIBLE else View.GONE
    }
    
    private fun updateRegistrationVisibility(event: Event) {
        val currentUserId = AuthManager.getCurrentUser()?.id
        val isOrganizer = event.organizer.id == currentUserId
        val isUserRegistered = viewModel.isUserRegisteredForEvent(event)
        val registrationStatus = viewModel.getRegistrationStatusForEvent(event)
        
        Log.d(TAG, "Обновление видимости регистрации: организатор=$isOrganizer, зарегистрирован=$isUserRegistered, статус=$registrationStatus, мероприятие=${event.id}")
        
        binding.buttonCancelRegistration.visibility = View.GONE
        
        if (isOrganizer) {
            // Организаторы не могут регистрироваться на свои мероприятия
            binding.registrationSection.visibility = View.GONE
            binding.textViewRegistrationClosed.text = "Вы организатор этого мероприятия"
            binding.textViewRegistrationClosed.visibility = View.VISIBLE
        } else if (isUserRegistered) {
            // Пользователь уже зарегистрирован с активным статусом
            binding.registrationSection.visibility = View.GONE
            
            // Отображаем статус регистрации
            val statusText = if (registrationStatus != null) {
                "Вы зарегистрированы на это мероприятие (${viewModel.getRegistrationStatusText(registrationStatus)})"
            } else {
                "Вы зарегистрированы на это мероприятие"
            }
            binding.textViewRegistrationClosed.text = statusText
            binding.textViewRegistrationClosed.visibility = View.VISIBLE
            
            // Показываем кнопку отмены только для активных регистраций
            if (registrationStatus == "PENDING_APPROVAL" || registrationStatus == "CONFIRMED") {
                binding.buttonCancelRegistration.visibility = View.VISIBLE
            }
        } else if (registrationStatus == "CANCELLED_BY_USER" || registrationStatus == "REJECTED_BY_ORGANIZER") {
            // Регистрация была отменена или отклонена, но пользователь может зарегистрироваться снова
            if (event.isRegistrationOpen() && !event.isFull()) {
                binding.registrationSection.visibility = View.VISIBLE
                binding.textViewRegistrationClosed.visibility = View.GONE
            } else {
                binding.registrationSection.visibility = View.GONE
                if (!event.isRegistrationOpen()) {
                    binding.textViewRegistrationClosed.text = "Это мероприятие не принимает регистрации"
                } else {
                    binding.textViewRegistrationClosed.text = "Это мероприятие достигло максимального количества участников"
                }
                binding.textViewRegistrationClosed.visibility = View.VISIBLE
            }
        } else if (!event.isRegistrationOpen()) {
            // Мероприятие не принимает регистрации
            binding.registrationSection.visibility = View.GONE
            binding.textViewRegistrationClosed.text = "Это мероприятие не принимает регистрации"
            binding.textViewRegistrationClosed.visibility = View.VISIBLE
        } else if (event.isFull()) {
            // Мероприятие заполнено
            binding.registrationSection.visibility = View.GONE
            binding.textViewRegistrationClosed.text = "Это мероприятие достигло максимального количества участников"
            binding.textViewRegistrationClosed.visibility = View.VISIBLE
        } else {
            // Пользователь может зарегистрироваться
            binding.registrationSection.visibility = View.VISIBLE
            binding.textViewRegistrationClosed.visibility = View.GONE
        }
    }

    private fun formatDate(date: String): String {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
        val displayFormat = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault())
        
        val parsedDate = dateFormat.parse(date)
        return displayFormat.format(parsedDate!!)
    }

    private fun getStatusText(status: String): String {
        return when (status) {
            "DRAFT" -> getString(R.string.status_draft)
            "PLANNED" -> getString(R.string.status_planned)
            "REGISTRATION_OPEN" -> getString(R.string.status_registration_open)
            "REGISTRATION_CLOSED" -> getString(R.string.status_registration_closed)
            "ACTIVE" -> getString(R.string.status_active)
            "COMPLETED" -> getString(R.string.status_completed)
            "CANCELLED" -> getString(R.string.status_cancelled)
            else -> status
        }
    }

    private fun getStatusColor(status: String): Int {
        return when (status) {
            "DRAFT" -> Color.parseColor("#9E9E9E") // Серый
            "PLANNED" -> Color.parseColor("#FFA000") // Янтарный
            "REGISTRATION_OPEN" -> Color.parseColor("#2962FF") // Синий
            "REGISTRATION_CLOSED" -> Color.parseColor("#FF6D00") // Оранжевый
            "ACTIVE" -> Color.parseColor("#00C853") // Зеленый
            "COMPLETED" -> Color.parseColor("#6200EA") // Фиолетовый
            "CANCELLED" -> Color.parseColor("#D50000") // Красный
            else -> Color.parseColor("#000000") // Черный
        }
    }

    private fun updateRegistrationStatus(event: Event) {
        val currentUserId = AuthManager.getCurrentUser()?.id
        val isOrganizer = event.organizer.id == currentUserId
        val isUserRegistered = viewModel.isUserRegisteredForEvent(event)
        val registrationStatus = viewModel.getRegistrationStatusForEvent(event)
        
        Log.d(TAG, "Обновление статуса регистрации: организатор=$isOrganizer, зарегистрирован=$isUserRegistered, статус=$registrationStatus, мероприятие=${event.id}")
        
        binding.buttonCancelRegistration.visibility = View.GONE
        
        if (isOrganizer) {
            // Организаторы не могут регистрироваться на свои мероприятия
            binding.registrationSection.visibility = View.GONE
            binding.textViewRegistrationClosed.text = "Вы организатор этого мероприятия"
            binding.textViewRegistrationClosed.visibility = View.VISIBLE
        } else if (isUserRegistered) {
            // Пользователь уже зарегистрирован с активным статусом
            binding.registrationSection.visibility = View.GONE
            
            // Отображаем статус регистрации
            val statusText = if (registrationStatus != null) {
                "Вы зарегистрированы на это мероприятие (${viewModel.getRegistrationStatusText(registrationStatus)})"
            } else {
                "Вы зарегистрированы на это мероприятие"
            }
            binding.textViewRegistrationClosed.text = statusText
            binding.textViewRegistrationClosed.visibility = View.VISIBLE
            
            // Показываем кнопку отмены только для активных регистраций
            if (registrationStatus == "PENDING_APPROVAL" || registrationStatus == "CONFIRMED") {
                binding.buttonCancelRegistration.visibility = View.VISIBLE
            }
        } else if (registrationStatus == "CANCELLED_BY_USER" || registrationStatus == "REJECTED_BY_ORGANIZER") {
            // Регистрация была отменена или отклонена, но пользователь может зарегистрироваться снова
            if (event.isRegistrationOpen() && !event.isFull()) {
                binding.registrationSection.visibility = View.VISIBLE
                binding.textViewRegistrationClosed.visibility = View.GONE
            } else {
                binding.registrationSection.visibility = View.GONE
                if (!event.isRegistrationOpen()) {
                    binding.textViewRegistrationClosed.text = "Это мероприятие не принимает регистрации"
                } else {
                    binding.textViewRegistrationClosed.text = "Это мероприятие достигло максимального количества участников"
                }
                binding.textViewRegistrationClosed.visibility = View.VISIBLE
            }
        } else if (!event.isRegistrationOpen()) {
            // Мероприятие не принимает регистрации
            binding.registrationSection.visibility = View.GONE
            binding.textViewRegistrationClosed.text = "Это мероприятие не принимает регистрации"
            binding.textViewRegistrationClosed.visibility = View.VISIBLE
        } else if (event.isFull()) {
            // Мероприятие заполнено
            binding.registrationSection.visibility = View.GONE
            binding.textViewRegistrationClosed.text = "Это мероприятие достигло максимального количества участников"
            binding.textViewRegistrationClosed.visibility = View.VISIBLE
        } else {
            // Пользователь может зарегистрироваться
            binding.registrationSection.visibility = View.VISIBLE
            binding.textViewRegistrationClosed.visibility = View.GONE
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
} 