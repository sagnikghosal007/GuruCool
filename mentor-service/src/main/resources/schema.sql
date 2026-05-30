CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

CREATE TABLE IF NOT EXISTS mentor_profiles (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id UUID NOT NULL UNIQUE,
    full_name VARCHAR(100),
    email VARCHAR(255),
    bio TEXT,
    current_company VARCHAR(200),
    current_role VARCHAR(200),
    experience_years INTEGER,
    linkedin_url VARCHAR(500),
    verification_status VARCHAR(20) NOT NULL DEFAULT 'UNVERIFIED'
        CHECK (verification_status IN ('UNVERIFIED','PENDING','INNER_CIRCLE','OUTER_CIRCLE')),
    college_id UUID,
    average_rating DECIMAL(3,2) DEFAULT 0.00,
    total_sessions INTEGER NOT NULL DEFAULT 0,
    total_ratings INTEGER NOT NULL DEFAULT 0,
    profile_picture_url VARCHAR(500),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    version BIGINT DEFAULT 0
);

CREATE INDEX IF NOT EXISTS idx_mentor_user_id ON mentor_profiles(user_id);
CREATE INDEX IF NOT EXISTS idx_mentor_college_id ON mentor_profiles(college_id);
CREATE INDEX IF NOT EXISTS idx_mentor_verification_status ON mentor_profiles(verification_status);
CREATE INDEX IF NOT EXISTS idx_mentor_average_rating ON mentor_profiles(average_rating DESC);

CREATE TABLE IF NOT EXISTS expertise_tags (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    mentor_id UUID NOT NULL REFERENCES mentor_profiles(id) ON DELETE CASCADE,
    tag VARCHAR(100) NOT NULL,
    category VARCHAR(100),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    version BIGINT DEFAULT 0
);

CREATE INDEX IF NOT EXISTS idx_expertise_mentor_id ON expertise_tags(mentor_id);
CREATE INDEX IF NOT EXISTS idx_expertise_tag ON expertise_tags(tag);

CREATE TABLE IF NOT EXISTS mentor_availability (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    mentor_id UUID NOT NULL REFERENCES mentor_profiles(id) ON DELETE CASCADE,
    day_of_week VARCHAR(10) NOT NULL,
    start_time TIME NOT NULL,
    end_time TIME NOT NULL,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    version BIGINT DEFAULT 0
);

CREATE INDEX IF NOT EXISTS idx_availability_mentor_id ON mentor_availability(mentor_id);

CREATE TABLE IF NOT EXISTS mentor_ratings (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    mentor_id UUID NOT NULL REFERENCES mentor_profiles(id) ON DELETE CASCADE,
    student_id UUID NOT NULL,
    session_id UUID NOT NULL,
    rating INTEGER NOT NULL CHECK (rating BETWEEN 1 AND 5),
    feedback TEXT,
    is_anonymous BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    version BIGINT DEFAULT 0,
    UNIQUE(session_id, student_id)
);

CREATE INDEX IF NOT EXISTS idx_rating_mentor_id ON mentor_ratings(mentor_id);
CREATE INDEX IF NOT EXISTS idx_rating_session_id ON mentor_ratings(session_id);

CREATE TABLE IF NOT EXISTS mentor_verification_requests (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    mentor_id UUID NOT NULL REFERENCES mentor_profiles(id) ON DELETE CASCADE,
    college_id UUID,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING'
        CHECK (status IN ('UNVERIFIED','PENDING','INNER_CIRCLE','OUTER_CIRCLE')),
    document_url VARCHAR(500),
    reviewed_at TIMESTAMP WITH TIME ZONE,
    review_note TEXT,
    reviewed_by UUID,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    version BIGINT DEFAULT 0
);

CREATE INDEX IF NOT EXISTS idx_verification_mentor_id ON mentor_verification_requests(mentor_id);
