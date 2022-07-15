package com.emarket.order.rabbitmq;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StopWatch;

import com.emarket.order.models.Order;
import com.emarket.order.services.CSVService;
import com.emarket.order.services.OrdersService;

@RabbitListener(queues = "${emarket.csvProcessing.queue.name}")
public class CSVProcessingConsumer {
Logger logger = LoggerFactory.getLogger(CSVProcessingConsumer.class);
	
	@Autowired
	private CSVService fileService;
	
	@RabbitHandler
	public void receiveMsg(final String filePath) {
		StopWatch stopWatch = new StopWatch();
		stopWatch.start();

		logger.info("Received csv file path: " + filePath);
		fileService.process(filePath);
		
		stopWatch.stop();

		logger.info("Consuming CSV file Done in " + stopWatch.getTotalTimeSeconds() + "s");
	}
}
