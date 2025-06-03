package com.example.sportevents.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SearchView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.sportevents.R
import com.example.sportevents.databinding.FragmentHomeBinding
import com.example.sportevents.ui.adapters.EventAdapter
import com.example.sportevents.util.NetworkResult

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private lateinit var viewModel: HomeViewModel
    private lateinit var eventAdapter: EventAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = ViewModelProvider(this).get(HomeViewModel::class.java)
        
        setupRecyclerView()
        setupObservers()
        setupSearchView()
        setupFilterButtons()
        setupSwipeRefresh()
        
        // Load initial data
        viewModel.loadEvents()
        viewModel.loadSportTypes()
        viewModel.loadEventTypes()
    }
    
    private fun setupRecyclerView() {
        eventAdapter = EventAdapter { eventId ->
            val bundle = Bundle().apply {
                putInt("eventId", eventId)
            }
            findNavController().navigate(R.id.action_navigation_home_to_eventDetailFragment, bundle)
        }
        
        binding.recyclerViewEvents.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = eventAdapter
        }
    }
    
    private fun setupSwipeRefresh() {
        binding.swipeRefreshLayout.setOnRefreshListener {
            // Clear any search text
            binding.searchView.setQuery("", false)
            
            // Reload data
            viewModel.loadEvents()
            viewModel.loadSportTypes()
            viewModel.loadEventTypes()
        }
        
        // Set colors for the refresh indicator
        binding.swipeRefreshLayout.setColorSchemeResources(
            R.color.purple_500,
            R.color.teal_200,
            R.color.purple_700
        )
    }
    
    private fun setupObservers() {
        viewModel.events.observe(viewLifecycleOwner) { result ->
            when (result) {
                is NetworkResult.Success -> {
                    binding.progressBar.visibility = View.GONE
                    binding.swipeRefreshLayout.isRefreshing = false
                    
                    if (result.data.isEmpty()) {
                        binding.textViewNoEvents.visibility = View.VISIBLE
                        binding.recyclerViewEvents.visibility = View.GONE
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
                    binding.textViewNoEvents.text = "Error: ${result.message}"
                    binding.recyclerViewEvents.visibility = View.GONE
                    Toast.makeText(requireContext(), result.message, Toast.LENGTH_LONG).show()
                }
                is NetworkResult.Loading -> {
                    if (!binding.swipeRefreshLayout.isRefreshing) {
                        binding.progressBar.visibility = View.VISIBLE
                    }
                    binding.textViewNoEvents.visibility = View.GONE
                    binding.recyclerViewEvents.visibility = View.GONE
                }
            }
        }
        
        viewModel.sportTypes.observe(viewLifecycleOwner) { result ->
            if (result is NetworkResult.Success) {
                // Setup sport type filter chip group
                binding.chipGroupSportTypes.removeAllViews()
                
                result.data.forEach { sportType ->
                    val chip = com.google.android.material.chip.Chip(requireContext()).apply {
                        text = sportType.name
                        id = sportType.id
                        isCheckable = true
                        setChipBackgroundColorResource(R.color.primary_light)
                        setTextColor(resources.getColor(R.color.text_primary, null))
                    }
                    
                    chip.setOnCheckedChangeListener { _, isChecked ->
                        if (isChecked) {
                            viewModel.addSportTypeFilter(sportType.id)
                        } else {
                            viewModel.removeSportTypeFilter(sportType.id)
                        }
                        viewModel.applyFilters()
                    }
                    
                    binding.chipGroupSportTypes.addView(chip)
                }
            }
        }
        
        viewModel.eventTypes.observe(viewLifecycleOwner) { result ->
            if (result is NetworkResult.Success) {
                // Setup event type filter chip group
                binding.chipGroupEventTypes.removeAllViews()
                
                result.data.forEach { eventType ->
                    val chip = com.google.android.material.chip.Chip(requireContext()).apply {
                        text = eventType.name
                        id = eventType.id
                        isCheckable = true
                        setChipBackgroundColorResource(R.color.secondary_light)
                        setTextColor(resources.getColor(R.color.text_primary, null))
                    }
                    
                    chip.setOnCheckedChangeListener { _, isChecked ->
                        if (isChecked) {
                            viewModel.addEventTypeFilter(eventType.id)
                        } else {
                            viewModel.removeEventTypeFilter(eventType.id)
                        }
                        viewModel.applyFilters()
                    }
                    
                    binding.chipGroupEventTypes.addView(chip)
                }
            }
        }
    }
    
    private fun setupSearchView() {
        binding.searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                viewModel.setSearchQuery(query)
                viewModel.applyFilters()
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                if (newText.isNullOrBlank()) {
                    viewModel.setSearchQuery(null)
                    viewModel.applyFilters()
                }
                return true
            }
        })
    }
    
    private fun setupFilterButtons() {
        binding.buttonShowFilters.setOnClickListener {
            if (binding.filterCard.visibility == View.VISIBLE) {
                binding.filterCard.visibility = View.GONE
                binding.buttonShowFilters.text = getString(R.string.show_filters)
            } else {
                binding.filterCard.visibility = View.VISIBLE
                binding.buttonShowFilters.text = getString(R.string.close_filters)
            }
        }
        
        binding.buttonClearFilters.setOnClickListener {
            binding.searchView.setQuery("", false)
            binding.chipGroupSportTypes.clearCheck()
            binding.chipGroupEventTypes.clearCheck()
            viewModel.clearFilters()
            viewModel.loadEvents()
        }

        binding.buttonApplyFilters.setOnClickListener {
            viewModel.applyFilters()
            binding.filterCard.visibility = View.GONE
            binding.buttonShowFilters.text = getString(R.string.show_filters)
        }

        // Настройка табов для фильтрации по статусу
        binding.statusTabLayout.addOnTabSelectedListener(object : com.google.android.material.tabs.TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: com.google.android.material.tabs.TabLayout.Tab?) {
                when (tab?.position) {
                    0 -> viewModel.setStatusFilter(null) // Все
                    1 -> viewModel.setStatusFilter("REGISTRATION_OPEN") // Открыта регистрация
                    2 -> viewModel.setStatusFilter("ACTIVE") // Активные
                    3 -> viewModel.setStatusFilter("COMPLETED") // Завершенные
                }
                viewModel.applyFilters()
            }

            override fun onTabUnselected(tab: com.google.android.material.tabs.TabLayout.Tab?) {}
            override fun onTabReselected(tab: com.google.android.material.tabs.TabLayout.Tab?) {}
        })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}