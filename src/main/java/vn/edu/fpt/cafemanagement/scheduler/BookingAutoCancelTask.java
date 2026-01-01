package vn.edu.fpt.cafemanagement.scheduler;

import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import vn.edu.fpt.cafemanagement.entities.Table;
import vn.edu.fpt.cafemanagement.entities.TableBooking;
import vn.edu.fpt.cafemanagement.repositories.TableBookingRepository;
import vn.edu.fpt.cafemanagement.repositories.TableRepository;

import java.time.LocalDateTime;
import java.util.List;

@Component
@EnableScheduling
public class BookingAutoCancelTask {
    private TableBookingRepository bookingRepository;
    private TableRepository tableRepository;

    public BookingAutoCancelTask(TableBookingRepository bookingRepository, TableRepository tableRepository) {
        this.bookingRepository = bookingRepository;
        this.tableRepository = tableRepository;
    }

    @Scheduled(cron = "0 */1 * * * *")
    public void autoCancelBookings() {
        LocalDateTime now = LocalDateTime.now().minusMinutes(15);

        List<TableBooking> expiredBookings = bookingRepository
                .findExpiredBooking(now);
        Table table;
        System.out.println("Do AutoCanceled after 1 minutes");

        for (TableBooking booking : expiredBookings) {
            booking.setStatus("auto-canceled");
            table = booking.getTable();
            table.setStatus("available");
            tableRepository.save(table);
            bookingRepository.save(booking);
        }
    }
}
