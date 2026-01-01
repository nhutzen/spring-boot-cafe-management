package vn.edu.fpt.cafemanagement.services;

import org.springframework.stereotype.Service;
import vn.edu.fpt.cafemanagement.entities.Table;
import vn.edu.fpt.cafemanagement.entities.TableBooking;
import vn.edu.fpt.cafemanagement.repositories.TableBookingRepository;
import vn.edu.fpt.cafemanagement.repositories.TableRepository;

import java.util.List;

@Service
public class TableService {
    private final TableRepository tableRepository;
    private final TableBookingRepository tableBookingRepository;

    public TableService(TableRepository tableRepository, TableBookingRepository tableBookingRepository) {
        this.tableRepository = tableRepository;
        this.tableBookingRepository = tableBookingRepository;
    }

    public List<Table> getTablesList() {
        return tableRepository.getTablesList();
    }

    public Table findById(int id) {
        return tableRepository.findById(id).orElse(null);
    }

    public void updateTableStatus(int id,String status){
        Table table = tableRepository.findById(id).orElse(null);
        table.setStatus(status);
        tableRepository.save(table);

        System.out.println("status: " + status);

        if ("available".equals(status)){
            autoCompleteBookingOfTable(table.getTableId());
        }
    }

    public void autoCompleteBookingOfTable (int tableId){
        List<TableBooking> bookings = tableBookingRepository.findActiveBookingByTable(tableId);

//        if (booking != null &&
//                (booking.getStatus().equalsIgnoreCase("CHECKED-IN"))) {
//
//            booking.setStatus("completed");
////            booking.setCheckoutTime(LocalDateTime.now());
//            tableBookingRepository.save(booking);
        for (TableBooking booking : bookings) {
            booking.setStatus("completed");
            tableBookingRepository.save(booking);
        }
    }

    public List<Integer> getCapacityList() {
        return tableRepository.capacityList();
    }

    public List<Table> getAvailableTables(){
        return tableRepository.getTableByStatus("available");
    }


    public List<Table> getOccupiedTables(){
        return tableRepository.getTableByStatus("occupied");
    }

    public void save(Table table){
        tableRepository.save(table);
    }


}
