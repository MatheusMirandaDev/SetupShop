package br.com.setupshop.product.infrastructure.persistence;

import br.com.setupshop.product.domain.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JpaProductRepository extends JpaRepository<Product, Long> {
}