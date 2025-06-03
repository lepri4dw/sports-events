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
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.sportevents.data.models.EventRegistration
import com.example.sportevents.databinding.FragmentParticipantListBinding
import com.example.sportevents.ui.adapters.ParticipantAdapter
import com.example.sportevents.util.AuthManager
import com.example.sportevents.util.NetworkResult

class ParticipantListFragment : Fragment() {
    private val TAG = "ParticipantListFragment"
    
    private var _binding: FragmentParticipantListBinding? = null
    private val binding get() = _binding!!
    
    private lateinit var viewModel: ParticipantListViewModel
    private lateinit var adapter: ParticipantAdapter
    
    private var eventId: Int = 0
    private var isOrganizer = false
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentParticipantListBinding.inflate(inflater, container, false)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = ViewModelProvider(this).get(ParticipantListViewModel::class.java)
        
        // Получаем ID мероприятия из аргументов
        eventId = arguments?.getInt("eventId") ?: 0
        
        if (eventId == 0) {
            // Если ID не был передан, возвращаемся назад
            Toast.makeText(requireContext(), "Ошибка: ID мероприятия не указан", Toast.LENGTH_SHORT).show()
            findNavController().popBackStack()
            return
        }
        
        setupRecyclerView()
        setupObservers()
        
        // Загружаем данные
        viewModel.loadEvent(eventId)
        viewModel.loadParticipants(eventId)
    }
    
    private fun setupRecyclerView() {
        // Создаем адаптер
        adapter = ParticipantAdapter(
            isOrganizer = false, // Значение будет обновлено после загрузки мероприятия
            onApproveClick = { registration ->
                approveRegistration(registration)
            },
            onRejectClick = { registration ->
                rejectRegistration(registration)
            },
            onCancelClick = { registration ->
                // В текущей реализации отмена регистрации не поддерживается через этот экран
            }
        )
        
        // Настраиваем RecyclerView
        binding.recyclerViewParticipants.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerViewParticipants.adapter = adapter
    }
    
    private fun setupObservers() {
        // Наблюдение за загрузкой мероприятия
        viewModel.event.observe(viewLifecycleOwner) { result ->
            when (result) {
                is NetworkResult.Success -> {
                    val event = result.data
                    binding.textViewEventName.text = event.title
                    
                    // Проверяем, является ли текущий пользователь организатором
                    val currentUserId = AuthManager.getCurrentUser()?.id
                    isOrganizer = viewModel.canManageParticipants(event.organizer.id, currentUserId)
                    
                    // Обновляем адаптер с новым статусом организатора
                    adapter = ParticipantAdapter(
                        isOrganizer = isOrganizer,
                        onApproveClick = { registration ->
                            approveRegistration(registration)
                        },
                        onRejectClick = { registration ->
                            rejectRegistration(registration)
                        },
                        onCancelClick = { registration ->
                            // В текущей реализации отмена регистрации не поддерживается через этот экран
                        }
                    )
                    binding.recyclerViewParticipants.adapter = adapter
                    
                    // Если у нас уже есть данные об участниках, обновляем их в адаптере
                    viewModel.participants.value?.let {
                        if (it is NetworkResult.Success) {
                            updateParticipantsList(it.data)
                        }
                    }
                }
                is NetworkResult.Error -> {
                    Toast.makeText(requireContext(), result.message, Toast.LENGTH_LONG).show()
                }
                else -> {}
            }
        }
        
        // Наблюдение за загрузкой участников
        viewModel.participants.observe(viewLifecycleOwner) { result ->
            when (result) {
                is NetworkResult.Success -> {
                    binding.progressBar.visibility = View.GONE
                    updateParticipantsList(result.data)
                }
                is NetworkResult.Error -> {
                    binding.progressBar.visibility = View.GONE
                    Toast.makeText(requireContext(), result.message, Toast.LENGTH_LONG).show()
                }
                is NetworkResult.Loading -> {
                    binding.progressBar.visibility = View.VISIBLE
                }
            }
        }
        
        // Наблюдение за результатом обновления статуса
        viewModel.statusUpdateResult.observe(viewLifecycleOwner) { result ->
            when (result) {
                is NetworkResult.Success -> {
                    Toast.makeText(requireContext(), "Статус успешно обновлен", Toast.LENGTH_SHORT).show()
                }
                is NetworkResult.Error -> {
                    Toast.makeText(requireContext(), "Ошибка обновления статуса: ${result.message}", Toast.LENGTH_LONG).show()
                }
                else -> {}
            }
        }
    }
    
    private fun updateParticipantsList(participants: List<EventRegistration>) {
        if (participants.isEmpty()) {
            binding.textViewNoParticipants.visibility = View.VISIBLE
            binding.recyclerViewParticipants.visibility = View.GONE
        } else {
            binding.textViewNoParticipants.visibility = View.GONE
            binding.recyclerViewParticipants.visibility = View.VISIBLE
            adapter.submitList(participants)
        }
    }
    
    private fun approveRegistration(registration: EventRegistration) {
        if (!isOrganizer) {
            Toast.makeText(requireContext(), "Только организатор может подтверждать регистрации", Toast.LENGTH_SHORT).show()
            return
        }
        
        viewModel.approveRegistration(registration.id)
    }
    
    private fun rejectRegistration(registration: EventRegistration) {
        if (!isOrganizer) {
            Toast.makeText(requireContext(), "Только организатор может отклонять регистрации", Toast.LENGTH_SHORT).show()
            return
        }
        
        viewModel.rejectRegistration(registration.id)
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
} 