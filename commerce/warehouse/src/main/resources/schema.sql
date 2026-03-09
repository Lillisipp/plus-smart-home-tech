CREATE SCHEMA IF NOT EXISTS warehouse;
SET search_path TO warehouse;

CREATE TABLE IF NOT EXISTS warehouse
(
    product_id UUID PRIMARY KEY,
    fragile    BOOLEAN,
    width      DOUBLE PRECISION,
    height     DOUBLE PRECISION,
    depth      DOUBLE PRECISION,
    weight     DOUBLE PRECISION,
    quantity   INTEGER DEFAULT 0
);

CREATE TABLE IF NOT EXISTS order_bookings (
    order_id    UUID PRIMARY KEY,
    delivery_id UUID,
    created_at  TIMESTAMP NOT NULL DEFAULT now()
    );

CREATE TABLE IF NOT EXISTS order_booking_items (
    order_id   UUID NOT NULL REFERENCES order_bookings(order_id) ON DELETE CASCADE,
    product_id UUID NOT NULL,
    quantity   BIGINT NOT NULL,
    PRIMARY KEY (order_id, product_id)
    );