package br.com.setupshop.product.application.usecase.list;


import br.com.setupshop.product.domain.model.Product;
import br.com.setupshop.product.domain.repository.ProductRepository;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ListProductsUseCaseTest {

    @Test
    void shouldReturnAllProducts() {
        ProductRepository productRepository = mock(ProductRepository.class);
        ListProductsUseCase useCase = new ListProductsUseCase(productRepository);
        Product product1 = new Product(
                "Teclado AULA F75",
                "Teclado Mecanico para computador",
                new BigDecimal("300.00")
        );

        Product product2 = new Product(
                "Mouse Mchose A9",
                "Mouse para computador",
                new BigDecimal("180.00")
        );

        when(productRepository.findAll()).thenReturn(List.of(product1, product2));

        var result = useCase.execute();

        assertEquals(2, result.size());
        assertSame(product1, result.get(0));
        assertSame(product2, result.get(1));

        verify(productRepository).findAll();
    }

    @Test
    void shouldReturnEmptyListWhenNoProductsExist() {
        ProductRepository productRepository = mock(ProductRepository.class);
        ListProductsUseCase useCase = new ListProductsUseCase(productRepository);

        when(productRepository.findAll()).thenReturn(List.of());

        var result = useCase.execute();

        assertTrue(result.isEmpty());
        verify(productRepository).findAll();
    }
}