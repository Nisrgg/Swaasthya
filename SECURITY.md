# 🔒 Security Configuration Guide

## Firebase Configuration

### ⚠️ IMPORTANT: Never commit `google-services.json` to version control!

The `google-services.json` file contains sensitive Firebase API keys and should be kept secure.

### Setup Instructions

1. **Download your Firebase configuration:**
   - Go to [Firebase Console](https://console.firebase.google.com/)
   - Select your project: `swaasthya-app`
   - Go to Project Settings → General → Your apps
   - Download `google-services.json` for Android

2. **Place the file in the correct location:**
   ```bash
   # Copy the downloaded file to:
   app/google-services.json
   ```

3. **Set environment variables (Recommended):**
   ```bash
   # For development, you can set these environment variables:
   export GOOGLE_WEB_CLIENT_ID="659375898281-ff2sc78flq7jfao4qudhutn7f8is02fo.apps.googleusercontent.com"
   export API_BASE_URL="http://192.168.74.120:8080/"
   ```

4. **For production builds:**
   - Use BuildConfig fields
   - Use environment-specific configuration files
   - Never hardcode sensitive values

## Backend Configuration

### Environment Variables

Create a `.env` file in the `backend/` directory:

```bash
# Database Configuration
DB_HOST=localhost
DB_PORT=5432
DB_USER=your_db_user
DB_PASSWORD=your_secure_password
DB_NAME=hospital_db

# Firebase Configuration
FIREBASE_PROJECT_ID=swaasthya-app
FIREBASE_ENABLED=true

# Server Configuration
PORT=8080
GIN_MODE=release

# CORS Configuration
CORS_ALLOWED_ORIGINS=https://yourdomain.com
```

### Security Best Practices

1. **Never commit sensitive files:**
   - `google-services.json`
   - `.env` files
   - Service account keys
   - Private keys

2. **Use environment variables:**
   - Database credentials
   - API keys
   - Firebase configuration

3. **Rotate credentials regularly:**
   - Change database passwords
   - Regenerate API keys
   - Update Firebase service accounts

4. **Use different configurations for different environments:**
   - Development
   - Staging
   - Production

## Current Security Status

✅ **Fixed Issues:**
- Removed `google-services.json` from version control
- Updated `.gitignore` to prevent future exposure
- Created environment-based configuration system
- Removed hardcoded credentials from source code

⚠️ **Still Needs Attention:**
- Set up proper environment variable management
- Configure production Firebase project
- Set up proper CORS configuration for production
- Implement proper secret management system

## Emergency Actions

If you suspect credentials have been compromised:

1. **Immediately rotate all exposed credentials:**
   - Regenerate Firebase API keys
   - Change database passwords
   - Update Google OAuth client secrets

2. **Review access logs:**
   - Check Firebase usage logs
   - Monitor database access
   - Review API usage patterns

3. **Update all environments:**
   - Development
   - Staging
   - Production
