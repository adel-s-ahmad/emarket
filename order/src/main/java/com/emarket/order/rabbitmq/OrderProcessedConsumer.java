package com.emarket.order.rabbitmq;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StopWatch;

import com.emarket.order.models.Order;
import com.emarket.order.services.OrdersService;

@RabbitListener(queues = "${emarket.orderprocessed.queue.name}")
public class OrderProcessedConsumer{

	Logger logger = LoggerFactory.getLogger(OrderProcessedConsumer.class);
	
	@Autowired
	private OrdersService orderService;
	
	@RabbitHandler
	public void receiveMsg(final Order order) {
		StopWatch stopWatch = new StopWatch();
		stopWatch.start();

		logger.info("Received processed order with id: " + order.getId());
		orderService.updateOrder(order);
		
		stopWatch.stop();

		logger.info("Consumer procssed order Done in " + stopWatch.getTotalTimeSeconds() + "s");
	}
}

