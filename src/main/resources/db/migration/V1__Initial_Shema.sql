CREATE SCHEMA IF NOT EXISTS spendwise;

-- 1. Create Users Table
CREATE TABLE spendwise.users (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    full_name VARCHAR(255) NOT NULL,
    email VARCHAR(255) UNIQUE NOT NULL,
    password_hash VARCHAR(255),
    auth_provider VARCHAR(50),
    sms_webhook_id UUID,
    created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    onboarded BOOLEAN DEFAULT FALSE
);

-- 2. Create Accounts Table
CREATE TABLE spendwise.accounts (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL,
    account_name VARCHAR(100) NOT NULL,
    account_type VARCHAR(50) NOT NULL,
    balance NUMERIC(19, 4) DEFAULT 0.0000,
    created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT fk_accounts_user FOREIGN KEY (user_id) 
        REFERENCES spendwise.users(id) ON DELETE CASCADE
);

-- 3. Create Transactions Table
CREATE TABLE spendwise.transactions (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL,
    account_id UUID NOT NULL,
    amount NUMERIC(19, 4) NOT NULL,
    type VARCHAR(50) NOT NULL,
    description VARCHAR(255),
    category VARCHAR(100),
    merchant_name VARCHAR(255),
    transaction_date TIMESTAMPTZ NOT NULL,
    source VARCHAR(50),
    external_id VARCHAR(255),
    created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    is_manual BOOLEAN DEFAULT TRUE,
    transaction_code VARCHAR(255),

    CONSTRAINT fk_transactions_user FOREIGN KEY (user_id) 
        REFERENCES spendwise.users(id) ON DELETE CASCADE,
    CONSTRAINT fk_transactions_account FOREIGN KEY (account_id) 
        REFERENCES spendwise.accounts(id) ON DELETE CASCADE
);

-- 4. Create Triggers for updated_at (Optional but recommended)
-- This ensures the updated_at column refreshes automatically on record changes.