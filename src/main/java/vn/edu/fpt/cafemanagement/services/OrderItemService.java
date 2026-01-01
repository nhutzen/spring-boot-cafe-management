package vn.edu.fpt.cafemanagement.services;

import org.springframework.stereotype.Service;
import vn.edu.fpt.cafemanagement.repositories.OrderItemRepository;

import java.util.List;

@Service
public class OrderItemService {

    OrderItemRepository orderItemRepository;

    public OrderItemService(OrderItemRepository orderItemRepository) {
        this.orderItemRepository = orderItemRepository;
    }

    public List<Integer> getProductIdsByCustomerId(int customerId) {
        return orderItemRepository.getProductIdsByCustomerId(customerId);
    }
}
