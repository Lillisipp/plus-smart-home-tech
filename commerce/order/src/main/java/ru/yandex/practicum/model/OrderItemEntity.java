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
@Table(name = "order_items", schema = "orders")
public class OrderItemEntity {
    @EmbeddedId
    private OrderItemId id;

    @MapsId("orderId")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @Column(name = "quantity", nullable = false)
    private long quantity;

    @Embeddable
    @Getter @Setter
    @NoArgsConstructor @AllArgsConstructor
    public static class OrderItemId implements Serializable {
        @Column(name = "order_id", nullable = false)
        private UUID orderId;

        @Column(name = "product_id", nullable = false)
        private UUID productId;
    }
}
