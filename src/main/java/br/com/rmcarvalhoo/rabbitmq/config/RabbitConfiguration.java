package br.com.rmcarvalhoo.rabbitmq.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.converter.MappingJackson2MessageConverter;
import org.springframework.messaging.handler.annotation.support.DefaultMessageHandlerMethodFactory;
import org.springframework.messaging.handler.annotation.support.MessageHandlerMethodFactory;

@Slf4j
@Configuration
public class RabbitConfiguration {

    private static final String EXCHANGE_NAME = "retry-exchange";
    public static final String QUEUE = "mainQueue";
    private static final String RETRY_QUEUE = "mainQueue.retry";
    public static final String UNDELIVERED_QUEUE = "mainQueue.undelivered";
    private static final String MAIN_ROUTING_KEY = "Routing_Key";

    @Value("${rabbitmq.retry.delay-in-ms}")
    private Integer retryDelay;

    @Bean
    public RabbitTemplate rabbitTemplate(final ConnectionFactory connectionFactory) {
        log.info("## Creating RabbitTemplate");
        final RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMessageConverter(producerJackson2MessageConverter());
        return rabbitTemplate;
    }

    @Bean
    MessageHandlerMethodFactory messageHandlerMethodFactory() {
        log.info("## Creating MessageHandlerMethodFactory");
        DefaultMessageHandlerMethodFactory messageHandlerMethodFactory = new DefaultMessageHandlerMethodFactory();
        messageHandlerMethodFactory.setMessageConverter(new MappingJackson2MessageConverter());
        return messageHandlerMethodFactory;
    }

    @Bean
    public Jackson2JsonMessageConverter producerJackson2MessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    TopicExchange exchange() {
        log.info("## Creating TopicExchange");
        return new TopicExchange(EXCHANGE_NAME);
    }

    @Bean
    Queue mainQueue() {
        log.info("## Creating mainQueue");
        return QueueBuilder.durable(QUEUE)
                .deadLetterExchange(EXCHANGE_NAME)
                .deadLetterRoutingKey(RETRY_QUEUE)
                .quorum()
                .build();
    }

    @Bean
    Queue retryQueue() {
        log.info("## Creating retryQueue");
        return QueueBuilder.durable(RETRY_QUEUE)
                .deadLetterExchange(EXCHANGE_NAME)
                .deadLetterRoutingKey(MAIN_ROUTING_KEY)
                .ttl(retryDelay)
                .quorum()
                .build();
    }

    @Bean
    Queue undeliveredQueue() {
        log.info("## Creating undeliveredQueue");
        return QueueBuilder.durable(UNDELIVERED_QUEUE)
                .quorum()
                .build();
    }

    @Bean
    Binding mainBinding(Queue mainQueue, TopicExchange exchange) {
        log.info("## Creating mainBinding");
        return BindingBuilder.bind(mainQueue).to(exchange).with(MAIN_ROUTING_KEY);
    }

    @Bean
    Binding retryBinding(Queue retryQueue, TopicExchange exchange) {
        log.info("## Creating retryBinding");
        return BindingBuilder.bind(retryQueue).to(exchange).with(RETRY_QUEUE);
    }

    @Bean
    Binding undeliveredBinding(Queue undeliveredQueue, TopicExchange exchange) {
        log.info("## Creating undeliveredBinding");
        return BindingBuilder.bind(undeliveredQueue).to(exchange).with(UNDELIVERED_QUEUE);
    }

}
