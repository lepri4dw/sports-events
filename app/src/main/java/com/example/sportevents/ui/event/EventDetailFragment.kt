package com.example.sportevents.ui.event

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
                    displayEventDetails(result.data)
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

    private fun displayEventDetails(event: Event) {
        binding.textViewTitle.text = event.title
        binding.textViewDescription.text = event.description
        binding.textViewSportType.text = "Sport: ${event.sport_type.name}"
        binding.textViewEventType.text = "Event type: ${event.event_type.name}"
        
        binding.textViewLocation.text = "Location: ${event.getLocationDisplayText()}"
        
        val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
        val displayFormat = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault())
        
        val startDate = dateFormat.parse(event.start_datetime)
        binding.textViewStartDate.text = "Starts: ${displayFormat.format(startDate!!)}"
        
        event.end_datetime?.let {
            val endDate = dateFormat.parse(it)
            binding.textViewEndDate.text = "Ends: ${displayFormat.format(endDate!!)}"
            binding.textViewEndDate.visibility = View.VISIBLE
        } ?: run {
            binding.textViewEndDate.visibility = View.GONE
        }
        
        binding.textViewOrganizer.text = "Organized by: ${event.organizer.display_name}"
        
        val participantsText = if (event.max_participants != null) {
            "${event.current_participants_count}/${event.max_participants} participants"
        } else {
            "${event.current_participants_count} participants"
        }
        binding.textViewParticipants.text = participantsText
        
        event.entry_fee?.let {
            binding.textViewEntryFee.text = "Entry fee: $${it}"
            binding.textViewEntryFee.visibility = View.VISIBLE
        } ?: run {
            binding.textViewEntryFee.visibility = View.GONE
        }
        
        event.contact_email?.let {
            binding.textViewContactEmail.text = "Contact email: $it"
            binding.textViewContactEmail.visibility = View.VISIBLE
        } ?: run {
            binding.textViewContactEmail.visibility = View.GONE
        }
        
        event.contact_phone?.let {
            binding.textViewContactPhone.text = "Contact phone: $it"
            binding.textViewContactPhone.visibility = View.VISIBLE
        } ?: run {
            binding.textViewContactPhone.visibility = View.GONE
        }
        
        // Update registration section visibility
        updateRegistrationVisibility(event)
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

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
} 