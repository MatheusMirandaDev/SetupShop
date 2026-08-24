package br.com.setupshop.product.infrastructure.web;

import br.com.setupshop.product.application.usecase.create.CreateProductCommand;
import br.com.setupshop.product.application.usecase.create.CreateProductUseCase;
import br.com.setupshop.product.application.usecase.deactivate.DeactivateProductUseCase;
import br.com.setupshop.product.application.usecase.get.GetProductByIdUseCase;
import br.com.setupshop.product.application.usecase.list.ListProductsUseCase;
import br.com.setupshop.product.application.usecase.update.UpdateProductCommand;
import br.com.setupshop.product.application.usecase.update.UpdateProductUseCase;
import br.com.setupshop.product.domain.model.Product;
import br.com.setupshop.product.infrastructure.web.dto.CreateProductRequest;
import br.com.setupshop.product.infrastructure.web.dto.PageResponse;
import br.com.setupshop.product.infrastructure.web.dto.ProductResponse;
import br.com.setupshop.product.infrastructure.web.dto.UpdateProductRequest;
import br.com.setupshop.product.infrastructure.web.error.ApiErrorResponse;
import br.com.setupshop.product.infrastructure.web.error.ValidationErrorResponse;
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
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(
    name = "Products",
    description = "Operations for managing products"
)
@RestController
@RequestMapping("/products")
public class ProductController {

    private final CreateProductUseCase createProductUseCase;
    private final GetProductByIdUseCase getProductByIdUseCase;
    private final ListProductsUseCase listProductsUseCase;
    private final UpdateProductUseCase updateProductUseCase;
    private final DeactivateProductUseCase deactivateProductUseCase;

    public ProductController(
        CreateProductUseCase productUseCase,
        GetProductByIdUseCase productByIdUseCase,
        ListProductsUseCase listProductsUseCase,
        UpdateProductUseCase updateProductUseCase,
        DeactivateProductUseCase deactivateProductUseCase) {
        this.createProductUseCase = productUseCase;
        this.getProductByIdUseCase = productByIdUseCase;
        this.listProductsUseCase = listProductsUseCase;
        this.updateProductUseCase = updateProductUseCase;
        this.deactivateProductUseCase = deactivateProductUseCase;
    }

    @Operation(
        summary = "Create a product",
        description = "Creates an active product. Name and price are required; description is optional."
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "201",
            description = "Product created successfully",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ProductResponse.class)
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Invalid product data",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(oneOf = {
                    ValidationErrorResponse.class,
                    ApiErrorResponse.class
                })
            )
        )
    })
    @PostMapping
    public ResponseEntity<ProductResponse> createProduct(
        @Valid @RequestBody CreateProductRequest request) {

        CreateProductCommand command =
            new CreateProductCommand(request.name(), request.description(), request.price());

        Product product = createProductUseCase.execute(command);

        var response =
            new ProductResponse(
                product.getId(),
                product.getName(),
                product.getDescription(),
                product.getPrice(),
                product.isActive());

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Operation(
        summary = "Get a product by ID",
        description = "Returns the product identified by the provided ID."
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "Product found",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ProductResponse.class)
            )
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Product not found",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ApiErrorResponse.class)
            )
        )
    })
    @GetMapping("/{id}")
    public ResponseEntity<ProductResponse> getProductById(
        @Parameter(description = "Product identifier", example = "1")
        @PathVariable Long id) {

        var product = getProductByIdUseCase.execute(id);

        var response =
            new ProductResponse(
                product.getId(),
                product.getName(),
                product.getDescription(),
                product.getPrice(),
                product.isActive());
        return ResponseEntity.ok(response);
    }

    @Operation(
        summary = "List products",
        description = "Returns a paginated list of products."
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "Products listed successfully",
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
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<PageResponse<ProductResponse>> listProducts(
        @Parameter(description = "Zero-based page number", example = "0")
        @RequestParam(name = "page", defaultValue = "0") int page,
        @Parameter(description = "Number of products per page, between 1 and 100", example = "20")
        @RequestParam(name = "size", defaultValue = "20") int size) {

        PageQuery pageQuery = new PageQuery(page, size);

        var pageResult = listProductsUseCase.execute(pageQuery);

        var responses =
            pageResult.content().stream()
                .map(
                    product ->
                        new ProductResponse(
                            product.getId(),
                            product.getName(),
                            product.getDescription(),
                            product.getPrice(),
                            product.isActive()))
                .toList();

        PageResponse<ProductResponse> pageResponse =
            new PageResponse<>(
                responses,
                pageResult.page(),
                pageResult.size(),
                pageResult.totalElements(),
                pageResult.totalPages());

        return ResponseEntity.ok(pageResponse);
    }

    @Operation(
        summary = "Partially update a product",
        description = "Updates only the provided product fields. At least one field must be provided."
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "Product updated successfully",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ProductResponse.class)
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Invalid request or product data",
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
            description = "Product not found",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ApiErrorResponse.class)
            )
        )
    })
    @PatchMapping("/{id}")
    public ResponseEntity<ProductResponse> updateProduct(
        @Parameter(description = "Product identifier", example = "1")
        @PathVariable Long id,
        @Valid @RequestBody UpdateProductRequest request) {

        var updateProductCommand =
            new UpdateProductCommand(request.name(), request.description(), request.price());

        var product = updateProductUseCase.execute(id, updateProductCommand);

        var response =
            new ProductResponse(
                product.getId(),
                product.getName(),
                product.getDescription(),
                product.getPrice(),
                product.isActive());

        return ResponseEntity.ok(response);
    }

    @Operation(
        summary = "Deactivate a product",
        description = "Logically deactivates the product without removing its record."
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "204",
            description = "Product deactivated successfully"
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Product not found",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ApiErrorResponse.class)
            )
        )
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deactivateProduct(
        @Parameter(description = "Product identifier", example = "1")
        @PathVariable Long id) {
        deactivateProductUseCase.execute(id);
        return ResponseEntity.noContent().build();
    }
}
