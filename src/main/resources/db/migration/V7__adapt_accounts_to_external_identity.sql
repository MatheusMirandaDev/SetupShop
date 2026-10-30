ALTER TABLE accounts
    ADD COLUMN issuer VARCHAR(255) NOT NULL,
    ADD COLUMN subject VARCHAR(255) NOT NULL,
    DROP COLUMN password_hash,
    ADD CONSTRAINT uk_accounts_issuer_subject UNIQUE (issuer, subject);
