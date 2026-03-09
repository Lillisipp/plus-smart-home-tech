package ru.yandex.practicum.service;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.cache.ProductPriceCache;
import ru.yandex.practicum.commerce.order.dto.OrderDto;
import ru.yandex.practicum.commerce.payment.dto.PaymentDto;
import ru.yandex.practicum.commerce.payment.enums.PaymentState;
import ru.yandex.practicum.error.NotEnoughInfoInOrderToCalculateException;
import ru.yandex.practicum.mapper.PaymentMapper;
import ru.yandex.practicum.model.PaymentEntity;
import ru.yandex.practicum.repository.PaymentRepository;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentService {

    private static final BigDecimal VAT_RATE = new BigDecimal("0.10");

    private final PaymentRepository paymentRepository;
    private final ProductPriceCache priceCache;
    private final PaymentMapper paymentMapper;

    @Transactional(readOnly = true)
    public BigDecimal productCost(@Valid OrderDto order) {
        log.info("productCost: orderId={}, items={}", order.getOrderId(), order.getProducts().size());

        BigDecimal total = BigDecimal.ZERO;

        for (var e : order.getProducts().entrySet()) {
            UUID productId = e.getKey();
            long qty = e.getValue();

            BigDecimal price = priceCache.getPrice(productId); // [ИЗМЕНЕНИЕ] кэш
            total = total.add(price.multiply(BigDecimal.valueOf(qty)));
        }

        return scale(total);
    }

    @Transactional(readOnly = true)
    public BigDecimal getTotalCost(@Valid OrderDto order) {
        log.info("getTotalCost: orderId={}", order.getOrderId());

        if (order.getProductPrice() == null || order.getDeliveryPrice() == null) {
            throw new NotEnoughInfoInOrderToCalculateException("productPrice and deliveryPrice are required");
        }

        BigDecimal product = scale(order.getProductPrice());
        BigDecimal delivery = scale(order.getDeliveryPrice());
        BigDecimal fee = scale(product.multiply(VAT_RATE));

        return scale(product.add(fee).add(delivery));
    }

    @Transactional
    public PaymentDto payment(@Valid OrderDto order) {
        log.info("payment: orderId={}", order.getOrderId());

        if (order.getDeliveryPrice() == null) {
            throw new NotEnoughInfoInOrderToCalculateException("deliveryPrice is required");
        }

        BigDecimal productTotal = (order.getProductPrice() != null)
                ? scale(order.getProductPrice())
                : productCost(order); // тоже через кэш

        BigDecimal deliveryTotal = scale(order.getDeliveryPrice());
        BigDecimal feeTotal = scale(productTotal.multiply(VAT_RATE));
        BigDecimal totalPayment = scale(productTotal.add(feeTotal).add(deliveryTotal));

        PaymentEntity entity = paymentRepository.findByOrderId(order.getOrderId())
                .orElseGet(PaymentEntity::new);

        entity.setOrderId(order.getOrderId());
        entity.setPaymentState(PaymentState.PENDING);
        entity.setProductTotal(productTotal);
        entity.setDeliveryTotal(deliveryTotal);
        entity.setFeeTotal(feeTotal);
        entity.setTotalPayment(totalPayment);

        PaymentEntity saved = paymentRepository.save(entity);

        PaymentDto dto = paymentMapper.toDto(saved);
        log.info("payment: paymentId={}, status={}, total={}",
                dto.getPaymentId(), dto.getStatus(), dto.getTotalPayment());

        return dto;
    }

    @Transactional
    public void paymentSuccess(UUID paymentId) {
        log.info("paymentSuccess: paymentId={}", paymentId);
        PaymentEntity e = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new IllegalArgumentException("NoOrderFound: paymentId=" + paymentId));
        e.setPaymentState(PaymentState.SUCCESS);
        paymentRepository.save(e);
    }

    @Transactional
    public void paymentFailed(UUID paymentId) {
        log.info("paymentFailed: paymentId={}", paymentId);
        PaymentEntity e = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new IllegalArgumentException("NoOrderFound: paymentId=" + paymentId));
        e.setPaymentState(PaymentState.FAILED);
        paymentRepository.save(e);
    }

    private BigDecimal scale(BigDecimal v) {
        return v.setScale(2, RoundingMode.HALF_UP);
    }
}