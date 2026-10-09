package fly2us.tn.payment.dto;

import lombok.Data;

@Data
public class DossierDTO {
    private Long id;
    private String dossierNumber;
    private Long clientId;
    private String type;
    private String country;
    private String status;
    private String priority;
    private String university;
    private String program;
}