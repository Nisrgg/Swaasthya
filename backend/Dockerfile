# Build stage
FROM golang:1.22-alpine AS builder

WORKDIR /app

# Copy go mod files
COPY go.mod go.sum ./

# Download dependencies
RUN go mod download

# Copy source code
COPY . .

# Build the application
RUN CGO_ENABLED=0 GOOS=linux go build -a -installsuffix cgo -o main .

# Final stage
FROM alpine:latest

RUN apk --no-cache add ca-certificates
WORKDIR /root/

# Copy the binary from builder stage
COPY --from=builder /app/main .

# Copy wait-for-it script
COPY --from=builder /app/wait-for-it.sh .

# Make wait-for-it executable
RUN chmod +x wait-for-it.sh

# Expose port
EXPOSE 8080

# Command to run
CMD ["./main"]

