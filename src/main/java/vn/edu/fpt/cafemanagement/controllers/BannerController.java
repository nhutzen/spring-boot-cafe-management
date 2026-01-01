package vn.edu.fpt.cafemanagement.controllers;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import vn.edu.fpt.cafemanagement.entities.Banner;
import vn.edu.fpt.cafemanagement.entities.Voucher;
import vn.edu.fpt.cafemanagement.repositories.BannerRepository;
import vn.edu.fpt.cafemanagement.services.BannerService;
import vn.edu.fpt.cafemanagement.util.SignUtil;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/dashboard/banners")
public class BannerController {
    private final BannerService bannerService;

    public BannerController(BannerService bannerService) {
        this.bannerService = bannerService;
    }

    @GetMapping
    public String listBanner(
            Model model,
            @PageableDefault(size = 10, page = 0, sort = "id") Pageable pageable) {
        Page<Banner> bannerPage = bannerService.findAllBanners(pageable);
        model.addAttribute("bannerPage", bannerPage);

        List<Banner> activeBanners = bannerService.findAllBanners().stream().filter(Banner::isActive).collect(Collectors.toList());

        model.addAttribute("banners", bannerPage.getContent());
        model.addAttribute("activeBanners", activeBanners);

        return "dashboard/banners/list";
    }

    @GetMapping("/create")
    public String createBanner(Model model) {
        model.addAttribute("banner", new Banner());
        model.addAttribute("pageTitle", "Add Banner");
        return "dashboard/banners/create";
    }

    @GetMapping("/edit/{id}")
    public String editBanner(@PathVariable Integer id, Model model, RedirectAttributes redirectAttributes) {
        Optional<Banner> banner = bannerService.findBannerById(id);
        if (banner.isPresent()) {
            model.addAttribute("banner", banner.get());
            model.addAttribute("pageTitle", "Edit Banner");
            return "dashboard/banners/edit";
        } else {
            redirectAttributes.addFlashAttribute("errorInfo", "This banner not found.");
            return "redirect:/dashboard/banners";
        }
    }
    @PostMapping("/save")
    public String saveBanner(@ModelAttribute("banner") Banner banner,
                             @RequestParam("imageFile") MultipartFile imageFile,
                             Model model,
                             RedirectAttributes redirectAttributes) {

        boolean isEdit = banner.getId() > 0;
        String targetView = isEdit ? "dashboard/banners/edit" : "dashboard/banners/create";

        String existingImagePath = banner.getImagePath(); // Lấy từ Model Attribute (nếu có từ hidden field)

        if (isEdit && (existingImagePath == null || existingImagePath.isEmpty())) {

            Optional<Banner> existingBannerOpt = bannerService.findBannerById(banner.getId());
            if (existingBannerOpt.isPresent()) {
                existingImagePath = existingBannerOpt.get().getImagePath();
                banner.setImagePath(existingImagePath); // Gán lại cho model attribute
            }
        }



        // 1. Title Validation
        if (banner.getTitle() == null || banner.getTitle().isBlank()) {
            model.addAttribute("error", "Banner Title cannot be empty.");
            model.addAttribute("pageTitle", isEdit ? "Edit Banner (" + banner.getTitle() + ")" : "Add Banner");
            return targetView;
        }

        // 2. Order Number Validation (Giữ nguyên)
        if (banner.getOrderNumber() < 0) {
            model.addAttribute("error", "Order Number must be a non-negative value");
            model.addAttribute("pageTitle", isEdit ? "Edit Banner (" + banner.getTitle() + ")" : "Add Banner");
            return targetView;
        }

        // 3. Image Validation (Sửa logic)
        boolean hasNewFile = !imageFile.isEmpty();
        boolean hasExistingPath = existingImagePath != null && !existingImagePath.isEmpty();


        if (!hasNewFile && !hasExistingPath) {
            model.addAttribute("error", "Image file is required for this banner.");
            model.addAttribute("pageTitle", isEdit ? "Edit Banner (" + banner.getTitle() + ")" : "Add Banner");
            return targetView;
        }



        try {
            String uploadDir = "D:/SWP/Project/uploads/";
            File dir = new File(uploadDir);
            if (!dir.exists()) dir.mkdirs();

            if (hasNewFile) {

                String originalFileName = imageFile.getOriginalFilename();
                String extension = "";
                if (originalFileName != null && originalFileName.contains(".")) {
                    extension = originalFileName.substring(originalFileName.lastIndexOf("."));
                }

                String newFileName = java.util.UUID.randomUUID().toString() + extension;
                Path path = Paths.get(uploadDir + newFileName);

                Files.copy(imageFile.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);
                banner.setImagePath("/uploads/" + newFileName);

            } else if (isEdit && hasExistingPath) {

                banner.setImagePath(existingImagePath);

            } else if (!isEdit) {

                banner.setImagePath(null);
            }

            // Lưu DB
            bannerService.save(banner);
            redirectAttributes.addFlashAttribute("completeInfo", "Banner has been saved successfully!");
        } catch (Exception e) {
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("errorInfo", "Error saving banner: " + e.getMessage());
        }

        return "redirect:/dashboard/banners";
    }


    @GetMapping("/delete/{id}")
    public String delete(@PathVariable Integer id, RedirectAttributes redirectAttributes) {
        // 1. Thực hiện logic xóa
        bannerService.delete(id);
        redirectAttributes.addFlashAttribute("completeInfo", "Banner has been deleted successfully!");

        return "redirect:/dashboard/banners";
    }

}



