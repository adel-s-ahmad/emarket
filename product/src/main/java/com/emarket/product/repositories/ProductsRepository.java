package com.emarket.product.repositories;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.emarket.product.models.Product;

@Repository
public interface ProductsRepository extends JpaRepository<Product, Integer> {
	Optional<Product> findBySKU(String sku);
	Boolean existsBySKU(String sku);
}
