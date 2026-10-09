package fly2us.tn.dossier.controller;

import fly2us.tn.dossier.model.DocumentRequirement;
import fly2us.tn.dossier.service.DocumentRequirementService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/document-requirements")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:4200")  // ✅ AJOUTER
public class DocumentRequirementController {

    private final DocumentRequirementService service;

    @GetMapping
    public ResponseEntity<List<DocumentRequirement>> getAll() {
        return ResponseEntity.ok(service.getAll());
    }

    @GetMapping("/visa-type/{visaType}")
    public ResponseEntity<List<DocumentRequirement>> getByVisaType(@PathVariable String visaType) {
        return ResponseEntity.ok(service.getByVisaType(visaType));
    }

    @GetMapping("/{id}")
    public ResponseEntity<DocumentRequirement> getById(@PathVariable Long id) {
        return ResponseEntity.ok(service.getById(id));
    }

    @PostMapping
    public ResponseEntity<DocumentRequirement> create(@RequestBody DocumentRequirement req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(req));
    }

    @PutMapping("/{id}")
    public ResponseEntity<DocumentRequirement> update(@PathVariable Long id, @RequestBody DocumentRequirement req) {
        return ResponseEntity.ok(service.update(id, req));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}