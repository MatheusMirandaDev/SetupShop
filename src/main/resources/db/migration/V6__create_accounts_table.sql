CREATE TABLE accounts
(
    id            BIGSERIAL PRIMARY KEY,
    email         VARCHAR(255)             NOT NULL UNIQUE,
    password_hash VARCHAR(255)             NOT NULL,
    customer_id   BIGINT UNIQUE REFERENCES customers (id),
    role          VARCHAR(20)              NOT NULL,
    created_at    TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at    TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CHECK (role <> 'CUSTOMER' OR customer_id IS NOT NULL)
);

CREATE TRIGGER trg_accounts_updated_at
    BEFORE UPDATE
    ON accounts
    FOR EACH ROW
EXECUTE FUNCTION update_updated_at_column();
