package ru.yandex.practicum.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.commerce.cart.dto.ShoppingCartDto;
import ru.yandex.practicum.commerce.delivery.dto.DeliveryDto;
import ru.yandex.practicum.commerce.order.dto.CreateNewOrderRequest;
import ru.yandex.practicum.commerce.order.dto.OrderDto;
import ru.yandex.practicum.commerce.order.dto.ProductReturnRequest;
import ru.yandex.practicum.commerce.order.enums.OrderState;
import ru.yandex.practicum.commerce.payment.dto.PaymentDto;
import ru.yandex.practicum.commerce.warehouse.dto.AcceptReturnRequest;
import ru.yandex.practicum.commerce.warehouse.dto.AddressDto;
import ru.yandex.practicum.commerce.warehouse.dto.AssemblyProductsForOrderRequest;
import ru.yandex.practicum.commerce.warehouse.dto.BookedProductsDto;
import ru.yandex.practicum.controller.DeliveryFeignClient;
import ru.yandex.practicum.controller.PaymentFeignClient;
import ru.yandex.practicum.controller.WarehouseFeignClient;
import ru.yandex.practicum.error.NotAuthorizedUserException;
import ru.yandex.practicum.mapper.OrderCreateMapper;
import ru.yandex.practicum.mapper.OrderDeliveryMapper;
import ru.yandex.practicum.mapper.OrderItemFactory;
import ru.yandex.practicum.mapper.OrderMapper;
import ru.yandex.practicum.model.Order;
import ru.yandex.practicum.model.OrderItemEntity;
import ru.yandex.practicum.repository.OrderRepository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final OrderMapper orderMapper;
    private final OrderItemFactory orderItemFactory;
    private final OrderCreateMapper orderCreateMapper;
    private final OrderDeliveryMapper orderDeliveryMapper;
    private final WarehouseFeignClient warehouseClient;
    private final DeliveryFeignClient deliveryClient;
    private final PaymentFeignClient paymentClient;

    @Transactional(readOnly = true)
    public List<OrderDto> getClientOrders(String username) {
        log.info("ENTER getClientOrders: username={}", username);

        if (username == null || username.isBlank()) {
            log.warn("getClientOrders: username is blank");
            throw new NotAuthorizedUserException("Username must not be blank");
        }

        List<Order> orders = orderRepository.findAllByUsernameOrderByCreatedAtDesc(username);

        List<OrderDto> result = orders.stream()
                .map(orderMapper::toDto)
                .toList();
        log.info("EXIT getClientOrders: username={}, orders={}", username, result.size());
        return result;
    }

    @Transactional
    public OrderDto createNewOrder(CreateNewOrderRequest req) {
        String username = "plug";

        ShoppingCartDto cart = req.getShoppingCart();
        UUID cartId = cart.getShoppingCartId();
        Map<UUID, Long> products = cart.getProducts();
        int itemsCount = products == null ? 0 : products.size();

        log.info("createNewOrder: username={}, cartId={}, items={}", username, cartId, itemsCount);

        BookedProductsDto booked = warehouseClient.checkProductQuantityEnoughForShoppingCart(cart);

        Order header = orderCreateMapper.toEntity(req, username);
        header.setDeliveryWeight(booked.getDeliveryWeight());
        header.setDeliveryVolume(booked.getDeliveryVolume());
        header.setFragile(booked.getFragile());

        Order savedHeader = orderRepository.save(header);

        Set<OrderItemEntity> items = orderItemFactory.buildItems(products, savedHeader);
        savedHeader.setItems(items);

        Order savedFull = orderRepository.save(savedHeader);

        AddressDto fromAddress = warehouseClient.getWarehouseAddress();

        DeliveryDto deliveryRequest = orderDeliveryMapper.toPlanDeliveryRequest(savedFull, req, fromAddress);

        DeliveryDto createdDelivery = deliveryClient.planDelivery(deliveryRequest);

        savedFull.setDeliveryId(createdDelivery.getDeliveryId());
        savedFull.setStatus(OrderState.ON_PAYMENT);

        Order savedFinal = orderRepository.save(savedFull);

        OrderDto dto = orderMapper.toDto(savedFinal);

        log.info("createNewOrder: orderId={}, status={}, deliveryId={}",
                dto.getOrderId(), dto.getState(), dto.getDeliveryId());

        return dto;
    }

    @Transactional
    public OrderDto assembly(UUID orderId) {
        log.info("assembly: orderId={}", orderId);

        Order order = getOrderOrThrow(orderId);

        ensureStatus(order, OrderState.PAID);

        Map<UUID, Long> products = orderMapper.toProductsMap(order.getItems());

        AssemblyProductsForOrderRequest request = new AssemblyProductsForOrderRequest(orderId, products);

        BookedProductsDto booked = warehouseClient.assemblyProductsForOrder(request);

        order.setDeliveryWeight(booked.getDeliveryWeight());
        order.setDeliveryVolume(booked.getDeliveryVolume());
        order.setFragile(booked.getFragile());

        order.setStatus(OrderState.ASSEMBLED);

        Order saved = orderRepository.save(order);
        OrderDto dto = orderMapper.toDto(saved);

        log.info("assembly: orderId={}, state={}", dto.getOrderId(), dto.getState());
        return dto;
    }

    @Transactional
    public OrderDto assemblyFailed(UUID orderId) {
        log.info("assemblyFailed: orderId={}", orderId);

        Order order = getOrderOrThrow(orderId);
        order.setStatus(OrderState.ASSEMBLY_FAILED);

        OrderDto dto = orderMapper.toDto(orderRepository.save(order));

        log.info("assemblyFailed: orderId={}, state={}", dto.getOrderId(), dto.getState());
        return dto;
    }

    @Transactional
    public OrderDto calculateDeliveryCost(UUID orderId) {
        log.info("calculateDeliveryCost: orderId={}", orderId);

        Order order = getOrderOrThrow(orderId);

        OrderDto dtoForCalc = orderMapper.toDto(order);
        BigDecimal deliveryCost = deliveryClient.deliveryCost(dtoForCalc);

        order.setDeliveryPrice(deliveryCost);
        Order saved = orderRepository.save(order);

        OrderDto dto = orderMapper.toDto(saved);

        log.info("calculateDeliveryCost: orderId={}, deliveryPrice={}", dto.getOrderId(), dto.getDeliveryPrice());
        return dto;
    }

    @Transactional
    public OrderDto calculateTotalCost(UUID orderId) {
        log.info("calculateTotalCost: orderId={}", orderId);

        Order order = getOrderOrThrow(orderId);
        OrderDto dtoForCalc = orderMapper.toDto(order);
        BigDecimal productCost = paymentClient.productCost(dtoForCalc);

        BigDecimal deliveryCost = order.getDeliveryPrice() != null
                ? order.getDeliveryPrice()
                : deliveryClient.deliveryCost(dtoForCalc);

        OrderDto dtoForTotal = orderMapper.toDto(order);
        dtoForTotal.setProductPrice(productCost);
        dtoForTotal.setDeliveryPrice(deliveryCost);

        BigDecimal total = paymentClient.getTotalCost(dtoForTotal);

        order.setProductPrice(productCost);
        order.setDeliveryPrice(deliveryCost);
        order.setTotalPrice(total);

        Order saved = orderRepository.save(order);
        OrderDto dto = orderMapper.toDto(saved);

        log.info("calculateTotalCost: orderId={}, totalPrice={}", dto.getOrderId(), dto.getTotalPrice());
        return dto;
    }

    @Transactional
    public OrderDto payment(UUID orderId) {
        log.info("payment: orderId={}", orderId);

        Order order = getOrderOrThrow(orderId);
        ensureStatus(order, OrderState.ON_PAYMENT);

        OrderDto dtoForPayment = orderMapper.toDto(order);

        PaymentDto payment = paymentClient.payment(dtoForPayment);
        order.setPaymentId(payment.getPaymentId()); // [ПРОВЕРЬ поле paymentId]

        order.setStatus(OrderState.PAID);
        Order saved = orderRepository.save(order);

        OrderDto dto = orderMapper.toDto(saved);

        log.info("payment: orderId={}, state={}, paymentId={}",
                dto.getOrderId(), dto.getState(), dto.getPaymentId());

        return dto;
    }

    @Transactional
    public OrderDto paymentFailed(UUID orderId) {
        log.info("paymentFailed: orderId={}", orderId);

        Order order = getOrderOrThrow(orderId);
        order.setStatus(OrderState.PAYMENT_FAILED);

        OrderDto dto = orderMapper.toDto(orderRepository.save(order));

        log.info("paymentFailed: orderId={}, state={}", dto.getOrderId(), dto.getState());
        return dto;
    }

    @Transactional
    public OrderDto delivery(UUID orderId) {
        log.info("delivery: orderId={}", orderId);

        Order order = getOrderOrThrow(orderId);
        ensureStatus(order, OrderState.ON_DELIVERY);

        deliveryClient.deliverySuccess(orderId); // [ИЗМЕНЕНИЕ] сообщаем delivery-сервису

        order.setStatus(OrderState.DELIVERED);
        OrderDto dto = orderMapper.toDto(orderRepository.save(order));

        log.info("delivery: orderId={}, state={}", dto.getOrderId(), dto.getState());
        return dto;
    }

    @Transactional
    public OrderDto deliveryFailed(UUID orderId) {
        log.info("deliveryFailed: orderId={}", orderId);

        Order order = getOrderOrThrow(orderId);

        deliveryClient.deliveryFailed(orderId); // [ИЗМЕНЕНИЕ] сообщаем delivery-сервису

        order.setStatus(OrderState.DELIVERY_FAILED);

        OrderDto dto = orderMapper.toDto(orderRepository.save(order));

        log.info("deliveryFailed: orderId={}, state={}", dto.getOrderId(), dto.getState());
        return dto;
    }

    @Transactional
    public OrderDto productReturn(ProductReturnRequest request) {
        UUID orderId = request.getOrderId();
        int items = request.getProducts() == null ? 0 : request.getProducts().size();

        log.info("productReturn: orderId={}, items={}", orderId, items);

        Order order = getOrderOrThrow(orderId);

        // Возврат на склад
        AcceptReturnRequest warehouseRequest = new AcceptReturnRequest(request.getProducts());
        warehouseClient.acceptReturn(warehouseRequest);

        order.setStatus(OrderState.PRODUCT_RETURNED);

        OrderDto dto = orderMapper.toDto(orderRepository.save(order));

        log.info("productReturn: orderId={}, state={}", dto.getOrderId(), dto.getState());
        return dto;
    }

    @Transactional
    public OrderDto complete(UUID orderId) {
        log.info("complete: orderId={}", orderId);

        Order order = getOrderOrThrow(orderId);

        ensureStatus(order, OrderState.DELIVERED);

        order.setStatus(OrderState.COMPLETED);

        OrderDto dto = orderMapper.toDto(orderRepository.save(order));

        log.info("complete: orderId={}, state={}", dto.getOrderId(), dto.getState());
        return dto;
    }

    private Order getOrderOrThrow(UUID orderId) {
        return orderRepository.findWithItemsByOrderId(orderId) // [ИЗМЕНЕНИЕ]
                .orElseThrow(() -> new IllegalArgumentException("NoOrderFound: " + orderId));
    }

    private void ensureStatus(Order order, OrderState expected) {
        if (order.getStatus() != expected) {
            throw new IllegalStateException("Order state must be " + expected + ", actual=" + order.getStatus());
        }
    }

}
