package nbc.devmountain.domain.order.repository;

import nbc.devmountain.domain.order.model.Order;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface OrderRepository extends JpaRepository<Order, Long> {
    List<Order> findByUser_UserId(Long userId);

    Optional<Order> findById(Long Id);

    boolean existsByOrderId(String orderId);

    Optional<Order> findByOrderId(String orderId);
}
