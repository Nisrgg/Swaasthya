package main

import (
	"log"
	"os"

	"hospital-backend/database"
	"hospital-backend/handlers"
	"hospital-backend/middleware"

	"github.com/gin-contrib/cors"
	"github.com/gin-gonic/gin"
	"github.com/joho/godotenv"
)

func main() {
	// Load environment variables
	if err := godotenv.Load(); err != nil {
		log.Println("No .env file found")
	}

	// Initialize database
	if err := database.InitDB(); err != nil {
		log.Fatal("Failed to connect to database:", err)
	}

	// Initialize Firebase Admin SDK
	if err := middleware.InitFirebase(); err != nil {
		log.Fatal("Failed to initialize Firebase:", err)
	}

	// Set Gin mode
	if os.Getenv("GIN_MODE") == "release" {
		gin.SetMode(gin.ReleaseMode)
	}

	// Create Gin router
	r := gin.Default()

	// CORS configuration
	r.Use(cors.New(cors.Config{
		AllowOrigins:     []string{"*"}, // In production, specify your frontend URL
		AllowMethods:     []string{"GET", "POST", "PUT", "DELETE", "OPTIONS"},
		AllowHeaders:     []string{"*"},
		ExposeHeaders:    []string{"Content-Length"},
		AllowCredentials: true,
	}))

	// Health check endpoint
	r.GET("/health", func(c *gin.Context) {
		c.JSON(200, gin.H{
			"status":  "ok",
			"message": "Hospital API is running",
		})
	})

	// API routes
	api := r.Group("/api")
	{
		// Public routes (no auth required)
		api.GET("/doctors", handlers.GetDoctors)
		api.GET("/doctors/:id", handlers.GetDoctorByID)
		
		// Mobile-optimized public routes
		api.GET("/mobile/doctors", handlers.GetDoctorsMobile)
		api.GET("/mobile/search/doctors", handlers.SearchDoctorsMobile)

		// Protected routes (require Firebase auth)
		protected := api.Group("/")
		protected.Use(middleware.ValidateFirebaseToken())
		{
			protected.POST("/appointments", handlers.CreateAppointment)
			protected.GET("/appointments", handlers.GetUserAppointments)
			protected.PUT("/appointments/:id", handlers.UpdateAppointment)
			protected.DELETE("/appointments/:id", handlers.CancelAppointment)
			
			// Mobile-optimized protected routes
			protected.POST("/mobile/appointments", handlers.CreateAppointmentMobile)
			protected.GET("/mobile/appointments", handlers.GetUserAppointmentsMobile)
		}
	}

	// Start server
	port := os.Getenv("PORT")
	if port == "" {
		port = "8080"
	}

	log.Printf("Server starting on port %s", port)
	if err := r.Run(":" + port); err != nil {
		log.Fatal("Failed to start server:", err)
	}
}

