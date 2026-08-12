package br.com.setupshop.product.application.usecase.list;

import br.com.setupshop.product.domain.model.Product;
import br.com.setupshop.product.domain.repository.ProductRepository;
import br.com.setupshop.shared.pagination.PageQuery;
import br.com.setupshop.shared.pagination.PageResult;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ListProductsUseCase {

  private final ProductRepository productRepository;

  public ListProductsUseCase(ProductRepository productRepository) {
    this.productRepository = productRepository;
  }

  @Transactional(readOnly = true)
  public PageResult<Product> execute(PageQuery pageQuery) {
    return productRepository.findAll(pageQuery);
  }
}
