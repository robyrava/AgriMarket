package com.agrimarket.order.service.shipping;

import com.agrimarket.order.model.Order;
import org.springframework.stereotype.Component;

@Component("STANDARD")
public class StandardShippingStrategy implements ShippingStrategy {
    @Override
    public double calculateCost(Order order) {
        return 5.00 + (order.getItems().size() * 1.00); // Base cost 5 + 1 per item
    }
}
