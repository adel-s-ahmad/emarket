package com.emarket.product.services;

import java.util.Date;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.emarket.product.repositories.ProductsRepository;
import com.emarket.product.models.Order;
import com.emarket.product.models.OrderSource;
import com.emarket.product.models.OrderStatus;
import com.emarket.product.models.Product;
import com.emarket.product.models.ProductStock;
import com.emarket.product.rabbitmq.OrderProcessedProducer;

@Service
public class ProductsService {

	Logger logger = LoggerFactory.getLogger(ProductsService.class);
	
	@Autowired
	private ProductsRepository productRepo;
	
	@Autowired
	private OrderProcessedProducer orderProcessedProducer;
	
	public List<Product> getAllProducts() {
		return productRepo.findAll();
	}
	
	public Product getProductBySKU(String sku) throws NoSuchElementException {
		Optional<Product> product = productRepo.findBySKU(sku);
		if(!product.isPresent()) {
			throw new NoSuchElementException("Product not found");
		}
		return product.get();
	}
	
	public boolean productExists(String sku) {
		return productRepo.existsBySKU(sku);
	}
	
	public void subtractFromStock(String sku, String country, long quantity) {
		Product p = productRepo.findBySKU(sku).get();
		ProductStock stock = p.getStock().stream()
				.filter(s -> s.getCountry().equals(country))
				.findFirst()
				.get();
		long oldQuantity = stock.getQuantity();
		//if quantity is minus it means the new quantity will increase (may be it is a returned orders)
		stock.setQuantity(oldQuantity - quantity);
		productRepo.save(p);
	}
	
	public boolean stockAvailable(String sku, String country, long quantity, String source) {
		logger.info(String.format("stockAvailable(%s, %s, %s)", sku, country, quantity));
		Product p = productRepo.findBySKU(sku).get();
		// if there is no stock available for the country and the order placed from csv file
		// we will need to create a new stock for this specific country
		//otherwise return false
		if(p.getStock().stream()
				.anyMatch(s -> s.getCountry().equals(country))) {
			ProductStock stock = p.getStock().stream()
					.filter(s -> s.getCountry().equals(country))
					.findFirst()
					.get();
			logger.info("available stock = " + stock.getQuantity());
			return stock.getQuantity() >= quantity;
		} else if(source != OrderSource.api.toString()) {
			ProductStock s = new ProductStock();
			s.setCountry(country);
			s.setProduct(p);
			s.setQuantity(500);
			p.getStock().add(s);
			productRepo.save(p);
			return true;
		} else {
			return false;
		}
		
	}
	
	public void processOrder(Order order) {
		logger.info("processing order with id: " + order.getId());
		
		if(productExists(order.getSku())) {
			logger.info("product exist in DB, checking stock availability...");
			if(stockAvailable(order.getSku(), order.getCountry(), order.getQuantity(), order.getSource())) {
				logger.info("required stock is available, attempting to update the stock...");
				subtractFromStock(order.getSku(), order.getCountry(), order.getQuantity());
				logger.info("stock updated with the new value successfully, updating the order status to processed...");
				order.setStatus(OrderStatus.Processed.toString());
			} else {
				logger.info("required stock is not available, updating the order status to canceled...");
				order.setStatus(OrderStatus.canceled.toString());
				order.setNotes("insufficient stock");
			}
		} else {
			if(order.getSource() != OrderSource.api.toString()) {
				logger.info("csv product is not exist in DB, creating it with arbitrary large quantity in the order country");
				createProduct(order.getSku(), order.getName(), order.getCountry(), order.getQuantity());
				logger.info("product created, invoking the same method to process it.");
				processOrder(order);
				return;
			} else {
				logger.info("product not found, updating the order status to failed...");
				order.setStatus(OrderStatus.Failed.toString());
				order.setNotes("product not found");
			}
		}
		
		logger.info("sending updated order to the queue...");
		orderProcessedProducer.sendMsg(order);
	}
	
	public void createProduct(String sku, String name, String country, long quantity) {
		Product p = new Product();
		p.setSKU(sku);
		p.setName(name);
		p.setCategory("test category");
		p.setDescription("blah blah blah");
		p.setCreatedAt(new Date());
		
		ProductStock s = new ProductStock();
		s.setCountry(country);
		s.setProduct(p);
		s.setQuantity(500);
		
		p.getStock().add(s);
		
		productRepo.save(p);
	}
}
