package fly2us.tn.user.dto;

import fly2us.tn.user.model.Role;
import lombok.Data;

@Data
public class RegisterRequest {
    private String email;
    private String password;
    private String firstName;
    private String lastName;
    private String phone;
    private Role role; // ADMIN ou CLIENT
}