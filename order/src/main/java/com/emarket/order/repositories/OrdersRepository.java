package com.emarket.order.repositories;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.emarket.order.models.Order;

@Repository
public interface OrdersRepository extends JpaRepository<Order, Integer> {
}
