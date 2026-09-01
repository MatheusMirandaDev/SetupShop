package br.com.setupshop.customer.domain.model;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.Locale;

@Getter
@Entity
@Table(name = "customers")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Customer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 200)
    private String name;

    @Column(nullable = false, length = 255, unique = true)
    private String email;

    @Column(nullable = false, length = 11)
    private String phone;

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
            throw new IllegalArgumentException("Customer name cannot be null or blank");
        }

        String normalizedName = name.strip();

        if (normalizedName.length() > 200) {
            throw new IllegalArgumentException("Customer name cannot exceed 200 characters");
        }
        return normalizedName;
    }

    private static String validateAndNormalizeEmail(String email) {
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("Customer email cannot be null or blank");
        }

        String normalizedEmail = email.strip().toLowerCase(Locale.ROOT);

        if (normalizedEmail.length() > 255) {
            throw new IllegalArgumentException("Customer email cannot exceed 255 characters");
        }

        boolean hasValidFormat =
            normalizedEmail.matches("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$");

        if (!hasValidFormat) {
            throw new IllegalArgumentException("Customer email has invalid format");
        }

        return normalizedEmail;
    }

    private static String validatePhone(String phone) {
        if (phone == null || phone.isBlank()) {
            throw new IllegalArgumentException("Customer phone cannot be null or blank");
        }

        boolean hasExactlyElevenDigits = phone.matches("[0-9]{11}");

        if (!hasExactlyElevenDigits) {
            throw new IllegalArgumentException("Customer phone must contain exactly 11 characters");
        }
        return phone;
    }

    public Customer(String name, String email, String phone) {
        this.name = validateAndNormalizeName(name);
        this.email = validateAndNormalizeEmail(email);
        this.phone = validatePhone(phone);
        this.active = true;

    }
}
