package fly2us.tn.dossier.service;

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

    private final Path rootLocation = Paths.get("uploads");

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

            // Générer un nom unique
            String fileName = UUID.randomUUID() + "_" + file.getOriginalFilename();
            Path destinationFile = rootLocation.resolve(fileName);

            // Copier le fichier
            Files.copy(file.getInputStream(), destinationFile, StandardCopyOption.REPLACE_EXISTING);

            return destinationFile.toString();
        } catch (IOException e) {
            throw new RuntimeException("Erreur lors du stockage du fichier", e);
        }
    }

    // ====== SUPPRIMER UN FICHIER ======
    public void delete(String filePath) {
        try {
            Files.deleteIfExists(Paths.get(filePath));
        } catch (IOException e) {
            throw new RuntimeException("Erreur lors de la suppression du fichier", e);
        }
    }

    // ====== CHARGER UN FICHIER ======
    public byte[] load(String filePath) {
        try {
            return Files.readAllBytes(Paths.get(filePath));
        } catch (IOException e) {
            throw new RuntimeException("Erreur lors du chargement du fichier", e);
        }
    }
}