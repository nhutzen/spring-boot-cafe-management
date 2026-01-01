package vn.edu.fpt.cafemanagement.entities;

import jakarta.persistence.*;

import java.util.List;

@Entity
public class Category {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "cate_id")
    private int cateId;

    @Column(name = "cate_name", nullable = false, length = 100)
    private String cateName;

    @Column(name = "is_active")
    boolean isActive;

    @OneToMany(mappedBy = "category", cascade = CascadeType.ALL)
    private List<Product> products;

    public Category() {
    }

    public Category(int cateId, String cateName, boolean isActive, List<Product> products) {
        this.cateId = cateId;
        this.cateName = cateName;
        this.isActive = isActive;
        this.products = products;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }

    public int getCateId() {
        return cateId;
    }

    public void setCateId(int cateId) {
        this.cateId = cateId;
    }

    public String getCateName() {
        return cateName;
    }

    public void setCateName(String cateName) {
        this.cateName = cateName;
    }

    public List<Product> getProducts() {
        return products;
    }

    public void setProducts(List<Product> products) {
        this.products = products;
    }
}
