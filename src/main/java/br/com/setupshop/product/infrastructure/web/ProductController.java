package br.com.setupshop.product.infrastructure.web;

import br.com.setupshop.product.application.usecase.create.CreateProductCommand;
import br.com.setupshop.product.application.usecase.create.CreateProductUseCase;
import br.com.setupshop.product.application.usecase.get.GetProductByIdUseCase;
import br.com.setupshop.product.application.usecase.list.ListProductsUseCase;
import br.com.setupshop.product.application.usecase.update.UpdateProductCommand;
import br.com.setupshop.product.application.usecase.update.UpdateProductUseCase;
import br.com.setupshop.product.domain.model.Product;
import br.com.setupshop.product.infrastructure.web.dto.CreateProductRequest;
import br.com.setupshop.product.infrastructure.web.dto.ProductResponse;
import br.com.setupshop.product.infrastructure.web.dto.UpdateProductRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/products")
public class ProductController {

    private final CreateProductUseCase createProductUseCase;
    private final GetProductByIdUseCase getProductByIdUseCase;
    private final ListProductsUseCase listProductsUseCase;
    private final UpdateProductUseCase updateProductUseCase;

    public ProductController(CreateProductUseCase productUseCase, GetProductByIdUseCase productByIdUseCase, ListProductsUseCase listProductsUseCase, UpdateProductUseCase updateProductUseCase) {
        this.createProductUseCase = productUseCase;
        this.getProductByIdUseCase = productByIdUseCase;
        this.listProductsUseCase = listProductsUseCase;
        this.updateProductUseCase = updateProductUseCase;
    }

    @PostMapping
    public ResponseEntity<ProductResponse> createProduct(@Valid @RequestBody CreateProductRequest request) {

        CreateProductCommand command = new CreateProductCommand(
                request.name(),
                request.description(),
                request.price()
        );

        Product product = createProductUseCase.execute(command);

        var response = new ProductResponse(
                product.getId(),
                product.getName(),
                product.getDescription(),
                product.getPrice(),
                product.isActive()
        );

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductResponse> getProductById(@PathVariable Long id) {

        var product = getProductByIdUseCase.execute(id);

        var response = new ProductResponse(
                product.getId(),
                product.getName(),
                product.getDescription(),
                product.getPrice(),
                product.isActive()
        );
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<List<ProductResponse>> listProducts() {

        var products =  listProductsUseCase.execute();

        var responses = products.stream()
                .map(product -> new ProductResponse(
                        product.getId(),
                        product.getName(),
                        product.getDescription(),
                        product.getPrice(),
                        product.isActive()
                ))
                .toList();

        return ResponseEntity.ok(responses);
    }

    @PatchMapping("/{id}")
    public ResponseEntity<ProductResponse> updateProduct(@PathVariable Long id, @Valid @RequestBody UpdateProductRequest request) {

        var updateProductCommand = new UpdateProductCommand(
                request.name(),
                request.description(),
                request.price()
        ) ;

        var product = updateProductUseCase.execute(id, updateProductCommand);

        var response = new ProductResponse(
                product.getId(),
                product.getName(),
                product.getDescription(),
                product.getPrice(),
                product.isActive()
        );

        return ResponseEntity.ok(response);
    }
}