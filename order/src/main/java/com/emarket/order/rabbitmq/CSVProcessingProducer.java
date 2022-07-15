package com.emarket.order.rabbitmq;

import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import com.emarket.order.models.Order;

@Service
public class CSVProcessingProducer {
	@Autowired
	@Qualifier("csvProcessingQueue")
	private Queue queue;

	@Autowired
	private RabbitTemplate rabbitTemplate;

	public void sendMsg(String filePath) {
		rabbitTemplate.convertAndSend(queue.getName(), filePath);
		System.out.println("Sent CSV file path: " + filePath + " to queue: " + queue.getName());
	}
}
