package fly2us.tn.user.controller;

import fly2us.tn.user.dto.UserDto;
import fly2us.tn.user.model.Role;
import fly2us.tn.user.model.User;
import fly2us.tn.user.service.UserService;
import lombok.RequiredArgsConstructor;
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

    private final UserService userService;

    @GetMapping("/hello")
    public String hello() {
        return "Hello from User Service!";
    }

    // ====== INSCRIPTION ======
    @PostMapping("/signup")
    public ResponseEntity<?> register(@RequestBody Map<String, String> request) {
        try {
            System.out.println("📝 Inscription reçue:");
            System.out.println("  - email: " + request.get("email"));
            System.out.println("  - firstName: " + request.get("firstName"));
            System.out.println("  - lastName: " + request.get("lastName"));

            String email = request.get("email");
            String password = request.get("password");
            String firstName = request.get("firstName");
            String lastName = request.get("lastName");
            String phone = request.get("phone");
            String roleStr = request.get("role");

            // ✅ Vérifier les champs obligatoires
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
            System.out.println("✅ Utilisateur créé: " + user.getEmail());
            return ResponseEntity.ok(user);
        } catch (Exception e) {
            System.err.println("❌ Erreur: " + e.getMessage());
            e.printStackTrace();
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