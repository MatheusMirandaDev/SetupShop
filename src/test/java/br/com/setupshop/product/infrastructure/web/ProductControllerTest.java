package br.com.setupshop.product.infrastructure.web;

import br.com.setupshop.product.application.usecase.create.CreateProductCommand;
import br.com.setupshop.product.application.usecase.create.CreateProductUseCase;
import br.com.setupshop.product.application.usecase.get.GetProductByIdUseCase;
import br.com.setupshop.product.domain.exception.ProductNotFoundException;
import br.com.setupshop.product.domain.model.Product;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ProductController.class)
class ProductControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CreateProductUseCase createProductUseCase;

    @MockitoBean
    private GetProductByIdUseCase getProductByIdUseCase;

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
}