package fly2us.tn.dossier.model;  // ✅ Package correct

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "visas")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Visa {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private VisaType type;

    @Column(nullable = false, unique = true)
    private String country;

    private String flag;
    private String image;

    @Column(columnDefinition = "TEXT")
    private String description;

    @ElementCollection
    private List<String> requirements = new ArrayList<>();

    private String duration;
    private String processingTime;
    private Double price;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}