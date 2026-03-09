CREATE SCHEMA IF NOT EXISTS orders;
SET search_path TO orders;


CREATE TABLE IF NOT EXISTS orders (
    order_id         UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    username         VARCHAR(255) NOT NULL,
    status           VARCHAR(50)  NOT NULL,
    shopping_cart_id UUID,
    payment_id       UUID,
    delivery_id      UUID,
    delivery_weight  DOUBLE PRECISION NOT NULL DEFAULT 0,
    delivery_volume  DOUBLE PRECISION NOT NULL DEFAULT 0,
    fragile          BOOLEAN NOT NULL DEFAULT FALSE,
    total_price      NUMERIC(19,2),
    delivery_price   NUMERIC(19,2),
    product_price    NUMERIC(19,2),
    created_at       TIMESTAMP NOT NULL DEFAULT now(),
    updated_at       TIMESTAMP NOT NULL DEFAULT now()
    );

CREATE TABLE IF NOT EXISTS order_items (
    order_id UUID REFERENCES orders(order_id) ON DELETE CASCADE,
    product_id UUID NOT NULL,
    quantity BIGINT NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_orders_username ON orders(username);