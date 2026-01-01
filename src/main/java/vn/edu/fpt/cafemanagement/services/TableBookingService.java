package vn.edu.fpt.cafemanagement.services;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.edu.fpt.cafemanagement.entities.Customer;
import vn.edu.fpt.cafemanagement.entities.TableBooking;
import vn.edu.fpt.cafemanagement.repositories.TableBookingRepository;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class TableBookingService {
    private final TableBookingRepository tableBookingRepository;

    public TableBookingService(TableBookingRepository tableBookingRepository) {
        this.tableBookingRepository = tableBookingRepository;
    }

    @Transactional
    public TableBooking createBooking(TableBooking tableBooking) {
        LocalDateTime bookingTime = tableBooking.getBookingTime();
        LocalDateTime now = LocalDateTime.now();

        int count = tableBookingRepository.countByCustomerAndDate(tableBooking.getCustomer().getCusId(), bookingTime.toLocalDate() );

        if(count > 2){
            throw new IllegalArgumentException("You are allowed to book 3 tables per day");
        }


        long diffMinutes = Duration.between(now, bookingTime).toMinutes();
        if (diffMinutes < 30) {
            throw new RuntimeException("Booking time must be 30+ minutes from now!");
        }

        if (diffMinutes > 120) {
            throw new RuntimeException("You can only book a table within 2 hours before your arrival");
        }

//        if (bookingTime.getHour() >= 22) {
//            throw new RuntimeException("You cannot book table after 22:00!");
//        }

        tableBooking.setStatus("pending");

        return tableBookingRepository.save(tableBooking);
    }

    @Transactional
    public TableBooking save(TableBooking tableBooking) {
        return tableBookingRepository.save(tableBooking);
    }

    public List<TableBooking> findByCustomerId(int customerId) {
        return tableBookingRepository.findByCustomer_CusIdOrderByBookingTimeDesc(customerId);
    }

    public TableBooking findById(int id) {
        return tableBookingRepository.findById(id).orElse(null);
    }

    public Page<TableBooking> findByCustomer_CusId(Integer customerId, Pageable pageable) {
        return tableBookingRepository.findByCustomer_CusIdOrderByBookingTimeDesc(customerId, pageable);
    }

    public Page<TableBooking> findAll(Pageable pageable) {
        return tableBookingRepository.findAll(pageable);
    }

    public Page<TableBooking> findTableBookingCustomer(Integer cusId, String status, LocalDateTime start, LocalDateTime end, Pageable pageable) {
        boolean hasDate = start != null && end != null;
        boolean hasStatus = status != null && !status.isBlank();

        if (!hasDate && !hasStatus) {
            return tableBookingRepository.findByCustomer_CusIdOrderByBookingTimeDesc(cusId, pageable);
        } else if (hasDate && !hasStatus) {
            return tableBookingRepository.findByCustomer_CusIdAndBookingTimeBetweenOrderByBookingTimeDesc(cusId, start, end, pageable);
        } else if (hasStatus && !hasDate) {
            return tableBookingRepository.findByCustomer_CusIdAndStatusOrderByBookingTimeDesc(cusId, status, pageable);
        } else{
            return tableBookingRepository.findByCustomer_CusIdAndStatusAndBookingTimeBetweenOrderByBookingTimeDesc(cusId, status, start, end, pageable);
        }
    }

    public Page<TableBooking> findTableBookingManager(String status, LocalDateTime start, LocalDateTime end, Pageable pageable) {
        boolean hasDate = start != null && end != null;
        boolean hasStatus = status != null && !status.isBlank();

        if (!hasDate && !hasStatus) {
            return tableBookingRepository.findAllByOrderByBookingTimeDesc(pageable);
        } else if (hasDate && !hasStatus) {
            return tableBookingRepository.findByBookingTimeBetweenOrderByBookingTimeDesc(start, end, pageable);
        } else if (hasStatus && !hasDate) {
            return tableBookingRepository.findByStatusOrderByBookingTimeDesc( status, pageable);
        } else{
            return tableBookingRepository.findByStatusAndBookingTimeBetweenOrderByBookingTimeDesc(status, start, end, pageable);
        }
    }

    public List<TableBooking> findActiveBookingByTable(int tableId){
        return tableBookingRepository.findActiveBookingByTable(tableId);
    }

    public TableBooking findActiveBookingByCustomer(Customer customer) {
        if (customer == null) {
            return null;
        }

        TableBooking checkedInBooking = tableBookingRepository.findFirstByCustomerCusIdAndStatus(customer.getCusId(), "checked-in");

        if (checkedInBooking != null) {
            return checkedInBooking;
        }

        return tableBookingRepository.findFirstByCustomerCusIdAndStatus(customer.getCusId(), "booked");
    }


}