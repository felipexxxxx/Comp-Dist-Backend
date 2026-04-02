package com.healthsys.patient.patient.dto;

import com.healthsys.patient.patient.domain.Sex;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;

public record CreatePatientRequest(
    @NotBlank @Size(max = 150) String name,
    @NotNull @Past LocalDate birthDate,
    @NotNull Sex sex,
    @NotBlank @Size(max = 30) String phone
) {
}
