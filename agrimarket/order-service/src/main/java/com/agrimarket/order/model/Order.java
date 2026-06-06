package com.agrimarket.order.model;

import com.agrimarket.order.model.state.*;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "orders")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "customer_id", nullable = false)
    private Long customerId;

    @Column(nullable = false)
    private Double totale;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrderStatus stato;

    @Column(name = "tipo_spedizione")
    private String tipoSpedizione;

    @Column(name = "costo_spedizione")
    private Double costoSpedizione;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @JoinColumn(name = "order_id")
    @Builder.Default
    private List<OrderItem> items = new ArrayList<>();

    @Transient
    private OrderState currentState;

    @PostLoad
    private void init() {
        if (stato != null) {
            this.currentState = StateFactory.getState(this.stato);
        } else {
            this.currentState = new PendingState();
            this.stato = OrderStatus.PENDING;
        }
    }

    public void setState(OrderState state) {
        this.currentState = state;
        this.stato = state.getStatus();
    }

    // State Pattern Delegation
    public void nextState() {
        if (currentState == null) init();
        currentState.next(this);
    }

    public void cancel() {
        if (currentState == null) init();
        currentState.cancel(this);
    }
}
