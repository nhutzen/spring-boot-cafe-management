package vn.edu.fpt.cafemanagement.entities;

import jakarta.persistence.*;
import jakarta.persistence.Table;

import java.util.List;

@Entity
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "pro_id")
    private int proId;

    @Column(name = "pro_name", nullable = false, length = 100)
    private String proName;

    @Column(name = "img")
    private String img;

    @ManyToOne
    @JoinColumn(name = "cate_id", nullable = false)
    private Category category;

    @Column(name = "price", nullable = false) //Decimal(10,2), PATTERN = ###,###
    private double price;

    @Column(name = "status", length = 20)
    private String status;

    @Column(name = "description", length = 500)
    private String description;

    @Column(name = "is_active")
    private boolean isActive;

    private int quantity;
    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL)
    private List<Feedback> feedbacks;

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL)
    private List<OrderItem> orderItems;

    public Product() {
    }

//    public Product(int proId, String proName, String img, Category category, double price, String status,
//                   String description, boolean isActive, List<Feedback> feedbacks, List<OrderItem> orderItems) {
//        this.proId = proId;
//        this.proName = proName;
//        this.img = img;
//        this.category = category;
//        this.price = price;
//        this.status = status;
//        this.description = description;
//        this.isActive = isActive;
//        this.feedbacks = feedbacks;
//        this.orderItems = orderItems;
//    }

    public Product(int proId, String proName, String img, Category category, double price, String status, String description, boolean isActive, int quantity, List<Feedback> feedbacks, List<OrderItem> orderItems) {
        this.proId = proId;
        this.proName = proName;
        this.img = img;
        this.category = category;
        this.price = price;
        this.status = status;
        this.description = description;
        this.isActive = isActive;
        this.quantity = quantity;
        this.feedbacks = feedbacks;
        this.orderItems = orderItems;
    }

    public int getProId() {
        return proId;
    }

    public void setProId(int proId) {
        this.proId = proId;
    }

    public String getProName() {
        return proName;
    }

    public void setProName(String proName) {
        this.proName = proName;
    }

    public String getImg() {
        return img;
    }

    public void setImg(String img) {
        this.img = img;
    }

    public Category getCategory() {
        return category;
    }

    public void setCategory(Category category) {
        this.category = category;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }

    public List<Feedback> getFeedbacks() {
        return feedbacks;
    }

    public void setFeedbacks(List<Feedback> feedbacks) {
        this.feedbacks = feedbacks;
    }

    public List<OrderItem> getOrderItems() {
        return orderItems;
    }

    public void setOrderItems(List<OrderItem> orderItems) {
        this.orderItems = orderItems;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }


}
