package br.com.setupshop.product.application.usecase.create;

import br.com.setupshop.product.domain.model.Product;
import br.com.setupshop.product.domain.repository.ProductRepository;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class CreateProductUseCaseTest {

    @Test
    void shouldCreateAndSaveProduct() {
        // Given
        ProductRepository productRepository = mock(ProductRepository.class);
        CreateProductUseCase productUseCase = new CreateProductUseCase(productRepository);
        CreateProductCommand productCommand = new CreateProductCommand(
                "Teclado AULA F75",
                "Teclado Mecanico para computador",
                new BigDecimal("300.00")
        );
        when(productRepository.save(any(Product.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        Product productResult = productUseCase.execute(productCommand);

        // Then
        assertEquals("Teclado AULA F75", productResult.getName());
        assertEquals("Teclado Mecanico para computador", productResult.getDescription());
        assertEquals(new BigDecimal("300.00"), productResult.getPrice());
        assertTrue(productResult.isActive());
        verify(productRepository, times(1)).save(any(Product.class));
    }

    @Test
    void shouldNotSaveProductWhenDataIsInvalid() {
        // Given
        ProductRepository productRepository = mock(ProductRepository.class);
        CreateProductUseCase productUseCase = new CreateProductUseCase(productRepository);
        CreateProductCommand productCommand = new CreateProductCommand(
                null,
                "Teclado Mecanico para computador",
                new BigDecimal("300.00")
        );

        // When - Then
        assertThrows(IllegalArgumentException.class, () -> productUseCase.execute(productCommand));
        verify(productRepository, never()).save(any(Product.class));
    }
}