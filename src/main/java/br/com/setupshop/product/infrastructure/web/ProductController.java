package br.com.setupshop.product.infrastructure.web;

import br.com.setupshop.product.application.usecase.create.CreateProductCommand;
import br.com.setupshop.product.application.usecase.create.CreateProductUseCase;
import br.com.setupshop.product.domain.model.Product;
import br.com.setupshop.product.infrastructure.web.dto.CreateProductRequest;
import br.com.setupshop.product.infrastructure.web.dto.ProductResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/products")
public class ProductController {


    private final CreateProductUseCase productUseCase;

    public ProductController(CreateProductUseCase productUseCase) {
        this.productUseCase = productUseCase;
    }

    @PostMapping
    public ResponseEntity<ProductResponse> createProduct(@Valid @RequestBody CreateProductRequest request) {

        CreateProductCommand command = new CreateProductCommand(
                request.name(),
                request.description(),
                request.price()
        );

        Product product = productUseCase.execute(command);

        ProductResponse response = new ProductResponse(
                product.getId(),
                product.getName(),
                product.getDescription(),
                product.getPrice(),
                product.isActive()
        );


        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

}