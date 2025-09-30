package com.example.swaasthya.data.model

import java.util.UUID

// API Response Models
data class ApiResponse<T>(
    val success: Boolean,
    val message: String?,
    val data: T?,
    val error: String?
)

data class PaginationInfo(
    val page: Int,
    val limit: Int,
    val total: Int,
    val total_pages: Int,
    val has_next: Boolean,
    val has_prev: Boolean
)

// User Models
data class ApiUser(
    val id: String,
    val firebase_uid: String,
    val name: String,
    val email: String,
    val role: String,
    val created_at: String,
    val updated_at: String
)

// Doctor Models
data class Doctor(
    val id: String,
    val user_id: String,
    val specialization: String,
    val experience: Int,
    val phone: String,
    val available_slots: Map<String, List<String>>,
    val created_at: String,
    val updated_at: String,
    val user: ApiUser?
)

data class DoctorResponse(
    val doctors: List<Doctor>,
    val pagination: PaginationInfo
)

// Appointment Models
data class Appointment(
    val id: String,
    val doctor_id: String,
    val patient_id: String,
    val appointment_date: String,
    val slot: String,
    val status: String,
    val notes: String?,
    val created_at: String,
    val updated_at: String,
    val doctor: Doctor?
)

data class AppointmentResponse(
    val appointments: List<Appointment>,
    val pagination: PaginationInfo
)

data class CreateAppointmentRequest(
    val doctor_id: String,
    val appointment_date: String,
    val slot: String,
    val notes: String?
)

data class CreateAppointmentResponse(
    val appointment_id: String,
    val appointment_date: String,
    val appointment_time: String,
    val slot: String
)

// Search Models
data class SearchDoctorsResponse(
    val doctors: List<Doctor>,
    val query: String?,
    val specialization: String?
)
