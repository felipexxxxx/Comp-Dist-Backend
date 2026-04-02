package com.healthsys.notification.listener;

import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class NotificationEventListener {

    private static final Logger LOGGER = LoggerFactory.getLogger(NotificationEventListener.class);

    @RabbitListener(queues = "${app.messaging.queue}")
    public void handleNotificationEvent(Map<String, Object> payload) {
        LOGGER.info("Notification event received: {}", payload);
    }
}
