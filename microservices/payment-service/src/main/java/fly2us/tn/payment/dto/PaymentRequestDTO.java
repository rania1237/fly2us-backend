package fly2us.tn.payment.dto;

import lombok.Data;

@Data
public class PaymentRequestDTO {
    private Long dossierId;
    private Long clientId;
    private String method;
    private String cardNumber;
    private String cardHolder;
    private String clientEmail;
}