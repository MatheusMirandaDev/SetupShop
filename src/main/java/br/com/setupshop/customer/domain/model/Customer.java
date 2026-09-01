package br.com.setupshop.customer.domain.model;

import lombok.Getter;

import java.time.Instant;
import java.util.Locale;

@Getter
public class Customer {

    private Long id;

    private String name;

    private String email;

    private String phone;

    private boolean active;

    private Instant createdAt;

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
