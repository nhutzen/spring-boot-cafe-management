package vn.edu.fpt.cafemanagement.entities;

import jakarta.persistence.*;
import jakarta.persistence.Table;

@Entity
@Table(name = "[orderitem]")
public class OrderItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "orderitem_id")
    private int orderItemId;

    @ManyToOne
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @ManyToOne
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(name = "quantity", nullable = false)
    private int quantity;

    @Column(name = "note")
    private String note;


    private double unitPrice;


    public OrderItem() {
    }

    public OrderItem(int orderItemId, Order order, Product product, int quantity, String note) {
        this.orderItemId = orderItemId;
        this.order = order;
        this.product = product;
        this.quantity = quantity;
        this.note = note;
    }

    public OrderItem(int orderItemId, Order order, Product product, int quantity, String note, double unitPrice) {
        this.orderItemId = orderItemId;
        this.order = order;
        this.product = product;
        this.quantity = quantity;
        this.note = note;
        this.unitPrice = unitPrice;
    }

    public int getOrderItemId() {
        return orderItemId;
    }

    public void setOrderItemId(int orderItemId) {
        this.orderItemId = orderItemId;
    }

    public Order getOrder() {
        return order;
    }

    public void setOrder(Order order) {
        this.order = order;
    }

    public Product getProduct() {
        return product;
    }

    public void setProduct(Product product) {
        this.product = product;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public double getUnitPrice() {
        return unitPrice;
    }

    public void setUnitPrice(double unitPrice) {
        this.unitPrice = unitPrice;
    }
}
