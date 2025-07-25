package Team3rd.DaeCar.DaeCar.global.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {
    
    public static final String ROOM_EXCHANGE = "room.exchange";
    public static final String ROOM_CREATED_QUEUE = "room.created.queue";
    public static final String ROOM_JOINED_QUEUE = "room.joined.queue";
    public static final String ROOM_CREATED_ROUTING_KEY = "room.created";
    public static final String ROOM_JOINED_ROUTING_KEY = "room.joined";
    
    @Bean
    public TopicExchange roomExchange() {
        return new TopicExchange(ROOM_EXCHANGE);
    }
    
    @Bean
    public Queue roomCreatedQueue() {
        return QueueBuilder.durable(ROOM_CREATED_QUEUE).build();
    }
    
    @Bean
    public Queue roomJoinedQueue() {
        return QueueBuilder.durable(ROOM_JOINED_QUEUE).build();
    }
    
    @Bean
    public Binding roomCreatedBinding() {
        return BindingBuilder
            .bind(roomCreatedQueue())
            .to(roomExchange())
            .with(ROOM_CREATED_ROUTING_KEY);
    }
    
    @Bean
    public Binding roomJoinedBinding() {
        return BindingBuilder
            .bind(roomJoinedQueue())
            .to(roomExchange())
            .with(ROOM_JOINED_ROUTING_KEY);
    }
    
    @Bean
    public Jackson2JsonMessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }
    
    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(messageConverter());
        return template;
    }
}