package com.agrimarket.order.model.state;

public class StateFactory {
    public static OrderState getState(OrderStatus status) {
        if (status == null) return new PendingState();
        return switch (status) {
            case PENDING -> new PendingState();
            case RESERVED -> new ReservedState();
            case CONFIRMED -> new ConfirmedState();
            case CANCELLED -> new CancelledState();
        };
    }
}
