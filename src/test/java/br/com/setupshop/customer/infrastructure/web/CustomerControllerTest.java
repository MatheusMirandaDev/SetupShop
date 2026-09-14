package br.com.setupshop.customer.infrastructure.web;

import br.com.setupshop.customer.application.usecase.create.CreateCustomerCommand;
import br.com.setupshop.customer.application.usecase.create.CreateCustomerUseCase;
import br.com.setupshop.customer.application.usecase.get.GetCustomerByIdUseCase;
import br.com.setupshop.customer.domain.exception.CustomerNotFoundException;
import br.com.setupshop.customer.domain.exception.EmailAlreadyExistsException;
import br.com.setupshop.customer.domain.model.Customer;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CustomerController.class)
class CustomerControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CreateCustomerUseCase createCustomerUseCase;

    @MockitoBean
    private GetCustomerByIdUseCase getCustomerByIdUseCase;

    @Test
    void shouldCreateCustomerAndReturnCreated() throws Exception {
        String name = "Matheus Miranda";
        String email = "matheus.miranda@gmail.com";
        String phone = "61999999999";

        Customer createdCustomer = new Customer(name, email, phone);

        when(createCustomerUseCase.execute(any(CreateCustomerCommand.class))).thenReturn(createdCustomer);

        String requestBody =
            """
                {
                  "name": "%s",
                  "email": "%s",
                  "phone": "%s"
                }
                """
                .formatted(name, email, phone);

        mockMvc
            .perform(post("/customers").contentType(APPLICATION_JSON).content(requestBody))
            .andExpect(status().isCreated())
            .andExpect(content().contentTypeCompatibleWith(APPLICATION_JSON))
            .andExpect(jsonPath("$.name").value(name))
            .andExpect(jsonPath("$.email").value(email))
            .andExpect(jsonPath("$.phone").value(phone))
            .andExpect(jsonPath("$.active").value(true));

        ArgumentCaptor<CreateCustomerCommand> commandCaptor =
            ArgumentCaptor.forClass(CreateCustomerCommand.class);

        verify(createCustomerUseCase).execute(commandCaptor.capture());
        CreateCustomerCommand capturedCommand = commandCaptor.getValue();

        assertEquals(name, capturedCommand.name());
        assertEquals(email, capturedCommand.email());
        assertEquals(phone, capturedCommand.phone());
    }

    @Test
    void shouldReturnBadRequestWhenCustomerNameIsBlank() throws Exception {
        String requestBody =
            """
                {
                  "name": "",
                  "email": "matheus.miranda@gmail.com",
                  "phone": "61999999999"
                }
                """;

        mockMvc
            .perform(post("/customers").contentType(APPLICATION_JSON).content(requestBody))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.timestamp").exists())
            .andExpect(jsonPath("$.status").value(400))
            .andExpect(jsonPath("$.error").value("Bad Request"))
            .andExpect(jsonPath("$.message").value("Validation failed"))
            .andExpect(jsonPath("$.path").value("/customers"))
            .andExpect(jsonPath("$.fieldErrors.name").value("must not be blank"));

        verifyNoInteractions(createCustomerUseCase);
    }

    @Test
    void shouldReturnConflictWhenCustomerEmailAlreadyExists() throws Exception {
        String requestBody =
            """
                {
                  "name": "Matheus Miranda",
                  "email": "matheus.miranda@gmail.com",
                  "phone": "61999999999"
                }
                """;

        when(createCustomerUseCase.execute(any(CreateCustomerCommand.class)))
            .thenThrow(new EmailAlreadyExistsException("matheus.miranda@gmail.com"));

        mockMvc
            .perform(post("/customers").contentType(APPLICATION_JSON).content(requestBody))
            .andExpect(status().isConflict())
            .andExpect(jsonPath("$.timestamp").exists())
            .andExpect(jsonPath("$.status").value(409))
            .andExpect(jsonPath("$.error").value("Conflict"))
            .andExpect(jsonPath("$.message")
                .value("Customer email already exists: matheus.miranda@gmail.com"))
            .andExpect(jsonPath("$.path").value("/customers"));

        verify(createCustomerUseCase).execute(any(CreateCustomerCommand.class));
    }

    @Test
    void shouldReturnBadRequestWhenCustomerEmailIsInvalid() throws Exception {
        String requestBody =
            """
                {
                  "name": "Matheus Miranda",
                  "email": "matheus.miranda @com",
                  "phone": "61999999999"
                }
                """;

        mockMvc
            .perform(post("/customers").contentType(APPLICATION_JSON).content(requestBody))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.timestamp").exists())
            .andExpect(jsonPath("$.status").value(400))
            .andExpect(jsonPath("$.error").value("Bad Request"))
            .andExpect(jsonPath("$.message").value("Validation failed"))
            .andExpect(jsonPath("$.path").value("/customers"))
            .andExpect(jsonPath("$.fieldErrors.email").value("must be a well-formed email address"));

        verifyNoInteractions(createCustomerUseCase);
    }

    @Test
    void shouldReturnBadRequestWhenCustomerPhoneIsInvalid() throws Exception {
        String requestBody =
            """
                {
                  "name": "Matheus Miranda",
                  "email": "matheus.miranda@gmail.com",
                  "phone": "9999"
                }
                """;

        mockMvc
            .perform(post("/customers").contentType(APPLICATION_JSON).content(requestBody))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.timestamp").exists())
            .andExpect(jsonPath("$.status").value(400))
            .andExpect(jsonPath("$.error").value("Bad Request"))
            .andExpect(jsonPath("$.message").value("Validation failed"))
            .andExpect(jsonPath("$.path").value("/customers"))
            .andExpect(jsonPath("$.fieldErrors.phone").value("must match \"[0-9]{11}\""));

        verifyNoInteractions(createCustomerUseCase);
    }

    @Test
    void shouldReturnCustomerWhenFound() throws Exception {
        Long customerId = 1L;

        String name = "Matheus Miranda";
        String email = "matheus.miranda@gmail.com";
        String phone = "61999999999";

        Customer customer = mock(Customer.class);

        when(getCustomerByIdUseCase.execute(customerId)).thenReturn(customer);
        when(customer.getId()).thenReturn(customerId);
        when(customer.getName()).thenReturn(name);
        when(customer.getEmail()).thenReturn(email);
        when(customer.getPhone()).thenReturn(phone);
        when(customer.isActive()).thenReturn(true);

        mockMvc
            .perform(get("/customers/{id}", customerId))
            .andExpect(status().isOk())
            .andExpect(content().contentTypeCompatibleWith(APPLICATION_JSON))
            .andExpect(jsonPath("$.id").value(customerId))
            .andExpect(jsonPath("$.name").value(name))
            .andExpect(jsonPath("$.email").value(email))
            .andExpect(jsonPath("$.phone").value(phone))
            .andExpect(jsonPath("$.active").value(true));

        verify(getCustomerByIdUseCase).execute(customerId);
    }

    @Test
    void shouldReturnNotFoundWhenCustomerDoesNotExist() throws Exception {
        Long customerId = 1000L;

        when(getCustomerByIdUseCase.execute(customerId))
            .thenThrow(new CustomerNotFoundException(customerId));

        mockMvc
            .perform(get("/customers/{id}", customerId))
            .andExpect(status().isNotFound())
            .andExpect(content().contentTypeCompatibleWith(APPLICATION_JSON))
            .andExpect(jsonPath("$.timestamp").exists())
            .andExpect(jsonPath("$.status").value(404))
            .andExpect(jsonPath("$.error").value("Not Found"))
            .andExpect(jsonPath("$.message").value("Customer not found with id: " + customerId))
            .andExpect(jsonPath("$.path").value("/customers/" + customerId));

        verify(getCustomerByIdUseCase).execute(customerId);
    }
}
