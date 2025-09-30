package com.example.swaasthya.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.swaasthya.data.model.Doctor
import com.example.swaasthya.data.model.DoctorResponse
import com.example.swaasthya.data.repository.HospitalRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DoctorsViewModel @Inject constructor(
    private val hospitalRepository: HospitalRepository
) : ViewModel() {
    
    private val _doctorsState = MutableStateFlow<DoctorsState>(DoctorsState.Loading)
    val doctorsState: StateFlow<DoctorsState> = _doctorsState.asStateFlow()
    
    private val _searchState = MutableStateFlow<SearchState>(SearchState.Idle)
    val searchState: StateFlow<SearchState> = _searchState.asStateFlow()
    
    private var currentPage = 1
    private var currentSpecialization: String? = null
    
    init {
        loadDoctors()
    }
    
    fun loadDoctors(page: Int = 1, specialization: String? = null) {
        viewModelScope.launch {
            _doctorsState.value = DoctorsState.Loading
            
            hospitalRepository.getDoctors(page, 20, specialization)
                .onSuccess { response ->
                    _doctorsState.value = DoctorsState.Success(response)
                    currentPage = page
                    currentSpecialization = specialization
                }
                .onFailure { error ->
                    _doctorsState.value = DoctorsState.Error(error.message ?: "Failed to load doctors")
                }
        }
    }
    
    fun loadMoreDoctors() {
        val currentState = _doctorsState.value
        if (currentState is DoctorsState.Success && currentState.response.pagination.has_next) {
            viewModelScope.launch {
                hospitalRepository.getDoctors(currentPage + 1, 20, currentSpecialization)
                    .onSuccess { response ->
                        val updatedDoctors = currentState.response.doctors + response.doctors
                        val updatedResponse = response.copy(doctors = updatedDoctors)
                        _doctorsState.value = DoctorsState.Success(updatedResponse)
                        currentPage++
                    }
                    .onFailure { error ->
                        _doctorsState.value = DoctorsState.Error(error.message ?: "Failed to load more doctors")
                    }
            }
        }
    }
    
    fun searchDoctors(query: String, specialization: String? = null) {
        viewModelScope.launch {
            _searchState.value = SearchState.Loading
            
            hospitalRepository.searchDoctors(query, specialization)
                .onSuccess { response ->
                    _searchState.value = SearchState.Success(response.doctors)
                }
                .onFailure { error ->
                    _searchState.value = SearchState.Error(error.message ?: "Search failed")
                }
        }
    }
    
    fun clearSearch() {
        _searchState.value = SearchState.Idle
    }
    
    fun refreshDoctors() {
        loadDoctors(1, currentSpecialization)
    }
}

sealed class DoctorsState {
    object Loading : DoctorsState()
    data class Success(val response: DoctorResponse) : DoctorsState()
    data class Error(val message: String) : DoctorsState()
}

sealed class SearchState {
    object Idle : SearchState()
    object Loading : SearchState()
    data class Success(val doctors: List<Doctor>) : SearchState()
    data class Error(val message: String) : SearchState()
}
