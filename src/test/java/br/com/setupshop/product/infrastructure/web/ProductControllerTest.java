package br.com.setupshop.product.infrastructure.web;

import br.com.setupshop.product.application.usecase.create.CreateProductCommand;
import br.com.setupshop.product.application.usecase.create.CreateProductUseCase;
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
                .andExpect(status().isBadRequest());

        verifyNoInteractions(createProductUseCase);
    }
}