package com.habitpet.controllers;

import com.habitpet.dtos.CreateHabitRequest;
import com.habitpet.dtos.HabitResponse;
import com.habitpet.models.User;
import com.habitpet.services.HabitService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/habits")
@RequiredArgsConstructor
public class HabitController {

    private final HabitService habitService;

    @PostMapping
    public ResponseEntity<HabitResponse> create(
            @AuthenticationPrincipal User currentUser,
            @Valid @RequestBody CreateHabitRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(habitService.create(currentUser.getId(), request));
    }

    @GetMapping
    public ResponseEntity<List<HabitResponse>> list(
            @AuthenticationPrincipal User currentUser) {
        return ResponseEntity.ok(habitService.listActive(currentUser.getId()));
    }

    @PutMapping("/{id}")
    public ResponseEntity<HabitResponse> update(
            @AuthenticationPrincipal User currentUser,
            @PathVariable String id,
            @Valid @RequestBody CreateHabitRequest request) {
        return ResponseEntity.ok(habitService.update(currentUser.getId(), id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> archive(
            @AuthenticationPrincipal User currentUser,
            @PathVariable String id) {
        habitService.archive(currentUser.getId(), id);
        return ResponseEntity.noContent().build();
    }
}
