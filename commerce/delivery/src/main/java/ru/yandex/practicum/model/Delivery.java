package ru.yandex.practicum.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import ru.yandex.practicum.commerce.delivery.enums.DeliveryState;

import java.util.UUID;

@Getter
@Setter
@RequiredArgsConstructor
@Entity
@Table(name = "delivery")
public class Delivery {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "delivery_id", nullable = false)
    private UUID deliveryId;

    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "from_address_id")
    private DeliveryAddress fromAddress;

    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "to_address_id")
    private DeliveryAddress toAddress;

    @Column(name = "order_id", unique = true)
    private UUID orderId;

    @Enumerated(EnumType.STRING)
    @Column(name = "delivery_state", nullable = false, length = 15)
    private DeliveryState deliveryState;

    @Column(name = "delivery_weight", nullable = false)
    private double deliveryWeight;

    @Column(name = "delivery_volume", nullable = false)
    private double deliveryVolume;

    @Column(name = "fragile", nullable = false)
    private boolean fragile;
}