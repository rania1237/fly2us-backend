package fly2us.tn.payment.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "visa_tarifs",
        uniqueConstraints = @UniqueConstraint(columnNames = {"visa_type", "priority"}))
@Data
@NoArgsConstructor
@AllArgsConstructor
public class VisaTarif {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "visa_type", nullable = false)
    private String visaType;         // ETUDIANT, TOURISTIQUE, SCHENGEN

    @Column(nullable = false)
    private String priority;         // NORMAL, URGENT, EXPRESS

    @Column(name = "service_fee", nullable = false)
    private Double serviceFee;       // Frais de service Fly2US

    @Column(name = "tls_fee")
    private Double tlsFee = 0.0;     // Frais TLS/Ambassade

    @Column(name = "total_amount", nullable = false)
    private Double totalAmount;      // serviceFee + tlsFee

    @Column(nullable = false)
    private String currency = "TND";

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false)
    private Boolean active = true;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        if (totalAmount == null) {
            totalAmount = (serviceFee != null ? serviceFee : 0.0)
                    + (tlsFee != null ? tlsFee : 0.0);
        }
    }
}