package com.agrimarket.order.service.shipping;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class ShippingStrategyFactory {

    private final Map<String, ShippingStrategy> strategies;

    @Autowired
    public ShippingStrategyFactory(Map<String, ShippingStrategy> strategies) {
        this.strategies = strategies;
    }

    public ShippingStrategy getStrategy(String type) {
        ShippingStrategy strategy = strategies.get(type);
        if (strategy == null) {
            throw new IllegalArgumentException("Unknown shipping strategy type: " + type);
        }
        return strategy;
    }
}
