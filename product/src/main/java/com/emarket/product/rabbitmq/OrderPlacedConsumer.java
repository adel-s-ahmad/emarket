package com.emarket.product.rabbitmq;

import java.math.BigInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StopWatch;

import com.emarket.product.models.Order;
import com.emarket.product.services.ProductsService;

@RabbitListener(queues = "${emarket.orderplaced.queue.name}")
public class OrderPlacedConsumer{
	
	Logger logger = LoggerFactory.getLogger(OrderPlacedConsumer.class);

	@Autowired
	private ProductsService productService;
	
	@RabbitHandler
	public void receiveMsg(final Order order) {
		try {		
			StopWatch stopWatch = new StopWatch();
			stopWatch.start();
	
			logger.info("Received order with id: " + order.getId());
			productService.processOrder(order);
			
			stopWatch.stop();
			logger.info("Consumer processing order Done in " + stopWatch.getTotalTimeSeconds() + "s");
		} catch(Exception ex) {
			logger.error(ex.getMessage(), ex);
		}
	}
}
