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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
public class PlaceOrderUseCase {

    private final CustomerRepository customerRepository;
    private final ProductRepository productRepository;
    private final OrderRepository orderRepository;

    public PlaceOrderUseCase(
        CustomerRepository customerRepository,
        ProductRepository productRepository,
        OrderRepository orderRepository) {
        this.customerRepository = customerRepository;
        this.productRepository = productRepository;
        this.orderRepository = orderRepository;
    }

    @Transactional
    public Order execute(PlaceOrderCommand command) {

        if (command == null || command.customerId() == null || command.customerId() <= 0) {
            throw new IllegalArgumentException("Customer ID must be valid");
        }
        if (command.items() == null || command.items().isEmpty()) {
            throw new IllegalArgumentException("Order items must not be empty");
        }
        for (PlaceOrderItemCommand item : command.items()) {
            if (item == null || item.productId() == null || item.productId() <= 0) {
                throw new IllegalArgumentException("Product ID must be valid");
            }
            if (item.quantity() <= 0) {
                throw new IllegalArgumentException("Quantity must be greater than zero");
            }
        }

        Long customerId = command.customerId();
        Customer customer = customerRepository
            .findById(customerId)
            .orElseThrow(() -> new CustomerNotFoundException(customerId));

        if (!customer.isActive()){
            throw new InactiveCustomerException(customerId);
        }

        List<OrderItem> orderItemList = new ArrayList<>();

        for (PlaceOrderItemCommand itemCommand : command.items()) {
            Long productId = itemCommand.productId();
            Product product = productRepository
                .findById(productId)
                .orElseThrow(() -> new ProductNotFoundException(productId));

            if (!product.isActive()){
                throw new InactiveProductException(productId);
            }

            orderItemList.add(new OrderItem(productId, itemCommand.quantity(), product.getPrice()));
        }

        Order order = new Order(
            customerId,
            orderItemList
        );

        return orderRepository.save(order);
    }

}
