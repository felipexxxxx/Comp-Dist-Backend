package com.healthsys.patient.messaging;

import com.healthsys.patient.config.MessagingProperties;
import com.healthsys.patient.patient.domain.Patient;
import java.time.Instant;
import java.util.Map;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Component
public class PatientEventPublisher {

    private final RabbitTemplate rabbitTemplate;
    private final MessagingProperties messagingProperties;

    public PatientEventPublisher(RabbitTemplate rabbitTemplate, MessagingProperties messagingProperties) {
        this.rabbitTemplate = rabbitTemplate;
        this.messagingProperties = messagingProperties;
    }

    public void publishPatientCreated(Patient patient) {
        publish("PATIENT_CREATED", "healthsys.patient.created", patient);
    }

    public void publishPatientUpdated(Patient patient) {
        publish("PATIENT_UPDATED", "healthsys.patient.updated", patient);
    }

    private void publish(String eventType, String routingKey, Patient patient) {
        Map<String, Object> payload = Map.of(
            "eventType", eventType,
            "resourceId", patient.getId().toString(),
            "occurredAt", Instant.now().toString(),
            "payload", Map.of(
                "name", patient.getFullName(),
                "sex", patient.getSex().name(),
                "phone", patient.getPhone(),
                "active", patient.isActive()
            )
        );

        rabbitTemplate.convertAndSend(messagingProperties.getExchange(), routingKey, payload);
    }
}
