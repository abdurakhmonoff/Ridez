package com.abdurakhmanov.ridez.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.abdurakhmanov.ridez.data.repository.LocationRepository
import com.abdurakhmanov.ridez.data.response.CurrentAddress
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val locationRepository: LocationRepository
) : ViewModel() {

    /**
     * Set true if you want to start getting live location or false to stop
     */
    private val _showLiveLocation = MutableStateFlow(true)
    val showLiveLocation: StateFlow<Boolean>
        get() = _showLiveLocation

    /**
     * Use this flow to get live location (updates  constantly)
     */
    val liveLocation = locationRepository.getLiveLocation(_showLiveLocation)

    /**
     * Geocoded address response
     */
    private val _currentAddress = MutableStateFlow<CurrentAddress?>(null)
    val currentAddress: StateFlow<CurrentAddress?>
        get() = _currentAddress

    init {
        _showLiveLocation.value = true
    }

    /**
     * Set the show argument to true if you want to start getting live location or false to stop
     */
    fun showLiveLocation(show: Boolean) = viewModelScope.launch {
        _showLiveLocation.value = show
    }

    /**
     * Use this function to geocode location (latitude and longtitude)
     */
    suspend fun getCurrentAddress(apiKey: String, latitude: Double, longitude: Double) {
        val request = locationRepository.getAddress(apiKey, latitude, longitude)
        if (request.isSuccessful) {
            if (request.body() != null && request.code() == 200) {
                _currentAddress.value = request.body()
            }
        }
    }
}