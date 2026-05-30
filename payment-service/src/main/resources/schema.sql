CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

CREATE TABLE IF NOT EXISTS payment_orders (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    mock_order_id VARCHAR(100) UNIQUE,
    mock_payment_id VARCHAR(100),
    mock_signature VARCHAR(200),
    booking_id UUID NOT NULL,
    student_id UUID NOT NULL,
    mentor_id UUID,
    amount DECIMAL(19,4) NOT NULL,
    currency VARCHAR(10) NOT NULL DEFAULT 'INR',
    status VARCHAR(20) NOT NULL DEFAULT 'CREATED'
        CHECK (status IN ('CREATED','CAPTURED','FAILED','REFUNDED')),
    idempotency_key VARCHAR(100) NOT NULL UNIQUE,
    failure_reason TEXT,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    version BIGINT DEFAULT 0
);

CREATE INDEX IF NOT EXISTS idx_payment_booking_id ON payment_orders(booking_id);
CREATE INDEX IF NOT EXISTS idx_payment_student_id ON payment_orders(student_id);
CREATE INDEX IF NOT EXISTS idx_payment_status ON payment_orders(status);
CREATE INDEX IF NOT EXISTS idx_payment_mock_order_id ON payment_orders(mock_order_id);

CREATE TABLE IF NOT EXISTS payment_ledger (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    payment_order_id UUID NOT NULL REFERENCES payment_orders(id) ON DELETE CASCADE,
    event_type VARCHAR(20) NOT NULL CHECK (event_type IN ('DEBIT','CREDIT','REFUND','FEE')),
    amount DECIMAL(19,4) NOT NULL,
    description TEXT,
    metadata TEXT,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    version BIGINT DEFAULT 0
);

CREATE INDEX IF NOT EXISTS idx_ledger_payment_order_id ON payment_ledger(payment_order_id);

CREATE TABLE IF NOT EXISTS idempotency_keys (
    idempotency_key VARCHAR(100) PRIMARY KEY,
    response_payload TEXT,
    service_name VARCHAR(100),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    expires_at TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE TABLE IF NOT EXISTS mentor_payouts (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    mentor_id UUID NOT NULL,
    amount DECIMAL(19,4) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING'
        CHECK (status IN ('PENDING','PROCESSED','FAILED')),
    mock_payout_id VARCHAR(100),
    period_start TIMESTAMP WITH TIME ZONE,
    period_end TIMESTAMP WITH TIME ZONE,
    processed_at TIMESTAMP WITH TIME ZONE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    version BIGINT DEFAULT 0
);

CREATE INDEX IF NOT EXISTS idx_payout_mentor_id ON mentor_payouts(mentor_id);
