package br.com.setupshop.product.infrastructure.web;

import br.com.setupshop.product.application.usecase.create.CreateProductCommand;
import br.com.setupshop.product.application.usecase.create.CreateProductUseCase;
import br.com.setupshop.product.application.usecase.deactivate.DeactivateProductUseCase;
import br.com.setupshop.product.application.usecase.get.GetProductByIdUseCase;
import br.com.setupshop.product.application.usecase.list.ListProductsUseCase;
import br.com.setupshop.product.application.usecase.update.UpdateProductCommand;
import br.com.setupshop.product.application.usecase.update.UpdateProductUseCase;
import br.com.setupshop.product.domain.exception.ProductNotFoundException;
import br.com.setupshop.product.domain.model.Product;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ProductController.class)
class ProductControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CreateProductUseCase createProductUseCase;

    @MockitoBean
    private GetProductByIdUseCase getProductByIdUseCase;

    @MockitoBean
    private ListProductsUseCase listProductsUseCase;

    @MockitoBean
    private UpdateProductUseCase updateProductUseCase;

    @MockitoBean
    private DeactivateProductUseCase deactivateProductUseCase;

    @Test
    void shouldCreateProductAndReturnCreated() throws Exception {
        // Given
        String name = "Teclado AULA F75";
        String description = "Teclado mecanico";
        BigDecimal price = new BigDecimal("300.00");

        Product createdProduct = new Product(name, description, price);

        when(createProductUseCase.execute(any(CreateProductCommand.class))).thenReturn(createdProduct);

        String requestBody = """
            {
              "name": "%s",
              "description": "%s",
              "price": %s
            }
            """.formatted(name, description, price);

        // When - Then
        mockMvc.perform(post("/products")
                        .contentType(APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isCreated())
                .andExpect(content().contentTypeCompatibleWith(APPLICATION_JSON))
                .andExpect(jsonPath("$.name").value(name))
                .andExpect(jsonPath("$.description").value(description))
                .andExpect(jsonPath("$.price").value(price.doubleValue()))
                .andExpect(jsonPath("$.active").value(true));

        ArgumentCaptor<CreateProductCommand> commandCaptor =
                ArgumentCaptor.forClass(CreateProductCommand.class);

        verify(createProductUseCase).execute(commandCaptor.capture());

        CreateProductCommand capturedCommand = commandCaptor.getValue();

        assertEquals(name, capturedCommand.name());
        assertEquals(description, capturedCommand.description());
        assertEquals(0, price.compareTo(capturedCommand.price()));
    }

    @Test
    void shouldReturnBadRequestWhenProductNameIsBlank() throws Exception {
        // Given
        String requestBody = """
                {
                  "name": "",
                  "description": "Teclado mecanico",
                  "price": 300.00
                }
                """;

        // When - Then
        mockMvc.perform(post("/products")
                        .contentType(APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message").value("Validation failed"))
                .andExpect(jsonPath("$.path").value("/products"))
                .andExpect(jsonPath("$.fieldErrors.name").value("must not be blank"));

        verifyNoInteractions(createProductUseCase);
    }

    @Test
    void shouldReturnBadRequestWhenDomainRejectsProduct()  throws Exception {
        // Given
        String requestBody = """
                {
                  "name": "Teclado AULA F75",
                  "description": "Teclado mecanico",
                  "price": 300.999
                }
                """;

        when(createProductUseCase.execute(any(CreateProductCommand.class)))
                .thenThrow(new IllegalArgumentException("Price must have at most two decimal places"));

        // When - Then
        mockMvc.perform(post("/products")
                        .contentType(APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message").value("Price must have at most two decimal places"))
                .andExpect(jsonPath("$.path").value("/products"));

        verify(createProductUseCase)
                .execute(any(CreateProductCommand.class));
    }

    @Test
    void shouldReturnProductWhenFound() throws Exception {
        Long productId = 1L;
        String name = "Teclado AULA F75";
        String description = "Teclado mecanico";
        BigDecimal price = new BigDecimal("300.00");

        Product createdProduct = new Product(name, description, price);

        when(getProductByIdUseCase.execute(productId)).thenReturn(createdProduct);

        mockMvc.perform(get("/products/{id}",  productId))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(APPLICATION_JSON))
                .andExpect(jsonPath("$.name").value(name))
                .andExpect(jsonPath("$.description").value(description))
                .andExpect(jsonPath("$.price").value(price.doubleValue()))
                .andExpect(jsonPath("$.active").value(true));

        verify(getProductByIdUseCase).execute(productId);
    }

    @Test
    void shouldReturnNotFoundWhenProductDoesNotExist() throws Exception {
        Long productId = 1000L;

        when(getProductByIdUseCase.execute(productId))
                .thenThrow(new ProductNotFoundException(productId));

        mockMvc.perform(get("/products/{id}",  productId))
                .andExpect(status().isNotFound())
                .andExpect(content().contentTypeCompatibleWith(APPLICATION_JSON))
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.message").value("Product not found with id: " + productId))
                .andExpect(jsonPath("$.path").value("/products/" + productId));

        verify(getProductByIdUseCase).execute(productId);
    }

    @Test
    void shouldReturnAllProducts() throws Exception {

        String firstProductName = "Teclado AULA F75";
        String firstProductDescription = "Teclado mecanico";
        BigDecimal firstProductPrice = new BigDecimal("300.00");
        Product product1 = new Product(firstProductName, firstProductDescription, firstProductPrice);

        String secondProductName = "Mouse Mchose A9";
        String secondProductDescription = "Mouse para computador";
        BigDecimal secondProductPrice = new BigDecimal("180.00");
        Product product2 = new Product(secondProductName, secondProductDescription, secondProductPrice);

        when(listProductsUseCase.execute()).thenReturn(List.of(product1, product2));

        mockMvc.perform(get("/products"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(APPLICATION_JSON))
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].name").value(firstProductName))
                .andExpect(jsonPath("$[0].description").value(firstProductDescription))
                .andExpect(jsonPath("$[0].price").value(firstProductPrice.doubleValue()))
                .andExpect(jsonPath("$[0].active").value(true))
                .andExpect(jsonPath("$[1].name").value(secondProductName))
                .andExpect(jsonPath("$[1].description").value(secondProductDescription))
                .andExpect(jsonPath("$[1].price").value(secondProductPrice.doubleValue()))
                .andExpect(jsonPath("$[1].active").value(true));

        verify(listProductsUseCase).execute();
    }

    @Test
    void shouldReturnEmptyListWhenNoProductsExist() throws Exception {
        when(listProductsUseCase.execute()).thenReturn(List.of());

        mockMvc.perform(get("/products"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(APPLICATION_JSON))
                .andExpect(jsonPath("$.length()").value(0));

        verify(listProductsUseCase).execute();
    }

    @Test
    void shouldPartiallyUpdateProductAndReturnOk() throws Exception {
        var productId =  1L;
        var nameUpdated = "Mouse Mchose A9 PRO";
        var priceUpdated = new BigDecimal("200.00");
        var expectedDescription = "Mouse para computador";

        Product updatedProduct = new Product(
                nameUpdated,
                expectedDescription,
                priceUpdated
        );


        when(updateProductUseCase.execute(eq(productId), any(UpdateProductCommand.class))).thenReturn(updatedProduct);

        String requestBody = """
                {
                  "name": "Mouse Mchose A9 PRO",
                  "description": null,
                  "price": 200.00
                }
                """;

        mockMvc.perform(patch("/products/{id}", productId)
                        .contentType(APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(APPLICATION_JSON))
                .andExpect(jsonPath("$.name").value(nameUpdated))
                .andExpect(jsonPath("$.description").value(expectedDescription))
                .andExpect(jsonPath("$.price").value(priceUpdated.doubleValue()))
                .andExpect(jsonPath("$.active").value(true));

        ArgumentCaptor<UpdateProductCommand> argumentCaptor =
                ArgumentCaptor.forClass(UpdateProductCommand.class);

        verify(updateProductUseCase)
                .execute(eq(productId), argumentCaptor.capture());

        var capturedCommand = argumentCaptor.getValue();

        assertEquals(nameUpdated, capturedCommand.name());
        assertNull(capturedCommand.description());
        assertEquals(0, priceUpdated.compareTo(capturedCommand.price()));
    }

    @Test
    void shouldReturnBadRequestWhenUpdateRequestIsInvalid() throws Exception {
        var productId =  1L;

        String description = "A".repeat(501);
        String requestBody = """
        {
          "description": "%s"
        }
        """.formatted(description);

        mockMvc.perform(patch("/products/{id}", productId)
                        .contentType(APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(APPLICATION_JSON))
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message").value("Validation failed"))
                .andExpect(jsonPath("$.path").value("/products/" + productId))
                .andExpect(jsonPath("$.fieldErrors.description").exists());

        verifyNoInteractions(updateProductUseCase);
    }

    @Test
    void shouldReturnNotFoundWhenUpdatingNonexistentProduct() throws Exception {
        var productId =  1L;

        String requestBody = """
        {
          "price": 200
        }
        """;

        when(updateProductUseCase.execute(eq(productId), any(UpdateProductCommand.class))).thenThrow(new ProductNotFoundException(productId));

        mockMvc.perform(patch("/products/{id}", productId)
                        .contentType(APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isNotFound())
                .andExpect(content().contentTypeCompatibleWith(APPLICATION_JSON))
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.message").value("Product not found with id: " + productId))
                .andExpect(jsonPath("$.path").value("/products/" + productId));

        verify(updateProductUseCase).execute(eq(productId), any(UpdateProductCommand.class));
    }

    @Test
    void shouldDeactivateProductAndReturnNoContent() throws Exception {
        var productId =  1L;

        mockMvc.perform(delete("/products/{id}", productId))
                .andExpect(status().isNoContent())
                .andExpect(content().string(""));

        verify(deactivateProductUseCase).execute(productId);
    }

    @Test
    void shouldReturnNotFoundWhenDeactivatingNonexistentProduct() throws Exception {
        var productId =  100L;

        doThrow(new ProductNotFoundException(productId)).when(deactivateProductUseCase).execute(productId);

        mockMvc.perform(delete("/products/{id}", productId))
                .andExpect(status().isNotFound())
                .andExpect(content().contentTypeCompatibleWith(APPLICATION_JSON))
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.message").value("Product not found with id: " + productId))
                .andExpect(jsonPath("$.path").value("/products/" + productId));

        verify(deactivateProductUseCase).execute(productId);
    }
}