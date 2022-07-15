package com.emarket.order.services;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.emarket.order.helper.CSVHelper;
import com.emarket.order.models.Order;
import com.emarket.order.rabbitmq.CSVProcessingProducer;
import com.emarket.order.rabbitmq.OrderPlacedProducer;

@Service
public class CSVService {
	
	Logger logger = LoggerFactory.getLogger(CSVService.class);
	
	@Autowired
	private CSVProcessingProducer csvProducer;
	
	@Autowired
	private OrdersService orderService;
	
	@Autowired
	private OrderPlacedProducer orderProducer;

	@Value("${emarket.csv.ftp.path}")
	private String uploadPath;

	private Path root;

	public void createDirectory() {
		try {
			root = Paths.get(uploadPath);
			logger.info("root directory is: " + root);
			if(!Files.exists(root)) {
				Files.createDirectory(root);
			}
		} catch (IOException e) {
			throw new RuntimeException("Could not initialize folder for upload!");
		}
	}

	public String save(MultipartFile file) {
		try {
			logger.info("creating root directory...");
			createDirectory();
			logger.info("getting the new file name...");
			String fileName = java.util.UUID.randomUUID() + "_" + file.getOriginalFilename();
			logger.info("saving the file to local disk (should be FTP server in production)...");
			Files.copy(file.getInputStream(), this.root.resolve(fileName));
			return fileName;
		} catch (Exception ex) {
			throw new RuntimeException("Could not store the file. Error: " + ex.getMessage());
		}
	}
	
	public void sendToQueue(String filePath) {
		csvProducer.sendMsg(filePath);
	}
	
	public void process(String filePath) {
		try {
			logger.info("processing file: " + filePath);
			Path fullPath = root.resolve(filePath);
			File file = new File(fullPath.toString());
			
			try (InputStream in = new FileInputStream(file))
	        {
				List<Order> orders = CSVHelper.csvToOrders(in);
				logger.info("saving file records as new orders in DB...");
				orderService.saveRange(orders);
				
				logger.info("sending each record as order to queue");
				for(Order order : orders) {
					placeOrderInQueue(order);
				}
				logger.info("placed all orders in queue successfully.");
	        }
	        catch (IOException e) {
	            e.printStackTrace();
	        }
			
		} catch (Exception e) {
			throw new RuntimeException("failed to store csv data: " + e.getMessage());
		}
	}
	
	private void placeOrderInQueue(Order order) {
		orderProducer.sendMsg(order);
	}

//	public void save(MultipartFile file) {
//		try {
//			List<Order> orders = CSVHelper.csvToOrders(file.getInputStream());
//			orderService.saveRange(orders);
//		} catch (IOException e) {
//			throw new RuntimeException("failed to store csv data: " + e.getMessage());
//		}
//	}
}
