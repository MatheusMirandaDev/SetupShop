package br.com.setupshop.product.application.usecase.list;

import br.com.setupshop.product.domain.model.Product;
import br.com.setupshop.product.domain.repository.ProductRepository;
import br.com.setupshop.shared.pagination.PageQuery;
import br.com.setupshop.shared.pagination.PageResult;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ListProductsUseCaseTest {

  @Test
  void shouldReturnPaginatedProducts() {
    ProductRepository productRepository = mock(ProductRepository.class);
    ListProductsUseCase useCase = new ListProductsUseCase(productRepository);
    Product product1 =
        new Product(
            "Teclado AULA F75", "Teclado Mecanico para computador", new BigDecimal("300.00"));

    Product product2 =
        new Product("Mouse Mchose A9", "Mouse para computador", new BigDecimal("180.00"));

    PageQuery pageQuery = new PageQuery(0, 20);
    PageResult<Product> pageResult = new PageResult<>(List.of(product1, product2), 0, 20, 2L, 1);

    when(productRepository.findAll(pageQuery)).thenReturn(pageResult);

    var result = useCase.execute(pageQuery);

    assertEquals(2, result.content().size());
    assertEquals(0, result.page());
    assertEquals(20, result.size());
    assertEquals(2L, result.totalElements());
    assertEquals(1, result.totalPages());
    assertSame(product1, result.content().get(0));
    assertSame(product2, result.content().get(1));

    verify(productRepository).findAll(pageQuery);
  }

  @Test
  void shouldReturnEmptyListWhenNoProductsExist() {
    ProductRepository productRepository = mock(ProductRepository.class);
    ListProductsUseCase useCase = new ListProductsUseCase(productRepository);

    PageQuery pageQuery = new PageQuery(0, 20);
    PageResult<Product> pageResult = new PageResult<>(List.of(), 0, 20, 0L, 0);

    when(productRepository.findAll(pageQuery)).thenReturn(pageResult);

    var result = useCase.execute(pageQuery);

    assertTrue(result.content().isEmpty());
    verify(productRepository).findAll(pageQuery);
  }
}
