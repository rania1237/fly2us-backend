package fly2us.tn.dossier.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Service
public class FileStorageService {

    private static final Logger logger = LoggerFactory.getLogger(FileStorageService.class);

    private final Path rootLocation = Paths.get("uploads").toAbsolutePath().normalize();

    public FileStorageService() {
        try {
            Files.createDirectories(rootLocation);
        } catch (IOException e) {
            throw new RuntimeException("Impossible de créer le dossier d'upload", e);
        }
    }

    // ====== STOCKER UN FICHIER ======
    public String store(MultipartFile file) {
        try {
            // Vérifier que le fichier n'est pas vide
            if (file.isEmpty()) {
                throw new RuntimeException("Le fichier est vide");
            }

            // ✅ SÉCURITÉ : Extraire UNIQUEMENT le nom de fichier (sans chemin)
            String originalFilename = file.getOriginalFilename();
            String safeFilename = (originalFilename != null)
                    ? Paths.get(originalFilename).getFileName().toString()
                    : "unknown";

            // ✅ SÉCURITÉ : Sanitiser le nom (retirer caractères dangereux)
            safeFilename = safeFilename.replaceAll("[^a-zA-Z0-9._-]", "_");

            // Générer un nom unique
            String fileName = UUID.randomUUID() + "_" + safeFilename;
            Path destinationFile = rootLocation.resolve(fileName).normalize();

            // ✅ SÉCURITÉ : Vérifier que le chemin final reste dans rootLocation
            if (!destinationFile.startsWith(rootLocation)) {
                throw new RuntimeException("Tentative de Path Traversal détectée");
            }

            // Copier le fichier
            Files.copy(file.getInputStream(), destinationFile, StandardCopyOption.REPLACE_EXISTING);

            logger.info("✅ Fichier stocké: {}", fileName);
            return destinationFile.toString();
        } catch (IOException e) {
            logger.error("❌ Erreur lors du stockage du fichier: {}", e.getMessage(), e);
            throw new RuntimeException("Erreur lors du stockage du fichier", e);
        }
    }

    // ====== SUPPRIMER UN FICHIER ======
    public void delete(String filePath) {
        try {
            // ✅ SÉCURITÉ : Vérifier que le chemin à supprimer est bien dans rootLocation
            Path fileToDelete = Paths.get(filePath).toAbsolutePath().normalize();
            if (!fileToDelete.startsWith(rootLocation)) {
                throw new RuntimeException("Tentative de suppression hors du dossier d'upload");
            }

            Files.deleteIfExists(fileToDelete);
            logger.info("✅ Fichier supprimé: {}", filePath);
        } catch (IOException e) {
            logger.error("❌ Erreur lors de la suppression du fichier: {}", e.getMessage(), e);
            throw new RuntimeException("Erreur lors de la suppression du fichier", e);
        }
    }

    // ====== CHARGER UN FICHIER ======
    public byte[] load(String filePath) {
        try {
            // ✅ SÉCURITÉ : Vérifier que le chemin à charger est bien dans rootLocation
            Path fileToLoad = Paths.get(filePath).toAbsolutePath().normalize();
            if (!fileToLoad.startsWith(rootLocation)) {
                throw new RuntimeException("Tentative de lecture hors du dossier d'upload");
            }

            return Files.readAllBytes(fileToLoad);
        } catch (IOException e) {
            logger.error("❌ Erreur lors du chargement du fichier: {}", e.getMessage(), e);
            throw new RuntimeException("Erreur lors du chargement du fichier", e);
        }
    }
}