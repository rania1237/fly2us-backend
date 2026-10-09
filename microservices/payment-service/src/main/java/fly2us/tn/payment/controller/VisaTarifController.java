package fly2us.tn.payment.controller;

import fly2us.tn.payment.model.VisaTarif;
import fly2us.tn.payment.service.VisaTarifService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/visa-tarifs")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:4200")
public class VisaTarifController {

    private final VisaTarifService service;

    @GetMapping
    public ResponseEntity<List<VisaTarif>> getAll() {
        return ResponseEntity.ok(service.getAll());
    }

    @GetMapping("/active")
    public ResponseEntity<List<VisaTarif>> getActive() {
        return ResponseEntity.ok(service.getActive());
    }

    @GetMapping("/{id}")
    public ResponseEntity<VisaTarif> getById(@PathVariable Long id) {
        return ResponseEntity.ok(service.getById(id));
    }

    // ✅ Nouveau : recherche par type + priorité
    @GetMapping("/search")
    public ResponseEntity<VisaTarif> search(
            @RequestParam String visaType,
            @RequestParam String priority) {
        return ResponseEntity.ok(service.getTarifFor(visaType, priority));
    }

    @PostMapping
    public ResponseEntity<VisaTarif> create(@RequestBody VisaTarif tarif) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(tarif));
    }

    @PutMapping("/{id}")
    public ResponseEntity<VisaTarif> update(@PathVariable Long id, @RequestBody VisaTarif tarif) {
        return ResponseEntity.ok(service.update(id, tarif));
    }

    // ✅ Nouveau : sauvegarde en masse (grille 3x3)
    @PostMapping("/batch")
    public ResponseEntity<Void> saveAll(@RequestBody List<VisaTarif> tarifs) {
        service.saveAll(tarifs);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}