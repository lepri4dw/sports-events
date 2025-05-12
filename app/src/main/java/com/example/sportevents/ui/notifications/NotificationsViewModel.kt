package com.example.sportevents.ui.notifications

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class NotificationsViewModel : ViewModel() {
    private val _text = MutableLiveData<String>().apply {
        value = "Notifications will be implemented in future versions"
    }
    val text: LiveData<String> = _text
}