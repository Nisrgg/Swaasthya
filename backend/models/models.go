package models

import (
	"time"

	"github.com/google/uuid"
)

// User represents a user in the system
type User struct {
	ID          uuid.UUID `json:"id" db:"id"`
	FirebaseUID string    `json:"firebase_uid" db:"firebase_uid"`
	Name        string    `json:"name" db:"name"`
	Email       string    `json:"email" db:"email"`
	Role        string    `json:"role" db:"role"`
	CreatedAt   time.Time `json:"created_at" db:"created_at"`
	UpdatedAt   time.Time `json:"updated_at" db:"updated_at"`
}

// Doctor represents a doctor profile
type Doctor struct {
	ID              uuid.UUID       `json:"id" db:"id"`
	UserID          uuid.UUID       `json:"user_id" db:"user_id"`
	Specialization  string          `json:"specialization" db:"specialization"`
	Experience      int             `json:"experience" db:"experience"`
	Phone           string          `json:"phone" db:"phone"`
	AvailableSlots  map[string][]string `json:"available_slots" db:"available_slots"`
	CreatedAt       time.Time       `json:"created_at" db:"created_at"`
	UpdatedAt       time.Time       `json:"updated_at" db:"updated_at"`
	// Joined fields
	User            *User           `json:"user,omitempty"`
}

// Appointment represents an appointment
type Appointment struct {
	ID              uuid.UUID `json:"id" db:"id"`
	DoctorID        uuid.UUID `json:"doctor_id" db:"doctor_id"`
	PatientID       uuid.UUID `json:"patient_id" db:"patient_id"`
	AppointmentDate time.Time `json:"appointment_date" db:"appointment_date"`
	Slot            string    `json:"slot" db:"slot"`
	Status          string    `json:"status" db:"status"`
	Notes           string    `json:"notes" db:"notes"`
	CreatedAt       time.Time `json:"created_at" db:"created_at"`
	UpdatedAt       time.Time `json:"updated_at" db:"updated_at"`
	// Joined fields
	Doctor          *Doctor   `json:"doctor,omitempty"`
	Patient         *User     `json:"patient,omitempty"`
}

// CreateAppointmentRequest represents the request to create an appointment
type CreateAppointmentRequest struct {
	DoctorID        uuid.UUID `json:"doctor_id" binding:"required"`
	AppointmentDate string    `json:"appointment_date" binding:"required"`
	Slot            string    `json:"slot" binding:"required"`
	Notes           string    `json:"notes"`
}

// UpdateAppointmentRequest represents the request to update an appointment
type UpdateAppointmentRequest struct {
	AppointmentDate string `json:"appointment_date"`
	Slot            string `json:"slot"`
	Status          string `json:"status"`
	Notes           string `json:"notes"`
}


