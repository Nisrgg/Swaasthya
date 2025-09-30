package middleware

import (
	"context"
	"net/http"
	"os"
	"strings"

	"firebase.google.com/go/v4"
	"firebase.google.com/go/v4/auth"
	"github.com/gin-gonic/gin"
)

var (
	FirebaseApp *firebase.App
	AuthClient  *auth.Client
)

// InitFirebase initializes Firebase Admin SDK
func InitFirebase() error {
	// Check if Firebase is enabled via environment variable
	if os.Getenv("FIREBASE_ENABLED") != "true" {
		return nil // Firebase is disabled, skip initialization
	}

	// In production, use service account key file
	// For now, we'll use the Firebase project ID
	projectID := os.Getenv("FIREBASE_PROJECT_ID")
	if projectID == "" {
		projectID = "hospital-app-e1548" // Replace with your actual project ID
	}

	ctx := context.Background()
	
	// Initialize Firebase app
	app, err := firebase.NewApp(ctx, &firebase.Config{
		ProjectID: projectID,
	})
	if err != nil {
		return err
	}

	FirebaseApp = app

	// Initialize Auth client
	authClient, err := app.Auth(ctx)
	if err != nil {
		return err
	}

	AuthClient = authClient
	return nil
}

// ValidateFirebaseToken middleware validates Firebase JWT tokens
func ValidateFirebaseToken() gin.HandlerFunc {
	return func(c *gin.Context) {
		// If Firebase is not initialized, skip authentication for testing
		if AuthClient == nil {
			// For testing purposes, set dummy user data
			c.Set("user_id", "test-user-123")
			c.Set("user_email", "test@example.com")
			c.Set("user_name", "Test User")
			c.Next()
			return
		}

		// Get Authorization header
		authHeader := c.GetHeader("Authorization")
		if authHeader == "" {
			c.JSON(http.StatusUnauthorized, gin.H{
				"error": "Authorization header required",
			})
			c.Abort()
			return
		}

		// Extract token from "Bearer <token>"
		tokenParts := strings.Split(authHeader, " ")
		if len(tokenParts) != 2 || tokenParts[0] != "Bearer" {
			c.JSON(http.StatusUnauthorized, gin.H{
				"error": "Invalid authorization header format",
			})
			c.Abort()
			return
		}

		token := tokenParts[1]

		// Verify token with Firebase
		ctx := context.Background()
		tokenResult, err := AuthClient.VerifyIDToken(ctx, token)
		if err != nil {
			c.JSON(http.StatusUnauthorized, gin.H{
				"error": "Invalid token",
			})
			c.Abort()
			return
		}

		// Extract user information
		userID := tokenResult.UID
		email := tokenResult.Claims["email"].(string)
		name := tokenResult.Claims["name"].(string)

		// Store user info in context
		c.Set("user_id", userID)
		c.Set("user_email", email)
		c.Set("user_name", name)

		c.Next()
	}
}

