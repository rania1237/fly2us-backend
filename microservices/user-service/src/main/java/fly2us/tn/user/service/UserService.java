package fly2us.tn.user.service;

import fly2us.tn.user.dto.UserDto;
import fly2us.tn.user.model.Role;
import fly2us.tn.user.model.User;
import fly2us.tn.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    // ====== INSCRIPTION ======
    public User register(String email, String password, String firstName, String lastName, String phone, Role role) {
        // ✅ Vérifier si l'email existe déjà
        if (userRepository.existsByEmail(email)) {
            throw new RuntimeException("Email déjà utilisé: " + email);
        }

        User user = new User();
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(password));
        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setPhone(phone != null && !phone.isEmpty() ? phone : "+21600000000");
        user.setActive(true);

        // Sécurité : si on essaie de créer un ADMIN, forcer CLIENT
        if (role == Role.ADMIN) {
            user.setRole(Role.CLIENT);
        } else {
            user.setRole(role != null ? role : Role.CLIENT);
        }

        return userRepository.save(user);
    }

    // ====== CRÉATION ADMIN ======
    public User registerAdmin(String email, String password, String firstName, String lastName, String phone) {
        if (userRepository.existsByEmail(email)) {
            throw new RuntimeException("Email déjà utilisé: " + email);
        }

        User user = new User();
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(password));
        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setPhone(phone != null && !phone.isEmpty() ? phone : "+21600000000");
        user.setRole(Role.ADMIN);
        user.setActive(true);

        return userRepository.save(user);
    }

    // ====== LOGIN ======
    public String login(String email, String password) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé: " + email));

        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new RuntimeException("Mot de passe incorrect");
        }

        return jwtService.generateToken(user.getEmail(), user.getRole(), user.getId());
    }

    // ====== CRUD ======
    public List<UserDto> getAllUsers() {
        return userRepository.findAll().stream()
                .map(UserDto::fromEntity)
                .collect(Collectors.toList());
    }

    public UserDto getUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));
        return UserDto.fromEntity(user);
    }

    public UserDto getUserByEmail(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé avec email: " + email));
        return UserDto.fromEntity(user);
    }

    public void deleteUser(Long id) {
        userRepository.deleteById(id);
    }
}