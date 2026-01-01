package vn.edu.fpt.cafemanagement.services;

import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import vn.edu.fpt.cafemanagement.entities.Banner;
import vn.edu.fpt.cafemanagement.entities.Staff;
import vn.edu.fpt.cafemanagement.repositories.StaffRepository;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
public class StaffService {
    private final StaffRepository staffRepository;

    public StaffService(StaffRepository staffRepository) {
        this.staffRepository = staffRepository;
    }

    public List<Staff> findAllStaffs() {
        return staffRepository.findAll();
    }


    public List<Staff> getList() {
        return staffRepository.findAll();
    }

    public Staff findById(int artistId) {
        return staffRepository.findById(artistId).orElse(null);
    }

    @Transactional
    //Create, Update
    public Staff save(Staff staff) {
        return staffRepository.save(staff);
    }

    public Staff findByUsername(String username) {
        return staffRepository.findByUsername(username).orElse(null);
    }


    public Staff findByEmail(String email) {
        return staffRepository.findByEmail(email).orElse(null);
    }

    public Staff saveManager(Staff staff) {
        return staffRepository.save(staff);
    }

    @Transactional
    public void deleteById(int id) {
        staffRepository.deleteById(id);
    }

    public Page<Staff> findByIsActiveTrue(Pageable pageable) {
        return staffRepository.findAll(pageable);
    }

    public Staff getDefaultManager() {
        return staffRepository.findAll().isEmpty() ? null : staffRepository.findAll().get(0);
    }

    public boolean isUsernameTaken(String username, Integer idToExclude) {
        Optional<Staff> existing = staffRepository.findByUsername(username);
        // nếu không tồn tại -> false (chưa bị lấy)
        // nếu tồn tại và id của existing khác idToExclude -> true (bị lấy bởi người khác)
        return existing.isPresent() && !Objects.equals(existing.get().getManagerId(), idToExclude);
    }

    public boolean isEmailTaken(String email, Integer idToExclude) {
        Optional<Staff> existing = staffRepository.findByEmail(email);
        return existing.isPresent() && !Objects.equals(existing.get().getManagerId(), idToExclude);
    }

    public boolean isPhoneTaken(String phone, Integer idToExclude) {
        Optional<Staff> existing = staffRepository.findByPhoneNumber(phone);
        return existing.isPresent() && !Objects.equals(existing.get().getManagerId(), idToExclude);
    }

    public Page<Staff> getActiveStaffs(Pageable pageable) {
        return staffRepository.findByIsActiveTrue(pageable);
    }

    public Page<Staff> getDeletedStaffs(Pageable pageable) {
        return staffRepository.findByIsActiveFalse(pageable);
    }

    public Page<Staff> searchStaff(String keyword, Pageable pageable) {
        return staffRepository.search(keyword.trim(), pageable);
    }


    public List<Staff> getActiveStaffs() {
        return staffRepository.findByIsActiveTrue();
    }

    public List<Staff> getDeletedStaffs() {
        return staffRepository.findByIsActiveFalse();
    }

    @Transactional
    public void softDelete(int id) {
        Staff staff = staffRepository.findById(id).orElse(null);
        if (staff != null) {
            staff.setActive(false);
            staffRepository.save(staff);
        }
    }

    @Transactional
    public void hardDelete(int id) {
        staffRepository.deleteById(id);
    }

    @Transactional
    public void restore(int id) {
        Staff staff = staffRepository.findById(id).orElse(null);
        if (staff != null) {
            staff.setActive(true);
            staffRepository.save(staff);
        }
    }
}