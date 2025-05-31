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

        // Get eventId from arguments bundle
        eventId = arguments?.getInt("eventId") ?: 0
        
        viewModel.loadEvent(eventId)
        
        // If user is logged in, check if they're already registered
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
                    
                    // Check if this is a private event and the user is not the organizer
                    val currentUserId = AuthManager.getCurrentUser()?.id
                    if (!result.data.is_public && result.data.organizer.id != currentUserId) {
                        Toast.makeText(requireContext(), "This event is private and you don't have access to it", Toast.LENGTH_LONG).show()
                        // Navigate back
                        findNavController().popBackStack()
                        return@observe
                    }
                    
                    binding.contentLayout.visibility = View.VISIBLE
                    updateEventDetails(result.data)
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
                    Toast.makeText(requireContext(), "Registration successful", Toast.LENGTH_SHORT).show()
                    viewModel.loadEvent(eventId) // Reload event data
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
        
        viewModel.userRegistrations.observe(viewLifecycleOwner) { result ->
            if (result is NetworkResult.Success) {
                Log.d(TAG, "Got ${result.data.size} user registrations")
                // Check if the registration visibility needs updating
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
    
    private fun updateRegistrationVisibility(event: Event) {
        val currentUserId = AuthManager.getCurrentUser()?.id
        val isOrganizer = event.organizer.id == currentUserId
        val isUserRegistered = viewModel.isUserRegisteredForEvent(event)
        
        Log.d(TAG, "Updating registration visibility: organizer=$isOrganizer, registered=$isUserRegistered, event=${event.id}")
        
        if (isOrganizer) {
            // Organizers can't register for their own events
            binding.registrationSection.visibility = View.GONE
            binding.textViewRegistrationClosed.text = "You are the organizer of this event"
            binding.textViewRegistrationClosed.visibility = View.VISIBLE
        } else if (isUserRegistered) {
            // User is already registered
            binding.registrationSection.visibility = View.GONE
            binding.textViewRegistrationClosed.text = "You are already registered for this event"
            binding.textViewRegistrationClosed.visibility = View.VISIBLE
        } else if (!event.isRegistrationOpen()) {
            // Event not accepting registrations
            binding.registrationSection.visibility = View.GONE
            binding.textViewRegistrationClosed.text = "This event is not accepting registrations"
            binding.textViewRegistrationClosed.visibility = View.VISIBLE
        } else if (event.isFull()) {
            // Event is full
            binding.registrationSection.visibility = View.GONE
            binding.textViewRegistrationClosed.text = "This event has reached maximum participants"
            binding.textViewRegistrationClosed.visibility = View.VISIBLE
        } else {
            // User can register
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
        
        Log.d(TAG, "Updating registration status: organizer=$isOrganizer, registered=$isUserRegistered, event=${event.id}")
        
        if (isOrganizer) {
            // Organizers can't register for their own events
            binding.registrationSection.visibility = View.GONE
            binding.textViewRegistrationClosed.text = "You are the organizer of this event"
            binding.textViewRegistrationClosed.visibility = View.VISIBLE
        } else if (isUserRegistered) {
            // User is already registered
            binding.registrationSection.visibility = View.GONE
            binding.textViewRegistrationClosed.text = "You are already registered for this event"
            binding.textViewRegistrationClosed.visibility = View.VISIBLE
        } else if (!event.isRegistrationOpen()) {
            // Event not accepting registrations
            binding.registrationSection.visibility = View.GONE
            binding.textViewRegistrationClosed.text = "This event is not accepting registrations"
            binding.textViewRegistrationClosed.visibility = View.VISIBLE
        } else if (event.isFull()) {
            // Event is full
            binding.registrationSection.visibility = View.GONE
            binding.textViewRegistrationClosed.text = "This event has reached maximum participants"
            binding.textViewRegistrationClosed.visibility = View.VISIBLE
        } else {
            // User can register
            binding.registrationSection.visibility = View.VISIBLE
            binding.textViewRegistrationClosed.visibility = View.GONE
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
} 