package com.acme.salary.employee;

import com.acme.salary.common.ConflictException;
import com.acme.salary.common.NotFoundException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** HTTP contract of the API: status codes, validation and error shape. The service is mocked. */
@WebMvcTest(EmployeeController.class)
class EmployeeControllerTest {

    private static final String VALID_BODY = """
            {
              "employeeCode": "EMP-00042",
              "fullName": "Asha Verma",
              "email": "asha@acme.com",
              "jobTitle": "Software Engineer",
              "department": "Engineering",
              "countryCode": "IN",
              "currencyCode": "INR",
              "annualSalary": 1500000,
              "hireDate": "2022-04-01"
            }
            """;

    @Autowired
    private MockMvc mvc;

    @MockitoBean
    private EmployeeService service;

    @Test
    void createReturns201WithLocationHeader() throws Exception {
        when(service.create(any())).thenReturn(response(42L));

        mvc.perform(post("/api/employees").contentType(MediaType.APPLICATION_JSON).content(VALID_BODY))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "http://localhost/api/employees/42"))
                .andExpect(jsonPath("$.annualSalaryUsd").value(18000.00));
    }

    @Test
    void createReturns400WithFieldErrorsForInvalidInput() throws Exception {
        String invalid = """
                { "employeeCode": "42", "fullName": "", "email": "not-an-email",
                  "jobTitle": "Engineer", "department": "Engineering",
                  "countryCode": "IND", "currencyCode": "INR",
                  "annualSalary": -5, "hireDate": "2022-04-01" }
                """;

        mvc.perform(post("/api/employees").contentType(MediaType.APPLICATION_JSON).content(invalid))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.employeeCode").exists())
                .andExpect(jsonPath("$.errors.fullName").exists())
                .andExpect(jsonPath("$.errors.email").exists())
                .andExpect(jsonPath("$.errors.countryCode").exists())
                .andExpect(jsonPath("$.errors.annualSalary").exists());
        verifyNoInteractions(service);
    }

    @Test
    void createReturns409OnConflict() throws Exception {
        when(service.create(any())).thenThrow(new ConflictException("Email asha@acme.com is already in use."));

        mvc.perform(post("/api/employees").contentType(MediaType.APPLICATION_JSON).content(VALID_BODY))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.detail").value("Email asha@acme.com is already in use."));
    }

    @Test
    void getReturns404ForUnknownEmployee() throws Exception {
        when(service.get(99L)).thenThrow(new NotFoundException("Employee 99 not found."));

        mvc.perform(get("/api/employees/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    void searchRejectsUnknownSortField() throws Exception {
        mvc.perform(get("/api/employees").param("sort", "password"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detail").value(org.hamcrest.Matchers.containsString("Cannot sort by")));
    }

    @Test
    void searchRejectsPageSizeAboveLimit() throws Exception {
        mvc.perform(get("/api/employees").param("size", "500"))
                .andExpect(status().isBadRequest());
    }

    private static EmployeeResponse response(long id) {
        return new EmployeeResponse(id, "EMP-00042", "Asha Verma", "asha@acme.com", "Software Engineer",
                "Engineering", "IN", "INR", new BigDecimal("1500000.00"), new BigDecimal("18000.00"),
                LocalDate.of(2022, 4, 1), 0L);
    }
}
