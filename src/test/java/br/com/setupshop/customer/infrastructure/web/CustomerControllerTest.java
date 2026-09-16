package br.com.setupshop.customer.infrastructure.web;

import br.com.setupshop.customer.application.usecase.create.CreateCustomerCommand;
import br.com.setupshop.customer.application.usecase.create.CreateCustomerUseCase;
import br.com.setupshop.customer.application.usecase.get.GetCustomerByIdUseCase;
import br.com.setupshop.customer.application.usecase.list.ListCustomersUseCase;
import br.com.setupshop.customer.domain.exception.CustomerNotFoundException;
import br.com.setupshop.customer.domain.exception.EmailAlreadyExistsException;
import br.com.setupshop.customer.domain.model.Customer;
import br.com.setupshop.shared.pagination.PageQuery;
import br.com.setupshop.shared.pagination.PageResult;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

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

    @MockitoBean
    private ListCustomersUseCase listCustomersUseCase;

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

    @Test
    void shouldReturnPaginatedCustomers() throws Exception {
        String name1 = "Matheus";
        String email1 = "matheus.miranda@gmail.com";
        String phone1 = "61888888888";

        Customer customer1 = mock(Customer.class);
        when(customer1.getId()).thenReturn(1L);
        when(customer1.getName()).thenReturn(name1);
        when(customer1.getEmail()).thenReturn(email1);
        when(customer1.getPhone()).thenReturn(phone1);
        when(customer1.isActive()).thenReturn(true);

        String name2 = "Miranda";
        String email2 = "miranda.batista@gmail.com";
        String phone2 = "61999999999";

        Customer customer2 = mock(Customer.class);
        when(customer2.getId()).thenReturn(2L);
        when(customer2.getName()).thenReturn(name2);
        when(customer2.getEmail()).thenReturn(email2);
        when(customer2.getPhone()).thenReturn(phone2);
        when(customer2.isActive()).thenReturn(true);

        PageQuery pageQuery = new PageQuery(0, 20);
        PageResult<Customer> pageResult = new PageResult<>(
            List.of(customer1, customer2), 0, 20, 2, 1);

        when(listCustomersUseCase.execute(pageQuery)).thenReturn(pageResult);

        mockMvc
            .perform(get("/customers")
                .param("page", "0")
                .param("size", "20"))
            .andExpect(status().isOk())
            .andExpect(content().contentTypeCompatibleWith(APPLICATION_JSON))
            .andExpect(jsonPath("$.content.length()").value(2))
            .andExpect(jsonPath("$.content[0].id").value(1L))
            .andExpect(jsonPath("$.content[0].name").value(name1))
            .andExpect(jsonPath("$.content[0].email").value(email1))
            .andExpect(jsonPath("$.content[0].phone").value(phone1))
            .andExpect(jsonPath("$.content[0].active").value(true))
            .andExpect(jsonPath("$.content[1].id").value(2L))
            .andExpect(jsonPath("$.content[1].name").value(name2))
            .andExpect(jsonPath("$.content[1].email").value(email2))
            .andExpect(jsonPath("$.content[1].phone").value(phone2))
            .andExpect(jsonPath("$.content[1].active").value(true))
            .andExpect(jsonPath("$.page").value(0))
            .andExpect(jsonPath("$.size").value(20))
            .andExpect(jsonPath("$.totalElements").value(2))
            .andExpect(jsonPath("$.totalPages").value(1));

        verify(listCustomersUseCase).execute(pageQuery);
    }

    @Test
    void shouldUseDefaultPaginationWhenParametersAreNotProvided() throws Exception {
        String name1 = "Matheus";
        String email1 = "matheus.miranda@gmail.com";
        String phone1 = "61888888888";

        Customer customer1 = mock(Customer.class);
        when(customer1.getId()).thenReturn(1L);
        when(customer1.getName()).thenReturn(name1);
        when(customer1.getEmail()).thenReturn(email1);
        when(customer1.getPhone()).thenReturn(phone1);
        when(customer1.isActive()).thenReturn(true);

        String name2 = "Miranda";
        String email2 = "miranda.batista@gmail.com";
        String phone2 = "61999999999";

        Customer customer2 = mock(Customer.class);
        when(customer2.getId()).thenReturn(2L);
        when(customer2.getName()).thenReturn(name2);
        when(customer2.getEmail()).thenReturn(email2);
        when(customer2.getPhone()).thenReturn(phone2);
        when(customer2.isActive()).thenReturn(true);

        PageQuery pageQuery = new PageQuery(0, 20);
        PageResult<Customer> pageResult = new PageResult<>(
            List.of(customer1, customer2), 0, 20, 2, 1);

        when(listCustomersUseCase.execute(pageQuery)).thenReturn(pageResult);

        mockMvc
            .perform(get("/customers"))
            .andExpect(status().isOk())
            .andExpect(content().contentTypeCompatibleWith(APPLICATION_JSON))
            .andExpect(jsonPath("$.content.length()").value(2))
            .andExpect(jsonPath("$.content[0].id").value(1L))
            .andExpect(jsonPath("$.content[0].name").value(name1))
            .andExpect(jsonPath("$.content[0].email").value(email1))
            .andExpect(jsonPath("$.content[0].phone").value(phone1))
            .andExpect(jsonPath("$.content[0].active").value(true))
            .andExpect(jsonPath("$.content[1].id").value(2L))
            .andExpect(jsonPath("$.content[1].name").value(name2))
            .andExpect(jsonPath("$.content[1].email").value(email2))
            .andExpect(jsonPath("$.content[1].phone").value(phone2))
            .andExpect(jsonPath("$.content[1].active").value(true))
            .andExpect(jsonPath("$.page").value(0))
            .andExpect(jsonPath("$.size").value(20))
            .andExpect(jsonPath("$.totalElements").value(2))
            .andExpect(jsonPath("$.totalPages").value(1));

        verify(listCustomersUseCase).execute(pageQuery);
    }

    @Test
    void shouldReturnBadRequestWhenPageIsNegative() throws Exception {
        mockMvc
            .perform(get("/customers")
                .param("page", "-1")
                .param("size", "20"))
            .andExpect(status().isBadRequest())
            .andExpect(content().contentTypeCompatibleWith(APPLICATION_JSON))
            .andExpect(jsonPath("$.timestamp").exists())
            .andExpect(jsonPath("$.status").value(400))
            .andExpect(jsonPath("$.error").value("Bad Request"))
            .andExpect(jsonPath("$.message").value("Page number cannot be negative"))
            .andExpect(jsonPath("$.path").value("/customers"));

        verifyNoInteractions(listCustomersUseCase);
    }

    @Test
    void shouldReturnBadRequestWhenPageSizeIsInvalid() throws Exception {
        mockMvc
            .perform(get("/customers")
                .param("page", "0")
                .param("size", "101"))
            .andExpect(status().isBadRequest())
            .andExpect(content().contentTypeCompatibleWith(APPLICATION_JSON))
            .andExpect(jsonPath("$.timestamp").exists())
            .andExpect(jsonPath("$.status").value(400))
            .andExpect(jsonPath("$.error").value("Bad Request"))
            .andExpect(jsonPath("$.message").value("Page size must be between 1 and 100"))
            .andExpect(jsonPath("$.path").value("/customers"));

        verifyNoInteractions(listCustomersUseCase);
    }
}
