package com.habitpet.controllers;

import com.habitpet.dtos.CreateRecordRequest;
import com.habitpet.dtos.RecordResponse;
import com.habitpet.models.User;
import com.habitpet.services.RecordService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/records")
@RequiredArgsConstructor
public class RecordController {

    private final RecordService recordService;

    @PostMapping
    public ResponseEntity<RecordResponse> checkIn(
            @AuthenticationPrincipal User currentUser,
            @Valid @RequestBody CreateRecordRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(recordService.checkIn(currentUser.getId(), request));
    }
}