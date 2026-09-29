package br.com.setupshop.customer.infrastructure.web;

import br.com.setupshop.customer.application.usecase.create.CreateCustomerCommand;
import br.com.setupshop.customer.application.usecase.create.CreateCustomerUseCase;
import br.com.setupshop.customer.application.usecase.deactivate.DeactivateCustomerUseCase;
import br.com.setupshop.customer.application.usecase.get.GetCustomerByIdUseCase;
import br.com.setupshop.customer.application.usecase.list.ListCustomersUseCase;
import br.com.setupshop.customer.application.usecase.update.UpdateCustomerCommand;
import br.com.setupshop.customer.application.usecase.update.UpdateCustomerUseCase;
import br.com.setupshop.customer.domain.model.Customer;
import br.com.setupshop.customer.infrastructure.web.dto.CreateCustomerRequest;
import br.com.setupshop.customer.infrastructure.web.dto.CustomerResponse;
import br.com.setupshop.customer.infrastructure.web.dto.UpdateCustomerRequest;
import br.com.setupshop.shared.infrastructure.web.dto.PageResponse;
import br.com.setupshop.shared.infrastructure.web.error.ApiErrorResponse;
import br.com.setupshop.shared.infrastructure.web.error.ValidationErrorResponse;
import br.com.setupshop.shared.pagination.PageQuery;
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
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(
    name = "Customers",
    description = "Operations for managing customers"
)
@RestController
@RequestMapping("/customers")
public class CustomerController {

    private final CreateCustomerUseCase createCustomerUseCase;
    private final GetCustomerByIdUseCase getCustomerByIdUseCase;
    private final ListCustomersUseCase listCustomersUseCase;
    private final UpdateCustomerUseCase updateCustomerUseCase;
    private final DeactivateCustomerUseCase deactivateCustomerUseCase;

    public CustomerController(
        CreateCustomerUseCase createCustomerUseCase,
        GetCustomerByIdUseCase getCustomerByIdUseCase,
        ListCustomersUseCase listCustomersUseCase,
        UpdateCustomerUseCase updateCustomerUseCase,
        DeactivateCustomerUseCase deactivateCustomerUseCase) {
        this.createCustomerUseCase = createCustomerUseCase;
        this.getCustomerByIdUseCase = getCustomerByIdUseCase;
        this.listCustomersUseCase = listCustomersUseCase;
        this.updateCustomerUseCase = updateCustomerUseCase;
        this.deactivateCustomerUseCase = deactivateCustomerUseCase;
    }

    @Operation(
        summary = "Create a customer",
        description = "Creates an active customer. Name, email and phone are required."
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "201",
            description = "Customer created successfully",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = CustomerResponse.class)
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Invalid customer data",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(oneOf = {
                    ValidationErrorResponse.class,
                    ApiErrorResponse.class
                })
            )
        ),
        @ApiResponse(
            responseCode = "409",
            description = "Email already exists",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ApiErrorResponse.class)
            )
        )
    })
    @PostMapping
    public ResponseEntity<CustomerResponse> createCustomer(
        @Valid @RequestBody CreateCustomerRequest request) {

        CreateCustomerCommand command =
            new CreateCustomerCommand(request.name(), request.email(), request.phone());

        Customer customer = createCustomerUseCase.execute(command);

        CustomerResponse response =
            new CustomerResponse(
                customer.getId(),
                customer.getName(),
                customer.getEmail(),
                customer.getPhone(),
                customer.isActive());

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Operation(
        summary = "Get a customer by ID",
        description = "Returns the customer identified by the provided ID."
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "Customer found",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = CustomerResponse.class)
            )
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Customer not found",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ApiErrorResponse.class)
            )
        )
    })
    @GetMapping("/{id}")
    public ResponseEntity<CustomerResponse> getCustomerById(
        @Parameter(description = "Customer identifier", example = "1")
        @PathVariable Long id) {

        var customer = getCustomerByIdUseCase.execute(id);

        CustomerResponse response =
            new CustomerResponse(
                customer.getId(),
                customer.getName(),
                customer.getEmail(),
                customer.getPhone(),
                customer.isActive());

        return ResponseEntity.ok(response);
    }

    @Operation(
        summary = "List customers",
        description = "Returns a paginated list of customers."
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "Customers listed successfully",
            useReturnTypeSchema = true
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Invalid pagination parameters",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ApiErrorResponse.class)
            )
        )
    })
    @GetMapping
    public ResponseEntity<PageResponse<CustomerResponse>> listCustomers(
        @Parameter(description = "Zero-based page number", example = "0")
        @RequestParam(name = "page", defaultValue = "0") int page,
        @Parameter(description = "Number of customers per page, between 1 and 100", example = "20")
        @RequestParam(name = "size", defaultValue = "20") int size) {

        PageQuery pageQuery = new PageQuery(page, size);

        var pageResult = listCustomersUseCase.execute(pageQuery);

        var responses =
            pageResult.content().stream()
                .map(customer ->
                    new CustomerResponse(
                        customer.getId(),
                        customer.getName(),
                        customer.getEmail(),
                        customer.getPhone(),
                        customer.isActive()
                    )
                ).toList();

        PageResponse<CustomerResponse> pageResponse =
            new PageResponse<>(
                responses,
                pageResult.page(),
                pageResult.size(),
                pageResult.totalElements(),
                pageResult.totalPages()
            );

        return ResponseEntity.ok(pageResponse);
    }

    @Operation(
        summary = "Partially update a customer",
        description = "Updates only the provided customer fields. At least one field must be provided."
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "Customer updated successfully",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = CustomerResponse.class)
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Invalid request or customer data",
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
            description = "Customer not found",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ApiErrorResponse.class)
            )
        ),
        @ApiResponse(
            responseCode = "409",
            description = "Email already exists",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ApiErrorResponse.class)
            )
        )
    })
    @PatchMapping("/{id}")
    public ResponseEntity<CustomerResponse> updateCustomer(
        @Parameter(description = "Customer identifier", example = "1")
        @PathVariable Long id,
        @Valid @RequestBody UpdateCustomerRequest request) {

        UpdateCustomerCommand customerCommand =
            new UpdateCustomerCommand(
                request.name(),
                request.email(),
                request.phone()
            );

        var updatedCustomer = updateCustomerUseCase.execute(id, customerCommand);

        CustomerResponse customerResponse =
            new CustomerResponse(
                updatedCustomer.getId(),
                updatedCustomer.getName(),
                updatedCustomer.getEmail(),
                updatedCustomer.getPhone(),
                updatedCustomer.isActive()
            );

        return ResponseEntity.ok(customerResponse);
    }

    @Operation(
        summary = "Deactivate a customer",
        description = "Logically deactivates the customer without removing its record."
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "204",
            description = "Customer deactivated successfully"
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Customer not found",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ApiErrorResponse.class)
            )
        )
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deactivateCustomer(
        @Parameter(description = "Customer identifier", example = "1")
        @PathVariable Long id) {

        deactivateCustomerUseCase.execute(id);
        return ResponseEntity.noContent().build();
    }
}
