package org.example.nexfit.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.example.nexfit.service.TrainerAdminService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/admin/trainers")
@RequiredArgsConstructor
@Tag(name = "Admin Trainer Approval", description = "Admin APIs for trainer approval")
public class AdminTrainerController {

    private final TrainerAdminService trainerAdminService;

    @PostMapping("/{id}/approve")
    @Operation(summary = "Approve trainer")
    public ResponseEntity<Map<String, String>> approveTrainer(@PathVariable String id) {
        var trainer = trainerAdminService.approveTrainer(id);
        return ResponseEntity.ok(Map.of("status", trainer.getStatus().name().toLowerCase()));
    }

    @PostMapping("/{id}/reject")
    @Operation(summary = "Reject trainer")
    public ResponseEntity<Map<String, String>> rejectTrainer(@PathVariable String id) {
        var trainer = trainerAdminService.rejectTrainer(id);
        return ResponseEntity.ok(Map.of("status", trainer.getStatus().name().toLowerCase()));
    }
}
