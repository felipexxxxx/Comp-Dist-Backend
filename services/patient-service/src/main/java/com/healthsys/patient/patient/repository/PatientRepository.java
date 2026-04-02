package com.healthsys.patient.patient.repository;

import com.healthsys.patient.patient.domain.Patient;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PatientRepository extends JpaRepository<Patient, UUID> {

    List<Patient> findAllByOrderByCreatedAtDesc();
}
