package br.com.setupshop.product.infrastructure.persistence;

import br.com.setupshop.product.domain.model.Product;
import br.com.setupshop.shared.pagination.PageQuery;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class ProductRepositoryAdapterTest {

  @Test
  void shouldReturnPaginatedProducts() {
    JpaProductRepository jpaProductRepository = mock(JpaProductRepository.class);
    ProductRepositoryAdapter adapter = new ProductRepositoryAdapter(jpaProductRepository);

    String firstProductName = "Teclado AULA F75";
    String firstProductDescription = "Teclado mecanico";
    BigDecimal firstProductPrice = new BigDecimal("300.00");
    Product product1 = new Product(firstProductName, firstProductDescription, firstProductPrice);

    String secondProductName = "Mouse Mchose A9";
    String secondProductDescription = "Mouse para computador";
    BigDecimal secondProductPrice = new BigDecimal("180.00");
    Product product2 = new Product(secondProductName, secondProductDescription, secondProductPrice);

    PageQuery pageQuery = new PageQuery(1, 2);

    PageRequest pageRequest =
        PageRequest.of(pageQuery.page(), pageQuery.size(), Sort.by("id").ascending());

    PageImpl<Product> springPage = new PageImpl<>(List.of(product1, product2), pageRequest, 5L);

    when(jpaProductRepository.findAll(pageRequest)).thenReturn(springPage);

    var result = adapter.findAll(pageQuery);

    assertEquals(2, result.content().size());
    assertEquals(1, result.page());
    assertEquals(2, result.size());
    assertEquals(5L, result.totalElements());
    assertEquals(3, result.totalPages());

    assertSame(product1, result.content().get(0));
    assertSame(product2, result.content().get(1));

    verify(jpaProductRepository).findAll(pageRequest);
  }
}
