package vn.edu.fpt.cafemanagement.dto;

import java.time.LocalDate;


public class ShiftScheduleDTO {
    private String shiftPeriod; // Morning / Afternoon / Evening
    private String roleName;    // Waiter / Barista / Cashier
    private LocalDate shiftDate;     // 2025-06-01 ...
    private String managerName; // A / B / C

    public ShiftScheduleDTO(String shiftPeriod, String roleName, LocalDate date, String managerName) {
        this.shiftPeriod = shiftPeriod;
        this.roleName = roleName;
        this.shiftDate = date;
        this.managerName = managerName;
    }

    public ShiftScheduleDTO() {
    }

    public String getShiftPeriod() {
        return shiftPeriod;
    }

    public void setShiftPeriod(String shiftPeriod) {
        this.shiftPeriod = shiftPeriod;
    }

    public String getRoleName() {
        return roleName;
    }

    public void setRoleName(String roleName) {
        this.roleName = roleName;
    }

    public LocalDate getShiftDate() {
        return shiftDate;
    }

    public void setShiftDate(LocalDate shiftDate) {
        this.shiftDate = shiftDate;
    }

    public String getManagerName() {
        return managerName;
    }

    public void setManagerName(String managerName) {
        this.managerName = managerName;
    }
}
