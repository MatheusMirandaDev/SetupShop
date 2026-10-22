CREATE TABLE order_items
(
    id         BIGSERIAL PRIMARY KEY,
    order_id   BIGINT         NOT NULL REFERENCES orders (id),
    product_id BIGINT         NOT NULL REFERENCES products (id),
    quantity   INTEGER        NOT NULL,
    unit_price NUMERIC(19, 2) NOT NULL,

    CONSTRAINT ck_order_items_quantity_positive
        CHECK (quantity > 0),

    CONSTRAINT ck_order_items_unit_price_non_negative
        CHECK (unit_price >= 0)
);
