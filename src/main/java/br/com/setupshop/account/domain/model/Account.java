package br.com.setupshop.account.domain.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.Locale;

@Getter
@Entity
@Table(name = "accounts")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Account {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 255, unique = true)
    private String email;

    @Column(nullable = false, length = 255)
    private String passwordHash;

    @Column(name = "customer_id", unique = true)
    private Long customerId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private AccountRole role;

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

    private static String validateAndNormalizeEmail(String email) {
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("Account email cannot be null or blank");
        }

        String normalizedEmail = email.strip().toLowerCase(Locale.ROOT);

        if (normalizedEmail.length() > 255) {
            throw new IllegalArgumentException("Account email cannot exceed 255 characters");
        }

        boolean hasValidFormat =
            normalizedEmail.matches("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$");

        if (!hasValidFormat) {
            throw new IllegalArgumentException("Account email has invalid format");
        }

        return normalizedEmail;
    }

    private static String validatePasswordHash(String passwordHash) {
        if (passwordHash == null || passwordHash.isBlank()) {
            throw new IllegalArgumentException("Account password hash cannot be null or blank");
        }

        return passwordHash;
    }

    private static Long validateCustomerId(Long customerId, AccountRole role) {
        if (customerId == null && role == AccountRole.CUSTOMER) {
            throw new IllegalArgumentException("Account customer Id cannot be null");
        }

        if (customerId != null && customerId <= 0L) {
            throw new IllegalArgumentException("Account customer Id must be positive");
        }

        return customerId;
    }

    private static AccountRole validateRole(AccountRole role) {
        if (role == null) {
            throw new IllegalArgumentException("Account role cannot be null");
        }

        return role;
    }

    public Account(String email, String passwordHash, Long customerId, AccountRole role) {
        this.email = validateAndNormalizeEmail(email);
        this.passwordHash = validatePasswordHash(passwordHash);
        this.customerId = validateCustomerId(customerId, role);
        this.role = validateRole(role);
    }
}
