package vn.edu.fpt.cafemanagement.services;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import vn.edu.fpt.cafemanagement.entities.Banner;
import vn.edu.fpt.cafemanagement.repositories.BannerRepository;

import java.util.List;
import java.util.Optional;

@Service
public class BannerService {
    private final BannerRepository bannerRepository;

    public  BannerService(BannerRepository bannerRepository) {
        this.bannerRepository = bannerRepository;
    }

    public List<Banner> findAllBanners() {
        return bannerRepository.findAll();
    }

    public Optional<Banner> findBannerById(int id) {
        return bannerRepository.findById(id);
    }

    public Banner save(Banner banner) {
        return bannerRepository.save(banner);
    }

    public List<Banner> findActiveBannersForHome() {
        return bannerRepository.findByIsActiveOrderByOrderNumberAsc(true);
    }

    public void  delete(Integer id) {
        bannerRepository.deleteById(id);
    }

    public Page<Banner> findAllBanners(Pageable pageable) {
        return bannerRepository.findAll(pageable);
    }
}
