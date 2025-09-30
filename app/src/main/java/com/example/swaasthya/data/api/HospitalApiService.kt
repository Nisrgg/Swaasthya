package com.example.swaasthya.data.api

import com.example.swaasthya.data.model.*
import retrofit2.Response
import retrofit2.http.*

interface HospitalApiService {
    
    // Doctor endpoints
    @GET("api/mobile/doctors")
    suspend fun getDoctors(
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 20,
        @Query("specialization") specialization: String? = null
    ): Response<ApiResponse<DoctorResponse>>
    
    @GET("api/mobile/search/doctors")
    suspend fun searchDoctors(
        @Query("q") query: String? = null,
        @Query("specialization") specialization: String? = null
    ): Response<ApiResponse<SearchDoctorsResponse>>
    
    @GET("api/doctors/{id}")
    suspend fun getDoctorById(
        @Path("id") doctorId: String
    ): Response<ApiResponse<Doctor>>
    
    // Appointment endpoints
    @POST("api/mobile/appointments")
    suspend fun createAppointment(
        @Body request: CreateAppointmentRequest
    ): Response<ApiResponse<CreateAppointmentResponse>>
    
    @GET("api/mobile/appointments")
    suspend fun getUserAppointments(
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 20
    ): Response<ApiResponse<AppointmentResponse>>
    
    @PUT("api/appointments/{id}")
    suspend fun updateAppointment(
        @Path("id") appointmentId: String,
        @Body request: Map<String, Any>
    ): Response<ApiResponse<Appointment>>
    
    @DELETE("api/appointments/{id}")
    suspend fun cancelAppointment(
        @Path("id") appointmentId: String
    ): Response<ApiResponse<Unit>>
    
    // Health check
    @GET("health")
    suspend fun healthCheck(): Response<Map<String, String>>
}
