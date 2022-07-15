package com.emarket.order.rabbitmq;

import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.emarket.order.models.Order;

public class OrderPlacedProducer {
	@Autowired
	private Queue queue;

	@Autowired
	private RabbitTemplate rabbitTemplate;

	public void sendMsg(Order order) {
		rabbitTemplate.convertAndSend(queue.getName(), order);
		System.out.println("Sent order with id: " + order.getId());
	}
}
