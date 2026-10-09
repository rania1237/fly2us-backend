package fly2us.tn.dossier.model;

import com.fasterxml.jackson.annotation.JsonIgnore;  // ✅ AJOUTER CET IMPORT
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "dossiers")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Dossier {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "dossier_number", unique = true, nullable = false)
    private String dossierNumber;

    @Column(name = "client_id", nullable = false)
    private Long clientId;

    @Column(name = "agent_id")
    private Long agentId;

    @Column(nullable = false)
    private String type;

    @Column(nullable = false)
    private String country;

    @Column(nullable = false)
    private String status = "BROUILLON";

    private String priority = "NORMAL";
    private String university;
    private String program;

    @Column(name = "start_date")
    private LocalDate startDate;

    @Column(name = "end_date")
    private LocalDate endDate;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @Column(name = "rejection_reason", columnDefinition = "TEXT")
    private String rejectionReason;

    // ✅ AJOUT DE @JsonIgnore POUR ÉVITER LA BOUCLE INFINIE
    @JsonIgnore
    @OneToMany(mappedBy = "dossier", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<DossierDocument> documents = new ArrayList<>();

    @Column(name = "submitted_at")
    private LocalDateTime submittedAt;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}