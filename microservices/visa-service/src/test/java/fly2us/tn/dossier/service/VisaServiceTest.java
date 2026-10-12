package fly2us.tn.dossier.service;

import fly2us.tn.dossier.model.Visa;
import fly2us.tn.dossier.model.VisaType;
import fly2us.tn.dossier.repository.VisaRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class VisaServiceTest {

    @Mock
    private VisaRepository visaRepository;

    @InjectMocks
    private VisaService visaService;

    // ==========================================================
    // TEST 1 : Récupérer tous les visas
    // ==========================================================
    @Test
    public void testGetAllVisas_ShouldReturnAllVisas() {
        // ARRANGE
        Visa visa1 = new Visa();
        visa1.setCountry("France");
        visa1.setType(VisaType.ETUDIANT);

        Visa visa2 = new Visa();
        visa2.setCountry("Canada");
        visa2.setType(VisaType.TOURISTIQUE);

        when(visaRepository.findAll()).thenReturn(Arrays.asList(visa1, visa2));

        // ACT
        List<Visa> result = visaService.getAllVisas();

        // ASSERT
        assertNotNull(result);
        assertEquals(2, result.size(), "Doit retourner 2 visas");
        assertEquals("France", result.get(0).getCountry());
        verify(visaRepository, times(1)).findAll();
    }

    // ==========================================================
    // TEST 2 : Récupérer un visa par ID (cas existant)
    // ==========================================================
    @Test
    public void testGetVisaById_WhenExists_ShouldReturnVisa() {
        // ARRANGE
        Long visaId = 1L;
        Visa visa = new Visa();
        visa.setId(visaId);
        visa.setCountry("France");
        visa.setPrice(150.0);

        when(visaRepository.findById(visaId)).thenReturn(Optional.of(visa));

        // ACT
        Visa result = visaService.getVisaById(visaId);

        // ASSERT
        assertNotNull(result);
        assertEquals(visaId, result.getId());
        assertEquals("France", result.getCountry());
        assertEquals(150.0, result.getPrice());
    }

    // ==========================================================
    // TEST 3 : Récupérer un visa par ID inexistant → Exception
    // ==========================================================
    @Test
    public void testGetVisaById_WhenNotFound_ShouldThrowException() {
        // ARRANGE
        when(visaRepository.findById(999L)).thenReturn(Optional.empty());

        // ACT + ASSERT
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            visaService.getVisaById(999L);
        });

        assertEquals("Visa non trouvé", exception.getMessage());
    }

    // ==========================================================
    // TEST 4 : Récupérer les visas par type
    // ==========================================================
    @Test
    public void testGetVisasByType_ShouldReturnVisasOfThatType() {
        // ARRANGE
        Visa visa1 = new Visa();
        visa1.setCountry("France");
        visa1.setType(VisaType.ETUDIANT);

        when(visaRepository.findByType(VisaType.ETUDIANT))
                .thenReturn(Arrays.asList(visa1));

        // ACT : On envoie "etudiant" en minuscules pour tester la conversion
        List<Visa> result = visaService.getVisasByType("etudiant");

        // ASSERT
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(VisaType.ETUDIANT, result.get(0).getType());

        // Vérifie que la méthode du repository a bien été appelée avec l'enum converti
        verify(visaRepository, times(1)).findByType(VisaType.ETUDIANT);
    }

    // ==========================================================
    // TEST 5 : Créer un visa quand le pays est nouveau
    // ==========================================================
    @Test
    public void testCreateVisa_WhenCountryIsNew_ShouldSaveVisa() {
        // ARRANGE
        Visa newVisa = new Visa();
        newVisa.setCountry("Espagne");
        newVisa.setType(VisaType.SCHENGEN);
        newVisa.setPrice(200.0);

        when(visaRepository.existsByCountry("Espagne")).thenReturn(false);
        when(visaRepository.save(any(Visa.class))).thenAnswer(i -> i.getArgument(0));

        // ACT
        Visa result = visaService.createVisa(newVisa);

        // ASSERT
        assertNotNull(result);
        assertEquals("Espagne", result.getCountry());
        assertEquals(VisaType.SCHENGEN, result.getType());
        verify(visaRepository, times(1)).save(any(Visa.class));
    }

    // ==========================================================
    // TEST 6 : Créer un visa avec un pays déjà existant → Exception
    // ==========================================================
    @Test
    public void testCreateVisa_WhenCountryExists_ShouldThrowException() {
        // ARRANGE
        Visa newVisa = new Visa();
        newVisa.setCountry("France");

        when(visaRepository.existsByCountry("France")).thenReturn(true);

        // ACT + ASSERT
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            visaService.createVisa(newVisa);
        });

        assertEquals("Ce pays existe déjà", exception.getMessage());

        // Vérifie que save n'a JAMAIS été appelé
        verify(visaRepository, never()).save(any(Visa.class));
    }

    // ==========================================================
    // TEST 7 : Mettre à jour un visa existant
    // ==========================================================
    @Test
    public void testUpdateVisa_WhenExists_ShouldUpdateFields() {
        // ARRANGE
        Long visaId = 1L;

        // Visa existant en base
        Visa existingVisa = new Visa();
        existingVisa.setId(visaId);
        existingVisa.setCountry("AncienPays");
        existingVisa.setPrice(100.0);

        // Nouvelles données à appliquer
        Visa updatedDetails = new Visa();
        updatedDetails.setCountry("NouveauPays");
        updatedDetails.setPrice(250.0);
        updatedDetails.setType(VisaType.TOURISTIQUE);
        updatedDetails.setDuration("30 jours");
        updatedDetails.setProcessingTime("15 jours");

        when(visaRepository.findById(visaId)).thenReturn(Optional.of(existingVisa));
        when(visaRepository.save(any(Visa.class))).thenAnswer(i -> i.getArgument(0));

        // ACT
        Visa result = visaService.updateVisa(visaId, updatedDetails);

        // ASSERT
        assertNotNull(result);
        assertEquals("NouveauPays", result.getCountry(), "Le pays doit être mis à jour");
        assertEquals(250.0, result.getPrice(), "Le prix doit être mis à jour");
        assertEquals(VisaType.TOURISTIQUE, result.getType());
        assertEquals("30 jours", result.getDuration());
        verify(visaRepository, times(1)).save(any(Visa.class));
    }

    // ==========================================================
    // TEST 8 : Supprimer un visa
    // ==========================================================
    @Test
    public void testDeleteVisa_ShouldCallRepositoryDelete() {
        // ARRANGE
        Long visaId = 3L;
        doNothing().when(visaRepository).deleteById(visaId);

        // ACT
        visaService.deleteVisa(visaId);

        // ASSERT
        verify(visaRepository, times(1)).deleteById(visaId);
    }
}