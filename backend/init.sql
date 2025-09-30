-- Initialize database schema
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- Users table
CREATE TABLE IF NOT EXISTS users (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    firebase_uid VARCHAR(255) UNIQUE NOT NULL,
    name VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL,
    role VARCHAR(50) NOT NULL CHECK (role IN ('patient', 'doctor', 'admin')),
    created_at TIMESTAMP DEFAULT NOW(),
    updated_at TIMESTAMP DEFAULT NOW()
);

-- Doctors table
CREATE TABLE IF NOT EXISTS doctors (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id UUID REFERENCES users(id) ON DELETE CASCADE,
    specialization VARCHAR(255) NOT NULL,
    experience INTEGER NOT NULL DEFAULT 0,
    phone VARCHAR(20),
    available_slots JSONB DEFAULT '{}',
    created_at TIMESTAMP DEFAULT NOW(),
    updated_at TIMESTAMP DEFAULT NOW()
);

-- Appointments table
CREATE TABLE IF NOT EXISTS appointments (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    doctor_id UUID REFERENCES doctors(id) ON DELETE CASCADE,
    patient_id UUID REFERENCES users(id) ON DELETE CASCADE,
    appointment_date TIMESTAMP NOT NULL,
    slot VARCHAR(50) NOT NULL,
    status VARCHAR(50) DEFAULT 'scheduled' CHECK (status IN ('scheduled', 'completed', 'cancelled', 'rescheduled')),
    notes TEXT,
    created_at TIMESTAMP DEFAULT NOW(),
    updated_at TIMESTAMP DEFAULT NOW()
);

-- Create indexes for better performance
CREATE INDEX IF NOT EXISTS idx_users_firebase_uid ON users(firebase_uid);
CREATE INDEX IF NOT EXISTS idx_doctors_user_id ON doctors(user_id);
CREATE INDEX IF NOT EXISTS idx_appointments_doctor_id ON appointments(doctor_id);
CREATE INDEX IF NOT EXISTS idx_appointments_patient_id ON appointments(patient_id);
CREATE INDEX IF NOT EXISTS idx_appointments_date ON appointments(appointment_date);

-- Insert sample data
INSERT INTO users (firebase_uid, name, email, role) VALUES 
('sample_firebase_uid_1', 'Dr. John Smith', 'john.smith@hospital.com', 'doctor'),
('sample_firebase_uid_2', 'Dr. Sarah Johnson', 'sarah.johnson@hospital.com', 'doctor'),
('sample_firebase_uid_3', 'Patient User', 'patient@example.com', 'patient')
ON CONFLICT (firebase_uid) DO NOTHING;

INSERT INTO doctors (user_id, specialization, experience, phone, available_slots) VALUES 
((SELECT id FROM users WHERE firebase_uid = 'sample_firebase_uid_1'), 'Cardiologist', 10, '+1234567890', '{"Monday": ["09:00", "10:00", "11:00"], "Tuesday": ["09:00", "10:00"], "Wednesday": ["14:00", "15:00"]}'),
((SELECT id FROM users WHERE firebase_uid = 'sample_firebase_uid_2'), 'Dermatologist', 8, '+1234567891', '{"Monday": ["10:00", "11:00"], "Thursday": ["09:00", "10:00"], "Friday": ["14:00", "15:00"]}')
ON CONFLICT DO NOTHING;


