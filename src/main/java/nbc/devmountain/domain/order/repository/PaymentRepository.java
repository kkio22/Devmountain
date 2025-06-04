package nbc.devmountain.domain.order.repository;

import nbc.devmountain.domain.order.model.Payment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PaymentRepository extends JpaRepository<Payment, Long> {
}
