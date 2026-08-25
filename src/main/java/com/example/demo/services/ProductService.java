package com.example.demo.services;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.demo.entities.Category;
import com.example.demo.entities.Product;
import com.example.demo.entities.ProductImage;
import com.example.demo.repositories.*;

@Service
public class ProductService {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private ProductImageRepository productImageRepository;

    @Autowired
    private CategoryRepository categoryRepository;


    // Get products by category name or get all products
    public List<Product> getProductsByCategory(String categoryName) {

        if (categoryName != null && !categoryName.isEmpty()) {

            Optional<Category> categoryOpt =
                    categoryRepository.findByCategoryName(categoryName);

            if (categoryOpt.isPresent()) {

                Category category = categoryOpt.get();

                return productRepository
                        .findByCategory_CategoryId(
                                category.getCategoryId()
                        );

            } else {

                throw new RuntimeException(
                        "Category not found"
                );
            }

        } else {

            return productRepository.findAll();
        }
    }


    // Get products by category ID
    public List<Product> getProductsByCategoryId(
            Integer categoryId) {

        if (categoryId == null) {

            return productRepository.findAll();
        }

        Optional<Category> categoryOpt =
                categoryRepository.findById(categoryId);

        if (categoryOpt.isEmpty()) {

            throw new RuntimeException(
                    "Category not found"
            );
        }

        return productRepository
                .findByCategory_CategoryId(categoryId);
    }


    // Get all image URLs of a product
    public List<String> getProductImages(
            Integer productId) {

        List<ProductImage> productImages =
                productImageRepository
                        .findByProduct_ProductId(productId);

        List<String> imageUrls =
                new ArrayList<>();

        for (ProductImage image : productImages) {

            imageUrls.add(
                    image.getImageUrl()
            );
        }

        return imageUrls;
    }
}