package vn.edu.fpt.cafemanagement.services;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import vn.edu.fpt.cafemanagement.entities.Order;
import vn.edu.fpt.cafemanagement.entities.Product;
import vn.edu.fpt.cafemanagement.repositories.OrderRepository;
import vn.edu.fpt.cafemanagement.repositories.ProductRepository;

import java.util.List;

@Service
public class ProductService {

    private final ProductRepository productRepository;
    private final OrderRepository orderRepository;

    public ProductService(ProductRepository productRepository, OrderRepository orderRepository) {
        this.productRepository = productRepository;
        this.orderRepository = orderRepository;
    }

    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }

    public Page<Product> getAllProductsPage(Pageable pageable) {
        return productRepository.findAll(pageable);
    }

    public void saveProduct(Product product) {
        productRepository.save(product);
    }

    public void deleteProduct(Product product) {
        productRepository.delete(product);
    }

    public void updateProduct(Product product) {
        productRepository.save(product);
    }

    public List<Product> getActiveProducts() {
        return productRepository.findByIsActiveTrueAndCategoryIsActiveTrue();
    }

    public List<Product> getProductsByCategory(int categoryId) {
        return productRepository.findByIsActiveTrueAndCategoryCateIdAndCategoryIsActiveTrue(categoryId);
    }

    public Page<Product> getProductsByCategory(int categoryId, Pageable pageable) {
        return productRepository.findByIsActiveTrueAndCategoryCateIdAndCategoryIsActiveTrue(categoryId, pageable);
    }

    public Page<Product> getActiveProductsByCategory(int categoryId, Pageable pageable) {
        return productRepository.findByIsActiveTrueAndCategoryCateIdAndCategoryIsActiveTrue(categoryId, pageable);
    }

    public Page<Product> getNonActiveProductsByCategory(int categoryId, Pageable pageable) {
        return productRepository.findByIsActiveFalseAndCategoryCateIdAndCategoryIsActiveTrue(categoryId, pageable);
    }

    public List<Product> getNonActiveProductsByCategory(int categoryId) {
        return productRepository.findByIsActiveFalseAndCategoryCateIdAndCategoryIsActiveTrue(categoryId);
    }

    public Product getProductById(int productId) {
        return productRepository.findById(productId).orElse(null);
    }

    public Page<Product> getActiveProductsPaged(Pageable pageable) {
        return productRepository.findByIsActiveTrueAndCategoryIsActiveTrue(pageable);
    }

    public Page<Product> getAllProductsPaged(Pageable pageable) {
        return productRepository.findByIsActiveTrueAndCategoryIsActiveTrue(pageable);

    }

    public Page<Product> getAllNonActiveProductsPaged(Pageable pageable) {
        return productRepository.findByIsActiveFalseAndCategoryIsActiveTrue(pageable);

    }

    public Page<Product> getActiveProductsPaged(int page, int size) {
        Pageable pageable = PageRequest.of(page - 1, size);
        return productRepository.findByIsActiveTrue(pageable);
    }

    public List<Product> getNonActiveProducts() {
        return productRepository.findByIsActiveFalse();
    }

    public List<Product> getSearchProducts(String searchText) {
        return productRepository.findSearchProductsByAllCriteria(searchText);
    }

    public Page<Product> getSearchProducts(String searchText, Pageable pageable) {
        return productRepository.findSearchProductsByAllCriteria(searchText, pageable);
    }

    public Page<Product> getNonActiveSearchProducts(String searchText, Pageable pageable) {
        return productRepository.findNonActiveSearchProductsByAllCriteria(searchText, pageable);
    }

    // ---------------- ORDER LOGIC ----------------

    /**
     * Tính tổng phụ (subtotal) của danh sách sản phẩm
     * Dựa trên giá * số lượng
     */
    public double calculateSubtotal(List<Integer> productIds, List<Integer> quantities) {
        double subtotal = 0;
        for (int i = 0; i < productIds.size(); i++) {
            Product product = getProductById(productIds.get(i));
            if (product != null) {
                subtotal += product.getPrice() * quantities.get(i);
            }
        }
        return subtotal;
    }

    //Cập nhật tổng giá của đơn hàng
    public void saveOrderItems(Order order, List<Integer> productIds, List<Integer> quantities) {
        double subtotal = calculateSubtotal(productIds, quantities);
        order.setTotalPrice(subtotal);
        orderRepository.save(order);
    }

    public void deleteSortProduct(Product product) {
        product.setActive(false);
        product.setQuantity(0);
        product.setStatus("unavailable");
        productRepository.save(product);
    }

    /**
     * Lấy danh sách sản phẩm đang hoạt động (isActive=true)
     * theo category (nếu categoryId = 0 thì lấy tất cả),
     * có phân trang.
     */
    public Page<Product> getProductsByCategoryPaged(Integer categoryId, Pageable pageable) {
        if (categoryId == null || categoryId == 0) {
            return productRepository.findByIsActiveTrueAndCategoryIsActiveTrue(pageable);
        } else {
            return productRepository.findByIsActiveTrueAndCategoryCateIdAndCategoryIsActiveTrue(categoryId, pageable);
        }
    }

    public Page<Product> searchActiveProducts(String keyword, Pageable pageable) {
        return productRepository.findByProNameContainingIgnoreCaseAndIsActiveTrue(keyword, pageable);
    }

    public Page<Product> searchActiveProductsByCategory(Integer categoryId, String keyword, Pageable pageable) {
        return productRepository.findByCategoryAndNameContainingIgnoreCaseAndActiveTrue(categoryId, keyword, pageable);
    }

    public Page<Product> searchProductsByCategoryAndKeyword(Integer finalCategoryId, String keyword, Pageable pageable) {
        String searchKeyword = keyword != null ? keyword.trim() : "";

        // Kiểm tra nếu categoryId = 0, bạn sẽ gọi phương thức tìm kiếm chung hơn.
        // Tuy nhiên, nếu bạn sử dụng @Query trong Repository (như phương pháp tốt hơn),
        // bạn chỉ cần gọi 1 hàm duy nhất:

        // Sử dụng phương thức @Query đã định nghĩa:
        return productRepository.searchProductsByCategoryAndKeyword(finalCategoryId, searchKeyword, pageable);
    }


    public Page<Product> searchNonActiveProductsByCategoryAndKeyword(Integer finalCategoryId, String keyword, Pageable pageable) {
        String searchKeyword = keyword != null ? keyword.trim() : "";

        // Kiểm tra nếu categoryId = 0, bạn sẽ gọi phương thức tìm kiếm chung hơn.
        // Tuy nhiên, nếu bạn sử dụng @Query trong Repository (như phương pháp tốt hơn),
        // bạn chỉ cần gọi 1 hàm duy nhất:

        // Sử dụng phương thức @Query đã định nghĩa:
        return productRepository.searchNonActiveProductsByCategoryAndKeyword(finalCategoryId, searchKeyword, pageable);

    }
    public void updateStatusForZeroQuantity(List<Product> products) {
        boolean needSave = false;

        for (Product p : products) {
            if (p.getQuantity() == 0 && !"unavailable".equalsIgnoreCase(p.getStatus())) {
                p.setStatus("unavailable");
                needSave = true;
            }
            if (p.getQuantity() > 0 && "unavailable".equalsIgnoreCase(p.getStatus())) {
                p.setStatus("available");
                needSave = true;
            }
        }

        if (needSave) {
            productRepository.saveAll(products);
        }
    }

    public Page<Product> getProductsByCategoryPaged1(Integer categoryId, Pageable pageable) {
        if (categoryId == null || categoryId == 0) {
            return productRepository.findByIsActiveTrueAndCategoryIsActiveTrueAndQuantityGreaterThan(0, pageable);
        } else {
            return productRepository.findByIsActiveTrueAndCategoryCateIdAndCategoryIsActiveTrueAndQuantityGreaterThan(categoryId, 0, pageable);
        }
    }

    public Page<Product> searchActiveProducts1(String keyword, Pageable pageable) {
        return productRepository.findByProNameContainingIgnoreCaseAndIsActiveTrueAndQuantityGreaterThan(keyword, 0, pageable);
    }

    public Page<Product> getActiveProductsPaged1(Pageable pageable) {
        return productRepository.findByIsActiveTrueAndCategoryIsActiveTrueAndQuantityGreaterThan(0, pageable);
    }
}
