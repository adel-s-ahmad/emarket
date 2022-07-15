package com.emarket.product.rabbitmq;

import java.util.HashMap;
import java.util.Map;

import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.DefaultClassMapper;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MarshallingMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.amqp.support.converter.SimpleMessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import com.emarket.product.models.Order;
import com.fasterxml.jackson.databind.ObjectMapper;

@Configuration
public class OrderPlacedQueueConfig {
	@Value("${emarket.orderplaced.queue.name}")
	private String orderPlacedQueueName;

	@Value("${emarket.orderprocessed.queue.name}")
	private String orderProcessedQueueName;

	@Bean
	public Queue orderPlaceQueue() {
		return new Queue(orderPlacedQueueName);
	}
	
	@Bean
	public Queue orderProcessedQueue() {
		return new Queue(orderProcessedQueueName);
	}

//	@Bean
//	public OrderPlacedProducer producer() {
//		return new OrderPlacedProducer();
//	}

	@Bean
    public ObjectMapper getObjectMapper() {
        return new ObjectMapper();
    }
	 
	@Bean
	public MessageConverter jsonMessageConverter() {
		Jackson2JsonMessageConverter jsonConverter = new Jackson2JsonMessageConverter();
		jsonConverter.setClassMapper(classMapper());
		return jsonConverter;
	}
	
	@Bean
	public DefaultClassMapper classMapper() {
	    DefaultClassMapper classMapper = new DefaultClassMapper();
	    Map<String, Class<?>> idClassMapping = new HashMap<>();
	    idClassMapping.put("com.emarket.order.models.Order", Order.class);
	    classMapper.setIdClassMapping(idClassMapping);
	    classMapper.setTrustedPackages("*");
	    return classMapper;
	}
	
	@Bean
	public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
		final RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
		rabbitTemplate.setMessageConverter(jsonMessageConverter());
		return rabbitTemplate;
	}
	
	@Bean
	public OrderProcessedProducer producer() {
		return new OrderProcessedProducer();
	}

	@Bean
	public OrderPlacedConsumer consumer() {
		return new OrderPlacedConsumer();
	}
}
