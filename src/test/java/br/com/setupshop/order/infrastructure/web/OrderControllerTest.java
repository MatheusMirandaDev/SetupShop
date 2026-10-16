package br.com.setupshop.order.infrastructure.web;

import br.com.setupshop.customer.domain.exception.CustomerNotFoundException;
import br.com.setupshop.order.application.usecase.place.PlaceOrderCommand;
import br.com.setupshop.order.application.usecase.place.PlaceOrderItemCommand;
import br.com.setupshop.order.application.usecase.place.PlaceOrderUseCase;
import br.com.setupshop.order.domain.exception.InactiveCustomerException;
import br.com.setupshop.order.domain.exception.InactiveProductException;
import br.com.setupshop.order.domain.model.Order;
import br.com.setupshop.order.domain.model.OrderItem;
import br.com.setupshop.order.domain.model.OrderStatus;
import br.com.setupshop.product.domain.exception.ProductNotFoundException;
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
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;
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
}
