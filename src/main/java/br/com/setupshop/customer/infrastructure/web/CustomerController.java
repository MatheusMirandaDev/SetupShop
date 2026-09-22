package br.com.setupshop.customer.infrastructure.web;

import br.com.setupshop.customer.application.usecase.create.CreateCustomerCommand;
import br.com.setupshop.customer.application.usecase.create.CreateCustomerUseCase;
import br.com.setupshop.customer.application.usecase.get.GetCustomerByIdUseCase;
import br.com.setupshop.customer.application.usecase.list.ListCustomersUseCase;
import br.com.setupshop.customer.application.usecase.update.UpdateCustomerCommand;
import br.com.setupshop.customer.application.usecase.update.UpdateCustomerUseCase;
import br.com.setupshop.customer.domain.model.Customer;
import br.com.setupshop.customer.infrastructure.web.dto.CreateCustomerRequest;
import br.com.setupshop.customer.infrastructure.web.dto.CustomerResponse;
import br.com.setupshop.customer.infrastructure.web.dto.UpdateCustomerRequest;
import br.com.setupshop.shared.infrastructure.web.dto.PageResponse;
import br.com.setupshop.shared.pagination.PageQuery;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/customers")
public class CustomerController {

    private final CreateCustomerUseCase createCustomerUseCase;
    private final GetCustomerByIdUseCase getCustomerByIdUseCase;
    private final ListCustomersUseCase listCustomersUseCase;
    private final UpdateCustomerUseCase updateCustomerUseCase;

    public CustomerController(
        CreateCustomerUseCase createCustomerUseCase,
        GetCustomerByIdUseCase getCustomerByIdUseCase,
        ListCustomersUseCase listCustomersUseCase,
        UpdateCustomerUseCase updateCustomerUseCase) {
        this.createCustomerUseCase = createCustomerUseCase;
        this.getCustomerByIdUseCase = getCustomerByIdUseCase;
        this.listCustomersUseCase = listCustomersUseCase;
        this.updateCustomerUseCase = updateCustomerUseCase;
    }

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

    @GetMapping("/{id}")
    public ResponseEntity<CustomerResponse> getCustomerById(@PathVariable Long id) {

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

    @GetMapping
    public ResponseEntity<PageResponse<CustomerResponse>> listCustomers(
        @RequestParam(name = "page", defaultValue = "0") int page,
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

    @PatchMapping("/{id}")
    public ResponseEntity<CustomerResponse> updateCustomer(
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
}
