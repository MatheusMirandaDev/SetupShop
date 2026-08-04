package br.com.setupshop.product.domain.repository;

import br.com.setupshop.product.domain.model.Product;

import java.util.List;
import java.util.Optional;

public interface ProductRepository {
    Optional<Product> findById(Long id);
    Product save(Product product);
    List<Product> findAll();
}
