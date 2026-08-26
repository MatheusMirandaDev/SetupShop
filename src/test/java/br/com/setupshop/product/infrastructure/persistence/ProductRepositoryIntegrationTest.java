package br.com.setupshop.product.infrastructure.persistence;

import br.com.setupshop.product.domain.model.Product;
import br.com.setupshop.product.domain.repository.ProductRepository;
import br.com.setupshop.shared.pagination.PageQuery;
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

    @Test
    void shouldReturnPaginatedProductsSortedById() {

        Product product1 = new Product(
            "First product",
            "",
            new BigDecimal("100.00")
        );
        Product product2 = new Product(
            "Second product",
            "",
            new BigDecimal("200.00")
        );
        Product product3 = new Product(
            "Third product",
            "",
            new BigDecimal("300.00")
        );

        Product savedProduct1 = productRepository.save(product1);
        Product savedProduct2 = productRepository.save(product2);
        Product savedProduct3 = productRepository.save(product3);
        entityManager.flush();
        entityManager.clear();

        PageQuery pageQuery = new PageQuery(0, 2);
        var result = productRepository.findAll(pageQuery);

        assertEquals(2, result.content().size());
        assertEquals(0, result.page());
        assertEquals(2, result.size());
        assertEquals(3L, result.totalElements());
        assertEquals(2, result.totalPages());
        assertEquals(savedProduct1.getId(), result.content().get(0).getId());
        assertEquals(savedProduct2.getId(), result.content().get(1).getId());
        assertFalse(
            result.content().stream()
                .anyMatch(product -> product.getId().equals(savedProduct3.getId()))
        );
    }
}
