package vn.edu.fpt.cafemanagement.entities;

import jakarta.persistence.*;

import java.time.LocalDate;
import java.util.List;

@Entity
public class Voucher {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "voucher_id")
    private int voucherId;

    @Column(name = "voucher_name", nullable = false, length = 100)
    private String voucherName;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;

    @Column(name = "quantity", nullable = false)
    private int quantity;

    @Column(name = "discount_type", length = 20)
    private String discountType;

    @Column(name = "discount_value", nullable = false)
    private double discountValue;

    @Column(name = "min_order_value", nullable = false)
    private double minOrderValue;

    @Column(name = "code", nullable = false, length = 50)
    private String code;

    @Column(name = "is_active")
    private boolean isActive;

    @OneToMany(mappedBy = "voucher", cascade = CascadeType.ALL)
    private List<Order> orders;

    @Transient
    private String signature;

    public String getSignature() {
        return signature;
    }

    public void setSignature(String signature) {
        this.signature = signature;
    }

    public Voucher() {
    }

    public Voucher(int voucherId, String voucherName, LocalDate startDate, LocalDate endDate, int quantity,
                   String discountType, double discountValue, double minOrderValue, String code,
                   boolean isActive, List<Order> orders) {
        this.voucherId = voucherId;
        this.voucherName = voucherName;
        this.startDate = startDate;
        this.endDate = endDate;
        this.quantity = quantity;
        this.discountType = discountType;
        this.discountValue = discountValue;
        this.minOrderValue = minOrderValue;
        this.code = code;
        this.isActive = isActive;
        this.orders = orders;
    }

    public int getVoucherId() {
        return voucherId;
    }

    public void setVoucherId(int voucherId) {
        this.voucherId = voucherId;
    }

    public String getVoucherName() {
        return voucherName;
    }

    public void setVoucherName(String voucherName) {
        this.voucherName = voucherName;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public String getDiscountType() {
        return discountType;
    }

    public void setDiscountType(String discountType) {
        this.discountType = discountType;
    }

    public double getDiscountValue() {
        return discountValue;
    }

    public void setDiscountValue(double discountValue) {
        this.discountValue = discountValue;
    }

    public double getMinOrderValue() {
        return minOrderValue;
    }

    public void setMinOrderValue(double minOrderValue) {
        this.minOrderValue = minOrderValue;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        this.isActive = active;
    }

    public List<Order> getOrders() {
        return orders;
    }

    public void setOrders(List<Order> orders) {
        this.orders = orders;
    }
}
