package br.com.setupshop.product.infrastructure.persistence;

import br.com.setupshop.product.domain.model.Product;
import br.com.setupshop.product.domain.repository.ProductRepository;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.postgresql.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.springframework.context.annotation.Import;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@Testcontainers
@Import(ProductRepositoryAdapter.class)
public class ProductRepositoryIntegrationTest {

    @Container
    @ServiceConnection
    static final PostgreSQLContainer postgres =
        new PostgreSQLContainer("postgres:17-alpine");

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private EntityManager entityManager;

    @Test
    void shouldPersistAndFindProduct() {

        Product product = new Product(
            "Keyboard Aula F75",
            "Wireless mechanical keyboard",
            new BigDecimal("300.00")
        );

        Product savedProduct = productRepository.save(product);
        entityManager.flush();
        entityManager.clear();

        Product foundProduct = productRepository
            .findById(savedProduct.getId())
            .orElseThrow();

        assertNotNull(savedProduct.getId());
        assertEquals(savedProduct.getId(), foundProduct.getId());
        assertEquals(product.getName(), foundProduct.getName());
        assertEquals(product.getDescription(), foundProduct.getDescription());
        assertEquals(product.getPrice(), foundProduct.getPrice());
        assertTrue(foundProduct.isActive());
        assertNotNull(savedProduct.getId());
        assertNotNull(foundProduct.getCreatedAt());
        assertNotNull(foundProduct.getUpdatedAt());
    }
}
