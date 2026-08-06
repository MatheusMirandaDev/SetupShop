package br.com.setupshop.product.application.usecase.update;

import br.com.setupshop.product.domain.exception.ProductNotFoundException;
import br.com.setupshop.product.domain.model.Product;
import br.com.setupshop.product.domain.repository.ProductRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;

@Service
public class UpdateProductUseCase {

    private final ProductRepository productRepository;

    public UpdateProductUseCase(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }



    @Transactional
    public Product execute(Long id, UpdateProductCommand command) {
        boolean noFieldsProvided =
                command == null ||
                (command.name() == null &&
                command.description() == null &&
                command.price() == null);

        if (noFieldsProvided) { throw new IllegalArgumentException("At least one field must be provided"); }

        Product product = productRepository.findById(id).orElseThrow(() -> new ProductNotFoundException(id));

        if (command.name() != null) {product.changeName(command.name());}
        if (command.description() != null) {product.changeDescription(command.description());}
        if (command.price() != null) {product.changePrice(command.price());}

        return productRepository.save(product);
    }
}
