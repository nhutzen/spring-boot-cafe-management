package vn.edu.fpt.cafemanagement.controllers;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import vn.edu.fpt.cafemanagement.entities.Banner;
import vn.edu.fpt.cafemanagement.entities.Staff;
import vn.edu.fpt.cafemanagement.entities.Role;
import vn.edu.fpt.cafemanagement.repositories.StaffRepository;
import vn.edu.fpt.cafemanagement.services.StaffService;
import vn.edu.fpt.cafemanagement.services.RoleService;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.Period;
import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/dashboard/staff")
public class StaffController {

    private final StaffService staffService;
    private final RoleService roleService;
    private final StaffRepository staffRepository;

    public StaffController(StaffService staffService, RoleService roleService, StaffRepository staffRepository) {
        this.staffService = staffService;
        this.roleService = roleService;
        this.staffRepository = staffRepository;
    }

    @RequestMapping
    public String list(
            @RequestParam(value = "keyword", required = false) String keyword,
            @RequestParam(value = "page", defaultValue = "1") int page,
            Model model) {

        int size = 15; // số phần tử mỗi trang
        Pageable pageable = PageRequest.of(page - 1, size);
        Page<Staff> staffPage;

        if (keyword != null && !keyword.trim().isEmpty()) {
            staffPage = staffService.searchStaff(keyword.trim(), pageable);
            model.addAttribute("keyword", keyword);
        } else {
            staffPage = staffService.getActiveStaffs(pageable);
        }

        if (staffPage.getContent().isEmpty()) {
            model.addAttribute("notFound", "No staff found" + (keyword != null ? " for \"" + keyword + "\"" : ""));
            model.addAttribute("staffs", null);
            return "dashboard/staff/list";
        }

        List<Staff> activeStaffs = staffService.findAllStaffs()
                .stream()
                .filter(staff -> staff.isActive()
                        && staff.getRole() != null
                        && !"admin".equalsIgnoreCase(staff.getRole().getRoleName()))
                .toList();

        model.addAttribute("activeStaffs", activeStaffs);
        model.addAttribute("staffs", staffPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", staffPage.getTotalPages());

        return "dashboard/staff/list";
    }

    @GetMapping("/new")
    public String create(Model model) {
        model.addAttribute("staff", new Staff());
        model.addAttribute("roles", roleService.getAllRoles());
        return "dashboard/staff/create";
    }


    @GetMapping("/edit/{id}")
    public String edit(@PathVariable("id") int id, Model model) {
        Staff s = staffService.findById(id);
        model.addAttribute("staff", s);
        model.addAttribute("roles", roleService.getAllRoles());
        return "dashboard/staff/edit";
    }

    @GetMapping("/{id}")
    public String getStaffDetails(@PathVariable("id") int id, Model model) {
        Staff s = staffService.findById(id);
        List<Role> roles = roleService.getAllRoles();
        model.addAttribute("staff", s);
        model.addAttribute("roles", roles);
        return "dashboard/staff/details";
    }


    @PostMapping("/save")
    public String save(@ModelAttribute("staff") Staff staff,
                       @RequestParam("roleId") int roleId,
                       @RequestParam("photo") MultipartFile file, Model model, RedirectAttributes redirectAttributes) {
        Role role = roleService.getRoleById(roleId)
                .orElseThrow(() -> new IllegalArgumentException("Null"));
        staff.setRole(role);

        // Hash mật khẩu trước khi lưu
        boolean isEdit = staff.getManagerId() > 0;
        String newPassword = staff.getPassword();

        if (isEdit) {
            if (newPassword == null || newPassword.isBlank()) {
                Staff existingStaff = staffService.findById(staff.getManagerId());
                if (existingStaff != null) {
                    staff.setPassword(existingStaff.getPassword());
                } else {

                }
            } else {
                String hashedPassword = BCrypt.hashpw(newPassword, BCrypt.gensalt());
                staff.setPassword(hashedPassword);
            }
        } else {
            // Kiểm tra mật khẩu có được nhập ko
            if (newPassword == null || newPassword.isBlank()) {
            } else {

                String hashedPassword = BCrypt.hashpw(newPassword, BCrypt.gensalt());
                staff.setPassword(hashedPassword);
            }
        }


// Validate blank
        if (staff.getName().isBlank() || staff.getEmail().isBlank()
                || staff.getPhoneNumber().isBlank() || staff.getUsername().isBlank()
                || staff.getPassword().isBlank() || staff.getRole() == null) {

            model.addAttribute("error", "Please fill all required fields!");
            model.addAttribute("roles", roleService.getAllRoles()); // Load lại roles

            // Nếu là create thì quay lại create, nếu là edit thì quay lại edit
            return (staff.getManagerId() == 0) ? "dashboard/staff/create" : "dashboard/staff/edit";
        }


        Integer idForCheck = (staff.getManagerId() == 0) ? null : staff.getManagerId();
        // check username/email/phone trùng
        if (staffService.isUsernameTaken(staff.getUsername(), idForCheck)) {
            model.addAttribute("error", "Username already exists!");
            model.addAttribute("roles", roleService.getAllRoles());
            return (idForCheck == null) ? "dashboard/staff/create" : "dashboard/staff/edit";
        }

        if (staffService.isEmailTaken(staff.getEmail(), idForCheck)) {
            model.addAttribute("error", "Email already exists!");
            model.addAttribute("roles", roleService.getAllRoles());
            return (idForCheck == null) ? "dashboard/staff/create" : "dashboard/staff/edit";
        }

        if (staffService.isPhoneTaken(staff.getPhoneNumber(), idForCheck)) {
            model.addAttribute("error", "Phone number already exists!");
            model.addAttribute("roles", roleService.getAllRoles());
            return (idForCheck == null) ? "dashboard/staff/create" : "dashboard/staff/edit";
        }

// Validate blank
        if (staff.getName().isBlank() || staff.getEmail().isBlank()
                || staff.getPhoneNumber().isBlank() || staff.getUsername().isBlank()
                || staff.getPassword().isBlank() || staff.getRole() == null) {

            model.addAttribute("error", "Please fill all required fields!");
            model.addAttribute("roles", roleService.getAllRoles());
            return (staff.getManagerId() == 0) ? "dashboard/staff/create" : "dashboard/staff/edit";
        }

// Validate tên
        if (!staff.getName().matches("^[\\p{L} ]+$")) {
            model.addAttribute("error", "Name must contain only letters!");
            model.addAttribute("roles", roleService.getAllRoles());
            return (staff.getManagerId() == 0) ? "dashboard/staff/create" : "dashboard/staff/edit";
        }

// Validate email
        if (!staff.getEmail().matches("^[\\w.-]+@[\\w.-]+\\.[A-Za-z]{2,6}$")) {
            model.addAttribute("error", "Invalid email format!");
            model.addAttribute("roles", roleService.getAllRoles());
            return (staff.getManagerId() == 0) ? "dashboard/staff/create" : "dashboard/staff/edit";
        }

// Validate số điện thoại
        if (!staff.getPhoneNumber().matches("^0\\d{9}$")) {
            model.addAttribute("error", "Phone number must be 10 digits and start with 0!");
            model.addAttribute("roles", roleService.getAllRoles());
            return (staff.getManagerId() == 0) ? "dashboard/staff/create" : "dashboard/staff/edit";
        }

// Validate Date of Birth
        if (staff.getDateOfBirth() == null) {
            model.addAttribute("error", "Date of Birth is required!");
            model.addAttribute("roles", roleService.getAllRoles());
            return (staff.getManagerId() == 0) ? "dashboard/staff/create" : "dashboard/staff/edit";
        }

        LocalDate today = LocalDate.now();

// Không được chọn ngày trong tương lai
        if (staff.getDateOfBirth().isAfter(today)) {
            model.addAttribute("error", "Date of Birth cannot be in the future!");
            model.addAttribute("roles", roleService.getAllRoles());
            return (staff.getManagerId() == 0) ? "dashboard/staff/create" : "dashboard/staff/edit";
        }

// Tuổi phải >= 18
        if (Period.between(staff.getDateOfBirth(), today).getYears() < 18) {
            model.addAttribute("error", "Staff must be at least 18 years old!");
            model.addAttribute("roles", roleService.getAllRoles());
            return (staff.getManagerId() == 0) ? "dashboard/staff/create" : "dashboard/staff/edit";
        }

        try {
            String uploadDir = "D:/SWP/Project/uploads/";
            File dir = new File(uploadDir);
            if (!dir.exists()) dir.mkdirs();

            if (!file.isEmpty()) {
                String originalFileName = file.getOriginalFilename();
                String extension = "";
                if (originalFileName != null && originalFileName.contains(".")) {
                    extension = originalFileName.substring(originalFileName.lastIndexOf("."));
                }

                String newFileName = java.util.UUID.randomUUID().toString() + extension;
                Path path = Paths.get(uploadDir + newFileName);

                Files.copy(file.getInputStream(), path);


                staff.setImg("/uploads/" + newFileName);
            } else {
                if (isEdit) {
                    Staff existingStaff = staffService.findById(staff.getManagerId());

                    if (existingStaff != null && existingStaff.getImg() != null) {
                        staff.setImg(existingStaff.getImg());
                    }
                    // Nếu existingStaff == null hoặc ảnh cũ cũng null, staff.img sẽ là null
                }
            }

            staffService.save(staff);
            redirectAttributes.addFlashAttribute("completeInfo", "Staff has been saved successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorInfo", "Error saving staff: " + e.getMessage());
        }

        return "redirect:/dashboard/staff";
    }

    // Xóa mềm
    @PostMapping("/delete/{id}")
    public String softDelete(@PathVariable int id) {
        staffService.softDelete(id); // chỉ set isActive = false
        return "redirect:/dashboard/staff";
    }

    // DS
    @GetMapping("/deleted")
    public String deletedList(
            @RequestParam(value = "page", defaultValue = "1") int page,
            Model model) {

        int size = 15;
        Pageable pageable = PageRequest.of(page - 1, size);
        Page<Staff> deletedPage = staffService.getDeletedStaffs(pageable);

        model.addAttribute("staffs", deletedPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", deletedPage.getTotalPages());


        return "dashboard/staff/deleted-staff";
    }


    //Restore
    @GetMapping("/restore/{id}")
    public String restore(@PathVariable int id) {
        staffService.restore(id);
        return "redirect:/dashboard/staff/deleted";
    }

    //Xóa cứng
    @GetMapping("/delete-forever/{id}")
    public String hardDelete(@PathVariable int id) {
        staffService.hardDelete(id);
        return "redirect:/dashboard/staff/deleted";
    }


}
