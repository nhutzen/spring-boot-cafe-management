package vn.edu.fpt.cafemanagement.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import vn.edu.fpt.cafemanagement.entities.Banner;

import java.util.List;

@Repository
public interface BannerRepository extends JpaRepository<Banner,Integer> {
    List<Banner> findByIsActiveOrderByOrderNumberAsc(boolean isActive);
}
