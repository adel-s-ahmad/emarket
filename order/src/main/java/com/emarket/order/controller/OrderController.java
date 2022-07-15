package com.emarket.order.controller;

import java.net.URI;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import com.emarket.order.helper.CSVHelper;
import com.emarket.order.models.Order;
import com.emarket.order.services.CSVService;
import com.emarket.order.services.OrdersService;

@RestController
public class OrderController {

	Logger logger = LoggerFactory.getLogger(OrderController.class);

	@Autowired
	private OrdersService orderService;

	@Autowired
	private CSVService fileService;

	@GetMapping("orders/{id}")
	public ResponseEntity<Order> getOrder(@PathVariable int id) {
		Optional<Order> order = orderService.findOrderById(id);
		if(!order.isPresent()) {
			return ResponseEntity.notFound().build();
		}
		return ResponseEntity.ok(order.get());
	}

	@PostMapping("orders")
	public ResponseEntity<Object> createOrder(@RequestBody Order order) {
		Order savedOrder = orderService.createOrder(order);
		URI location = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}").buildAndExpand(savedOrder.getId())
				.toUri();
		return ResponseEntity.created(location).build();
	}

	@PostMapping("orders/bulk")
	public ResponseEntity<Object> createBulkOrders(@RequestParam("file") MultipartFile file) {
		logger.info("processing new csv file...");
		String message = "";
		if (CSVHelper.hasCSVFormat(file)) {
			try {
				logger.info("sending file to FTP server...");
				String newFileName = fileService.save(file);
				logger.info("file saved successfully with url: " + newFileName);
				logger.info("sending csv processing order to queue...");
				fileService.sendToQueue(newFileName);
				message = "Uploaded the file successfully";
				return ResponseEntity.accepted().build();
			} catch (Exception e) {
				message = "Could not upload the file: " + file.getOriginalFilename() + "!";
				return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED).body(e.getMessage());
			}
		}
		logger.info("file is not in a valid format");
		message = "Please upload a csv file!";
		return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(message);
	}
}
