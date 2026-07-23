package br.com.setupshop.product.domain.model;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class ProductTest {

    @Test
    void shouldCreateProductWithValidData() {
        // Given
        String name = "Teclado AULA F75";
        String description = "Teclado Mecanico para computador";
        BigDecimal price = new BigDecimal("300.00");

        // When
        Product product = new Product(name, description, price);

        // Then
        assertEquals("Teclado AULA F75", product.getName());
        assertEquals("Teclado Mecanico para computador", product.getDescription());
        assertEquals(new BigDecimal("300.00"), product.getPrice());
        assertTrue(product.isActive());
    }

    @Test
    void shouldNormalizeProductData() {
        // Given
        String name = "  Teclado AULA F75  ";
        String description = "  Teclado Mecanico para computador  ";
        BigDecimal price = new BigDecimal("300");

        // When
        Product product = new Product(name, description, price);

        // Then
        assertEquals("Teclado AULA F75", product.getName());
        assertEquals("Teclado Mecanico para computador", product.getDescription());
        assertEquals(new BigDecimal("300.00"), product.getPrice());
    }

    @Test
    void shouldRejectNullName() {
        // Given
        String name = null;
        String description = "Exemplo";
        BigDecimal price = new BigDecimal("300.00");

        // When - Then
        assertThrows(IllegalArgumentException.class, () -> new Product(name, description, price));
    }

    @Test
    void shouldRejectBlankName() {
        // Given
        String name = "  ";
        String description = "Exemplo";
        BigDecimal price = new BigDecimal("300.00");

        // When - Then
        assertThrows(IllegalArgumentException.class, () -> new Product(name, description, price));
    }

    @Test
    void shouldRejectNameExceedingMaxLength() {
        // Given
        String name = "A".repeat(201);
        String description = "Exemplo";
        BigDecimal price = new BigDecimal("300.00");

        // When - Then
        assertThrows(IllegalArgumentException.class, () -> new Product(name, description, price));
    }

    @Test
    void shouldAcceptNameAtMaximumLength() {
        // Given
        String name = "A".repeat(200);
        String description = "Exemplo";
        BigDecimal price = new BigDecimal("300.00");

        // When
        Product product = new Product(name, description, price);

        // Then
        assertEquals(name, product.getName());
        assertEquals(200, product.getName().length());
    }

    @Test
    void shouldAllowNullDescription() {
        // Given
        String name = "Exemplo";
        String description = null;
        BigDecimal price = new BigDecimal("300.00");

        // When
        Product product = new Product(name, description, price);

        // Then
        assertNull(product.getDescription());
    }

    @Test
    void shouldNormalizeBlankDescriptionToNull() {
        // Given
        String name = "Exemplo";
        String description = "   ";
        BigDecimal price = new BigDecimal("300.00");

        // When
        Product product = new Product(name, description, price);

        // Then
        assertNull(product.getDescription());
    }

    @Test
    void shouldRejectDescriptionExceedingMaxLength() {
        // Given
        String name = "Exemplo";
        String description = "A".repeat(501);
        BigDecimal price = new BigDecimal("300.00");

        // When - Then
        assertThrows(IllegalArgumentException.class, () -> new Product(name, description, price));
    }

    @Test
    void shouldAcceptDescriptionAtMaximumLength() {
        // Given
        String name = "Exemplo";
        String description = "A".repeat(500);
        BigDecimal price = new BigDecimal("300.00");

        // When
        Product product = new Product(name, description, price);

        // Then
        assertEquals(description, product.getDescription());
        assertEquals(500, product.getDescription().length());
    }

    @Test
    void shouldRejectNullPrice() {
        // Given
        String name = "Exemplo";
        String description = "Exemplo";
        BigDecimal price = null;

        // When - Then
        assertThrows(IllegalArgumentException.class, () -> new Product(name, description, price));
    }

    @Test
    void shouldNormalizePriceWithTrailingZeros() {
        // Given
        String name = "Exemplo";
        String description = "Exemplo";
        BigDecimal price = new BigDecimal("300.0000");

        // When
        Product product = new Product(name, description, price);

        // Then
        assertEquals(new BigDecimal("300.00"), product.getPrice());
    }

    @Test
    void shouldRejectPriceNegative() {
        // Given
        String name = "Exemplo";
        String description = "Exemplo";
        BigDecimal price = new BigDecimal("-1.00");

        // When - Then
        assertThrows(IllegalArgumentException.class, () -> new Product(name, description, price));
    }

    @Test
    void shouldRejectPriceThatRequiresRounding() {
        // Given
        String name = "Exemplo";
        String description = "Exemplo";
        BigDecimal price = new BigDecimal("300.9999");

        // When - Then
        assertThrows(IllegalArgumentException.class, () -> new Product(name, description, price));
    }

    @Test
    void shouldAcceptPriceZero() {
        // Given
        String name = "Exemplo";
        String description = "Exemplo";
        BigDecimal price = new BigDecimal("0.00");

        // When
        Product product = new Product(name, description, price);

        // Then
        assertEquals(price, product.getPrice());
    }
}