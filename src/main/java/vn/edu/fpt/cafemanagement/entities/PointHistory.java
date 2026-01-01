package vn.edu.fpt.cafemanagement.entities;

import jakarta.persistence.*;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

@Entity
@Table(name="pointhistory")
public class PointHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "point_history_id")
    private int pointHistoryId;

    @ManyToOne
    @JoinColumn(name = "cus_id", nullable = false)
    private Customer customer;

    @Column(name = "type_of_change", length = 100)
    private String typeOfChange;

    @Column(name = "change_time")
    private LocalDateTime changeTime;

    @Column(name = "amount", nullable = false)
    private int amount;

    @ManyToOne
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    public PointHistory() {
    }

    public PointHistory(int pointHistoryId, Customer customer, String typeOfChange, LocalDateTime changeTime,
                        int amount, Order order) {
        this.pointHistoryId = pointHistoryId;
        this.customer = customer;
        this.typeOfChange = typeOfChange;
        this.changeTime = changeTime;
        this.amount = amount;
        this.order = order;
    }

    public int getPointHistoryId() {
        return pointHistoryId;
    }

    public void setPointHistoryId(int pointHistoryId) {
        this.pointHistoryId = pointHistoryId;
    }

    public Customer getCustomer() {
        return customer;
    }

    public void setCustomer(Customer customer) {
        this.customer = customer;
    }

    public String getTypeOfChange() {
        return typeOfChange;
    }

    public void setTypeOfChange(String typeOfChange) {
        this.typeOfChange = typeOfChange;
    }

    public LocalDateTime getChangeTime() {
        return changeTime;
    }

    public void setChangeTime(LocalDateTime changeTime) {
        this.changeTime = changeTime;
    }

    public int getAmount() {
        return amount;
    }

    public void setAmount(int amount) {
        this.amount = amount;
    }

    public Order getOrder() {
        return order;
    }

    public void setOrder(Order order) {
        this.order = order;
    }
}
