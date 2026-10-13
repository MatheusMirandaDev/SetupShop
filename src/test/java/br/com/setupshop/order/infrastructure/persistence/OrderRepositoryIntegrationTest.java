package br.com.setupshop.order.infrastructure.persistence;

import br.com.setupshop.customer.domain.model.Customer;
import br.com.setupshop.customer.domain.repository.CustomerRepository;
import br.com.setupshop.customer.infrastructure.persistence.CustomerRepositoryAdapter;
import br.com.setupshop.order.domain.model.Order;
import br.com.setupshop.order.domain.model.OrderItem;
import br.com.setupshop.order.domain.repository.OrderRepository;
import br.com.setupshop.product.domain.model.Product;
import br.com.setupshop.product.domain.repository.ProductRepository;
import br.com.setupshop.product.infrastructure.persistence.ProductRepositoryAdapter;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Import;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@DataJpaTest
@Testcontainers
@Import({OrderRepositoryAdapter.class, CustomerRepositoryAdapter.class, ProductRepositoryAdapter.class})
class OrderRepositoryIntegrationTest {

    @Container
    @ServiceConnection
    static final PostgreSQLContainer postgres =
        new PostgreSQLContainer("postgres:17-alpine");

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private EntityManager entityManager;

    @Test
    void shouldSaveOrderWithMultipleItems() {

        Customer customer = new Customer(
            "Matheus Miranda",
            "matheus.MIRANDA@gmail.com",
            "61999999999"
        );
        Customer savedCustomer = customerRepository.save(customer);
        assertNotNull(savedCustomer.getId());

        Product product1 = new Product(
            "Keyboard Aula F75",
            "Wireless mechanical keyboard",
            new BigDecimal("300.00")
        );
        Product savedProduct1 = productRepository.save(product1);
        assertNotNull(savedProduct1.getId());

        OrderItem item1 =
            new OrderItem(savedProduct1.getId(), 2, savedProduct1.getPrice());

        Product product2 = new Product(
            "Mouse A9 PRO",
            "Mouse gamer",
            new BigDecimal("150.00")
        );
        Product savedProduct2 = productRepository.save(product2);
        assertNotNull(savedProduct2.getId());

        OrderItem item2 =
            new OrderItem(savedProduct2.getId(), 3, savedProduct2.getPrice());

        List<OrderItem> orderItems = new ArrayList<>();
        orderItems.add(item1);
        orderItems.add(item2);

        Order order = new Order(savedCustomer.getId(), orderItems);
        Order savedOrder = orderRepository.save(order);
        assertNotNull(savedOrder.getId());
        entityManager.flush();
        entityManager.clear();

        Order foundOrder =
            entityManager.find(Order.class, savedOrder.getId());

        assertNotNull(foundOrder);

        OrderItem foundItem1 = foundOrder.getOrderItems().stream()
            .filter(item -> savedProduct1.getId().equals(item.getProductId()))
            .findFirst()
            .orElseThrow();

        OrderItem foundItem2 = foundOrder.getOrderItems().stream()
            .filter(item -> savedProduct2.getId().equals(item.getProductId()))
            .findFirst()
            .orElseThrow();

        assertEquals(savedCustomer.getId(), foundOrder.getCustomerId());
        assertEquals(savedProduct1.getId(), foundItem1.getProductId());
        assertEquals(savedProduct2.getId(), foundItem2.getProductId());
        assertEquals(2, foundOrder.getOrderItems().size());
        assertEquals(new BigDecimal("300.00"), foundItem1.getUnitPrice());
        assertEquals(2, foundItem1.getQuantity());
        assertEquals(new BigDecimal("150.00"), foundItem2.getUnitPrice());
        assertEquals(3, foundItem2.getQuantity());
        assertEquals(new BigDecimal("1050.00"), foundOrder.getTotal());
        assertEquals("CREATED", foundOrder.getStatus().toString());
        assertNotNull(foundOrder.getCreatedAt());
    }

}
