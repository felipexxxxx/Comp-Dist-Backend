package com.healthsys.notification.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitConfig {

    @Bean
    public TopicExchange topicExchange(MessagingProperties properties) {
        return new TopicExchange(properties.getExchange());
    }

    @Bean
    public Queue notificationsQueue(MessagingProperties properties) {
        return new Queue(properties.getQueue(), true);
    }

    @Bean
    public Binding notificationsBinding(
        Queue notificationsQueue,
        TopicExchange topicExchange,
        MessagingProperties properties
    ) {
        return BindingBuilder.bind(notificationsQueue)
            .to(topicExchange)
            .with(properties.getRoutingKey());
    }

    @Bean
    public Jackson2JsonMessageConverter jackson2JsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }
}
