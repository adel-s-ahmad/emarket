package com.emarket.product.rabbitmq;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import com.emarket.product.controller.ProductsController;
import com.emarket.product.models.Order;
@Service
public class OrderProcessedProducer {
	
	Logger logger = LoggerFactory.getLogger(ProductsController.class);
	
	@Autowired
	@Qualifier("orderProcessedQueue")
	private Queue queue;

	@Autowired
	private RabbitTemplate rabbitTemplate;

	public void sendMsg(Order order) {
		rabbitTemplate.convertAndSend(queue.getName(), order);
		logger.info("Sent processed order with id: " + order.getId() + " to queue: " + queue.getName());
	}
}
