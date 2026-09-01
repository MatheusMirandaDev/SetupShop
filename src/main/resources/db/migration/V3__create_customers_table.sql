CREATE TABLE customers
(
    id         BIGSERIAL PRIMARY KEY,
    name       VARCHAR(200)             NOT NULL,
    email      VARCHAR(255)             NOT NULL UNIQUE,
    phone      VARCHAR(11)              NOT NULL,
    active     BOOLEAN                  NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP

        CONSTRAINT ck_customers_name_not_blank
            CHECK (BTRIM(name) <> ''),

    CONSTRAINT ck_customers_email_not_blank
        CHECK (BTRIM(email) <> ''),

    CONSTRAINT ck_customers_phone_not_blank
        CHECK (BTRIM(phone) <> ''),

    CONSTRAINT ck_customers_phone_exactly_eleven_digits
        CHECK (phone ~ '^[0-9]{11}$')
);

CREATE TRIGGER trg_customers_updated_at
    BEFORE UPDATE
    ON customers
    FOR EACH ROW
EXECUTE FUNCTION update_updated_at_column();