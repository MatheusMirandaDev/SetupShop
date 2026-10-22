package br.com.setupshop.order.infrastructure.web;

import br.com.setupshop.customer.domain.exception.CustomerNotFoundException;
import br.com.setupshop.order.application.usecase.get.GetOrderByIdUseCase;
import br.com.setupshop.order.application.usecase.list.ListOrdersUseCase;
import br.com.setupshop.order.application.usecase.place.PlaceOrderCommand;
import br.com.setupshop.order.application.usecase.place.PlaceOrderItemCommand;
import br.com.setupshop.order.application.usecase.place.PlaceOrderUseCase;
import br.com.setupshop.order.domain.exception.InactiveCustomerException;
import br.com.setupshop.order.domain.exception.InactiveProductException;
import br.com.setupshop.order.domain.exception.OrderNotFoundException;
import br.com.setupshop.order.domain.model.Order;
import br.com.setupshop.order.domain.model.OrderItem;
import br.com.setupshop.order.domain.model.OrderStatus;
import br.com.setupshop.product.domain.exception.ProductNotFoundException;
import br.com.setupshop.shared.pagination.PageQuery;
import br.com.setupshop.shared.pagination.PageResult;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(OrderController.class)
class OrderControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private PlaceOrderUseCase placeOrderUseCase;

    @MockitoBean
    private GetOrderByIdUseCase getOrderByIdUseCase;

    @MockitoBean
    private ListOrdersUseCase listOrdersUseCase;

    @Test
    void shouldPlaceOrderAndReturnCreated() throws Exception {

        List<OrderItem> orderItems = new ArrayList<>();

        Long productId = 1L;
        int quantity = 2;
        BigDecimal unitPrice = new BigDecimal("300.00");
        OrderItem orderItem = new OrderItem(productId, quantity, unitPrice);
        orderItems.add(orderItem);

        Long customerId = 1L;
        Order order = new Order(customerId, orderItems);

        when(placeOrderUseCase.execute(any(PlaceOrderCommand.class))).thenReturn(order);

        String requestBody =
            """
                {
                   "customerId": %s,
                   "items": [
                     {
                       "productId": %s,
                       "quantity": %s
                     }
                   ]
                 }
                """
                .formatted(customerId, productId, quantity);

        mockMvc
            .perform(post("/orders").contentType(APPLICATION_JSON).content(requestBody))
            .andExpect(status().isCreated())
            .andExpect(content().contentTypeCompatibleWith(APPLICATION_JSON))
            .andExpect(jsonPath("$.customerId").value(customerId))
            .andExpect(jsonPath("$.items[0].productId").value(productId))
            .andExpect(jsonPath("$.items[0].quantity").value(quantity))
            .andExpect(jsonPath("$.items[0].unitPrice").value(unitPrice.doubleValue()))
            .andExpect(jsonPath("$.status").value(OrderStatus.CREATED.toString()))
            .andExpect(jsonPath("$.total").value(600.00));

        ArgumentCaptor<PlaceOrderCommand> commandCaptor =
            ArgumentCaptor.forClass(PlaceOrderCommand.class);

        verify(placeOrderUseCase).execute(commandCaptor.capture());

        PlaceOrderCommand capturedCommand = commandCaptor.getValue();
        PlaceOrderItemCommand capturedItemCommand = capturedCommand.items().getFirst();

        assertEquals(customerId, capturedCommand.customerId());
        assertEquals(productId, capturedItemCommand.productId());
        assertEquals(quantity, capturedItemCommand.quantity());
    }

    @Test
    void shouldRejectOrderWhenItemQuantityIsZero() throws Exception {

        Long productId = 1L;
        int quantity = 0;
        Long customerId = 1L;

        String requestBody =
            """
                {
                   "customerId": %s,
                   "items": [
                     {
                       "productId": %s,
                       "quantity": %s
                     }
                   ]
                 }
                """
                .formatted(customerId, productId, quantity);

        mockMvc
            .perform(post("/orders").contentType(APPLICATION_JSON).content(requestBody))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.timestamp").exists())
            .andExpect(jsonPath("$.status").value(400))
            .andExpect(jsonPath("$.error").value("Bad Request"))
            .andExpect(jsonPath("$.message").value("Validation failed"))
            .andExpect(jsonPath("$.path").value("/orders"))
            .andExpect(jsonPath("$.fieldErrors['items[0].quantity']").value("must be greater than or equal to 1"));

        verifyNoInteractions(placeOrderUseCase);
    }

    @Test
    void shouldRejectOrderWithEmptyItems() throws Exception {

        Long customerId = 1L;

        String requestBody =
            """
                {
                   "customerId": %s,
                   "items": []
                 }
                """
                .formatted(customerId);

        mockMvc
            .perform(post("/orders").contentType(APPLICATION_JSON).content(requestBody))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.timestamp").exists())
            .andExpect(jsonPath("$.status").value(400))
            .andExpect(jsonPath("$.error").value("Bad Request"))
            .andExpect(jsonPath("$.message").value("Validation failed"))
            .andExpect(jsonPath("$.path").value("/orders"))
            .andExpect(jsonPath("$.fieldErrors.items").value("must not be empty"));

        verifyNoInteractions(placeOrderUseCase);
    }

    @Test
    void shouldReturnConflictWhenProductIsInactive() throws Exception {

        Long productId = 1L;
        int quantity = 1;
        Long customerId = 1L;

        when(placeOrderUseCase.execute(any(PlaceOrderCommand.class))).thenThrow(new InactiveProductException(productId));

        String requestBody =
            """
                {
                   "customerId": %s,
                   "items": [
                     {
                       "productId": %s,
                       "quantity": %s
                     }
                   ]
                 }
                """
                .formatted(customerId, productId, quantity);

        mockMvc
            .perform(post("/orders").contentType(APPLICATION_JSON).content(requestBody))
            .andExpect(status().isConflict())
            .andExpect(jsonPath("$.timestamp").exists())
            .andExpect(jsonPath("$.status").value(409))
            .andExpect(jsonPath("$.error").value("Conflict"))
            .andExpect(jsonPath("$.message").value("Product with id: " + productId + " is inactive."))
            .andExpect(jsonPath("$.path").value("/orders"));
    }

    @Test
    void shouldReturnConflictWhenCustomerIsInactive() throws Exception {

        Long productId = 1L;
        int quantity = 1;
        Long customerId = 1L;

        when(placeOrderUseCase.execute(any(PlaceOrderCommand.class))).thenThrow(new InactiveCustomerException(customerId));

        String requestBody =
            """
                {
                   "customerId": %s,
                   "items": [
                     {
                       "productId": %s,
                       "quantity": %s
                     }
                   ]
                 }
                """
                .formatted(customerId, productId, quantity);

        mockMvc
            .perform(post("/orders").contentType(APPLICATION_JSON).content(requestBody))
            .andExpect(status().isConflict())
            .andExpect(jsonPath("$.timestamp").exists())
            .andExpect(jsonPath("$.status").value(409))
            .andExpect(jsonPath("$.error").value("Conflict"))
            .andExpect(jsonPath("$.message").value("Customer with id: " + customerId + " is inactive."))
            .andExpect(jsonPath("$.path").value("/orders"));
    }

    @Test
    void shouldReturnNotFoundWhenProductDoesNotExist() throws Exception {
        Long productId = 1L;
        int quantity = 1;
        Long customerId = 1L;

        when(placeOrderUseCase.execute(any(PlaceOrderCommand.class))).thenThrow(new ProductNotFoundException(productId));

        String requestBody =
            """
                {
                   "customerId": %s,
                   "items": [
                     {
                       "productId": %s,
                       "quantity": %s
                     }
                   ]
                 }
                """
                .formatted(customerId, productId, quantity);

        mockMvc
            .perform(post("/orders").contentType(APPLICATION_JSON).content(requestBody))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.timestamp").exists())
            .andExpect(jsonPath("$.status").value(404))
            .andExpect(jsonPath("$.error").value("Not Found"))
            .andExpect(jsonPath("$.message").value("Product not found with id: " + productId))
            .andExpect(jsonPath("$.path").value("/orders"));
    }

    @Test
    void shouldReturnNotFoundWhenCustomerDoesNotExist() throws Exception {
        Long productId = 1L;
        int quantity = 1;
        Long customerId = 1L;

        when(placeOrderUseCase.execute(any(PlaceOrderCommand.class))).thenThrow(new CustomerNotFoundException(customerId));

        String requestBody =
            """
                {
                   "customerId": %s,
                   "items": [
                     {
                       "productId": %s,
                       "quantity": %s
                     }
                   ]
                 }
                """
                .formatted(customerId, productId, quantity);

        mockMvc
            .perform(post("/orders").contentType(APPLICATION_JSON).content(requestBody))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.timestamp").exists())
            .andExpect(jsonPath("$.status").value(404))
            .andExpect(jsonPath("$.error").value("Not Found"))
            .andExpect(jsonPath("$.message").value("Customer not found with id: " + customerId))
            .andExpect(jsonPath("$.path").value("/orders"));
    }

    @Test
    void shouldReturnOrderById() throws Exception {

        Long orderId = 19L;
        Long customerId = 5L;
        Long productId = 24L;
        int quantity = 3;
        BigDecimal unitPrice = new BigDecimal("140.00");

        OrderItem orderItem = new OrderItem(productId, quantity, unitPrice);
        Order order = mock(Order.class);

        when(getOrderByIdUseCase.execute(orderId)).thenReturn(order);
        when(order.getId()).thenReturn(orderId);
        when(order.getCustomerId()).thenReturn(customerId);
        when(order.getOrderItems()).thenReturn(List.of(orderItem));
        when(order.getStatus()).thenReturn(OrderStatus.CREATED);
        when(order.getTotal()).thenReturn(unitPrice.multiply(new BigDecimal(quantity)));

        mockMvc
            .perform(get("/orders/{id}", orderId))
            .andExpect(status().isOk())
            .andExpect(content().contentTypeCompatibleWith(APPLICATION_JSON))
            .andExpect(jsonPath("$.id").value(orderId))
            .andExpect(jsonPath("$.customerId").value(customerId))
            .andExpect(jsonPath("$.items[0].productId").value(productId))
            .andExpect(jsonPath("$.items[0].quantity").value(quantity))
            .andExpect(jsonPath("$.items[0].unitPrice").value(unitPrice.doubleValue()))
            .andExpect(jsonPath("$.items[0].subtotal").value(420.00))
            .andExpect(jsonPath("$.status").value(OrderStatus.CREATED.toString()))
            .andExpect(jsonPath("$.total").value(420.00));

        verify(getOrderByIdUseCase).execute(orderId);
    }

    @Test
    void shouldReturnNotFoundWhenOrderDoesNotExist() throws Exception {
        Long orderId = 1100L;

        when(getOrderByIdUseCase.execute(orderId))
            .thenThrow(new OrderNotFoundException(orderId));

        mockMvc
            .perform(get("/orders/{id}", orderId))
            .andExpect(status().isNotFound())
            .andExpect(content().contentTypeCompatibleWith(APPLICATION_JSON))
            .andExpect(jsonPath("$.timestamp").exists())
            .andExpect(jsonPath("$.status").value(404))
            .andExpect(jsonPath("$.error").value("Not Found"))
            .andExpect(jsonPath("$.message").value("Order not found with id: " + orderId))
            .andExpect(jsonPath("$.path").value("/orders/" + orderId));

        verify(getOrderByIdUseCase).execute(orderId);
    }

    @Test
    void shouldListOrdersWithPagination() throws Exception {
        Long orderId1 = 1L;
        Long customerId = 5L;
        int quantity1 = 3;
        BigDecimal unitPrice1 = new BigDecimal("140.00");

        Order order1 = mock(Order.class);

        when(order1.getId()).thenReturn(orderId1);
        when(order1.getCustomerId()).thenReturn(customerId);
        when(order1.getStatus()).thenReturn(OrderStatus.CREATED);
        when(order1.getTotal()).thenReturn(unitPrice1.multiply(new BigDecimal(quantity1)));

        Long orderId2 = 2L;
        int quantity2 = 2;
        BigDecimal unitPrice2 = new BigDecimal("300.00");

        Order order2 = mock(Order.class);

        when(order2.getId()).thenReturn(orderId2);
        when(order2.getCustomerId()).thenReturn(customerId);

        when(order2.getStatus()).thenReturn(OrderStatus.CREATED);
        when(order2.getTotal()).thenReturn(unitPrice2.multiply(new BigDecimal(quantity2)));

        PageQuery pageQuery = new PageQuery(0, 2);
        PageResult<Order> pageResult = new PageResult<>(
            List.of(order1, order2), 0, 2, 2, 1
        );

        when(listOrdersUseCase.execute(pageQuery)).thenReturn(pageResult);

        mockMvc
            .perform(get("/orders")
                .param("page", "0")
                .param("size", "2"))
            .andExpect(status().isOk())
            .andExpect(content().contentTypeCompatibleWith(APPLICATION_JSON))
            .andExpect(jsonPath("$.content[0].id").value(orderId1))
            .andExpect(jsonPath("$.content[0].customerId").value(customerId))
            .andExpect(jsonPath("$.content[0].status")
                .value(OrderStatus.CREATED.toString()))
            .andExpect(jsonPath("$.content[0].total")
                .value(new BigDecimal("420.00").doubleValue()))
            .andExpect(jsonPath("$.content[1].id").value(orderId2))
            .andExpect(jsonPath("$.content[1].customerId").value(customerId))
            .andExpect(jsonPath("$.content[1].status")
                .value(OrderStatus.CREATED.toString()))
            .andExpect(jsonPath("$.content[1].total")
                .value(new BigDecimal("600.00").doubleValue()))
            .andExpect(jsonPath("$.page").value(0))
            .andExpect(jsonPath("$.size").value(2))
            .andExpect(jsonPath("$.totalElements").value(2))
            .andExpect(jsonPath("$.totalPages").value(1));

        verify(listOrdersUseCase).execute(pageQuery);
    }

    @Test
    void shouldReturnBadRequestWhenOrderPageSizeIsInvalid() throws Exception {

        mockMvc
            .perform(get("/orders")
                .param("page", "0")
                .param("size", "121"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.timestamp").exists())
            .andExpect(jsonPath("$.status").value(400))
            .andExpect(jsonPath("$.error").value("Bad Request"))
            .andExpect(jsonPath("$.message")
                .value("Page size must be between 1 and 100"))
            .andExpect(jsonPath("$.path").value("/orders"));

        verifyNoInteractions(listOrdersUseCase);
    }
}
