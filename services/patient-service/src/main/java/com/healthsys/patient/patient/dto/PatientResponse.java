package com.healthsys.patient.patient.dto;

import com.healthsys.patient.patient.domain.Sex;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;

public record PatientResponse(
    UUID id,
    String name,
    LocalDate birthDate,
    Sex sex,
    String phone,
    boolean active,
    OffsetDateTime createdAt
) {
}
