package com.emarket.product.controller;

import java.util.List;
import java.util.NoSuchElementException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RestController;

import com.emarket.product.models.*;
import com.emarket.product.rabbitmq.OrderPlacedConsumer;
import com.emarket.product.services.ProductsService;

@RestController
public class ProductsController {

	Logger logger = LoggerFactory.getLogger(ProductsController.class);
	
	@Autowired
	private ProductsService productService;
	
	@GetMapping("products/{sku}")
	public ResponseEntity<Product> getProduct(@PathVariable String sku) {
		try {
			logger.info("getting product with sku: " + sku);
			Product product = productService.getProductBySKU(sku);
			return ResponseEntity.ok(product);
		} catch(NoSuchElementException nse) {
			return ResponseEntity.notFound().build();
		} catch(Exception ex) {
			throw ex;
		}
	}
}
