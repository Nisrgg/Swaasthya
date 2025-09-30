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

// Mobile-optimized response structure
type MobileResponse struct {
	Success bool        `json:"success"`
	Message string      `json:"message"`
	Data    interface{} `json:"data,omitempty"`
	Error   string      `json:"error,omitempty"`
}

type PaginationInfo struct {
	Page       int  `json:"page"`
	Limit      int  `json:"limit"`
	Total      int  `json:"total"`
	TotalPages int  `json:"total_pages"`
	HasNext    bool `json:"has_next"`
	HasPrev    bool `json:"has_prev"`
}

// Enhanced GetDoctors with pagination and mobile optimization
func GetDoctorsMobile(c *gin.Context) {
	// Get pagination parameters
	page, _ := strconv.Atoi(c.DefaultQuery("page", "1"))
	limit, _ := strconv.Atoi(c.DefaultQuery("limit", "20"))
	specialization := c.Query("specialization")
	
	// Validate pagination
	if page < 1 {
		page = 1
	}
	if limit < 1 || limit > 50 {
		limit = 20
	}
	
	offset := (page - 1) * limit

	// Build query with optional specialization filter
	query := `
		SELECT d.id, d.user_id, d.specialization, d.experience, d.phone, 
		       d.available_slots, d.created_at, d.updated_at,
		       u.name, u.email
		FROM doctors d
		JOIN users u ON d.user_id = u.id
	`
	
	args := []interface{}{}
	if specialization != "" {
		query += " WHERE d.specialization = $1"
		args = append(args, specialization)
		query += " ORDER BY u.name LIMIT $2 OFFSET $3"
		args = append(args, limit, offset)
	} else {
		query += " ORDER BY u.name LIMIT $1 OFFSET $2"
		args = append(args, limit, offset)
	}

	// Get total count for pagination
	countQuery := "SELECT COUNT(*) FROM doctors d JOIN users u ON d.user_id = u.id"
	if specialization != "" {
		countQuery += " WHERE d.specialization = $1"
	}
	
	var total int
	if specialization != "" {
		err := database.DB.QueryRow(countQuery, specialization).Scan(&total)
		if err != nil {
			c.JSON(http.StatusInternalServerError, MobileResponse{
				Success: false,
				Error:   "Failed to count doctors",
			})
			return
		}
	} else {
		err := database.DB.QueryRow(countQuery).Scan(&total)
		if err != nil {
			c.JSON(http.StatusInternalServerError, MobileResponse{
				Success: false,
				Error:   "Failed to count doctors",
			})
			return
		}
	}

	rows, err := database.DB.Query(query, args...)
	if err != nil {
		c.JSON(http.StatusInternalServerError, MobileResponse{
			Success: false,
			Error:   "Failed to fetch doctors",
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
			c.JSON(http.StatusInternalServerError, MobileResponse{
				Success: false,
				Error:   "Failed to scan doctor data",
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

	// Calculate pagination info
	totalPages := (total + limit - 1) / limit

	c.JSON(http.StatusOK, MobileResponse{
		Success: true,
		Message: "Doctors fetched successfully",
		Data: map[string]interface{}{
			"doctors": doctors,
			"pagination": PaginationInfo{
				Page:       page,
				Limit:      limit,
				Total:      total,
				TotalPages: totalPages,
				HasNext:    page < totalPages,
				HasPrev:    page > 1,
			},
		},
	})
}

// Enhanced appointment booking with better validation
func CreateAppointmentMobile(c *gin.Context) {
	var req models.CreateAppointmentRequest
	if err := c.ShouldBindJSON(&req); err != nil {
		c.JSON(http.StatusBadRequest, MobileResponse{
			Success: false,
			Error:   "Invalid request data: " + err.Error(),
		})
		return
	}

	// Get user ID from context (set by auth middleware)
	userID, exists := c.Get("user_id")
	if !exists {
		c.JSON(http.StatusUnauthorized, MobileResponse{
			Success: false,
			Error:   "User not authenticated",
		})
		return
	}

	// Parse appointment date
	appointmentDate, err := time.Parse("2006-01-02T15:04:05Z", req.AppointmentDate)
	if err != nil {
		c.JSON(http.StatusBadRequest, MobileResponse{
			Success: false,
			Error:   "Invalid date format. Use ISO 8601 format (YYYY-MM-DDTHH:MM:SSZ)",
		})
		return
	}

	// Check if doctor exists
	var doctorExists bool
	err = database.DB.QueryRow("SELECT EXISTS(SELECT 1 FROM doctors WHERE id = $1)", req.DoctorID).Scan(&doctorExists)
	if err != nil || !doctorExists {
		c.JSON(http.StatusBadRequest, MobileResponse{
			Success: false,
			Error:   "Doctor not found",
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
		c.JSON(http.StatusConflict, MobileResponse{
			Success: false,
			Error:   "Appointment slot is already booked",
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
		c.JSON(http.StatusInternalServerError, MobileResponse{
			Success: false,
			Error:   "Failed to create appointment",
		})
		return
	}

	c.JSON(http.StatusCreated, MobileResponse{
		Success: true,
		Message: "Appointment created successfully",
		Data: map[string]interface{}{
			"appointment_id": appointmentID,
			"appointment_date": appointmentDate.Format("2006-01-02"),
			"appointment_time": appointmentDate.Format("15:04"),
			"slot": req.Slot,
		},
	})
}

// Get user appointments with pagination
func GetUserAppointmentsMobile(c *gin.Context) {
	// Get user ID from context
	userID, exists := c.Get("user_id")
	if !exists {
		c.JSON(http.StatusUnauthorized, MobileResponse{
			Success: false,
			Error:   "User not authenticated",
		})
		return
	}

	// Get pagination parameters
	page, _ := strconv.Atoi(c.DefaultQuery("page", "1"))
	limit, _ := strconv.Atoi(c.DefaultQuery("limit", "20"))
	
	if page < 1 {
		page = 1
	}
	if limit < 1 || limit > 50 {
		limit = 20
	}
	
	offset := (page - 1) * limit

	// Get total count
	var total int
	err := database.DB.QueryRow(`
		SELECT COUNT(*) FROM appointments 
		WHERE patient_id = $1
	`, userID).Scan(&total)
	if err != nil {
		c.JSON(http.StatusInternalServerError, MobileResponse{
			Success: false,
			Error:   "Failed to count appointments",
		})
		return
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
		c.JSON(http.StatusInternalServerError, MobileResponse{
			Success: false,
			Error:   "Failed to fetch appointments",
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
			c.JSON(http.StatusInternalServerError, MobileResponse{
				Success: false,
				Error:   "Failed to scan appointment data",
			})
			return
		}

		doctor.ID = appointment.DoctorID
		doctor.User = &user
		appointment.Doctor = &doctor
		appointments = append(appointments, appointment)
	}

	// Calculate pagination info
	totalPages := (total + limit - 1) / limit

	c.JSON(http.StatusOK, MobileResponse{
		Success: true,
		Message: "Appointments fetched successfully",
		Data: map[string]interface{}{
			"appointments": appointments,
			"pagination": PaginationInfo{
				Page:       page,
				Limit:      limit,
				Total:      total,
				TotalPages: totalPages,
				HasNext:    page < totalPages,
				HasPrev:    page > 1,
			},
		},
	})
}

// Search doctors by specialization
func SearchDoctorsMobile(c *gin.Context) {
	query := c.Query("q")
	specialization := c.Query("specialization")
	
	if query == "" && specialization == "" {
		c.JSON(http.StatusBadRequest, MobileResponse{
			Success: false,
			Error:   "Search query or specialization is required",
		})
		return
	}

	searchQuery := `
		SELECT d.id, d.user_id, d.specialization, d.experience, d.phone, 
		       d.available_slots, d.created_at, d.updated_at,
		       u.name, u.email
		FROM doctors d
		JOIN users u ON d.user_id = u.id
		WHERE 1=1
	`
	
	args := []interface{}{}
	argIndex := 1
	
	if query != "" {
		searchQuery += " AND (u.name ILIKE $" + strconv.Itoa(argIndex) + " OR d.specialization ILIKE $" + strconv.Itoa(argIndex) + ")"
		args = append(args, "%"+query+"%")
		argIndex++
	}
	
	if specialization != "" {
		searchQuery += " AND d.specialization = $" + strconv.Itoa(argIndex)
		args = append(args, specialization)
		argIndex++
	}
	
	searchQuery += " ORDER BY u.name LIMIT 20"

	rows, err := database.DB.Query(searchQuery, args...)
	if err != nil {
		c.JSON(http.StatusInternalServerError, MobileResponse{
			Success: false,
			Error:   "Failed to search doctors",
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
			c.JSON(http.StatusInternalServerError, MobileResponse{
				Success: false,
				Error:   "Failed to scan doctor data",
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

	c.JSON(http.StatusOK, MobileResponse{
		Success: true,
		Message: "Search completed successfully",
		Data: map[string]interface{}{
			"doctors": doctors,
			"query": query,
			"specialization": specialization,
		},
	})
}

