package nbc.devmountain.domain.order.repository;

import nbc.devmountain.domain.order.model.Payment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PaymentRepository extends JpaRepository<Payment, Long> {
    Optional<Payment> findByPayIdAndUserUserId(Long payId, Long userId);
}
