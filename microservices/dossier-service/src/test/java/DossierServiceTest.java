import fly2us.tn.dossier.client.NotificationClient;
import fly2us.tn.dossier.model.Dossier;
import fly2us.tn.dossier.repository.DossierDocumentRepository;
import fly2us.tn.dossier.repository.DossierRepository;
import fly2us.tn.dossier.repository.DocumentRequirementRepository;
import fly2us.tn.dossier.service.DossierService;
import fly2us.tn.dossier.service.FileStorageService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class) // Active Mockito pour JUnit 5
public class DossierServiceTest {

    // 1. On simule (mock) toutes les dépendances de DossierService
    @Mock
    private DossierRepository dossierRepository;

    @Mock
    private DossierDocumentRepository documentRepository;

    @Mock
    private DocumentRequirementRepository requirementRepository;

    @Mock
    private FileStorageService fileStorageService;

    @Mock
    private NotificationClient notificationClient;

    // 2. On injecte ces mocks dans le service que l'on veut tester
    @InjectMocks
    private DossierService dossierService;

    // ==========================================
    // TEST 1 : Création d'un dossier
    // ==========================================
    @Test
    public void testCreateDossier_ShouldGenerateNumberAndSetStatusBrouillon() {
        // ARRANGE (Préparation)
        Dossier dossier = new Dossier();
        dossier.setType("ETUDIANT");
        dossier.setCountry("France");

        // On dit à Mockito : "Quand on appelle save(), retourne le dossier qu'on t'envoie"
        when(dossierRepository.save(any(Dossier.class))).thenAnswer(i -> i.getArgument(0));

        // ACT (Action)
        Dossier result = dossierService.createDossier(dossier);

        // ASSERT (Vérification)
        assertNotNull(result, "Le dossier retourné ne doit pas être null");
        assertTrue(result.getDossierNumber().startsWith("DOS-"), "Le numéro doit commencer par DOS-");
        assertEquals("BROUILLON", result.getStatus(), "Le statut initial doit être BROUILLON");
    }

    // ==========================================
    // TEST 2 : Vérifier si le dossier est complet (cas vide)
    // ==========================================
    @Test
    public void testIsDossierComplete_WhenNoRequirements_ShouldReturnTrue() {
        // ARRANGE
        Dossier dossier = new Dossier();
        dossier.setId(1L);
        dossier.setType("TOURISTIQUE");

        when(dossierRepository.findById(1L)).thenReturn(java.util.Optional.of(dossier));
        // On simule qu'il n'y a aucun document requis pour ce type de visa
        when(requirementRepository.findByVisaTypeAndRequired("TOURISTIQUE", true))
                .thenReturn(java.util.Collections.emptyList());

        // ACT
        boolean isComplete = dossierService.isDossierComplete(1L);

        // ASSERT
        assertTrue(isComplete, "Un dossier sans documents requis doit être considéré comme complet");
    }
}