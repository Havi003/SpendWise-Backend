-- Schema: spendwise
-- =====================================================

-- Enable UUID generation
CREATE EXTENSION IF NOT EXISTS "pgcrypto";

-- Create schema
CREATE SCHEMA IF NOT EXISTS spendwise;

-- =====================================================
-- USERS
-- =====================================================
CREATE TABLE IF NOT EXISTS spendwise.users (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    full_name VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE,
    password_hash VARCHAR(255),
    auth_provider VARCHAR(50) NOT NULL DEFAULT 'email',
    sms_webhook_id UUID NOT NULL UNIQUE DEFAULT gen_random_uuid(),
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

-- =====================================================
-- ACCOUNTS
-- =====================================================
CREATE TABLE IF NOT EXISTS spendwise.accounts (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL REFERENCES spendwise.users(id) ON DELETE CASCADE,
    account_name VARCHAR(100) NOT NULL,
    account_type VARCHAR(50) NOT NULL,
    balance DECIMAL(19,4) NOT NULL DEFAULT 0.00,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

-- =====================================================
-- TRANSACTIONS
-- =====================================================
CREATE TABLE IF NOT EXISTS spendwise.transactions (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL REFERENCES spendwise.users(id) ON DELETE CASCADE,
    account_id UUID NOT NULL REFERENCES spendwise.accounts(id) ON DELETE CASCADE,
    amount DECIMAL(19,4) NOT NULL,
    type VARCHAR(50) NOT NULL,
    description VARCHAR(255) NOT NULL,
    category VARCHAR(100) NOT NULL DEFAULT 'Uncategorized', -- Refactored from ai/user_category
    is_manual BOOLEAN NOT NULL DEFAULT false,               -- Refactored from is_ai_categorized
    merchant_name VARCHAR(255),
    transaction_code VARCHAR(255) UNIQUE,
    transaction_date TIMESTAMPTZ NOT NULL,
    source VARCHAR(50) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now()
);