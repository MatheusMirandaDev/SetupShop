package br.com.setupshop.customer.infrastructure.web;

import br.com.setupshop.customer.application.usecase.create.CreateCustomerCommand;
import br.com.setupshop.customer.application.usecase.create.CreateCustomerUseCase;
import br.com.setupshop.customer.application.usecase.get.GetCustomerByIdUseCase;
import br.com.setupshop.customer.domain.model.Customer;
import br.com.setupshop.customer.infrastructure.web.dto.CreateCustomerRequest;
import br.com.setupshop.customer.infrastructure.web.dto.CustomerResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/customers")
public class CustomerController {

    private final CreateCustomerUseCase createCustomerUseCase;
    private final GetCustomerByIdUseCase getCustomerByIdUseCase;

    public CustomerController(CreateCustomerUseCase createCustomerUseCase, GetCustomerByIdUseCase getCustomerByIdUseCase) {
        this.createCustomerUseCase = createCustomerUseCase;
        this.getCustomerByIdUseCase = getCustomerByIdUseCase;
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
}
