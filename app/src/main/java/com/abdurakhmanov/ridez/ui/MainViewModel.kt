package com.abdurakhmanov.ridez.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.abdurakhmanov.ridez.data.repository.LocationRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val locationRepository: LocationRepository
) : ViewModel() {

    private val showLiveLocation = MutableStateFlow(true)
    val liveLocation = locationRepository.getLiveLocation(showLiveLocation)

    init {
        showLiveLocation.value = true
    }

    fun showLiveLocation(show: Boolean) = viewModelScope.launch {
        showLiveLocation.value = show
    }

}