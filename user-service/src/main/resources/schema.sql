-- Users Database Schema
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

CREATE TABLE IF NOT EXISTS colleges (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    name VARCHAR(200) NOT NULL,
    domain VARCHAR(100) UNIQUE,
    is_verified BOOLEAN NOT NULL DEFAULT FALSE,
    logo_url VARCHAR(500),
    website VARCHAR(500),
    city VARCHAR(100),
    state VARCHAR(100),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    version BIGINT DEFAULT 0
);

CREATE INDEX IF NOT EXISTS idx_colleges_domain ON colleges(domain);

CREATE TABLE IF NOT EXISTS users (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    email VARCHAR(255) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    full_name VARCHAR(100) NOT NULL,
    role VARCHAR(20) NOT NULL CHECK (role IN ('STUDENT', 'MENTOR', 'COLLEGE_ADMIN', 'PLATFORM_ADMIN')),
    phone_number VARCHAR(20),
    profile_picture_url VARCHAR(500),
    college_id UUID REFERENCES colleges(id),
    is_email_verified BOOLEAN NOT NULL DEFAULT FALSE,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    email_verification_token VARCHAR(500),
    email_verification_token_expiry TIMESTAMP WITH TIME ZONE,
    last_login_at TIMESTAMP WITH TIME ZONE,
    failed_login_attempts INTEGER NOT NULL DEFAULT 0,
    locked_until TIMESTAMP WITH TIME ZONE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    version BIGINT DEFAULT 0
);

CREATE INDEX IF NOT EXISTS idx_users_email ON users(email);
CREATE INDEX IF NOT EXISTS idx_users_role ON users(role);
CREATE INDEX IF NOT EXISTS idx_users_college_id ON users(college_id);
CREATE INDEX IF NOT EXISTS idx_users_is_active ON users(is_active);

CREATE TABLE IF NOT EXISTS refresh_tokens (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    token VARCHAR(500) NOT NULL UNIQUE,
    expires_at TIMESTAMP WITH TIME ZONE NOT NULL,
    is_revoked BOOLEAN NOT NULL DEFAULT FALSE,
    ip_address VARCHAR(45),
    user_agent VARCHAR(500),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    version BIGINT DEFAULT 0
);

CREATE INDEX IF NOT EXISTS idx_refresh_tokens_user_id ON refresh_tokens(user_id);
CREATE INDEX IF NOT EXISTS idx_refresh_tokens_token ON refresh_tokens(token);

-- Insert sample colleges
INSERT INTO colleges (name, domain, is_verified, city, state) VALUES
    ('Indian Institute of Technology Delhi', 'iitd.ac.in', TRUE, 'New Delhi', 'Delhi'),
    ('Indian Institute of Technology Bombay', 'iitb.ac.in', TRUE, 'Mumbai', 'Maharashtra'),
    ('National Institute of Technology Trichy', 'nitt.edu', TRUE, 'Tiruchirappalli', 'Tamil Nadu'),
    ('Bits Pilani', 'bits-pilani.ac.in', TRUE, 'Pilani', 'Rajasthan'),
    ('VIT University', 'vit.ac.in', TRUE, 'Vellore', 'Tamil Nadu')
ON CONFLICT DO NOTHING;
