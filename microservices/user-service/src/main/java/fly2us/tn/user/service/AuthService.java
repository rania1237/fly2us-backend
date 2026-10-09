package fly2us.tn.user.service;

import fly2us.tn.user.dto.LoginRequest;
import fly2us.tn.user.dto.RegisterRequest;
import fly2us.tn.user.model.Role;
import fly2us.tn.user.model.User;
import fly2us.tn.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    @Transactional
    public User register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email déjà utilisé");
        }

        User user = new User();
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setPhone(request.getPhone());

        // Sécurité : si l'utilisateur essaie de s'inscrire en tant qu'ADMIN, forcer CLIENT
        if (request.getRole() == Role.ADMIN) {
            user.setRole(Role.CLIENT);
        } else {
            user.setRole(request.getRole() != null ? request.getRole() : Role.CLIENT);
        }

        user.setActive(true);
        return userRepository.save(user);
    }

    @Transactional
    public User registerAdmin(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email déjà utilisé");
        }

        User user = new User();
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setPhone(request.getPhone());
        user.setRole(Role.ADMIN);
        user.setActive(true);

        return userRepository.save(user);
    }

    public String login(LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new RuntimeException("Mot de passe incorrect");
        }

        return jwtService.generateToken(user.getEmail(), user.getRole(), user.getId());
    }
}