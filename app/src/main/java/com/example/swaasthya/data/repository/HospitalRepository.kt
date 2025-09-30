package com.example.swaasthya.data.repository

import com.example.swaasthya.data.api.HospitalApiService
import com.example.swaasthya.data.model.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class HospitalRepository @Inject constructor(
    private val apiService: HospitalApiService
) {
    
    suspend fun getDoctors(
        page: Int = 1,
        limit: Int = 20,
        specialization: String? = null
    ): Result<DoctorResponse> {
        return try {
            val response = apiService.getDoctors(page, limit, specialization)
            if (response.isSuccessful && response.body()?.success == true) {
                Result.success(response.body()!!.data!!)
            } else {
                Result.failure(Exception(response.body()?.error ?: "Failed to fetch doctors"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun searchDoctors(
        query: String? = null,
        specialization: String? = null
    ): Result<SearchDoctorsResponse> {
        return try {
            val response = apiService.searchDoctors(query, specialization)
            if (response.isSuccessful && response.body()?.success == true) {
                Result.success(response.body()!!.data!!)
            } else {
                Result.failure(Exception(response.body()?.error ?: "Failed to search doctors"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun getDoctorById(doctorId: String): Result<Doctor> {
        return try {
            val response = apiService.getDoctorById(doctorId)
            if (response.isSuccessful && response.body()?.success == true) {
                Result.success(response.body()!!.data!!)
            } else {
                Result.failure(Exception(response.body()?.error ?: "Failed to fetch doctor"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun createAppointment(request: CreateAppointmentRequest): Result<CreateAppointmentResponse> {
        return try {
            val response = apiService.createAppointment(request)
            if (response.isSuccessful && response.body()?.success == true) {
                Result.success(response.body()!!.data!!)
            } else {
                Result.failure(Exception(response.body()?.error ?: "Failed to create appointment"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun getUserAppointments(
        page: Int = 1,
        limit: Int = 20
    ): Result<AppointmentResponse> {
        return try {
            val response = apiService.getUserAppointments(page, limit)
            if (response.isSuccessful && response.body()?.success == true) {
                Result.success(response.body()!!.data!!)
            } else {
                Result.failure(Exception(response.body()?.error ?: "Failed to fetch appointments"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun updateAppointment(
        appointmentId: String,
        request: Map<String, Any>
    ): Result<Appointment> {
        return try {
            val response = apiService.updateAppointment(appointmentId, request)
            if (response.isSuccessful && response.body()?.success == true) {
                Result.success(response.body()!!.data!!)
            } else {
                Result.failure(Exception(response.body()?.error ?: "Failed to update appointment"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun cancelAppointment(appointmentId: String): Result<Unit> {
        return try {
            val response = apiService.cancelAppointment(appointmentId)
            if (response.isSuccessful && response.body()?.success == true) {
                Result.success(Unit)
            } else {
                Result.failure(Exception(response.body()?.error ?: "Failed to cancel appointment"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun healthCheck(): Result<Map<String, String>> {
        return try {
            val response = apiService.healthCheck()
            if (response.isSuccessful) {
                Result.success(response.body() ?: emptyMap())
            } else {
                Result.failure(Exception("Health check failed"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
