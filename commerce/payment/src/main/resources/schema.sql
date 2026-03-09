CREATE SCHEMA IF NOT EXISTS payment;

CREATE TABLE IF NOT EXISTS payment.payments (
    payment_id     UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    order_id       UUID NOT NULL UNIQUE,
    payment_state  VARCHAR(15) NOT NULL,

    product_total  NUMERIC(19,2) NOT NULL,
    delivery_total NUMERIC(19,2) NOT NULL,
    fee_total      NUMERIC(19,2) NOT NULL,
    total_payment  NUMERIC(19,2) NOT NULL
    );