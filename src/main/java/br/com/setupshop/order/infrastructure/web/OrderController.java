package br.com.setupshop.order.infrastructure.web;

import br.com.setupshop.order.application.usecase.get.GetOrderByIdUseCase;
import br.com.setupshop.order.application.usecase.place.PlaceOrderCommand;
import br.com.setupshop.order.application.usecase.place.PlaceOrderItemCommand;
import br.com.setupshop.order.application.usecase.place.PlaceOrderUseCase;
import br.com.setupshop.order.domain.model.Order;
import br.com.setupshop.order.domain.model.OrderItem;
import br.com.setupshop.order.infrastructure.web.dto.OrderItemResponse;
import br.com.setupshop.order.infrastructure.web.dto.OrderResponse;
import br.com.setupshop.order.infrastructure.web.dto.PlaceOrderItemRequest;
import br.com.setupshop.order.infrastructure.web.dto.PlaceOrderRequest;
import br.com.setupshop.shared.infrastructure.web.error.ApiErrorResponse;
import br.com.setupshop.shared.infrastructure.web.error.ValidationErrorResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

@Tag(
    name = "Orders",
    description = "Operations for managing orders"
)
@RestController
@RequestMapping("/orders")
public class OrderController {

    private final PlaceOrderUseCase placeOrderUseCase;
    private final GetOrderByIdUseCase getOrderByIdUseCase;

    public OrderController(
        PlaceOrderUseCase placeOrderUseCase,
        GetOrderByIdUseCase getOrderByIdUseCase) {
        this.placeOrderUseCase = placeOrderUseCase;
        this.getOrderByIdUseCase = getOrderByIdUseCase;
    }

    private OrderResponse toResponse(Order order) {
        List<OrderItemResponse> itemsResponses = new ArrayList<>();

        for (OrderItem item : order.getOrderItems()) {
            OrderItemResponse itemResponse =
                new OrderItemResponse(item.getProductId(), item.getQuantity(), item.getUnitPrice(), item.subtotal());

            itemsResponses.add(itemResponse);
        }

        return new OrderResponse(
            order.getId(),
            order.getCustomerId(),
            itemsResponses,
            order.getStatus(),
            order.getTotal()
        );
    }

    @Operation(
        summary = "Place Order",
        description = "Creates an order for an active customer with active products, capturing their unit prices before payment."
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "201",
            description = "Order placed successfully",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = OrderResponse.class)
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Invalid order data",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(oneOf = {
                    ValidationErrorResponse.class,
                    ApiErrorResponse.class
                })
            )
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Product or customer not found",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ApiErrorResponse.class)
            )
        ),
        @ApiResponse(
            responseCode = "409",
            description = "Product or customer is inactive",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ApiErrorResponse.class)
            )
        )
    })
    @PostMapping
    public ResponseEntity<OrderResponse> placeOrder(
        @Valid @RequestBody PlaceOrderRequest request) {

        List<PlaceOrderItemCommand> itemsCommand = new ArrayList<>();

        for (PlaceOrderItemRequest itemRequest : request.items()) {
            PlaceOrderItemCommand itemCommand =
                new PlaceOrderItemCommand(itemRequest.productId(), itemRequest.quantity());

            itemsCommand.add(itemCommand);
        }

        PlaceOrderCommand orderCommand =
            new PlaceOrderCommand(request.customerId(), itemsCommand);

        Order order = placeOrderUseCase.execute(orderCommand);

        OrderResponse response = toResponse(order);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Operation(
        summary = "Get an order by ID",
        description = "Returns the order identified by the provided ID."
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "Order found successfully",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = OrderResponse.class)
            )
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Order not found",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ApiErrorResponse.class)
            )
        )
    })
    @GetMapping("/{id}")
    public ResponseEntity<OrderResponse> getOrderById(
        @Parameter(description = "Order identifier", example = "1")
        @PathVariable Long id) {

        Order order = getOrderByIdUseCase.execute(id);

        OrderResponse response = toResponse(order);

        return ResponseEntity.ok(response);
    }
}
