package vn.edu.fpt.cafemanagement.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import vn.edu.fpt.cafemanagement.entities.Category;

import java.util.List;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Integer> {
    public List<Category> findCategoriesByCateNameContainingIgnoreCase(String cateName);

    List<Category> findByIsActiveTrue();

    List<Category> findByIsActiveFalse();

}