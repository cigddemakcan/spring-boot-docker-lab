package com.example._2postgresqlvolume.repository;

import com.example._2postgresqlvolume.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductRepository extends JpaRepository<Product, Long> {
}