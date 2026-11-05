ALTER TABLE accounts
    ADD CONSTRAINT ck_accounts_admin_without_customer
        CHECK (role <> 'ADMIN' OR customer_id IS NULL);
