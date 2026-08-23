package com.example.demo.repositories;

import com.example.demo.entities.ProductImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductImageRepository
        extends JpaRepository<ProductImage, Integer> {

    List<ProductImage> findByProduct_ProductId(Integer productId);
}