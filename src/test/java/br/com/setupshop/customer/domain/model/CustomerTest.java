package br.com.setupshop.customer.domain.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CustomerTest {

    @Test
    void shouldCreateCustomerWithValidData() {
        String name = "Matheus Miranda";
        String email = "matheus.miranda@gmail.com";
        String phone = "61999999999";

        Customer customer = new Customer(name, email, phone);

        assertEquals(name, customer.getName());
        assertEquals(email, customer.getEmail());
        assertEquals(phone, customer.getPhone());
        assertNull(customer.getId());
        assertTrue(customer.isActive());
    }

    @Test
    void shouldNormalizeCustomerData() {
        String name = "   Matheus Miranda   ";
        String email = "   MATHEUS.MIRANDA@GMAIL.COM   ";
        String phone = "61999999999";

        Customer customer = new Customer(name, email, phone);

        assertEquals("Matheus Miranda", customer.getName());
        assertEquals("matheus.miranda@gmail.com", customer.getEmail());
    }

    @Test
    void shouldRejectNullCustomerName() {
        String name = null;
        String email = "matheus.miranda@gmail.com";
        String phone = "61999999999";

        assertThrows(IllegalArgumentException.class, () -> new Customer(name, email, phone));
    }

    @Test
    void shouldRejectNullCustomerEmail() {
        String name = "Matheus Miranda";
        String email = null;
        String phone = "61999999999";

        assertThrows(IllegalArgumentException.class, () -> new Customer(name, email, phone));
    }

    @Test
    void shouldRejectNullCustomerPhone() {
        String name = "Matheus Miranda";
        String email = "matheus.miranda@gmail.com";
        String phone = null;

        assertThrows(IllegalArgumentException.class, () -> new Customer(name, email, phone));
    }

    @Test
    void shouldRejectBlankCustomerName() {
        String name = "   ";
        String email = "matheus.miranda@gmail.com";
        String phone = "61999999999";

        assertThrows(IllegalArgumentException.class, () -> new Customer(name, email, phone));
    }

    @Test
    void shouldRejectBlankCustomerEmail() {
        String name = "Matheus Miranda";
        String email = "   ";
        String phone = "61999999999";

        assertThrows(IllegalArgumentException.class, () -> new Customer(name, email, phone));
    }

    @Test
    void shouldRejectBlankCustomerPhone() {
        String name = "Matheus Miranda";
        String email = "matheus.miranda@gmail.com";
        String phone = "   ";

        assertThrows(IllegalArgumentException.class, () -> new Customer(name, email, phone));
    }

    @Test
    void shouldRejectCustomerNameExceedingMaximumLength() {
        String name = "a".repeat(201);
        String email = "matheus.miranda@gmail.com";
        String phone = "61999999999";

        assertThrows(IllegalArgumentException.class, () -> new Customer(name, email, phone));
    }

    @Test
    void shouldRejectCustomerEmailExceedingMaximumLength() {
        String name = "Matheus Miranda";
        String email = "a".repeat(246) + "@email.com";
        String phone = "61999999999";

        assertThrows(IllegalArgumentException.class, () -> new Customer(name, email, phone));
        assertEquals(256, email.length());

    }

    @Test
    void shouldRejectCustomerPhoneWithMoreThanElevenDigits() {
        String name = "Matheus Miranda";
        String email = "matheus.miranda@gmail.com";
        String phone = "9".repeat(12);

        assertThrows(IllegalArgumentException.class, () -> new Customer(name, email, phone));
    }

    @Test
    void shouldAcceptCustomerNameAtMaximumLength() {
        String name = "a".repeat(200);
        String email = "matheus.miranda@gmail.com";
        String phone = "61999999999";

        Customer customer = new Customer(name, email, phone);
        assertEquals(name, customer.getName());
    }

    @Test
    void shouldAcceptCustomerEmailAtMaximumLength() {
        String name = "Matheus Miranda";
        String email = "a".repeat(245) + "@email.com";
        String phone = "61999999999";

        Customer customer = new Customer(name, email, phone);
        assertEquals(email, customer.getEmail());
        assertEquals(255, email.length());
    }

    @Test
    void shouldAcceptCustomerPhoneWithExactlyElevenDigits() {
        String name = "Matheus Miranda";
        String email = "matheus.miranda@gmail.com";
        String phone = "9".repeat(11);

        Customer customer = new Customer(name, email, phone);
        assertEquals(phone, customer.getPhone());
    }

    @Test
    void shouldRejectCustomerPhoneWithTenDigits() {
        String name = "Matheus Miranda";
        String email = "matheus.miranda@gmail.com";
        String phone = "9".repeat(10);

        assertThrows(IllegalArgumentException.class, () -> new Customer(name, email, phone));
    }

    @Test
    void shouldRejectCustomerPhoneWithLetter() {
        String name = "Matheus Miranda";
        String email = "matheus.miranda@gmail.com";
        String phone = "9".repeat(10) + "A";

        assertThrows(IllegalArgumentException.class, () -> new Customer(name, email, phone));
    }

    @Test
    void shouldRejectCustomerEmailWithoutAtSign() {
        String name = "Matheus Miranda";
        String email = "matheus.mirandagmail.com";
        String phone = "61999999999";

        assertThrows(
            IllegalArgumentException.class,
            () -> new Customer(name, email, phone)
        );
    }

    @Test
    void shouldRejectCustomerEmailWithoutDomainExtension() {
        String name = "Matheus Miranda";
        String email = "matheus@gmail";
        String phone = "61999999999";

        assertThrows(
            IllegalArgumentException.class,
            () -> new Customer(name, email, phone)
        );
    }

}