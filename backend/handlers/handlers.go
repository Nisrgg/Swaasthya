package handlers

import (
	"database/sql"
	"encoding/json"
	"net/http"
	"strconv"
	"time"

	"hospital-backend/database"
	"hospital-backend/models"

	"github.com/gin-gonic/gin"
	"github.com/google/uuid"
)

// GetDoctors returns all doctors
func GetDoctors(c *gin.Context) {
	query := `
		SELECT d.id, d.user_id, d.specialization, d.experience, d.phone, 
		       d.available_slots, d.created_at, d.updated_at,
		       u.name, u.email
		FROM doctors d
		JOIN users u ON d.user_id = u.id
		ORDER BY u.name
	`

	rows, err := database.DB.Query(query)
	if err != nil {
		c.JSON(http.StatusInternalServerError, gin.H{
			"error": "Failed to fetch doctors",
		})
		return
	}
	defer rows.Close()

	var doctors []models.Doctor
	for rows.Next() {
		var doctor models.Doctor
		var user models.User
		var slotsJSON string

		err := rows.Scan(
			&doctor.ID, &doctor.UserID, &doctor.Specialization, &doctor.Experience,
			&doctor.Phone, &slotsJSON, &doctor.CreatedAt, &doctor.UpdatedAt,
			&user.Name, &user.Email,
		)
		if err != nil {
			c.JSON(http.StatusInternalServerError, gin.H{
				"error": "Failed to scan doctor data",
			})
			return
		}

		// Parse available slots JSON
		if err := json.Unmarshal([]byte(slotsJSON), &doctor.AvailableSlots); err != nil {
			doctor.AvailableSlots = make(map[string][]string)
		}

		user.ID = doctor.UserID
		doctor.User = &user
		doctors = append(doctors, doctor)
	}

	c.JSON(http.StatusOK, gin.H{
		"doctors": doctors,
	})
}

// GetDoctorByID returns a specific doctor by ID
func GetDoctorByID(c *gin.Context) {
	doctorID := c.Param("id")
	if doctorID == "" {
		c.JSON(http.StatusBadRequest, gin.H{
			"error": "Doctor ID is required",
		})
		return
	}

	query := `
		SELECT d.id, d.user_id, d.specialization, d.experience, d.phone, 
		       d.available_slots, d.created_at, d.updated_at,
		       u.name, u.email
		FROM doctors d
		JOIN users u ON d.user_id = u.id
		WHERE d.id = $1
	`

	var doctor models.Doctor
	var user models.User
	var slotsJSON string

	err := database.DB.QueryRow(query, doctorID).Scan(
		&doctor.ID, &doctor.UserID, &doctor.Specialization, &doctor.Experience,
		&doctor.Phone, &slotsJSON, &doctor.CreatedAt, &doctor.UpdatedAt,
		&user.Name, &user.Email,
	)

	if err == sql.ErrNoRows {
		c.JSON(http.StatusNotFound, gin.H{
			"error": "Doctor not found",
		})
		return
	}

	if err != nil {
		c.JSON(http.StatusInternalServerError, gin.H{
			"error": "Failed to fetch doctor",
		})
		return
	}

	// Parse available slots JSON
	if err := json.Unmarshal([]byte(slotsJSON), &doctor.AvailableSlots); err != nil {
		doctor.AvailableSlots = make(map[string][]string)
	}

	user.ID = doctor.UserID
	doctor.User = &user

	c.JSON(http.StatusOK, gin.H{
		"doctor": doctor,
	})
}

// CreateAppointment creates a new appointment
func CreateAppointment(c *gin.Context) {
	var req models.CreateAppointmentRequest
	if err := c.ShouldBindJSON(&req); err != nil {
		c.JSON(http.StatusBadRequest, gin.H{
			"error": "Invalid request data",
		})
		return
	}

	// Get user ID from context (set by auth middleware)
	userID, exists := c.Get("user_id")
	if !exists {
		c.JSON(http.StatusUnauthorized, gin.H{
			"error": "User not authenticated",
		})
		return
	}

	// Parse appointment date
	appointmentDate, err := time.Parse("2006-01-02T15:04:05Z", req.AppointmentDate)
	if err != nil {
		c.JSON(http.StatusBadRequest, gin.H{
			"error": "Invalid date format. Use ISO 8601 format",
		})
		return
	}

	// Check if doctor exists
	var doctorExists bool
	err = database.DB.QueryRow("SELECT EXISTS(SELECT 1 FROM doctors WHERE id = $1)", req.DoctorID).Scan(&doctorExists)
	if err != nil || !doctorExists {
		c.JSON(http.StatusBadRequest, gin.H{
			"error": "Doctor not found",
		})
		return
	}

	// Check if appointment slot is available
	var existingAppointment string
	err = database.DB.QueryRow(`
		SELECT id FROM appointments 
		WHERE doctor_id = $1 AND appointment_date = $2 AND slot = $3 AND status != 'cancelled'
	`, req.DoctorID, appointmentDate, req.Slot).Scan(&existingAppointment)

	if err != sql.ErrNoRows {
		c.JSON(http.StatusConflict, gin.H{
			"error": "Appointment slot is already booked",
		})
		return
	}

	// Create appointment
	appointmentID := uuid.New()
	query := `
		INSERT INTO appointments (id, doctor_id, patient_id, appointment_date, slot, status, notes)
		VALUES ($1, $2, $3, $4, $5, 'scheduled', $6)
	`

	_, err = database.DB.Exec(query, appointmentID, req.DoctorID, userID, appointmentDate, req.Slot, req.Notes)
	if err != nil {
		c.JSON(http.StatusInternalServerError, gin.H{
			"error": "Failed to create appointment",
		})
		return
	}

	c.JSON(http.StatusCreated, gin.H{
		"message":     "Appointment created successfully",
		"appointment_id": appointmentID,
	})
}

// GetUserAppointments returns appointments for the authenticated user
func GetUserAppointments(c *gin.Context) {
	// Get user ID from context
	userID, exists := c.Get("user_id")
	if !exists {
		c.JSON(http.StatusUnauthorized, gin.H{
			"error": "User not authenticated",
		})
		return
	}

	// Get query parameters
	limitStr := c.DefaultQuery("limit", "10")
	offsetStr := c.DefaultQuery("offset", "0")

	limit, err := strconv.Atoi(limitStr)
	if err != nil || limit <= 0 {
		limit = 10
	}

	offset, err := strconv.Atoi(offsetStr)
	if err != nil || offset < 0 {
		offset = 0
	}

	query := `
		SELECT a.id, a.doctor_id, a.patient_id, a.appointment_date, a.slot, 
		       a.status, a.notes, a.created_at, a.updated_at,
		       d.specialization, u.name as doctor_name, u.email as doctor_email
		FROM appointments a
		JOIN doctors d ON a.doctor_id = d.id
		JOIN users u ON d.user_id = u.id
		WHERE a.patient_id = $1
		ORDER BY a.appointment_date DESC
		LIMIT $2 OFFSET $3
	`

	rows, err := database.DB.Query(query, userID, limit, offset)
	if err != nil {
		c.JSON(http.StatusInternalServerError, gin.H{
			"error": "Failed to fetch appointments",
		})
		return
	}
	defer rows.Close()

	var appointments []models.Appointment
	for rows.Next() {
		var appointment models.Appointment
		var doctor models.Doctor
		var user models.User

		err := rows.Scan(
			&appointment.ID, &appointment.DoctorID, &appointment.PatientID,
			&appointment.AppointmentDate, &appointment.Slot, &appointment.Status,
			&appointment.Notes, &appointment.CreatedAt, &appointment.UpdatedAt,
			&doctor.Specialization, &user.Name, &user.Email,
		)
		if err != nil {
			c.JSON(http.StatusInternalServerError, gin.H{
				"error": "Failed to scan appointment data",
			})
			return
		}

		doctor.ID = appointment.DoctorID
		doctor.User = &user
		appointment.Doctor = &doctor
		appointments = append(appointments, appointment)
	}

	c.JSON(http.StatusOK, gin.H{
		"appointments": appointments,
	})
}

// UpdateAppointment updates an existing appointment
func UpdateAppointment(c *gin.Context) {
	appointmentID := c.Param("id")
	if appointmentID == "" {
		c.JSON(http.StatusBadRequest, gin.H{
			"error": "Appointment ID is required",
		})
		return
	}

	var req models.UpdateAppointmentRequest
	if err := c.ShouldBindJSON(&req); err != nil {
		c.JSON(http.StatusBadRequest, gin.H{
			"error": "Invalid request data",
		})
		return
	}

	// Get user ID from context
	userID, exists := c.Get("user_id")
	if !exists {
		c.JSON(http.StatusUnauthorized, gin.H{
			"error": "User not authenticated",
		})
		return
	}

	// Check if appointment exists and belongs to user
	var existingAppointmentID string
	err := database.DB.QueryRow(`
		SELECT id FROM appointments 
		WHERE id = $1 AND patient_id = $2
	`, appointmentID, userID).Scan(&existingAppointmentID)

	if err == sql.ErrNoRows {
		c.JSON(http.StatusNotFound, gin.H{
			"error": "Appointment not found",
		})
		return
	}

	if err != nil {
		c.JSON(http.StatusInternalServerError, gin.H{
			"error": "Failed to check appointment",
		})
		return
	}

	// Build update query dynamically
	query := "UPDATE appointments SET updated_at = NOW()"
	args := []interface{}{}
	argIndex := 1

	if req.AppointmentDate != "" {
		appointmentDate, err := time.Parse("2006-01-02T15:04:05Z", req.AppointmentDate)
		if err != nil {
			c.JSON(http.StatusBadRequest, gin.H{
				"error": "Invalid date format",
			})
			return
		}
		query += ", appointment_date = $" + strconv.Itoa(argIndex)
		args = append(args, appointmentDate)
		argIndex++
	}

	if req.Slot != "" {
		query += ", slot = $" + strconv.Itoa(argIndex)
		args = append(args, req.Slot)
		argIndex++
	}

	if req.Status != "" {
		query += ", status = $" + strconv.Itoa(argIndex)
		args = append(args, req.Status)
		argIndex++
	}

	if req.Notes != "" {
		query += ", notes = $" + strconv.Itoa(argIndex)
		args = append(args, req.Notes)
		argIndex++
	}

	query += " WHERE id = $" + strconv.Itoa(argIndex)
	args = append(args, appointmentID)

	_, err = database.DB.Exec(query, args...)
	if err != nil {
		c.JSON(http.StatusInternalServerError, gin.H{
			"error": "Failed to update appointment",
		})
		return
	}

	c.JSON(http.StatusOK, gin.H{
		"message": "Appointment updated successfully",
	})
}

// CancelAppointment cancels an appointment
func CancelAppointment(c *gin.Context) {
	appointmentID := c.Param("id")
	if appointmentID == "" {
		c.JSON(http.StatusBadRequest, gin.H{
			"error": "Appointment ID is required",
		})
		return
	}

	// Get user ID from context
	userID, exists := c.Get("user_id")
	if !exists {
		c.JSON(http.StatusUnauthorized, gin.H{
			"error": "User not authenticated",
		})
		return
	}

	// Update appointment status to cancelled
	query := `
		UPDATE appointments 
		SET status = 'cancelled', updated_at = NOW()
		WHERE id = $1 AND patient_id = $2
	`

	result, err := database.DB.Exec(query, appointmentID, userID)
	if err != nil {
		c.JSON(http.StatusInternalServerError, gin.H{
			"error": "Failed to cancel appointment",
		})
		return
	}

	rowsAffected, err := result.RowsAffected()
	if err != nil || rowsAffected == 0 {
		c.JSON(http.StatusNotFound, gin.H{
			"error": "Appointment not found",
		})
		return
	}

	c.JSON(http.StatusOK, gin.H{
		"message": "Appointment cancelled successfully",
	})
}


