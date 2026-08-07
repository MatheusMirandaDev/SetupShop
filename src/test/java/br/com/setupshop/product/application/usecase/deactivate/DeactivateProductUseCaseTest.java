package br.com.setupshop.product.application.usecase.deactivate;

import br.com.setupshop.product.domain.exception.ProductNotFoundException;
import br.com.setupshop.product.domain.model.Product;
import br.com.setupshop.product.domain.repository.ProductRepository;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

class DeactivateProductUseCaseTest {

    @Test
    void shouldDeactivateAndSaveProduct() {
        ProductRepository productRepository = mock(ProductRepository.class);
        DeactivateProductUseCase deactivateProductUseCase = new DeactivateProductUseCase(productRepository);
        var productId = 1L;

        Product product = new Product(
                "Exemplo",
                "Exemplo",
                new BigDecimal("0.00")
        );

        when(productRepository.findById(productId)).thenReturn(Optional.of(product));
        deactivateProductUseCase.execute(productId);

        assertFalse(product.isActive());
        verify(productRepository).findById(productId);
        verify(productRepository).save(product);
    }

    @Test
    void shouldThrowProductNotFoundExceptionWhenDeactivatingNonexistentProduct(){
        ProductRepository productRepository = mock(ProductRepository.class);
        DeactivateProductUseCase deactivateProductUseCase = new DeactivateProductUseCase(productRepository);
        var productId = 1L;

        when(productRepository.findById(productId)).thenReturn(Optional.empty());
        assertThrows(ProductNotFoundException.class, () -> deactivateProductUseCase.execute(productId));
        verify(productRepository).findById(productId);
        verify(productRepository, never()).save(any(Product.class));
    }

}