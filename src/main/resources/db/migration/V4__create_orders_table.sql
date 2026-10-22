CREATE TABLE orders
(
    id          BIGSERIAL PRIMARY KEY,
    customer_id BIGINT                   NOT NULL REFERENCES customers (id),
    status      VARCHAR(20)              NOT NULL DEFAULT 'CREATED',
    total       NUMERIC(19, 2)           NOT NULL,
    created_at  TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT ck_orders_total_non_negative
        CHECK (total >= 0)
);
