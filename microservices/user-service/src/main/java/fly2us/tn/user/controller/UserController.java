package fly2us.tn.user.controller;

import fly2us.tn.user.dto.UserDto;
import fly2us.tn.user.model.Role;
import fly2us.tn.user.model.User;
import fly2us.tn.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:4200")
public class UserController {

    private static final Logger logger = LoggerFactory.getLogger(UserController.class);

    private final UserService userService;

    @GetMapping("/hello")
    public String hello() {
        return "Hello from User Service!";
    }

    // ====== INSCRIPTION ======
    @PostMapping("/signup")
    public ResponseEntity<?> register(@RequestBody Map<String, String> request) {
        try {
            logger.debug("Inscription reçue: email={}, firstName={}, lastName={}",
                    request.get("email"), request.get("firstName"), request.get("lastName"));

            String email = request.get("email");
            String password = request.get("password");
            String firstName = request.get("firstName");
            String lastName = request.get("lastName");
            String phone = request.get("phone");
            String roleStr = request.get("role");

            if (email == null || email.isEmpty()) {
                return ResponseEntity.badRequest().body("Email est obligatoire");
            }
            if (password == null || password.length() < 8) {
                return ResponseEntity.badRequest().body("Mot de passe doit contenir au moins 8 caractères");
            }
            if (firstName == null || firstName.isEmpty()) {
                return ResponseEntity.badRequest().body("FirstName est obligatoire");
            }

            Role role = (roleStr != null && roleStr.equalsIgnoreCase("ADMIN")) ? Role.ADMIN : Role.CLIENT;

            User user = userService.register(email, password, firstName, lastName, phone, role);
            logger.info("Utilisateur créé: {}", user.getEmail());
            return ResponseEntity.ok(user);
        } catch (Exception e) {
            logger.error("Erreur lors de l'inscription: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // ====== INSCRIPTION ADMIN ======
    @PostMapping("/signup/admin")
    public ResponseEntity<?> registerAdmin(@RequestBody Map<String, String> request) {
        try {
            String email = request.get("email");
            String password = request.get("password");
            String firstName = request.get("firstName");
            String lastName = request.get("lastName");
            String phone = request.get("phone");

            User user = userService.registerAdmin(email, password, firstName, lastName, phone);
            return ResponseEntity.ok(user);
        } catch (Exception e) {
            logger.error("Erreur lors de l'inscription admin: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // ====== LOGIN ======
    @PostMapping("/signin")
    public ResponseEntity<?> login(@RequestBody Map<String, String> request) {
        try {
            String email = request.get("email");
            String password = request.get("password");

            String token = userService.login(email, password);
            Map<String, String> response = new HashMap<>();
            response.put("token", token);
            response.put("email", email);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Erreur lors de la connexion: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // ====== CRUD ======
    @GetMapping
    public List<UserDto> getAllUsers() {
        return userService.getAllUsers();
    }

    @GetMapping("/{id}")
    public UserDto getUserById(@PathVariable Long id) {
        return userService.getUserById(id);
    }

    @GetMapping("/email")
    public UserDto getUserByEmail(@RequestParam String email) {
        return userService.getUserByEmail(email);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }
}