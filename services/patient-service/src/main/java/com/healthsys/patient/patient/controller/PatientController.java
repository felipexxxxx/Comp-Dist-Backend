package com.healthsys.patient.patient.controller;

import com.healthsys.patient.patient.dto.CreatePatientRequest;
import com.healthsys.patient.patient.dto.PatientResponse;
import com.healthsys.patient.patient.dto.UpdatePatientRequest;
import com.healthsys.patient.patient.service.PatientService;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/patients")
public class PatientController {

    private final PatientService patientService;

    public PatientController(PatientService patientService) {
        this.patientService = patientService;
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPTIONIST')")
    @ResponseStatus(HttpStatus.CREATED)
    public PatientResponse createPatient(@Valid @RequestBody CreatePatientRequest request) {
        return patientService.createPatient(request);
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPTIONIST', 'HEALTH_PROFESSIONAL')")
    public List<PatientResponse> listPatients() {
        return patientService.listPatients();
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPTIONIST', 'HEALTH_PROFESSIONAL')")
    public PatientResponse getPatient(@PathVariable UUID id) {
        return patientService.getPatient(id);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPTIONIST')")
    public PatientResponse updatePatient(@PathVariable UUID id, @Valid @RequestBody UpdatePatientRequest request) {
        return patientService.updatePatient(id, request);
    }
}
