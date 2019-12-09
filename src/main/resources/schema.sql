CREATE SEQUENCE IF NOT EXISTS account_id_seq;

CREATE TABLE IF NOT EXISTS account
(
    id         BIGINT DEFAULT account_id_seq.nextval PRIMARY KEY,
    balance    DECIMAL(19, 4)           NOT NULL DEFAULT 0.0
        CONSTRAINT positive_account_balance CHECK (balance >= 0),
    currency   VARCHAR(3)               NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    closed_at  TIMESTAMP WITH TIME ZONE
);

CREATE SEQUENCE IF NOT EXISTS transaction_id_seq;

CREATE TABLE IF NOT EXISTS transaction
(
    id           BIGINT DEFAULT transaction_id_seq.nextval PRIMARY KEY,
    created_at   TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at   TIMESTAMP WITH TIME ZONE NOT NULL,
    status       INTEGER                  NOT NULL,
    error_reason TEXT
);

CREATE SEQUENCE IF NOT EXISTS change_id_seq;

CREATE TABLE IF NOT EXISTS change
(
    id             BIGINT DEFAULT change_id_seq.nextval PRIMARY KEY,
    account_id     BIGINT REFERENCES account ON DELETE CASCADE,
    transaction_id BIGINT REFERENCES transaction ON DELETE CASCADE,
    amount         DECIMAL(19, 4) NOT NULL DEFAULT 0.0
);
