package com.scaleup.publicapi;

import com.scaleup.publicapi.dto.CreateLeadRequest;
import com.scaleup.publicapi.dto.LeadCreatedResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/public/leads")
public class PublicLeadController {

    private final PublicLeadService publicLeadService;

    public PublicLeadController(
            PublicLeadService publicLeadService
    ) {
        this.publicLeadService =
                publicLeadService;
    }

    @PostMapping
    public ResponseEntity<LeadCreatedResponse> createLead(
            @Valid
            @RequestBody
            CreateLeadRequest request
    ) {

        LeadCreatedResponse response =
                publicLeadService.createLead(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }
}