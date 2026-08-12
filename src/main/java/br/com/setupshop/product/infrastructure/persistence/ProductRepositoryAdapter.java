package br.com.setupshop.product.infrastructure.persistence;

import br.com.setupshop.product.domain.model.Product;
import br.com.setupshop.product.domain.repository.ProductRepository;
import br.com.setupshop.shared.pagination.PageQuery;
import br.com.setupshop.shared.pagination.PageResult;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public class ProductRepositoryAdapter implements ProductRepository {

  private final JpaProductRepository productRepository;

  public ProductRepositoryAdapter(JpaProductRepository productRepository) {
    this.productRepository = productRepository;
  }

  @Override
  public Optional<Product> findById(Long id) {
    return productRepository.findById(id);
  }

  @Override
  public Product save(Product product) {
    return productRepository.save(product);
  }

  @Override
  public PageResult<Product> findAll(PageQuery pageQuery) {

    PageRequest pageRequest =
        PageRequest.of(pageQuery.page(), pageQuery.size(), Sort.by("id").ascending());

    var resultPage = productRepository.findAll(pageRequest);

    return new PageResult<>(
        resultPage.getContent(),
        resultPage.getNumber(),
        resultPage.getSize(),
        resultPage.getTotalElements(),
        resultPage.getTotalPages());
  }
}
