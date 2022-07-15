package com.emarket.order.services;

import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.emarket.order.models.Order;
import com.emarket.order.models.OrderSource;
import com.emarket.order.models.OrderStatus;
import com.emarket.order.rabbitmq.OrderPlacedProducer;
import com.emarket.order.repositories.OrdersRepository;

@Service
public class OrdersService {
	
	Logger logger = LoggerFactory.getLogger(OrdersService.class);
	
	@Autowired
	private OrdersRepository ordersRepo;
	
	@Autowired
	private OrderPlacedProducer orderPlacedQProducer;
	
	public Order createOrder(Order order) {
		try {
			order.setStatus(OrderStatus.Pending.toString());
			order.setCreatedAt(new Date());
			order.setSource(OrderSource.api.toString());
			
			Order savedOrder = ordersRepo.save(order);
			orderPlacedQProducer.sendMsg(savedOrder);
			return savedOrder;
		} catch(Exception ex) {
			logger.error(ex.getMessage(), ex);
			throw ex;
		}
	}
	
	public Optional<Order> findOrderById(int id) {
		return ordersRepo.findById(id);
	}
	
	public Order updateOrder(Order order) {
		logger.info("updating order with id: " + order.getId());
		Order savedOrder = ordersRepo.save(order);
		logger.info("order updated successfully");
		return savedOrder;
	}
	
	public void saveRange(List<Order> orders) {
		logger.info("saving " + orders.size() + " orders...");
		ordersRepo.saveAll(orders);
		logger.info("bulk orders saving process completed successfully.");
	}
}
