package br.com.setupshop.product.application.usecase.get;


import br.com.setupshop.product.domain.exception.ProductNotFoundException;
import br.com.setupshop.product.domain.model.Product;
import br.com.setupshop.product.domain.repository.ProductRepository;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

class GetProductByIdUseCaseTest {

    @Test
    void shouldReturnProductWhenFound() {
        ProductRepository productRepository = mock(ProductRepository.class);
        GetProductByIdUseCase productUseCase = new GetProductByIdUseCase(productRepository);
        Product product = new Product(
                "Teclado AULA F75",
                "Teclado Mecanico para computador",
                new BigDecimal("300.00")
        );
        Long productId = 1L;

        when(productRepository.findById(productId)).thenReturn(Optional.of(product));

        var result = productUseCase.execute(productId);

        assertSame(product, result);
        verify(productRepository).findById(productId);
    }

    @Test
    void shouldThrowProductNotFoundExceptionWhenProductDoesNotExist() {
        ProductRepository productRepository = mock(ProductRepository.class);
        GetProductByIdUseCase productUseCase = new GetProductByIdUseCase(productRepository);

        Long productId = 1L;

        when(productRepository.findById(productId)).thenReturn(Optional.empty());

        assertThrows(ProductNotFoundException.class, () -> productUseCase.execute(productId));
        verify(productRepository).findById(productId);
    }

}