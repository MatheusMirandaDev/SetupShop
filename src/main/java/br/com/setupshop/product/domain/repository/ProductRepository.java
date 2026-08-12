package br.com.setupshop.product.domain.repository;

import br.com.setupshop.product.domain.model.Product;
import br.com.setupshop.shared.pagination.PageQuery;
import br.com.setupshop.shared.pagination.PageResult;

import java.util.Optional;

public interface ProductRepository {
  Optional<Product> findById(Long id);

  Product save(Product product);

  PageResult<Product> findAll(PageQuery pageQuery);
}
