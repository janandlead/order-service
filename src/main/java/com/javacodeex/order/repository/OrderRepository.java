package com.javacodeex.order.repository;

import com.javacodeex.order.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Provides inherited JPA persistence operations for orders identified by {@link Long} IDs.
 */
public interface OrderRepository extends JpaRepository<Order, Long> {
}
