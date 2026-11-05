package br.com.setupshop.product.infrastructure.web;

import br.com.setupshop.product.application.usecase.create.CreateProductCommand;
import br.com.setupshop.product.application.usecase.create.CreateProductUseCase;
import br.com.setupshop.product.application.usecase.deactivate.DeactivateProductUseCase;
import br.com.setupshop.product.application.usecase.get.GetProductByIdUseCase;
import br.com.setupshop.product.application.usecase.list.ListProductsUseCase;
import br.com.setupshop.product.application.usecase.update.UpdateProductUseCase;
import br.com.setupshop.product.domain.model.Product;
import br.com.setupshop.shared.config.SecurityConfig;
import br.com.setupshop.shared.pagination.PageQuery;
import br.com.setupshop.shared.pagination.PageResult;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ProductController.class)
@Import(SecurityConfig.class)
@TestPropertySource(properties = {
    "AUTH0_ISSUER_URI=https://issuer.example/",
    "AUTH0_AUDIENCE=https://api.example"
})
class ProductSecurityTest {

    @Autowired
    MockMvc mockMvc;

    @MockitoBean
    JwtDecoder jwtDecoder;

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
    void shouldAllowUnauthenticatedProductListing() throws Exception {

        when(listProductsUseCase.execute(any(PageQuery.class)))
            .thenReturn(
                new PageResult<Product>(List.of(), 0, 20, 0L, 0)
            );

        mockMvc
            .perform(get("/products"))
            .andExpect(status().isOk());

    }

    @Test
    void shouldAllowUnauthenticatedProductLookupById() throws Exception {

        Long productId = 1L;
        String name = "Teclado AULA F75";
        String description = "Teclado mecanico";
        BigDecimal price = new BigDecimal("300.00");

        when(getProductByIdUseCase.execute(productId)).thenReturn(new Product(name, description, price));

        mockMvc
            .perform(get("/products/{id}", productId))
            .andExpect(status().isOk())
            .andExpect(content().contentTypeCompatibleWith(APPLICATION_JSON))
            .andExpect(jsonPath("$.name").value(name))
            .andExpect(jsonPath("$.description").value(description))
            .andExpect(jsonPath("$.price").value(price.doubleValue()))
            .andExpect(jsonPath("$.active").value(true));

        verify(getProductByIdUseCase).execute(productId);
    }

    @Test
    void shouldRejectUnauthenticatedProductCreation() throws Exception {

        String name = "Teclado AULA F75";
        String description = "Teclado mecanico";
        BigDecimal price = new BigDecimal("300.00");

        String requestBody =
            """
                {
                  "name": "%s",
                  "description": "%s",
                  "price": %s
                }
                """
                .formatted(name, description, price);

        mockMvc
            .perform(post("/products")
                .contentType(APPLICATION_JSON)
                .content(requestBody)
                .with(csrf()))
            .andExpect(status().isUnauthorized());

        verifyNoInteractions(createProductUseCase);

    }
}
