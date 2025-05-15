package com.example.sportevents.ui.dashboard

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
import com.example.sportevents.databinding.FragmentDashboardBinding
import com.example.sportevents.ui.adapters.EventAdapter
import com.example.sportevents.ui.auth.AuthViewModel
import com.example.sportevents.util.NetworkResult
import com.google.android.material.tabs.TabLayout

class DashboardFragment : Fragment() {

    private var _binding: FragmentDashboardBinding? = null
    private val binding get() = _binding!!
    
    private lateinit var dashboardViewModel: DashboardViewModel
    private lateinit var authViewModel: AuthViewModel
    private lateinit var eventAdapter: EventAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDashboardBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        dashboardViewModel = ViewModelProvider(this).get(DashboardViewModel::class.java)
        authViewModel = ViewModelProvider(this).get(AuthViewModel::class.java)
        
        setupRecyclerView()
        setupTabLayout()
        setupObservers()
        setupListeners()
        
        // Check if user is logged in
        if (authViewModel.isLoggedIn()) {
            binding.loginPromptLayout.visibility = View.GONE
            binding.userContentLayout.visibility = View.VISIBLE
            loadUserEvents()
        } else {
            binding.loginPromptLayout.visibility = View.VISIBLE
            binding.userContentLayout.visibility = View.GONE
        }
    }
    
    private fun setupRecyclerView() {
        eventAdapter = EventAdapter { eventId ->
            val bundle = Bundle().apply {
                putInt("eventId", eventId)
            }
            findNavController().navigate(R.id.action_navigation_dashboard_to_eventDetailFragment, bundle)
        }
        
        binding.recyclerViewEvents.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = eventAdapter
        }
    }
    
    private fun setupTabLayout() {
        binding.tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                when (tab?.position) {
                    0 -> dashboardViewModel.loadUserCreatedEvents()
                    1 -> dashboardViewModel.loadUserRegisteredEvents()
                }
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })
    }
    
    private fun setupObservers() {
        dashboardViewModel.events.observe(viewLifecycleOwner) { result ->
            when (result) {
                is NetworkResult.Success -> {
                    binding.progressBar.visibility = View.GONE
                    
                    if (result.data.isEmpty()) {
                        binding.textViewNoEvents.visibility = View.VISIBLE
                        binding.recyclerViewEvents.visibility = View.GONE
                        
                        val tabPosition = binding.tabLayout.selectedTabPosition
                        binding.textViewNoEvents.text = if (tabPosition == 0) {
                            "You haven't created any events yet"
                        } else {
                            "You haven't registered for any events yet"
                        }
                    } else {
                        binding.textViewNoEvents.visibility = View.GONE
                        binding.recyclerViewEvents.visibility = View.VISIBLE
                        eventAdapter.submitList(result.data)
                    }
                }
                is NetworkResult.Error -> {
                    binding.progressBar.visibility = View.GONE
                    binding.textViewNoEvents.visibility = View.VISIBLE
                    binding.textViewNoEvents.text = "Error: ${result.message}"
                    binding.recyclerViewEvents.visibility = View.GONE
                    Toast.makeText(requireContext(), result.message, Toast.LENGTH_LONG).show()
                }
                is NetworkResult.Loading -> {
                    binding.progressBar.visibility = View.VISIBLE
                    binding.textViewNoEvents.visibility = View.GONE
                    binding.recyclerViewEvents.visibility = View.GONE
                }
            }
        }
        
        // Observe login status changes
        authViewModel.currentUser.observe(viewLifecycleOwner) { user ->
            if (user != null) {
                binding.loginPromptLayout.visibility = View.GONE
                binding.userContentLayout.visibility = View.VISIBLE
                loadUserEvents()
            } else {
                binding.loginPromptLayout.visibility = View.VISIBLE
                binding.userContentLayout.visibility = View.GONE
            }
        }
    }
    
    private fun setupListeners() {
        binding.buttonLogin.setOnClickListener {
            findNavController().navigate(R.id.action_navigation_dashboard_to_loginFragment)
        }
        
        binding.buttonCreateEvent.setOnClickListener {
            findNavController().navigate(R.id.action_navigation_dashboard_to_createEventFragment)
        }
    }
    
    private fun loadUserEvents() {
        // Default to created events tab
        dashboardViewModel.loadUserCreatedEvents()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}