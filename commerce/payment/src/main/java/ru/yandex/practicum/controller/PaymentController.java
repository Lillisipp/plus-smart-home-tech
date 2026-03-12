package ru.yandex.practicum.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.yandex.practicum.commerce.order.dto.OrderDto;
import ru.yandex.practicum.commerce.payment.controller.PaymentApi;
import ru.yandex.practicum.commerce.payment.dto.PaymentDto;
import ru.yandex.practicum.service.PaymentService;

import java.math.BigDecimal;
import java.util.UUID;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/payment")
public class PaymentController implements PaymentApi {
    private final PaymentService paymentService;

    @PostMapping
    public PaymentDto payment(@RequestBody @Valid OrderDto order) {
        log.info("payment: orderId={}, products={}",
                order == null ? null : order.getOrderId(),
                order == null || order.getProducts() == null ? 0 : order.getProducts().size());

        PaymentDto dto = paymentService.payment(order);

        log.info("payment: paymentId={}, total={}", dto.getPaymentId(), dto.getTotalPayment());
        return dto;
    }

    @PostMapping("/productCost")
    public BigDecimal productCost(@RequestBody @Valid OrderDto order) {
        log.info("productCost: orderId={}", order == null ? null : order.getOrderId());
        BigDecimal cost = paymentService.productCost(order);
        log.info("productCost: orderId={}, cost={}", order.getOrderId(), cost);
        return cost;
    }

    @PostMapping("/totalCost")
    public BigDecimal getTotalCost(@RequestBody @Valid OrderDto order) {
        log.info("totalCost: orderId={}", order == null ? null : order.getOrderId());
        BigDecimal total = paymentService.getTotalCost(order);
        log.info("totalCost: orderId={}, total={}", order.getOrderId(), total);
        return total;
    }

    @PostMapping("/refund")
    public void paymentSuccess(@RequestBody UUID paymentId) {
        log.info("paymentSuccess: paymentId={}", paymentId);
        paymentService.paymentSuccess(paymentId);
        log.info("paymentSuccess: paymentId={} -> OK", paymentId);
    }

    @PostMapping("/failed")
    public void paymentFailed(@RequestBody UUID paymentId) {
        log.info("paymentFailed: paymentId={}", paymentId);
        paymentService.paymentFailed(paymentId);
        log.info("paymentFailed: paymentId={} -> OK", paymentId);
    }
}