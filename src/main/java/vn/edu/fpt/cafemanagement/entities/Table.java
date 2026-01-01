
package vn.edu.fpt.cafemanagement.entities;

import jakarta.persistence.*;


import java.util.List;

@Entity
@jakarta.persistence.Table(name = "[Table]")
public class Table {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "table_id")
    private int tableId;


    //Reserved, Available, Occupied
    //Bàn đc khách đặt trước sẽ set là Reserved
    //Sau 2 tiếng khách không tới sẽ set lại là Available
    //Khách tới thì sẽ set là Occupied
    @Column(name = "status", length = 20)
    private String status;


    private int capacity;

    @OneToMany(mappedBy = "table")
    private List<TableBooking> tableBookings;

    @OneToMany(mappedBy = "table")
    List<Order> orders;

    public Table() {
    }

    public Table(int tableId, String status, int capacity, List<TableBooking> tableBookings) {
        this.tableId = tableId;
        this.status = status;
        this.capacity = capacity;
        this.tableBookings = tableBookings;
    }

    public int getTableId() {
        return tableId;
    }

    public void setTableId(int tableId) {
        this.tableId = tableId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public List<TableBooking> getTableBookings() {
        return tableBookings;
    }

    public void setTableBookings(List<TableBooking> tableBookings) {
        this.tableBookings = tableBookings;
    }

    public int getCapacity() {
        return capacity;
    }

    public Table(int tableId, String status, int capacity, List<TableBooking> tableBookings, List<Order> orders) {
        this.tableId = tableId;
        this.status = status;
        this.capacity = capacity;
        this.tableBookings = tableBookings;
        this.orders = orders;
    }

    public List<Order> getOrders() {
        return orders;
    }

    public void setOrders(List<Order> orders) {
        this.orders = orders;
    }

    public void setCapacity(int capacity) {
        this.capacity = capacity;
    }
}
