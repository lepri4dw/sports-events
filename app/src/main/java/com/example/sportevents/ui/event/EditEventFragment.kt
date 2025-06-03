package com.example.sportevents.ui.event

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.example.sportevents.R
import com.example.sportevents.data.models.Event
import com.example.sportevents.data.models.EventType
import com.example.sportevents.data.models.SportType
import com.example.sportevents.databinding.FragmentEditEventBinding
import com.example.sportevents.util.NetworkResult
import java.math.BigDecimal
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class EditEventFragment : Fragment() {
    private val TAG = "EditEventFragment"
    
    private var _binding: FragmentEditEventBinding? = null
    private val binding get() = _binding!!
    
    private lateinit var viewModel: EditEventViewModel
    private var eventId: Int = 0
    
    private val sportTypes = mutableListOf<SportType>()
    private val eventTypes = mutableListOf<EventType>()
    private val statusOptions = listOf(
        "DRAFT" to "Черновик",
        "PLANNED" to "Запланировано",
        "REGISTRATION_OPEN" to "Регистрация открыта",
        "REGISTRATION_CLOSED" to "Регистрация закрыта",
        "ACTIVE" to "Активно",
        "COMPLETED" to "Завершено",
        "CANCELLED" to "Отменено"
    )
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentEditEventBinding.inflate(inflater, container, false)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = ViewModelProvider(this).get(EditEventViewModel::class.java)
        
        // Получаем ID мероприятия из аргументов
        eventId = arguments?.getInt("eventId") ?: 0
        
        if (eventId == 0) {
            // Если ID не был передан, возвращаемся назад
            Toast.makeText(requireContext(), "Ошибка: ID мероприятия не указан", Toast.LENGTH_SHORT).show()
            findNavController().popBackStack()
            return
        }
        
        setupUI()
        setupListeners()
        setupObservers()
        
        // Загружаем данные
        viewModel.loadEvent(eventId)
        viewModel.loadSportTypes()
        viewModel.loadEventTypes()
    }
    
    private fun setupUI() {
        binding.textViewTitle.text = getString(R.string.edit_event_title)
    }
    
    private fun setupListeners() {
        // Настройка выбора даты и времени для начала мероприятия
        binding.editTextStartDateTime.setOnClickListener {
            showDateTimePicker { dateTime ->
                binding.editTextStartDateTime.setText(dateTime)
            }
        }
        
        // Настройка выбора даты и времени для окончания мероприятия
        binding.editTextEndDateTime.setOnClickListener {
            showDateTimePicker { dateTime ->
                binding.editTextEndDateTime.setText(dateTime)
            }
        }
        
        // Настройка выбора даты и времени для срока регистрации
        binding.editTextRegistrationDeadline.setOnClickListener {
            showDateTimePicker { dateTime ->
                binding.editTextRegistrationDeadline.setText(dateTime)
            }
        }
        
        // Обработка сохранения
        binding.buttonSave.setOnClickListener {
            saveEvent()
        }
        
        // Обработка отмены
        binding.buttonCancel.setOnClickListener {
            findNavController().popBackStack()
        }
    }
    
    private fun setupObservers() {
        // Наблюдение за загрузкой мероприятия
        viewModel.event.observe(viewLifecycleOwner) { result ->
            when (result) {
                is NetworkResult.Success -> {
                    binding.progressBar.visibility = View.GONE
                    binding.formLayout.visibility = View.VISIBLE
                    populateForm(result.data)
                }
                is NetworkResult.Error -> {
                    binding.progressBar.visibility = View.GONE
                    Toast.makeText(requireContext(), result.message, Toast.LENGTH_LONG).show()
                    findNavController().popBackStack()
                }
                is NetworkResult.Loading -> {
                    binding.progressBar.visibility = View.VISIBLE
                    binding.formLayout.visibility = View.GONE
                }
            }
        }
        
        // Наблюдение за загрузкой типов спорта
        viewModel.sportTypes.observe(viewLifecycleOwner) { result ->
            when (result) {
                is NetworkResult.Success -> {
                    sportTypes.clear()
                    sportTypes.addAll(result.data)
                    
                    val adapter = ArrayAdapter(
                        requireContext(),
                        android.R.layout.simple_dropdown_item_1line,
                        sportTypes.map { it.name }
                    )
                    binding.spinnerSportType.setAdapter(adapter)
                    
                    // Если мероприятие уже загружено, выбираем нужный тип спорта
                    viewModel.event.value?.let {
                        if (it is NetworkResult.Success) {
                            val sportTypeIndex = sportTypes.indexOfFirst { sportType -> sportType.id == it.data.sport_type.id }
                            if (sportTypeIndex != -1) {
                                binding.spinnerSportType.setText(sportTypes[sportTypeIndex].name, false)
                            }
                        }
                    }
                }
                is NetworkResult.Error -> {
                    Toast.makeText(requireContext(), "Ошибка загрузки типов спорта: ${result.message}", Toast.LENGTH_SHORT).show()
                }
                else -> {}
            }
        }
        
        // Наблюдение за загрузкой типов мероприятий
        viewModel.eventTypes.observe(viewLifecycleOwner) { result ->
            when (result) {
                is NetworkResult.Success -> {
                    eventTypes.clear()
                    eventTypes.addAll(result.data)
                    
                    val adapter = ArrayAdapter(
                        requireContext(),
                        android.R.layout.simple_dropdown_item_1line,
                        eventTypes.map { it.name }
                    )
                    binding.spinnerEventType.setAdapter(adapter)
                    
                    // Если мероприятие уже загружено, выбираем нужный тип мероприятия
                    viewModel.event.value?.let {
                        if (it is NetworkResult.Success) {
                            val eventTypeIndex = eventTypes.indexOfFirst { eventType -> eventType.id == it.data.event_type.id }
                            if (eventTypeIndex != -1) {
                                binding.spinnerEventType.setText(eventTypes[eventTypeIndex].name, false)
                            }
                        }
                    }
                }
                is NetworkResult.Error -> {
                    Toast.makeText(requireContext(), "Ошибка загрузки типов мероприятий: ${result.message}", Toast.LENGTH_SHORT).show()
                }
                else -> {}
            }
        }
        
        // Наблюдение за результатом обновления мероприятия
        viewModel.updateResult.observe(viewLifecycleOwner) { result ->
            when (result) {
                is NetworkResult.Success -> {
                    binding.progressBar.visibility = View.GONE
                    binding.buttonSave.isEnabled = true
                    Toast.makeText(requireContext(), "Мероприятие успешно обновлено", Toast.LENGTH_SHORT).show()
                    
                    // Возвращаемся к деталям мероприятия
                    findNavController().popBackStack()
                }
                is NetworkResult.Error -> {
                    binding.progressBar.visibility = View.GONE
                    binding.buttonSave.isEnabled = true
                    Toast.makeText(requireContext(), "Ошибка обновления мероприятия: ${result.message}", Toast.LENGTH_LONG).show()
                }
                is NetworkResult.Loading -> {
                    binding.progressBar.visibility = View.VISIBLE
                    binding.buttonSave.isEnabled = false
                }
            }
        }
    }
    
    private fun populateForm(event: Event) {
        // Заполняем форму данными мероприятия
        binding.editTextEventTitle.setText(event.title)
        binding.editTextEventDescription.setText(event.description)
        
        // Устанавливаем тип спорта и тип мероприятия
        binding.spinnerSportType.setText(event.sport_type.name, false)
        binding.spinnerEventType.setText(event.event_type.name, false)
        
        // Местоположение
        binding.editTextLocation.setText(event.getLocationDisplayText())
        
        // Даты
        binding.editTextStartDateTime.setText(viewModel.formatDateForDisplay(event.start_datetime))
        binding.editTextEndDateTime.setText(viewModel.formatDateForDisplay(event.end_datetime))
        binding.editTextRegistrationDeadline.setText(viewModel.formatDateForDisplay(event.registration_deadline))
        
        // Максимальное количество участников
        binding.editTextMaxParticipants.setText(event.max_participants?.toString() ?: "")
        
        // Статус
        setupStatusSpinner(event.status)
        
        // Публичное мероприятие
        binding.switchPublic.isChecked = event.is_public
        
        // Стоимость участия
        binding.editTextEntryFee.setText(event.entry_fee?.toString() ?: "")
        
        // Контактная информация
        binding.editTextContactEmail.setText(event.contact_email ?: "")
        binding.editTextContactPhone.setText(event.contact_phone ?: "")
    }
    
    private fun setupStatusSpinner(currentStatus: String) {
        val statusNames = statusOptions.map { it.second }
        val adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_dropdown_item_1line,
            statusNames
        )
        binding.spinnerEventStatus.setAdapter(adapter)
        
        // Устанавливаем текущий статус
        val statusIndex = statusOptions.indexOfFirst { it.first == currentStatus }
        if (statusIndex != -1) {
            binding.spinnerEventStatus.setText(statusOptions[statusIndex].second, false)
        }
    }
    
    private fun getStatusCode(statusName: String): String {
        val status = statusOptions.find { it.second == statusName }
        return status?.first ?: "DRAFT"
    }
    
    private fun saveEvent() {
        // Проверка наличия обязательных полей
        if (binding.editTextEventTitle.text.isNullOrBlank()) {
            Toast.makeText(requireContext(), "Введите название мероприятия", Toast.LENGTH_SHORT).show()
            return
        }
        
        if (binding.editTextEventDescription.text.isNullOrBlank()) {
            Toast.makeText(requireContext(), "Введите описание мероприятия", Toast.LENGTH_SHORT).show()
            return
        }
        
        if (binding.spinnerSportType.text.isNullOrBlank()) {
            Toast.makeText(requireContext(), "Выберите тип спорта", Toast.LENGTH_SHORT).show()
            return
        }
        
        if (binding.spinnerEventType.text.isNullOrBlank()) {
            Toast.makeText(requireContext(), "Выберите тип мероприятия", Toast.LENGTH_SHORT).show()
            return
        }
        
        if (binding.editTextStartDateTime.text.isNullOrBlank()) {
            Toast.makeText(requireContext(), "Введите дату и время начала", Toast.LENGTH_SHORT).show()
            return
        }
        
        // Получаем индексы выбранных типов
        val selectedSportType = sportTypes.find { it.name == binding.spinnerSportType.text.toString() }
        val selectedEventType = eventTypes.find { it.name == binding.spinnerEventType.text.toString() }
        
        if (selectedSportType == null || selectedEventType == null) {
            Toast.makeText(requireContext(), "Ошибка: не удалось определить выбранные типы", Toast.LENGTH_SHORT).show()
            return
        }
        
        // Получаем код статуса
        val statusName = binding.spinnerEventStatus.text.toString()
        val statusCode = getStatusCode(statusName)
        
        // Парсинг максимального количества участников
        val maxParticipantsString = binding.editTextMaxParticipants.text.toString()
        val maxParticipants = if (maxParticipantsString.isNotBlank()) {
            try {
                maxParticipantsString.toInt()
            } catch (e: NumberFormatException) {
                null
            }
        } else {
            null
        }
        
        // Парсинг стоимости участия
        val entryFeeString = binding.editTextEntryFee.text.toString()
        val entryFee = if (entryFeeString.isNotBlank()) {
            try {
                BigDecimal(entryFeeString)
            } catch (e: NumberFormatException) {
                null
            }
        } else {
            null
        }
        
        // Форматирование дат для API
        val startDateTime = viewModel.formatDateForApi(binding.editTextStartDateTime.text.toString())
        val endDateTime = viewModel.formatDateForApi(binding.editTextEndDateTime.text.toString())
        val registrationDeadline = viewModel.formatDateForApi(binding.editTextRegistrationDeadline.text.toString())
        
        if (startDateTime == null) {
            Toast.makeText(requireContext(), "Ошибка форматирования даты начала", Toast.LENGTH_SHORT).show()
            return
        }
        
        // Вызов метода обновления мероприятия
        viewModel.updateEvent(
            eventId = eventId,
            title = binding.editTextEventTitle.text.toString(),
            description = binding.editTextEventDescription.text.toString(),
            sportTypeId = selectedSportType.id,
            eventTypeId = selectedEventType.id,
            location = binding.editTextLocation.text.toString(),
            startDateTime = startDateTime,
            endDateTime = endDateTime,
            registrationDeadline = registrationDeadline,
            maxParticipants = maxParticipants,
            status = statusCode,
            isPublic = binding.switchPublic.isChecked,
            entryFee = entryFee,
            contactEmail = binding.editTextContactEmail.text.toString(),
            contactPhone = binding.editTextContactPhone.text.toString()
        )
    }
    
    private fun showDateTimePicker(callback: (String) -> Unit) {
        val calendar = Calendar.getInstance()
        
        // Открываем диалог выбора даты
        val datePickerDialog = DatePickerDialog(
            requireContext(),
            { _, year, month, day ->
                // После выбора даты открываем диалог выбора времени
                val timePickerDialog = TimePickerDialog(
                    requireContext(),
                    { _, hourOfDay, minute ->
                        // Формируем строку с датой и временем
                        val dateTime = String.format(Locale.getDefault(), "%02d.%02d.%04d %02d:%02d", day, month + 1, year, hourOfDay, minute)
                        callback(dateTime)
                    },
                    calendar.get(Calendar.HOUR_OF_DAY),
                    calendar.get(Calendar.MINUTE),
                    true
                )
                timePickerDialog.show()
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )
        datePickerDialog.show()
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
} 