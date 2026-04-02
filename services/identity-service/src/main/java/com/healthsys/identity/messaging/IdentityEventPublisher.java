package com.healthsys.identity.messaging;

import com.healthsys.identity.config.MessagingProperties;
import com.healthsys.identity.user.domain.UserAccount;
import java.time.Instant;
import java.util.Map;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Component
public class IdentityEventPublisher {

    private final RabbitTemplate rabbitTemplate;
    private final MessagingProperties messagingProperties;

    public IdentityEventPublisher(RabbitTemplate rabbitTemplate, MessagingProperties messagingProperties) {
        this.rabbitTemplate = rabbitTemplate;
        this.messagingProperties = messagingProperties;
    }

    public void publishUserCreated(UserAccount userAccount) {
        Map<String, Object> payload = Map.of(
            "eventType", "USER_CREATED",
            "resourceId", userAccount.getId().toString(),
            "occurredAt", Instant.now().toString(),
            "payload", Map.of(
                "email", userAccount.getEmail(),
                "name", userAccount.getName(),
                "role", userAccount.getRole().name()
            )
        );

        rabbitTemplate.convertAndSend(messagingProperties.getExchange(), "healthsys.user.created", payload);
    }
}
