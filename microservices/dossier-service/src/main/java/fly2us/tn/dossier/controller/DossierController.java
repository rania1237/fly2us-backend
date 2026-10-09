package fly2us.tn.dossier.controller;

import fly2us.tn.dossier.model.Dossier;
import fly2us.tn.dossier.model.DossierDocument;
import fly2us.tn.dossier.service.DossierService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/dossiers")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:4200")
public class DossierController {

    private final DossierService dossierService;

    @PostMapping
    public ResponseEntity<Dossier> createDossier(@RequestBody Dossier dossier) {
        return ResponseEntity.status(HttpStatus.CREATED).body(dossierService.createDossier(dossier));
    }

    @GetMapping("/{dossierId}")
    public ResponseEntity<Dossier> getDossierById(@PathVariable Long dossierId) {
        return ResponseEntity.ok(dossierService.getDossierById(dossierId));
    }

    @PostMapping("/{dossierId}/documents/{requirementId}")
    public ResponseEntity<DossierDocument> uploadDocument(
            @PathVariable Long dossierId,
            @PathVariable Long requirementId,
            @RequestParam("file") MultipartFile file,
            @RequestParam Long userId) {
        return ResponseEntity.ok(dossierService.uploadDocument(dossierId, requirementId, file, userId));
    }

    @GetMapping("/{dossierId}/documents")
    public ResponseEntity<List<DossierDocument>> getDocumentsByDossier(@PathVariable Long dossierId) {
        return ResponseEntity.ok(dossierService.getDocumentsByDossier(dossierId));
    }

    @GetMapping("/{dossierId}/is-complete")
    public ResponseEntity<Boolean> isComplete(@PathVariable Long dossierId) {
        return ResponseEntity.ok(dossierService.isDossierComplete(dossierId));
    }

    @PostMapping("/{dossierId}/submit")
    public ResponseEntity<Dossier> submitDossier(
            @PathVariable Long dossierId,
            @RequestParam Long clientId) {
        return ResponseEntity.ok(dossierService.submitDossier(dossierId, clientId));
    }

    @PatchMapping("/{dossierId}/status")
    public ResponseEntity<Dossier> updateStatus(
            @PathVariable Long dossierId,
            @RequestBody Map<String, String> body) {
        String status = body.get("status");
        String comment = body.get("comment");

        Dossier dossier = dossierService.getDossierById(dossierId);
        dossier.setStatus(status);
        if (comment != null && !comment.isEmpty()) {
            dossier.setNotes(comment);
        }
        return ResponseEntity.ok(dossierService.updateDossier(dossierId, dossier));
    }

    @PutMapping("/documents/{documentId}/validate")
    public ResponseEntity<DossierDocument> validateDocument(
            @PathVariable Long documentId,
            @RequestParam Long adminId) {
        return ResponseEntity.ok(dossierService.validateDocument(documentId, adminId));
    }

    @PutMapping("/documents/{documentId}/reject")
    public ResponseEntity<DossierDocument> rejectDocument(
            @PathVariable Long documentId,
            @RequestParam Long adminId,
            @RequestParam String reason) {
        return ResponseEntity.ok(dossierService.rejectDocument(documentId, adminId, reason));
    }

    // ✅ NOUVELLE ROUTE : VOIR / TÉLÉCHARGER UN FICHIER
    @GetMapping("/documents/{documentId}/file")
    public ResponseEntity<byte[]> getDocumentFile(@PathVariable Long documentId) {
        try {
            DossierDocument doc = dossierService.getDocumentById(documentId);
            byte[] fileContent = java.nio.file.Files.readAllBytes(
                    java.nio.file.Paths.get(doc.getFilePath())
            );

            String contentType = doc.getFileType() != null ? doc.getFileType() : "application/octet-stream";

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.parseMediaType(contentType));
            headers.setContentDispositionFormData("inline", doc.getFileName());

            return new ResponseEntity<>(fileContent, headers, HttpStatus.OK);
        } catch (Exception e) {
            throw new RuntimeException("Erreur lecture fichier : " + e.getMessage());
        }
    }

    @GetMapping("/{dossierId}/pdf")
    public ResponseEntity<byte[]> downloadPdf(@PathVariable Long dossierId) {
        byte[] pdf = dossierService.generateDossierPdf(dossierId);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDispositionFormData("attachment", "dossier-" + dossierId + ".pdf");

        return new ResponseEntity<>(pdf, headers, HttpStatus.OK);
    }

    @GetMapping
    public ResponseEntity<List<Dossier>> getAllDossiers() {
        return ResponseEntity.ok(dossierService.getAllDossiers());
    }

    @GetMapping("/client/{clientId}")
    public ResponseEntity<List<Dossier>> getDossiersByClient(@PathVariable Long clientId) {
        return ResponseEntity.ok(dossierService.getDossiersByClient(clientId));
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<List<Dossier>> getDossiersByStatus(@PathVariable String status) {
        return ResponseEntity.ok(dossierService.getDossiersByStatus(status));
    }

    @DeleteMapping("/{dossierId}")
    public ResponseEntity<Void> deleteDossier(@PathVariable Long dossierId) {
        dossierService.deleteDossier(dossierId);
        return ResponseEntity.noContent().build();
    }
}