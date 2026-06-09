package com.agrimarket.order.service;

import com.agrimarket.order.dto.OrderCreateRequest;
import com.agrimarket.order.dto.OrderCreatedEvent;
import com.agrimarket.order.model.Order;
import com.agrimarket.order.model.OrderItem;
import com.agrimarket.order.model.OutboxEvent;
import com.agrimarket.order.model.state.OrderStatus;
import com.agrimarket.order.model.state.StateFactory;
import com.agrimarket.order.repository.OrderRepository;
import com.agrimarket.order.repository.OutboxEventRepository;
import com.agrimarket.order.service.shipping.ShippingStrategy;
import com.agrimarket.order.service.shipping.ShippingStrategyFactory;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final ShippingStrategyFactory shippingStrategyFactory;
    private final OutboxEventRepository outboxEventRepository;
    private final ObjectMapper objectMapper;

    @Transactional
    public Order createOrder(OrderCreateRequest request) {
        // Builder Pattern for OrderItem
        List<OrderItem> items = request.getItems().stream()
                .map(itemReq -> OrderItem.builder()
                        .productId(itemReq.getProductId())
                        .quantita(itemReq.getQuantita())
                        .prezzo(itemReq.getPrezzo())
                        .build())
                .collect(Collectors.toList());

        double totalItems = items.stream().mapToDouble(i -> i.getPrezzo() * i.getQuantita()).sum();

        // Strategy Pattern for Shipping Calculation
        ShippingStrategy shippingStrategy = shippingStrategyFactory.getStrategy(request.getTipoSpedizione().toUpperCase());

        // We create a temporary order to calculate shipping if shipping strategy requires it
        Order tempOrder = Order.builder()
                .items(items)
                .build();
        double shippingCost = shippingStrategy.calculateCost(tempOrder);

        // Builder Pattern for Order
        Order newOrder = Order.builder()
                .customerId(request.getCustomerId())
                .items(items)
                .totale(totalItems + shippingCost)
                .tipoSpedizione(request.getTipoSpedizione().toUpperCase())
                .costoSpedizione(shippingCost)
                .stato(OrderStatus.PENDING)
                .currentState(StateFactory.getState(OrderStatus.PENDING))
                .build();

        Order savedOrder = orderRepository.save(newOrder);

        // Transactional Outbox Pattern
        try {
            List<OrderCreatedEvent.OrderItemInfo> itemInfos = savedOrder.getItems().stream()
                    .map(item -> OrderCreatedEvent.OrderItemInfo.builder()
                            .productId(item.getProductId())
                            .quantita(item.getQuantita())
                            .build())
                    .collect(Collectors.toList());

            OrderCreatedEvent eventPayload = OrderCreatedEvent.builder()
                    .orderId(savedOrder.getId())
                    .customerId(savedOrder.getCustomerId())
                    .totale(savedOrder.getTotale())
                    .items(itemInfos)
                    .build();

            OutboxEvent outboxEvent = OutboxEvent.builder()
                    .aggregateType("Order")
                    .aggregateId(savedOrder.getId().toString())
                    .type("OrderCreated")
                    .payload(objectMapper.writeValueAsString(eventPayload))
                    .createdAt(LocalDateTime.now())
                    .processed(false)
                    .build();

            outboxEventRepository.save(outboxEvent);
        } catch (Exception e) {
            throw new RuntimeException("Error serializing order outbox event", e);
        }

        return savedOrder;
    }
    
    @Transactional(readOnly = true)
    public Order getOrder(Long id) {
        return orderRepository.findById(id).orElseThrow(() -> new RuntimeException("Order not found"));
    }
    
    @Transactional
    public Order updateOrderState(Long id, String action) {
        Order order = getOrder(id);
        if ("next".equalsIgnoreCase(action)) {
            order.nextState();
        } else if ("cancel".equalsIgnoreCase(action)) {
            order.cancel();
        }
        return orderRepository.save(order);
    }
}
