package fly2us.tn.user.service;

import fly2us.tn.user.dto.UserDto;
import fly2us.tn.user.model.Role;
import fly2us.tn.user.model.User;
import fly2us.tn.user.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {

    // ===== MOCKS : On simule toutes les dépendances de UserService =====
    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    // ===== SERVICE À TESTER : Mockito injecte les mocks dedans =====
    @InjectMocks
    private UserService userService;

    // ==========================================================
    // TEST 1 : Inscription réussie d'un nouvel utilisateur
    // ==========================================================
    @Test
    public void testRegister_WhenEmailIsNew_ShouldCreateUserWithEncodedPassword() {
        // ARRANGE (Préparation)
        String email = "rania@fly2us.tn";
        String rawPassword = "motdepasse123";
        String encodedPassword = "$2a$10$encodedHash";

        // On simule : "L'email n'existe pas encore"
        when(userRepository.existsByEmail(email)).thenReturn(false);
        // On simule : "L'encodage retourne ce hash"
        when(passwordEncoder.encode(rawPassword)).thenReturn(encodedPassword);
        // On simule : "Le save retourne l'utilisateur qu'on lui envoie"
        when(userRepository.save(any(User.class))).thenAnswer(i -> i.getArgument(0));

        // ACT (Action)
        User result = userService.register(email, rawPassword, "Rania", "Regei", "+21612345678", Role.CLIENT);

        // ASSERT (Vérification)
        assertNotNull(result, "L'utilisateur retourné ne doit pas être null");
        assertEquals(email, result.getEmail(), "L'email doit être celui fourni");
        assertEquals(encodedPassword, result.getPassword(), "Le mot de passe doit être encodé");
        assertEquals("Rania", result.getFirstName(), "Le prénom doit être correct");
        assertEquals(Role.CLIENT, result.getRole(), "Le rôle doit être CLIENT");
        assertTrue(result.isActive(), "L'utilisateur doit être actif");

        // On vérifie que le repository a bien été appelé
        verify(userRepository, times(1)).save(any(User.class));
    }

    // ==========================================================
    // TEST 2 : Inscription avec un email déjà utilisé → Exception
    // ==========================================================
    @Test
    public void testRegister_WhenEmailAlreadyExists_ShouldThrowException() {
        // ARRANGE
        String email = "existe@fly2us.tn";
        when(userRepository.existsByEmail(email)).thenReturn(true);

        // ACT + ASSERT (On vérifie que l'exception est bien levée)
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            userService.register(email, "password", "Test", "User", "+21600000000", Role.CLIENT);
        });

        assertEquals("Email déjà utilisé: " + email, exception.getMessage());

        // On vérifie que save n'a JAMAIS été appelé (car exception levée avant)
        verify(userRepository, never()).save(any(User.class));
    }

    // ==========================================================
    // TEST 3 : Sécurité - Créer un ADMIN via register() → devient CLIENT
    // ==========================================================
    @Test
    public void testRegister_WhenRoleIsAdmin_ShouldForceRoleToClient() {
        // ARRANGE
        String email = "hacker@fly2us.tn";
        when(userRepository.existsByEmail(email)).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("encoded");
        when(userRepository.save(any(User.class))).thenAnswer(i -> i.getArgument(0));

        // ACT : On essaie de créer un ADMIN, mais la sécurité doit forcer CLIENT
        User result = userService.register(email, "password", "Hacker", "Test", "+21600000000", Role.ADMIN);

        // ASSERT : Vérification CRITIQUE de la faille de sécurité
        assertEquals(Role.CLIENT, result.getRole(),
                "Un utilisateur créé via register() ne doit JAMAIS être ADMIN (sécurité)");
        assertNotEquals(Role.ADMIN, result.getRole(), "Le rôle ADMIN doit être interdit via register()");
    }

    // ==========================================================
    // TEST 4 : Inscription via registerAdmin (route protégée)
    // ==========================================================
    @Test
    public void testRegisterAdmin_ShouldCreateUserWithAdminRole() {
        // ARRANGE
        String email = "admin@fly2us.tn";
        when(userRepository.existsByEmail(email)).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("encoded");
        when(userRepository.save(any(User.class))).thenAnswer(i -> i.getArgument(0));

        // ACT
        User result = userService.registerAdmin(email, "adminPass", "Admin", "Fly2Us", "+21600000000");

        // ASSERT
        assertNotNull(result);
        assertEquals(Role.ADMIN, result.getRole(), "Le rôle doit être ADMIN via registerAdmin");
        assertEquals(email, result.getEmail());
    }

    // ==========================================================
    // TEST 5 : Login réussi → retourne un JWT
    // ==========================================================
    @Test
    public void testLogin_WhenCredentialsAreValid_ShouldReturnJwtToken() {
        // ARRANGE
        String email = "rania@fly2us.tn";
        String rawPassword = "monmotdepasse";
        String hashedPassword = "$2a$10$hashed";
        String expectedToken = "eyJhbGciOiJIUzI1NiIs...";

        User mockUser = new User();
        mockUser.setId(1L);
        mockUser.setEmail(email);
        mockUser.setPassword(hashedPassword);
        mockUser.setRole(Role.CLIENT);

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(mockUser));
        when(passwordEncoder.matches(rawPassword, hashedPassword)).thenReturn(true);
        when(jwtService.generateToken(email, Role.CLIENT, 1L)).thenReturn(expectedToken);

        // ACT
        String token = userService.login(email, rawPassword);

        // ASSERT
        assertEquals(expectedToken, token, "Le token retourné doit être celui du JwtService");
        verify(jwtService, times(1)).generateToken(email, Role.CLIENT, 1L);
    }

    // ==========================================================
    // TEST 6 : Login avec email inexistant → Exception
    // ==========================================================
    @Test
    public void testLogin_WhenUserNotFound_ShouldThrowException() {
        // ARRANGE
        when(userRepository.findByEmail("inconnu@fly2us.tn")).thenReturn(Optional.empty());

        // ACT + ASSERT
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            userService.login("inconnu@fly2us.tn", "password");
        });

        assertTrue(exception.getMessage().contains("Utilisateur non trouvé"));

        // On vérifie qu'on n'a jamais essayé de générer un token
        verify(jwtService, never()).generateToken(anyString(), any(), any());
    }

    // ==========================================================
    // TEST 7 : Login avec mauvais mot de passe → Exception
    // ==========================================================
    @Test
    public void testLogin_WhenPasswordIsWrong_ShouldThrowException() {
        // ARRANGE
        String email = "rania@fly2us.tn";
        User mockUser = new User();
        mockUser.setEmail(email);
        mockUser.setPassword("$2a$10$hashed");

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(mockUser));
        when(passwordEncoder.matches("mauvaisPass", "$2a$10$hashed")).thenReturn(false);

        // ACT + ASSERT
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            userService.login(email, "mauvaisPass");
        });

        assertEquals("Mot de passe incorrect", exception.getMessage());
        verify(jwtService, never()).generateToken(anyString(), any(), any());
    }

    // ==========================================================
    // TEST 8 : getAllUsers retourne la liste complète
    // ==========================================================
    @Test
    public void testGetAllUsers_ShouldReturnAllUsers() {
        // ARRANGE
        User user1 = new User();
        user1.setEmail("user1@fly2us.tn");
        user1.setFirstName("User1");

        User user2 = new User();
        user2.setEmail("user2@fly2us.tn");
        user2.setFirstName("User2");

        when(userRepository.findAll()).thenReturn(Arrays.asList(user1, user2));

        // ACT
        List<UserDto> result = userService.getAllUsers();

        // ASSERT
        assertNotNull(result);
        assertEquals(2, result.size(), "Il doit y avoir 2 utilisateurs retournés");
        verify(userRepository, times(1)).findAll();
    }

    // ==========================================================
    // TEST 9 : getUserById avec ID inexistant → Exception
    // ==========================================================
    @Test
    public void testGetUserById_WhenNotFound_ShouldThrowException() {
        // ARRANGE
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        // ACT + ASSERT
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            userService.getUserById(999L);
        });

        assertTrue(exception.getMessage().contains("Utilisateur non trouvé"));
    }

    // ==========================================================
    // TEST 10 : deleteUser appelle bien le repository
    // ==========================================================
    @Test
    public void testDeleteUser_ShouldCallRepositoryDelete() {
        // ARRANGE
        Long userId = 5L;
        doNothing().when(userRepository).deleteById(userId);

        // ACT
        userService.deleteUser(userId);

        // ASSERT
        verify(userRepository, times(1)).deleteById(userId);
    }
}