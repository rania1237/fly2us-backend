package fly2us.tn.dossier.controller;  // ✅ Package correct

import fly2us.tn.dossier.model.Visa;
import fly2us.tn.dossier.service.VisaService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/visas")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:4200")
public class VisaController {

    private final VisaService visaService;

    @GetMapping
    public ResponseEntity<List<Visa>> getAllVisas() {
        return ResponseEntity.ok(visaService.getAllVisas());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Visa> getVisaById(@PathVariable Long id) {
        return ResponseEntity.ok(visaService.getVisaById(id));
    }

    @GetMapping("/type/{type}")
    public ResponseEntity<List<Visa>> getVisasByType(@PathVariable String type) {
        return ResponseEntity.ok(visaService.getVisasByType(type));
    }

    @PostMapping
    public ResponseEntity<Visa> createVisa(@RequestBody Visa visa) {
        return ResponseEntity.status(HttpStatus.CREATED).body(visaService.createVisa(visa));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Visa> updateVisa(@PathVariable Long id, @RequestBody Visa visa) {
        return ResponseEntity.ok(visaService.updateVisa(id, visa));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteVisa(@PathVariable Long id) {
        visaService.deleteVisa(id);
        return ResponseEntity.noContent().build();
    }
}