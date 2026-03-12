package ru.yandex.practicum.commerce.payment.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.*;
import ru.yandex.practicum.commerce.payment.enums.PaymentState;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@Builder
@NoArgsConstructor
public class PaymentDto {

    @NotNull
    @PositiveOrZero
    private UUID paymentId;
    @NotNull
    @PositiveOrZero
    private BigDecimal totalPayment;
    @NotNull @PositiveOrZero
    private BigDecimal deliveryTotal;
    @NotNull @PositiveOrZero
    private BigDecimal feeTotal;
    @NotNull @PositiveOrZero
    private BigDecimal productTotal;
    @NotNull
    private PaymentState status;
}
