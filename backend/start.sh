#!/bin/bash

echo "🏥 Hospital Management App Setup Script"
echo "======================================"

# Check if Docker is running
if ! docker info > /dev/null 2>&1; then
    echo "❌ Docker is not running. Please start Docker first."
    exit 1
fi

echo "✅ Docker is running"

# Navigate to backend directory
cd backend

echo "📦 Starting PostgreSQL database..."
docker-compose up -d postgres

echo "⏳ Waiting for database to be ready..."
sleep 10

echo "🔧 Installing Go dependencies..."
go mod tidy

echo "📝 Setting up environment..."
if [ ! -f .env ]; then
    cp config.env .env
    echo "📋 Created .env file from template"
    echo "⚠️  Please update the Firebase project ID in .env file"
fi

echo "🚀 Starting Go server..."
echo "The server will start on http://localhost:8080"
echo "API endpoints will be available at http://localhost:8080/api/"
echo ""
echo "Press Ctrl+C to stop the server"
echo ""

go run main.go


