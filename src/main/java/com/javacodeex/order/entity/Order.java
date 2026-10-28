package com.javacodeex.order.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "orders")
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String productCode;

    @Column(nullable = false)
    private Integer quantity;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal price;

    @Column(nullable = false)
    private String status;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    /**
     * Creates an empty order for JPA to populate when loading persisted data.
     */
    protected Order() {
    }

    /**
     * Creates an order whose identifier is assigned when it is persisted.
     *
     * @param productCode code identifying the ordered product
     * @param quantity number of units ordered
     * @param price price supplied for the order
     * @param status order processing status
     * @param createdAt local date and time when the order was created
     */
    public Order(String productCode, Integer quantity, BigDecimal price, String status, LocalDateTime createdAt) {
        this.productCode = productCode;
        this.quantity = quantity;
        this.price = price;
        this.status = status;
        this.createdAt = createdAt;
    }

    /**
     * Returns the database-generated order identifier.
     *
     * @return the identifier, or {@code null} before it has been assigned
     */
    public Long getId() { return id; }

    /**
     * Returns the code of the ordered product.
     *
     * @return the product code
     */
    public String getProductCode() { return productCode; }

    /**
     * Returns the number of units ordered.
     *
     * @return the order quantity
     */
    public Integer getQuantity() { return quantity; }

    /**
     * Returns the price stored for the order.
     *
     * @return the order price
     */
    public BigDecimal getPrice() { return price; }

    /**
     * Returns the order's processing status.
     *
     * @return the order status
     */
    public String getStatus() { return status; }

    /**
     * Returns the local date and time when the order was created.
     *
     * @return the creation timestamp
     */
    public LocalDateTime getCreatedAt() { return createdAt; }
}
