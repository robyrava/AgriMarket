package com.agrimarket.order.service;

import com.agrimarket.order.config.RabbitMQConfig;
import com.agrimarket.order.dto.OrderCancelledEvent;
import com.agrimarket.order.dto.OrderConfirmedEvent;
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
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderService {

    private final OrderRepository orderRepository;
    private final ShippingStrategyFactory shippingStrategyFactory;
    private final OutboxEventRepository outboxEventRepository;
    private final ObjectMapper objectMapper;

    @Transactional
    public Order createOrder(OrderCreateRequest request) {
        List<OrderItem> items = request.getItems().stream()
                .map(itemReq -> OrderItem.builder()
                        .productId(itemReq.getProductId())
                        .quantita(itemReq.getQuantita())
                        .prezzo(itemReq.getPrezzo())
                        .build())
                .collect(Collectors.toList());

        double totalItems = items.stream().mapToDouble(i -> i.getPrezzo() * i.getQuantita()).sum();

        ShippingStrategy shippingStrategy = shippingStrategyFactory.getStrategy(request.getTipoSpedizione().toUpperCase());

        Order tempOrder = Order.builder()
                .items(items)
                .build();
        double shippingCost = shippingStrategy.calculateCost(tempOrder);

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

        savedOrder.nextState();
        orderRepository.save(savedOrder);

        try {
            OrderConfirmedEvent confirmedEvent = new OrderConfirmedEvent(savedOrder.getId());
            OutboxEvent outboxEvent = OutboxEvent.builder()
                    .aggregateType("Order")
                    .aggregateId(savedOrder.getId().toString())
                    .type("OrderConfirmed")
                    .routingKey(RabbitMQConfig.ROUTING_KEY_CONFIRMED)
                    .payload(objectMapper.writeValueAsString(confirmedEvent))
                    .createdAt(LocalDateTime.now())
                    .processed(false)
                    .build();

            outboxEventRepository.save(outboxEvent);
            log.info("Order {} created and confirmed, outbox event saved", savedOrder.getId());
        } catch (Exception e) {
            throw new RuntimeException("Error serializing order confirmed event", e);
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
        String previousStatus = order.getStato().name();

        if ("next".equalsIgnoreCase(action)) {
            order.nextState();
        } else if ("cancel".equalsIgnoreCase(action)) {
            order.cancel();
        }

        order = orderRepository.save(order);

        if (order.getStato() == OrderStatus.CANCELLED && !"CANCELLED".equals(previousStatus)) {
            try {
                OrderCancelledEvent cancelledEvent = new OrderCancelledEvent(order.getId(), "Cancelled by request");
                OutboxEvent outboxEvent = OutboxEvent.builder()
                        .aggregateType("Order")
                        .aggregateId(order.getId().toString())
                        .type("OrderCancelled")
                        .routingKey(RabbitMQConfig.ROUTING_KEY_CANCELLED)
                        .payload(objectMapper.writeValueAsString(cancelledEvent))
                        .createdAt(LocalDateTime.now())
                        .processed(false)
                        .build();
                outboxEventRepository.save(outboxEvent);
                log.info("Order {} cancelled, outbox event saved", order.getId());
            } catch (Exception e) {
                log.error("Error serializing order cancelled event", e);
            }
        }

        return order;
    }
}
