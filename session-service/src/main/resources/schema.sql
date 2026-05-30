CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

CREATE TABLE IF NOT EXISTS sessions (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    mentor_id UUID NOT NULL,
    title VARCHAR(200) NOT NULL,
    description TEXT,
    session_type VARCHAR(20) NOT NULL CHECK (session_type IN ('ONE_ON_ONE','GROUP','WEBINAR','BOOTCAMP')),
    scheduled_at TIMESTAMP WITH TIME ZONE NOT NULL,
    duration_minutes INTEGER NOT NULL CHECK (duration_minutes BETWEEN 15 AND 480),
    max_participants INTEGER NOT NULL DEFAULT 1,
    current_participants INTEGER NOT NULL DEFAULT 0,
    status VARCHAR(20) NOT NULL DEFAULT 'UPCOMING' CHECK (status IN ('UPCOMING','LIVE','COMPLETED','CANCELLED')),
    meeting_link VARCHAR(500),
    is_paid BOOLEAN NOT NULL DEFAULT FALSE,
    price_amount DECIMAL(19,4) DEFAULT 0.0000,
    price_currency VARCHAR(10) DEFAULT 'INR',
    cancellation_reason TEXT,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    version BIGINT DEFAULT 0
);

CREATE INDEX IF NOT EXISTS idx_session_mentor_id ON sessions(mentor_id);
CREATE INDEX IF NOT EXISTS idx_session_status ON sessions(status);
CREATE INDEX IF NOT EXISTS idx_session_scheduled_at ON sessions(scheduled_at);

CREATE TABLE IF NOT EXISTS session_bookings (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    session_id UUID NOT NULL REFERENCES sessions(id) ON DELETE CASCADE,
    student_id UUID NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING' CHECK (status IN ('CONFIRMED','PENDING','CANCELLED','ATTENDED','NO_SHOW')),
    booked_at TIMESTAMP WITH TIME ZONE,
    cancelled_at TIMESTAMP WITH TIME ZONE,
    payment_id UUID,
    idempotency_key VARCHAR(100) NOT NULL UNIQUE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    version BIGINT DEFAULT 0
);

CREATE INDEX IF NOT EXISTS idx_booking_session_id ON session_bookings(session_id);
CREATE INDEX IF NOT EXISTS idx_booking_student_id ON session_bookings(student_id);
CREATE INDEX IF NOT EXISTS idx_booking_status ON session_bookings(status);

CREATE TABLE IF NOT EXISTS session_recordings (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    session_id UUID NOT NULL REFERENCES sessions(id) ON DELETE CASCADE,
    recording_url VARCHAR(500) NOT NULL,
    duration_seconds INTEGER,
    uploaded_at TIMESTAMP WITH TIME ZONE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    version BIGINT DEFAULT 0
);

CREATE TABLE IF NOT EXISTS waitlist (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    session_id UUID NOT NULL REFERENCES sessions(id) ON DELETE CASCADE,
    student_id UUID NOT NULL,
    joined_at TIMESTAMP WITH TIME ZONE NOT NULL,
    position INTEGER NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    version BIGINT DEFAULT 0,
    UNIQUE(session_id, student_id)
);

CREATE INDEX IF NOT EXISTS idx_waitlist_session_id ON waitlist(session_id);
