package vn.edu.fpt.cafemanagement.entities;

import jakarta.persistence.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

@Entity
public class Customer implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "cus_id")
    private int cusId;

//Tăngj điểm cho khách
    @Column(name = "last_birthday_reward_year")
    private Integer lastBirthdayRewardYear;

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Column(name = "phone_number", length = 20)
    private String phoneNumber;

    @Column(name = "email", length = 100)
    private String email;

    @Column(name = "point", nullable = false)
    private Integer point = 0;

    @Column(name = "address")
    private String address;

    @Column(name = "password", nullable = false)
    private String password;

    @Column(name = "username", nullable = false, length = 100)
    private String username;

    @Column(name = "date_of_birth")
    private LocalDate dateOfBirth;

    @Column(name = "img")
    private String img;

    @Column(name = "failed_attempts", nullable = false)
    private int failedAttempts;

    @Column(name = "locked_until")
    private LocalDateTime lockedUntil;

    private boolean isGoogleAccount;

    @OneToMany(mappedBy = "customer", cascade = CascadeType.ALL)
    private List<Feedback> feedbacks;

    @OneToMany(mappedBy = "customer", cascade = CascadeType.ALL)
    private List<Order> orders;

    @OneToMany(mappedBy = "customer", cascade = CascadeType.ALL)
    private List<PointHistory> pointHistories;

    @OneToMany(mappedBy = "customer", cascade = CascadeType.ALL)
    private List<TableBooking> tableBookings;

    public Customer() {
    }

    public Customer(int cusId, String name, String phoneNumber, String email, int point, String address, String password
            , String username, LocalDate dateOfBirth, String img, int failedAttempts, LocalDateTime lockedUntil,
                    List<Feedback> feedbacks, List<Order> orders, List<PointHistory> pointHistories,
                    List<TableBooking> tableBookings) {
        this.cusId = cusId;
        this.name = name;
        this.phoneNumber = phoneNumber;
        this.email = email;
        this.point = point;
        this.address = address;
        this.password = password;
        this.username = username;
        this.dateOfBirth = dateOfBirth;
        this.img = img;
        this.failedAttempts = failedAttempts;
        this.lockedUntil = lockedUntil;
        this.feedbacks = feedbacks;
        this.orders = orders;
        this.pointHistories = pointHistories;
        this.tableBookings = tableBookings;
    }

    public Customer(int cusId, String name, String phoneNumber, String email, Integer point, String address, String password, String username, LocalDate dateOfBirth, String img, int failedAttempts, LocalDateTime lockedUntil, boolean isGoogleAccount, List<Feedback> feedbacks, List<Order> orders, List<PointHistory> pointHistories, List<TableBooking> tableBookings) {
        this.cusId = cusId;
        this.name = name;
        this.phoneNumber = phoneNumber;
        this.email = email;
        this.point = point;
        this.address = address;
        this.password = password;
        this.username = username;
        this.dateOfBirth = dateOfBirth;
        this.img = img;
        this.failedAttempts = failedAttempts;
        this.lockedUntil = lockedUntil;
        this.isGoogleAccount = isGoogleAccount;
        this.feedbacks = feedbacks;
        this.orders = orders;
        this.pointHistories = pointHistories;
        this.tableBookings = tableBookings;
    }

    public Integer getLastBirthdayRewardYear() {
        return lastBirthdayRewardYear;
    }

    public void setLastBirthdayRewardYear(Integer lastBirthdayRewardYear) {
        this.lastBirthdayRewardYear = lastBirthdayRewardYear;
    }

    public void setPoint(Integer point) {
        this.point = point;
    }

    public boolean isGoogleAccount() {
        return isGoogleAccount;
    }

    public void setGoogleAccount(boolean googleAccount) {
        isGoogleAccount = googleAccount;
    }

    public int getCusId() {
        return cusId;
    }

    public void setCusId(int cusId) {
        this.cusId = cusId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public int getPoint() {
        return point;
    }

    public void setPoint(int point) {
        this.point = point;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of();
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getUsername() {
        return username;
    }

    @Override
    public boolean isAccountNonExpired() {
        return UserDetails.super.isAccountNonExpired();
    }

    @Override
    public boolean isAccountNonLocked() {
        return UserDetails.super.isAccountNonLocked();
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return UserDetails.super.isCredentialsNonExpired();
    }

    @Override
    public boolean isEnabled() {
        return UserDetails.super.isEnabled();
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public LocalDate getDateOfBirth() {
        return dateOfBirth;
    }

    public void setDateOfBirth(LocalDate dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }

    public String getImg() {
        return img;
    }

    public void setImg(String img) {
        this.img = img;
    }

    public int getFailedAttempts() {
        return failedAttempts;
    }

    public void setFailedAttempts(int failedAttempts) {
        this.failedAttempts = failedAttempts;
    }

    public LocalDateTime getLockedUntil() {
        return lockedUntil;
    }

    public void setLockedUntil(LocalDateTime lockedUntil) {
        this.lockedUntil = lockedUntil;
    }

    public List<Feedback> getFeedbacks() {
        return feedbacks;
    }

    public void setFeedbacks(List<Feedback> feedbacks) {
        this.feedbacks = feedbacks;
    }

    public List<Order> getOrders() {
        return orders;
    }

    public void setOrders(List<Order> orders) {
        this.orders = orders;
    }

    public List<PointHistory> getPointHistories() {
        return pointHistories;
    }

    public void setPointHistories(List<PointHistory> pointHistories) {
        this.pointHistories = pointHistories;
    }

    public List<TableBooking> getTableBookings() {
        return tableBookings;
    }

    public void setTableBookings(List<TableBooking> tableBookings) {
        this.tableBookings = tableBookings;
    }
}
