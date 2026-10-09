package fly2us.tn.dossier.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "document_requirements")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DocumentRequirement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "visa_type", nullable = false)
    private String visaType;

    @Column(name = "document_name", nullable = false)
    private String documentName;

    @Column(name = "document_code", nullable = false)
    private String documentCode;

    private boolean required = true;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "accepted_formats")
    private String acceptedFormats = "PDF,JPG,PNG";

    @Column(name = "max_size_mb")
    private Integer maxSizeMb = 10;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}