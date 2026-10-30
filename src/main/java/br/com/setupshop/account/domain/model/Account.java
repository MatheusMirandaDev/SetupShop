package br.com.setupshop.account.domain.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.Locale;

@Getter
@Entity
@Table(
    name = "accounts",
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_accounts_issuer_subject", columnNames = {"issuer", "subject"})
    })

@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Account {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 255, unique = true)
    private String email;

    @Column(name = "customer_id", unique = true)
    private Long customerId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private AccountRole role;

    @Column(nullable = false, length = 255)
    private String issuer;

    @Column(nullable = false, length = 255)
    private String subject;

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

    private static String validateIssuer(String issuer) {
        if (issuer == null || issuer.isBlank()) {
            throw new IllegalArgumentException("Account issuer cannot be null or blank");
        }

        if (issuer.length() > 255) {
            throw new IllegalArgumentException("Account issuer cannot exceed 255 characters");
        }

        return issuer;
    }

    private static String validateSubject(String subject) {
        if (subject == null || subject.isBlank()) {
            throw new IllegalArgumentException("Account subject cannot be null or blank");
        }

        if (subject.length() > 255) {
            throw new IllegalArgumentException("Account subject cannot exceed 255 characters");
        }

        return subject;
    }

    public Account(String email, Long customerId, AccountRole role, String issuer, String subject) {
        this.email = validateAndNormalizeEmail(email);
        this.customerId = validateCustomerId(customerId, role);
        this.role = validateRole(role);
        this.issuer = validateIssuer(issuer);
        this.subject = validateSubject(subject);
    }
}
