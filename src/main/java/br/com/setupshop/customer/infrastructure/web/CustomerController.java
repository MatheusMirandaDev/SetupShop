package br.com.setupshop.customer.infrastructure.web;

import br.com.setupshop.customer.application.usecase.create.CreateCustomerCommand;
import br.com.setupshop.customer.application.usecase.create.CreateCustomerUseCase;
import br.com.setupshop.customer.domain.model.Customer;
import br.com.setupshop.customer.infrastructure.web.dto.CreateCustomerRequest;
import br.com.setupshop.customer.infrastructure.web.dto.CustomerResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/customers")
public class CustomerController {

    private final CreateCustomerUseCase createCustomerUseCase;

    public CustomerController(CreateCustomerUseCase createCustomerUseCase) {
        this.createCustomerUseCase = createCustomerUseCase;
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
}
