package vn.edu.fpt.cafemanagement.repositories;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import vn.edu.fpt.cafemanagement.entities.Category;
import vn.edu.fpt.cafemanagement.entities.Product;

import java.util.List;

@Repository
public interface ProductRepository extends JpaRepository<Product, Integer> {

    List<Product> findByIsActiveTrue();

    List<Product> findByIsActiveTrueAndCategoryCateId(Integer categoryId);

    Page<Product> findByIsActiveTrue(Pageable pageable);

    List<Product> findByIsActiveTrueAndCategoryIsActiveTrue();

    List<Product> findByIsActiveTrueAndCategoryCateIdAndCategoryIsActiveTrue(int categoryId);

    List<Product> findByIsActiveFalseAndCategoryCateIdAndCategoryIsActiveTrue(int categoryId);

    List<Product> findByIsActiveFalse();

    Page<Product> findByIsActiveTrueAndCategoryIsActiveTrue(Pageable pageable);

    Page<Product> findByIsActiveFalseAndCategoryIsActiveTrue(Pageable pageable);

    Page<Product> findByIsActiveTrueAndCategoryCateIdAndCategoryIsActiveTrue(int categoryId, Pageable pageable);

    //    List<Product> findByIsActiveTrueAndProNameContainingIgnoreCaseAndCategoryIsActiveTrue(String searchText);
    @Query(value = """
                SELECT p.* FROM product p
                LEFT JOIN category c ON c.cate_id = p.cate_id
                WHERE 
                    p.is_active = 1
                    AND c.is_active = 1
                              AND  p.quantity>0
                    AND p.pro_name COLLATE SQL_Latin1_General_CP1_CI_AI LIKE '%' + :searchText + '%'
            """, nativeQuery = true)
    // <--- QUAN TRỌNG: Đặt nativeQuery = true
    List<Product> findSearchProductsByAllCriteria(@Param("searchText") String searchText);

    @Query(
            value = """
                        SELECT p.* FROM product p
                        LEFT JOIN category c ON c.cate_id = p.cate_id
                        WHERE 
                            p.is_active = 1
                            AND c.is_active = 1
                                              AND p.quantity > 0 
                            AND p.pro_name COLLATE SQL_Latin1_General_CP1_CI_AI LIKE '%' + :searchText + '%'
                    """,
            // Sửa lỗi: Chỉ định truy vấn COUNT hợp lệ
            countQuery = """
                        SELECT COUNT(p.pro_id) FROM product p
                        LEFT JOIN category c ON c.cate_id = p.cate_id
                        WHERE 
                            p.is_active = 1
                            AND c.is_active = 1
                            AND p.pro_name COLLATE SQL_Latin1_General_CP1_CI_AI LIKE '%' + :searchText + '%'
                    """,
            nativeQuery = true
    )
    Page<Product> findSearchProductsByAllCriteria(@Param("searchText") String searchText, Pageable pageable);

//    Page<Product> findByIsActiveTrueAndCategoryIsActiveTrue(Pageable pageable);
//
//    Page<Product> findByIsActiveTrueAndCategoryCateIdAndCategoryIsActiveTrue(int categoryId, Pageable pageable);

    Page<Product> findByProNameContainingIgnoreCaseAndIsActiveTrue(String name, Pageable pageable);

    @Query("SELECT p FROM Product p WHERE p.category.cateId = :categoryId " +
            "AND LOWER(p.proName) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
            "AND p.isActive = true AND p.quantity > 0")
    Page<Product> findByCategoryAndNameContainingIgnoreCaseAndActiveTrue(
            @Param("categoryId") Integer categoryId,
            @Param("keyword") String keyword,
            Pageable pageable);


    @Query("SELECT p FROM Product p " +
            "WHERE (:categoryId = 0 OR p.category.cateId = :categoryId) " +
            "AND (LOWER(p.proName) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
            "AND p.isActive = true)")
    Page<Product> searchProductsByCategoryAndKeyword(
            @Param("categoryId") int categoryId,
            @Param("keyword") String keyword,
            Pageable pageable
    );


    @Query("SELECT p FROM Product p " +
            "WHERE (:categoryId = 0 OR p.category.cateId = :categoryId) " +
            "AND (LOWER(p.proName) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
            "AND p.isActive = false)")
    Page<Product> searchNonActiveProductsByCategoryAndKeyword(
            @Param("categoryId") int categoryId,
            @Param("keyword") String keyword,
            Pageable pageable
    );


    @Query(
            value = """
                        SELECT p.* FROM product p
                        LEFT JOIN category c ON c.cate_id = p.cate_id
                        WHERE 
                            p.is_active = 0
                            AND c.is_active = 1
                            AND p.pro_name COLLATE SQL_Latin1_General_CP1_CI_AI LIKE '%' + :searchText + '%'
                    """,
            // Sửa lỗi: Chỉ định truy vấn COUNT hợp lệ
            countQuery = """
                        SELECT COUNT(p.pro_id) FROM product p
                        LEFT JOIN category c ON c.cate_id = p.cate_id
                        WHERE 
                            p.is_active = 0
                            AND c.is_active = 1
                            AND p.pro_name COLLATE SQL_Latin1_General_CP1_CI_AI LIKE '%' + :searchText + '%'
                    """,
            nativeQuery = true
    )
    Page<Product> findNonActiveSearchProductsByAllCriteria(@Param("searchText") String searchText, Pageable pageable);

    Page<Product> findByIsActiveFalseAndCategoryCateIdAndCategoryIsActiveTrue(int categoryId, Pageable pageable);

    Page<Product> findByIsActiveTrueAndCategoryIsActiveTrueAndQuantityGreaterThan(int quantity, Pageable pageable);

    Page<Product> findByIsActiveTrueAndCategoryCateIdAndCategoryIsActiveTrueAndQuantityGreaterThan(int categoryId, int quantity, Pageable pageable);

    Page<Product> findByProNameContainingIgnoreCaseAndIsActiveTrueAndQuantityGreaterThan(String name, int quantity, Pageable pageable);

}