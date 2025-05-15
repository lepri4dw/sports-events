package com.example.sportevents.ui.event

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.example.sportevents.R
import com.example.sportevents.data.models.EventType
import com.example.sportevents.data.models.Location
import com.example.sportevents.data.models.SportType
import com.example.sportevents.databinding.FragmentCreateEventBinding
import com.example.sportevents.util.NetworkResult
import java.math.BigDecimal
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class CreateEventFragment : Fragment() {

    private var _binding: FragmentCreateEventBinding? = null
    private val binding get() = _binding!!
    private lateinit var viewModel: EventCreateViewModel
    
    private val calendar = Calendar.getInstance()
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
    private val displayDateFormat = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault())
    
    private var startDateTime: String? = null
    private var endDateTime: String? = null
    private var registrationDeadline: String? = null
    
    private var sportTypes = listOf<SportType>()
    private var eventTypes = listOf<EventType>()
    private var locations = listOf<Location>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCreateEventBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = ViewModelProvider(this).get(EventCreateViewModel::class.java)
        
        setupObservers()
        setupListeners()
        
        // Load initial data
        viewModel.loadSportTypes()
        viewModel.loadEventTypes()
        viewModel.loadLocations()
    }
    
    private fun setupObservers() {
        viewModel.sportTypes.observe(viewLifecycleOwner) { result ->
            when (result) {
                is NetworkResult.Success -> {
                    sportTypes = result.data
                    val adapter = ArrayAdapter(
                        requireContext(),
                        android.R.layout.simple_spinner_dropdown_item,
                        sportTypes.map { it.name }
                    )
                    binding.spinnerSportType.adapter = adapter
                }
                is NetworkResult.Error -> {
                    Toast.makeText(requireContext(), "Failed to load sport types: ${result.message}", Toast.LENGTH_SHORT).show()
                }
                is NetworkResult.Loading -> {
                    // Show loading if needed
                }
            }
        }
        
        viewModel.eventTypes.observe(viewLifecycleOwner) { result ->
            when (result) {
                is NetworkResult.Success -> {
                    eventTypes = result.data
                    val adapter = ArrayAdapter(
                        requireContext(),
                        android.R.layout.simple_spinner_dropdown_item,
                        eventTypes.map { it.name }
                    )
                    binding.spinnerEventType.adapter = adapter
                }
                is NetworkResult.Error -> {
                    Toast.makeText(requireContext(), "Failed to load event types: ${result.message}", Toast.LENGTH_SHORT).show()
                }
                is NetworkResult.Loading -> {
                    // Show loading if needed
                }
            }
        }
        
        viewModel.locations.observe(viewLifecycleOwner) { result ->
            when (result) {
                is NetworkResult.Success -> {
                    locations = result.data
                    val locationNames = mutableListOf("Custom location")
                    locationNames.addAll(locations.map { it.name })
                    
                    val adapter = ArrayAdapter(
                        requireContext(),
                        android.R.layout.simple_spinner_dropdown_item,
                        locationNames
                    )
                    binding.spinnerLocation.adapter = adapter
                }
                is NetworkResult.Error -> {
                    Toast.makeText(requireContext(), "Failed to load locations: ${result.message}", Toast.LENGTH_SHORT).show()
                }
                is NetworkResult.Loading -> {
                    // Show loading if needed
                }
            }
        }
        
        viewModel.createEventResult.observe(viewLifecycleOwner) { result ->
            when (result) {
                is NetworkResult.Success -> {
                    binding.progressBar.visibility = View.GONE
                    binding.buttonCreateEvent.isEnabled = true
                    Toast.makeText(requireContext(), "Event created successfully", Toast.LENGTH_SHORT).show()
                    findNavController().navigate(R.id.action_createEventFragment_to_navigation_dashboard)
                }
                is NetworkResult.Error -> {
                    binding.progressBar.visibility = View.GONE
                    binding.buttonCreateEvent.isEnabled = true
                    Toast.makeText(requireContext(), "Failed to create event: ${result.message}", Toast.LENGTH_LONG).show()
                }
                is NetworkResult.Loading -> {
                    binding.progressBar.visibility = View.VISIBLE
                    binding.buttonCreateEvent.isEnabled = false
                }
            }
        }
    }
    
    private fun setupListeners() {
        binding.spinnerLocation.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                binding.customLocationLayout.visibility = if (position == 0) View.VISIBLE else View.GONE
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                // Do nothing
            }
        }
        
        binding.buttonStartDate.setOnClickListener {
            showDateTimePicker { dateTime ->
                startDateTime = dateFormat.format(dateTime.time)
                binding.textViewStartDate.text = displayDateFormat.format(dateTime.time)
            }
        }
        
        binding.buttonEndDate.setOnClickListener {
            showDateTimePicker { dateTime ->
                endDateTime = dateFormat.format(dateTime.time)
                binding.textViewEndDate.text = displayDateFormat.format(dateTime.time)
            }
        }
        
        binding.buttonDeadline.setOnClickListener {
            showDateTimePicker { dateTime ->
                registrationDeadline = dateFormat.format(dateTime.time)
                binding.textViewDeadline.text = displayDateFormat.format(dateTime.time)
            }
        }
        
        binding.switchPublic.setOnCheckedChangeListener { _, isChecked ->
            binding.privateEventLayout.visibility = if (!isChecked) View.VISIBLE else View.GONE
        }
        
        binding.buttonCreateEvent.setOnClickListener {
            if (validateInputs()) {
                createEvent()
            }
        }
    }
    
    private fun showDateTimePicker(callback: (Calendar) -> Unit) {
        val currentCalendar = Calendar.getInstance()
        
        val datePickerDialog = DatePickerDialog(
            requireContext(),
            { _, year, month, dayOfMonth ->
                currentCalendar.set(Calendar.YEAR, year)
                currentCalendar.set(Calendar.MONTH, month)
                currentCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)
                
                TimePickerDialog(
                    requireContext(),
                    { _, hourOfDay, minute ->
                        currentCalendar.set(Calendar.HOUR_OF_DAY, hourOfDay)
                        currentCalendar.set(Calendar.MINUTE, minute)
                        currentCalendar.set(Calendar.SECOND, 0)
                        callback(currentCalendar)
                    },
                    currentCalendar.get(Calendar.HOUR_OF_DAY),
                    currentCalendar.get(Calendar.MINUTE),
                    true
                ).show()
            },
            currentCalendar.get(Calendar.YEAR),
            currentCalendar.get(Calendar.MONTH),
            currentCalendar.get(Calendar.DAY_OF_MONTH)
        )
        
        datePickerDialog.show()
    }
    
    private fun validateInputs(): Boolean {
        val title = binding.editTextTitle.text.toString().trim()
        val description = binding.editTextDescription.text.toString().trim()
        
        if (title.isEmpty() || description.isEmpty()) {
            Toast.makeText(requireContext(), "Please fill in all required fields", Toast.LENGTH_SHORT).show()
            return false
        }
        
        if (startDateTime == null) {
            Toast.makeText(requireContext(), "Please select a start date and time", Toast.LENGTH_SHORT).show()
            return false
        }
        
        if (binding.spinnerSportType.selectedItemPosition < 0 || sportTypes.isEmpty()) {
            Toast.makeText(requireContext(), "Please select a sport type", Toast.LENGTH_SHORT).show()
            return false
        }
        
        if (binding.spinnerEventType.selectedItemPosition < 0 || eventTypes.isEmpty()) {
            Toast.makeText(requireContext(), "Please select an event type", Toast.LENGTH_SHORT).show()
            return false
        }
        
        if (binding.spinnerLocation.selectedItemPosition == 0 && 
            binding.editTextCustomLocation.text.toString().trim().isEmpty()) {
            Toast.makeText(requireContext(), "Please enter a custom location", Toast.LENGTH_SHORT).show()
            return false
        }
        
        return true
    }
    
    private fun createEvent() {
        val title = binding.editTextTitle.text.toString().trim()
        val description = binding.editTextDescription.text.toString().trim()
        val sportTypeId = sportTypes[binding.spinnerSportType.selectedItemPosition].id
        val eventTypeId = eventTypes[binding.spinnerEventType.selectedItemPosition].id
        
        val locationId: Int? = if (binding.spinnerLocation.selectedItemPosition > 0) {
            locations[binding.spinnerLocation.selectedItemPosition - 1].id
        } else {
            null
        }
        
        val customLocation = if (binding.spinnerLocation.selectedItemPosition == 0) {
            binding.editTextCustomLocation.text.toString().trim()
        } else {
            null
        }
        
        val maxParticipants = binding.editTextMaxParticipants.text.toString().toIntOrNull()
        
        val entryFee = binding.editTextEntryFee.text.toString().let {
            if (it.isNotEmpty()) BigDecimal(it) else null
        }
        
        val contactEmail = binding.editTextContactEmail.text.toString().trim().let {
            if (it.isNotEmpty()) it else null
        }
        
        val contactPhone = binding.editTextContactPhone.text.toString().trim().let {
            if (it.isNotEmpty()) it else null
        }
        
        val isPublic = binding.switchPublic.isChecked
        
        viewModel.createEvent(
            title = title,
            description = description,
            sportTypeId = sportTypeId,
            eventTypeId = eventTypeId,
            locationId = locationId,
            customLocationText = customLocation,
            startDateTime = startDateTime!!,
            endDateTime = endDateTime,
            registrationDeadline = registrationDeadline,
            maxParticipants = maxParticipants,
            isPublic = isPublic,
            entryFee = entryFee,
            contactEmail = contactEmail,
            contactPhone = contactPhone
        )
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
} 