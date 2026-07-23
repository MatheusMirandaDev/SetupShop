package br.com.setupshop.product.application.usecase.create;

import br.com.setupshop.product.domain.model.Product;
import br.com.setupshop.product.domain.repository.ProductRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CreateProductUseCase {

    private final ProductRepository productRepository;

    public CreateProductUseCase(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    @Transactional
    public Product execute(CreateProductCommand command) {
        Product product = new Product(
                command.name(),
                command.description(),
                command.price()
        );

        return productRepository.save(product);
    }

}