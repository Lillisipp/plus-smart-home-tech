package ru.yandex.practicum.model;

import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "order_booking_items", schema = "warehouse")
public class OrderBookingItemEntity {

    @EmbeddedId
    private OrderBookingItemId id;

    @MapsId("orderId")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private OrderBooking booking;

    @Column(name = "quantity", nullable = false)
    private long quantity;

    @Embeddable
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OrderBookingItemId implements Serializable {

        @Column(name = "order_id", nullable = false)
        private UUID orderId;

        @Column(name = "product_id", nullable = false)
        private UUID productId;
    }
}
