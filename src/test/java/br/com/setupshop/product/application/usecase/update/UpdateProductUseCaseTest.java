package br.com.setupshop.product.application.usecase.update;

import br.com.setupshop.product.domain.exception.ProductNotFoundException;
import br.com.setupshop.product.domain.model.Product;
import br.com.setupshop.product.domain.repository.ProductRepository;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.times;

class UpdateProductUseCaseTest {

    @Test
    void shouldUpdateAndSaveProduct() {
        ProductRepository productRepository = mock(ProductRepository.class);
        UpdateProductUseCase productUseCase = new UpdateProductUseCase(productRepository);
        Product product = new Product(
                "Exemplo",
                "Exemplo",
                new BigDecimal("0.00")
        );

        var productId =  1L;

        UpdateProductCommand updateProductCommand = new UpdateProductCommand(
                "Teclado AULA F75",
                "Teclado Mecanico para computador",
                new BigDecimal("300.00")
        );

        when(productRepository.findById(productId)).thenReturn(Optional.of(product));
        when(productRepository.save(any(Product.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Product productResult = productUseCase.execute(productId,  updateProductCommand);

        assertEquals("Teclado AULA F75", productResult.getName());
        assertEquals("Teclado Mecanico para computador", productResult.getDescription());
        assertEquals(new BigDecimal("300.00"), productResult.getPrice());
        assertTrue(productResult.isActive());
        verify(productRepository).findById(productId);
        verify(productRepository).save(product);

    }

    @Test
    void shouldUpdateOnlyProvidedFields() {
        ProductRepository productRepository = mock(ProductRepository.class);
        UpdateProductUseCase productUseCase = new UpdateProductUseCase(productRepository);
        Product product = new Product(
                "Produto original",
                "Descricao original",
                new BigDecimal("100.00")
        );

        var productId =  1L;

        UpdateProductCommand updateProductCommand = new UpdateProductCommand(
                null,
                null,
                new BigDecimal("300.00")
        );

        when(productRepository.findById(productId)).thenReturn(Optional.of(product));
        when(productRepository.save(any(Product.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Product productResult = productUseCase.execute(productId,  updateProductCommand);
        assertEquals("Produto original", productResult.getName());
        assertEquals("Descricao original", productResult.getDescription());
        assertEquals(new BigDecimal("300.00"), productResult.getPrice());
        assertTrue(productResult.isActive());
        verify(productRepository).findById(productId);
        verify(productRepository).save(product);
    }

    @Test
    void shouldRejectUpdateWhenNoFieldsProvided() {
        ProductRepository productRepository = mock(ProductRepository.class);
        UpdateProductUseCase productUseCase = new UpdateProductUseCase(productRepository);

        var productId =  1L;

        UpdateProductCommand updateProductCommand = new UpdateProductCommand(
                null,
                null,
                null
        );

        assertThrows(IllegalArgumentException.class, () -> productUseCase.execute(productId,  updateProductCommand));
        verifyNoInteractions(productRepository);
    }

    @Test
    void shouldThrowProductNotFoundExceptionWhenUpdatingNonexistentProduct() {
        ProductRepository productRepository = mock(ProductRepository.class);
        UpdateProductUseCase productUseCase = new UpdateProductUseCase(productRepository);

        var productId =  1L;

        UpdateProductCommand updateProductCommand = new UpdateProductCommand(
                null,
                null,
                new BigDecimal("300.00")
        );

        when(productRepository.findById(productId)).thenReturn(Optional.empty());
        assertThrows(ProductNotFoundException.class, () -> productUseCase.execute(productId,  updateProductCommand));

        verify(productRepository).findById(productId);
        verify(productRepository, never()).save(any(Product.class));
    }

    @Test
    void shouldNotSaveProductWhenUpdateDataIsInvalid() {
        ProductRepository productRepository = mock(ProductRepository.class);
        UpdateProductUseCase productUseCase = new UpdateProductUseCase(productRepository);
        Product product = new Product(
                "Produto original",
                "Descricao original",
                new BigDecimal("100.00")
        );

        var productId =  1L;

        UpdateProductCommand updateProductCommand = new UpdateProductCommand(
                "   ",
                null,
                null
        );

        when(productRepository.findById(productId)).thenReturn(Optional.of(product));
        assertThrows(IllegalArgumentException.class, () -> productUseCase.execute(productId,  updateProductCommand));
        assertEquals("Produto original", product.getName());
        verify(productRepository).findById(productId);
        verify(productRepository, never()).save(any(Product.class));
    }
}