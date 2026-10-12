package br.com.setupshop.order.application.usecase.place;

import br.com.setupshop.customer.domain.exception.CustomerNotFoundException;
import br.com.setupshop.customer.domain.model.Customer;
import br.com.setupshop.customer.domain.repository.CustomerRepository;
import br.com.setupshop.order.domain.exception.InactiveCustomerException;
import br.com.setupshop.order.domain.exception.InactiveProductException;
import br.com.setupshop.order.domain.model.Order;
import br.com.setupshop.order.domain.model.OrderItem;
import br.com.setupshop.order.domain.repository.OrderRepository;
import br.com.setupshop.product.domain.exception.ProductNotFoundException;
import br.com.setupshop.product.domain.model.Product;
import br.com.setupshop.product.domain.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class PlaceOrderUseCaseTest {

    private CustomerRepository customerRepository;
    private ProductRepository productRepository;
    private OrderRepository orderRepository;
    private PlaceOrderUseCase placeOrderUseCase;

    @BeforeEach
    void setUp() {
        customerRepository = mock(CustomerRepository.class);
        productRepository = mock(ProductRepository.class);
        orderRepository = mock(OrderRepository.class);
        placeOrderUseCase = new PlaceOrderUseCase(customerRepository, productRepository, orderRepository);
    }

    @Test
    void shouldPlaceOrderWithCurrentProductPrice() {
        Long customerId = 1L;
        String customerName = "Matheus";
        String customerEmail = "matheus@gmail.com";
        String customerPhone = "61999999999";
        Customer customer = new Customer(customerName, customerEmail, customerPhone);

        Long productId = 1L;
        String productName = "Teclado AULA F75";
        String productDescription = "Teclado Mecanico para computador";
        BigDecimal productPrice = new BigDecimal("140.00");
        Product product = new Product(productName, productDescription, productPrice);

        PlaceOrderItemCommand placeOrderItemCommand =
            new PlaceOrderItemCommand(productId, 2);

        PlaceOrderCommand placeOrderCommand =
            new PlaceOrderCommand(customerId, List.of(placeOrderItemCommand));

        when(customerRepository.findById(customerId)).thenReturn(Optional.of(customer));
        when(productRepository.findById(productId)).thenReturn(Optional.of(product));
        when(orderRepository.save(any(Order.class))).thenAnswer(i -> i.getArgument(0));

        Order result = placeOrderUseCase.execute(placeOrderCommand);
        OrderItem item = result.getOrderItems().getFirst();

        assertEquals(customerId, result.getCustomerId());
        assertEquals(productId, item.getProductId());
        assertEquals(1, result.getOrderItems().size());
        assertEquals(2, item.getQuantity());
        assertEquals(new BigDecimal("140.00"), item.getUnitPrice());
        assertEquals(new BigDecimal("280.00"), result.total());
        verify(customerRepository).findById(customerId);
        verify(productRepository).findById(productId);
        verify(orderRepository).save(any(Order.class));
    }

    @Test
    void shouldRejectEmptyOrderBeforeSaving() {
        assertThrows(IllegalArgumentException.class, () ->
            placeOrderUseCase.execute(new PlaceOrderCommand(1L, List.of())));

        verify(orderRepository, never()).save(any(Order.class));
    }

    @Test
    void shouldPlaceOrderWithMultipleProducts() {
        Long customerId = 1L;
        String customerName = "Matheus";
        String customerEmail = "matheus@gmail.com";
        String customerPhone = "61999999999";
        Customer customer = new Customer(customerName, customerEmail, customerPhone);

        Long productId1 = 1L;
        String productName1 = "Teclado AULA F75";
        String productDescription1 = "Teclado Mecanico para computador";
        BigDecimal productPrice1 = new BigDecimal("140.00");
        Product product1 = new Product(productName1, productDescription1, productPrice1);

        PlaceOrderItemCommand placeOrderItemCommand1 =
            new PlaceOrderItemCommand(productId1, 2);

        Long productId2 = 2L;
        String productName2 = "Mouse A9 Pro";
        String productDescription2 = "Mouse para computador";
        BigDecimal productPrice2 = new BigDecimal("100.00");
        Product product2 = new Product(productName2, productDescription2, productPrice2);

        PlaceOrderItemCommand placeOrderItemCommand2 =
            new PlaceOrderItemCommand(productId2, 3);

        PlaceOrderCommand placeOrderCommand =
            new PlaceOrderCommand(customerId, List.of(placeOrderItemCommand1, placeOrderItemCommand2));

        when(customerRepository.findById(customerId)).thenReturn(Optional.of(customer));
        when(productRepository.findById(productId1)).thenReturn(Optional.of(product1));
        when(productRepository.findById(productId2)).thenReturn(Optional.of(product2));
        when(orderRepository.save(any(Order.class))).thenAnswer(i -> i.getArgument(0));

        Order result = placeOrderUseCase.execute(placeOrderCommand);
        OrderItem item1 = result.getOrderItems().getFirst();
        OrderItem item2 = result.getOrderItems().get(1);

        assertEquals(customerId, result.getCustomerId());
        assertEquals(productId1, item1.getProductId());
        assertEquals(productId2, item2.getProductId());
        assertEquals(2, result.getOrderItems().size());
        assertEquals(2, item1.getQuantity());
        assertEquals(3, item2.getQuantity());
        assertEquals(new BigDecimal("140.00"), item1.getUnitPrice());
        assertEquals(new BigDecimal("100.00"), item2.getUnitPrice());
        assertEquals(new BigDecimal("580.00"), result.total());
        verify(customerRepository).findById(customerId);
        verify(productRepository).findById(productId1);
        verify(productRepository).findById(productId2);
        verify(orderRepository).save(any(Order.class));
    }

    @Test
    void shouldRejectOrderWhenCustomerIsInactive() {
        Long customerId = 1L;
        String customerName = "Matheus";
        String customerEmail = "matheus@gmail.com";
        String customerPhone = "61999999999";
        Customer customer = new Customer(customerName, customerEmail, customerPhone);
        customer.deactivate();

        Long productId = 1L;

        PlaceOrderItemCommand placeOrderItemCommand =
            new PlaceOrderItemCommand(productId, 2);

        PlaceOrderCommand placeOrderCommand =
            new PlaceOrderCommand(customerId, List.of(placeOrderItemCommand));

        when(customerRepository.findById(customerId)).thenReturn(Optional.of(customer));
        assertThrows(InactiveCustomerException.class, () -> placeOrderUseCase.execute(placeOrderCommand));

        verify(orderRepository, never()).save(any(Order.class));
    }

    @Test
    void shouldRejectOrderWhenProductIsInactive() {
        Long customerId = 1L;
        String customerName = "Matheus";
        String customerEmail = "matheus@gmail.com";
        String customerPhone = "61999999999";
        Customer customer = new Customer(customerName, customerEmail, customerPhone);

        Long productId = 1L;
        String productName = "Teclado AULA F75";
        String productDescription = "Teclado Mecanico para computador";
        BigDecimal productPrice = new BigDecimal("140.00");
        Product product = new Product(productName, productDescription, productPrice);
        product.deactivate();

        PlaceOrderItemCommand placeOrderItemCommand =
            new PlaceOrderItemCommand(productId, 2);

        PlaceOrderCommand placeOrderCommand =
            new PlaceOrderCommand(customerId, List.of(placeOrderItemCommand));

        when(customerRepository.findById(customerId)).thenReturn(Optional.of(customer));
        when(productRepository.findById(productId)).thenReturn(Optional.of(product));
        assertThrows(InactiveProductException.class, () -> placeOrderUseCase.execute(placeOrderCommand));

        verify(orderRepository, never()).save(any(Order.class));
    }

    @Test
    void shouldRejectOrderWhenCustomerDoesNotExist() {
        Long customerId = 1L;
        Long productId = 1L;

        PlaceOrderItemCommand placeOrderItemCommand =
            new PlaceOrderItemCommand(productId, 2);

        PlaceOrderCommand placeOrderCommand =
            new PlaceOrderCommand(customerId, List.of(placeOrderItemCommand));

        when(customerRepository.findById(customerId)).thenReturn(Optional.empty());
        assertThrows(CustomerNotFoundException.class, () -> placeOrderUseCase.execute(placeOrderCommand));

        verify(orderRepository, never()).save(any(Order.class));
    }

    @Test
    void shouldRejectOrderWhenProductDoesNotExist() {
        Long customerId = 1L;
        String customerName = "Matheus";
        String customerEmail = "matheus@gmail.com";
        String customerPhone = "61999999999";
        Customer customer = new Customer(customerName, customerEmail, customerPhone);

        Long productId = 1L;

        PlaceOrderItemCommand placeOrderItemCommand =
            new PlaceOrderItemCommand(productId, 2);

        PlaceOrderCommand placeOrderCommand =
            new PlaceOrderCommand(customerId, List.of(placeOrderItemCommand));

        when(customerRepository.findById(customerId)).thenReturn(Optional.of(customer));
        when(productRepository.findById(productId)).thenReturn(Optional.empty());
        assertThrows(ProductNotFoundException.class, () -> placeOrderUseCase.execute(placeOrderCommand));

        verify(orderRepository, never()).save(any(Order.class));
    }

    @Test
    void shouldKeepPlacedOrderPriceWhenCatalogPriceChanges() {
        Long customerId = 1L;
        Long productId = 2L;
        Customer customer = new Customer("Matheus", "matheus@gmail.com", "61999999999");
        Product product = new Product("Keyboard", null, new BigDecimal("140.00"));
        PlaceOrderCommand command = new PlaceOrderCommand(
            customerId, List.of(new PlaceOrderItemCommand(productId, 2)));

        when(customerRepository.findById(customerId)).thenReturn(Optional.of(customer));
        when(productRepository.findById(productId)).thenReturn(Optional.of(product));
        when(orderRepository.save(any(Order.class))).thenAnswer(i -> i.getArgument(0));

        Order placedOrder = placeOrderUseCase.execute(command);
        product.changePrice(new BigDecimal("200.00"));

        assertEquals(new BigDecimal("140.00"), placedOrder.getOrderItems().getFirst().getUnitPrice());
        assertEquals(new BigDecimal("280.00"), placedOrder.getTotal());
        assertEquals(new BigDecimal("200.00"), product.getPrice());
    }

    @Test
    void shouldUseUpdatedProductPriceForNewOrder() {

        Long customerId = 1L;
        String customerName = "Matheus";
        String customerEmail = "matheus@gmail.com";
        String customerPhone = "61999999999";
        Customer customer = new Customer(customerName, customerEmail, customerPhone);

        Long productId = 1L;
        String productName = "Teclado AULA F75";
        String productDescription = "Teclado Mecanico para computador";
        BigDecimal productPrice = new BigDecimal("140.00");
        Product product = new Product(productName, productDescription, productPrice);

        PlaceOrderItemCommand placeOrderItemCommand1 =
            new PlaceOrderItemCommand(productId, 2);

        PlaceOrderCommand placeOrderCommand1 =
            new PlaceOrderCommand(customerId, List.of(placeOrderItemCommand1));

        when(customerRepository.findById(customerId)).thenReturn(Optional.of(customer));
        when(productRepository.findById(productId)).thenReturn(Optional.of(product));
        when(orderRepository.save(any(Order.class))).thenAnswer(i -> i.getArgument(0));

        Order result1 = placeOrderUseCase.execute(placeOrderCommand1);
        OrderItem item1 = result1.getOrderItems().getFirst();

        product.changePrice(new BigDecimal("200.00"));

        PlaceOrderItemCommand placeOrderItemCommand2 =
            new PlaceOrderItemCommand(productId, 2);

        PlaceOrderCommand placeOrderCommand2 =
            new PlaceOrderCommand(customerId, List.of(placeOrderItemCommand2));

        Order result2 = placeOrderUseCase.execute(placeOrderCommand2);
        OrderItem item2 = result2.getOrderItems().getFirst();

        assertEquals(new BigDecimal("140.00"), item1.getUnitPrice());
        assertEquals(new BigDecimal("200.00"), item2.getUnitPrice());

        assertEquals(new BigDecimal("280.00"), result1.getTotal());
        assertEquals(new BigDecimal("400.00"), result2.getTotal());
    }

}
