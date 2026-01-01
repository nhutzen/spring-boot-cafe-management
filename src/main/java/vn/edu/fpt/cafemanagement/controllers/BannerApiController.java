package vn.edu.fpt.cafemanagement.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import vn.edu.fpt.cafemanagement.entities.Banner;
import vn.edu.fpt.cafemanagement.services.BannerService;

import java.util.List;

@RestController
@RequestMapping("/api/banners")
public class BannerApiController {

    @Autowired
    private BannerService bannerService;

    @GetMapping
    public List<Banner> getBannersForHomePage() {
        // Lấy danh sách banners đã được lọc và sắp xếp
        return bannerService.findActiveBannersForHome();
    }
}