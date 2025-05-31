package com.example.sportevents.ui.profile

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.sportevents.R
import com.example.sportevents.data.models.User
import com.example.sportevents.databinding.FragmentProfileBinding
import com.example.sportevents.ui.adapters.EventAdapter
import com.example.sportevents.ui.auth.AuthViewModel
import com.example.sportevents.util.NetworkResult
import com.google.android.material.tabs.TabLayout

class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!
    
    private lateinit var profileViewModel: ProfileViewModel
    private lateinit var authViewModel: AuthViewModel
    private lateinit var eventAdapter: EventAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        profileViewModel = ViewModelProvider(this).get(ProfileViewModel::class.java)
        authViewModel = ViewModelProvider(this).get(AuthViewModel::class.java)
        
        setupRecyclerView()
        setupTabLayout()
        setupObservers()
        setupListeners()
        setupSwipeRefresh()
        
        // Check if user is logged in
        if (authViewModel.isLoggedIn()) {
            binding.loginPromptLayout.visibility = View.GONE
            binding.profileContentLayout.visibility = View.VISIBLE
            profileViewModel.loadUserProfile()
            profileViewModel.loadUserStatistics()
            loadUserEvents()
        } else {
            binding.loginPromptLayout.visibility = View.VISIBLE
            binding.profileContentLayout.visibility = View.GONE
        }
    }
    
    private fun setupRecyclerView() {
        eventAdapter = EventAdapter { eventId ->
            val bundle = Bundle().apply {
                putInt("eventId", eventId)
            }
            findNavController().navigate(R.id.action_navigation_notifications_to_eventDetailFragment, bundle)
        }
        
        binding.recyclerViewEvents.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = eventAdapter
        }
    }
    
    private fun setupSwipeRefresh() {
        binding.swipeRefreshLayout.setOnRefreshListener {
            // Reload events based on current tab
            val currentTab = binding.tabLayout.selectedTabPosition
            if (currentTab == 0) {
                profileViewModel.loadUserCreatedEvents()
            } else {
                profileViewModel.loadUserRegisteredEvents()
            }
        }
        
        // Set colors for the refresh indicator
        binding.swipeRefreshLayout.setColorSchemeResources(
            R.color.purple_500,
            R.color.teal_200,
            R.color.purple_700
        )
    }
    
    private fun setupTabLayout() {
        binding.tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                when (tab?.position) {
                    0 -> profileViewModel.loadUserCreatedEvents()
                    1 -> profileViewModel.loadUserRegisteredEvents()
                }
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })
    }
    
    private fun setupObservers() {
        // Observe user profile information
        profileViewModel.userProfile.observe(viewLifecycleOwner) { result ->
            when (result) {
                is NetworkResult.Success -> {
                    binding.progressBar.visibility = View.GONE
                    updateProfileUI(result.data)
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
        
        // Observe user statistics
        profileViewModel.userStatistics.observe(viewLifecycleOwner) { stats ->
            binding.textViewEventsCreated.text = "Created: ${stats.eventsCreated}"
            binding.textViewEventsJoined.text = "Joined: ${stats.eventsJoined}"
        }
        
        // Observe login status changes
        authViewModel.currentUser.observe(viewLifecycleOwner) { user ->
            if (user != null) {
                binding.loginPromptLayout.visibility = View.GONE
                binding.profileContentLayout.visibility = View.VISIBLE
                updateProfileUI(user)
                profileViewModel.loadUserStatistics()
                loadUserEvents()
            } else {
                binding.loginPromptLayout.visibility = View.VISIBLE
                binding.profileContentLayout.visibility = View.GONE
            }
        }
        
        // Observe events list
        profileViewModel.events.observe(viewLifecycleOwner) { result ->
            when (result) {
                is NetworkResult.Success -> {
                    binding.progressBar.visibility = View.GONE
                    binding.swipeRefreshLayout.isRefreshing = false
                    
                    if (result.data.isEmpty()) {
                        binding.textViewNoEvents.visibility = View.VISIBLE
                        binding.recyclerViewEvents.visibility = View.GONE
                        
                        val tabPosition = binding.tabLayout.selectedTabPosition
                        binding.textViewNoEvents.text = if (tabPosition == 0) {
                            "Вы еще не создали мероприятий"
                        } else {
                            "Вы еще не зарегистрировались на мероприятия"
                        }
                    } else {
                        binding.textViewNoEvents.visibility = View.GONE
                        binding.recyclerViewEvents.visibility = View.VISIBLE
                        eventAdapter.submitList(result.data)
                    }
                }
                is NetworkResult.Error -> {
                    binding.progressBar.visibility = View.GONE
                    binding.swipeRefreshLayout.isRefreshing = false
                    binding.textViewNoEvents.visibility = View.VISIBLE
                    binding.textViewNoEvents.text = "Ошибка: ${result.message}"
                    binding.recyclerViewEvents.visibility = View.GONE
                }
                is NetworkResult.Loading -> {
                    if (!binding.swipeRefreshLayout.isRefreshing) {
                        binding.progressBar.visibility = View.VISIBLE
                    }
                    binding.textViewNoEvents.visibility = View.GONE
                }
            }
        }
    }
    
    private fun setupListeners() {
        binding.buttonLogin.setOnClickListener {
            findNavController().navigate(R.id.action_navigation_notifications_to_loginFragment)
        }
        
        binding.buttonLogout.setOnClickListener {
            authViewModel.logout()
            binding.loginPromptLayout.visibility = View.VISIBLE
            binding.profileContentLayout.visibility = View.GONE
            Toast.makeText(requireContext(), "Вы вышли из аккаунта", Toast.LENGTH_SHORT).show()
        }
        
        binding.buttonCreateEvent.setOnClickListener {
            findNavController().navigate(R.id.action_navigation_notifications_to_createEventFragment)
        }
    }
    
    private fun updateProfileUI(user: User) {
        binding.textViewUserName.text = user.display_name
        binding.textViewUserEmail.text = user.email
    }
    
    private fun loadUserEvents() {
        // Default to created events tab
        profileViewModel.loadUserCreatedEvents()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
} 