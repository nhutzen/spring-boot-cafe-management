package vn.edu.fpt.cafemanagement.entities;

import jakarta.persistence.*;
import jakarta.persistence.Table;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "[Order]")
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "order_id")
    private int orderId;

    @ManyToOne
    @JoinColumn(name = "cus_id")
    private Customer customer;

    @ManyToOne
    @JoinColumn(name = "staff_id", nullable = false)
    private Staff staff;

    //Canceled, Served, Pending, Paid
    @Column(name = "status", length = 20)
    private String status;

    @Column(name = "total_price", nullable = false)
    private double totalPrice;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "points_used")
    private int pointsUsed;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @ManyToOne
    @JoinColumn(name = "made_by", referencedColumnName = "staff_id")
    private Staff madeBy;

    @ManyToOne
    @JoinColumn(name = "served_by", referencedColumnName = "staff_id")
    private Staff servedBy;

    @ManyToOne
    @JoinColumn(name = "voucher_id") //nên bỏ nullable = false, vì có thể order nào đó ko có voucher
    private Voucher voucher;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL)
    private List<OrderItem> orderItems;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL)
    private List<PointHistory> pointHistories;

    @ManyToOne
    @JoinColumn(name = "table_id")
    private vn.edu.fpt.cafemanagement.entities.Table table;

    public Order() {
    }

    public Order(int orderId, Customer customer, Staff staff, String status, double totalPrice, LocalDateTime createdAt,
                 int pointsUsed, LocalDateTime updatedAt, Staff madeBy, Staff servedBy,Voucher voucher, List<OrderItem> orderItems,
                 List<PointHistory> pointHistories, vn.edu.fpt.cafemanagement.entities.Table table) {
        this.orderId = orderId;
        this.customer = customer;
        this.staff = staff;
        this.status = status;
        this.totalPrice = totalPrice;
        this.createdAt = createdAt;
        this.pointsUsed = pointsUsed;
        this.updatedAt = updatedAt;
        this.madeBy = madeBy;
        this.servedBy = servedBy;
        this.voucher = voucher;
        this.orderItems = orderItems;
        this.pointHistories = pointHistories;
        this.table = table;
    }

    public int getOrderId() {
        return orderId;
    }

    public void setOrderId(int orderId) {
        this.orderId = orderId;
    }

    public Customer getCustomer() {
        return customer;
    }

    public void setCustomer(Customer customer) {
        this.customer = customer;
    }

    public Staff getStaff() {
        return staff;
    }

    public void setStaff(Staff staff) {
        this.staff = staff;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public double getTotalPrice() {
        return totalPrice;
    }

    public void setTotalPrice(double totalPrice) {
        this.totalPrice = totalPrice;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public int getPointsUsed() {
        return pointsUsed;
    }

    public void setPointsUsed(int pointsUsed) {
        this.pointsUsed = pointsUsed;
    }

    public Voucher getVoucher() {
        return voucher;
    }

    public void setVoucher(Voucher voucher) {
        this.voucher = voucher;
    }

    public List<OrderItem> getOrderItems() {
        return orderItems;
    }

    public void setOrderItems(List<OrderItem> orderItems) {
        this.orderItems = orderItems;
    }

    public List<PointHistory> getPointHistories() {
        return pointHistories;
    }

    public void setPointHistories(List<PointHistory> pointHistories) {
        this.pointHistories = pointHistories;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public Staff getMadeBy() {
        return madeBy;
    }
    public void setMadeBy(Staff madeBy) {
        this.madeBy = madeBy;
    }

    public Staff getServedBy() {
        return servedBy;
    }

    public void setServedBy(Staff servedBy) {
        this.servedBy = servedBy;
    }

    public vn.edu.fpt.cafemanagement.entities.Table getTable() {
        return table;
    }

    public void setTable(vn.edu.fpt.cafemanagement.entities.Table table) {
        this.table = table;
    }
}