package br.com.setupshop.product.domain.model;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;

@Getter
@Entity
@Table(name = "products")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 200)
    private String name;

    @Column(length = 500)
    private String description;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal price;

    @Column(nullable = false)
    private boolean active;

    @Column(
            name = "created_at",
            nullable = false,
            insertable = false,
            updatable = false
    )
    private Instant createdAt;

    @Column(
            name = "updated_at",
            nullable = false,
            insertable = false,
            updatable = false
    )
    private Instant updatedAt;

    private static String validateAndNormalizeName(String name) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Product name cannot be null or blank");
        }
        String normalizedName = name.strip();

        if (normalizedName.length() > 200) {
            throw new IllegalArgumentException("Product name cannot exceed 200 characters");
        }
        return normalizedName;
    }

    private static String validateAndNormalizeDescription(String description) {
        if (description == null || description.isBlank()) {
            return null;
        }

        String normalizedDescription = description.strip();

        if (normalizedDescription.length() > 500) {
            throw new IllegalArgumentException("Product description cannot exceed 500 characters");
        }
        return normalizedDescription;
    }

    private static BigDecimal validateAndNormalizePrice(BigDecimal price) {
        if (price == null ||  price.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Product price cannot be null or negative");
        }

        try {
            return price.setScale(2, RoundingMode.UNNECESSARY);
        } catch (ArithmeticException exception) {
            throw new IllegalArgumentException("Product price must have at most 2 decimal places");
        }
    }

    public void changeName(String newName) {
        this.name = validateAndNormalizeName(newName);
    }

    public void changeDescription(String newDescription) {
        this.description = validateAndNormalizeDescription(newDescription);
    }

    public void changePrice(BigDecimal newPrice) {
        this.price = validateAndNormalizePrice(newPrice);
    }

    public void deactivate() {
        this.active = false;
    }

    public Product(String name, String description, BigDecimal price) {
        this.name = validateAndNormalizeName(name);
        this.description = validateAndNormalizeDescription(description);
        this.price = validateAndNormalizePrice(price);
        this.active = true;
    }
}