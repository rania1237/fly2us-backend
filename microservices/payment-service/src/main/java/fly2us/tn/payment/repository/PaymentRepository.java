package fly2us.tn.payment.repository;

import fly2us.tn.payment.model.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {

    List<Payment> findByClientId(Long clientId);

    List<Payment> findByDossierId(Long dossierId);

    List<Payment> findByStatus(String status);

    Optional<Payment> findByTransactionId(String transactionId);
}