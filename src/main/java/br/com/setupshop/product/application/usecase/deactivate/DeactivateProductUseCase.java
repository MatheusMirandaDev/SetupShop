package br.com.setupshop.product.application.usecase.deactivate;

import br.com.setupshop.product.domain.exception.ProductNotFoundException;
import br.com.setupshop.product.domain.model.Product;
import br.com.setupshop.product.domain.repository.ProductRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DeactivateProductUseCase {

    private final ProductRepository productRepository;

    public DeactivateProductUseCase(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    @Transactional
    public void execute(Long id) {

        Product product = productRepository.findById(id).orElseThrow(() -> new ProductNotFoundException(id));
        product.deactivate();
        productRepository.save(product);
    }
}
