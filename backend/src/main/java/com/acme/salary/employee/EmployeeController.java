package com.acme.salary.employee;

import com.acme.salary.common.BadRequestException;
import com.acme.salary.common.PageResponse;
import jakarta.validation.Valid;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;

@RestController
@RequestMapping("/api/employees")
public class EmployeeController {

    static final int MAX_PAGE_SIZE = 100;

    private final EmployeeService service;

    public EmployeeController(EmployeeService service) {
        this.service = service;
    }

    @GetMapping
    public PageResponse<EmployeeResponse> search(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String country,
            @RequestParam(required = false) String department,
            @RequestParam(required = false) String jobTitle,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "25") int size,
            @RequestParam(defaultValue = "fullName") String sort,
            @RequestParam(defaultValue = "asc") String direction) {
        if (page < 0) {
            throw new BadRequestException("page must not be negative.");
        }
        if (size < 1 || size > MAX_PAGE_SIZE) {
            throw new BadRequestException("size must be between 1 and " + MAX_PAGE_SIZE + ".");
        }
        return service.search(new EmployeeFilter(search, country, department, jobTitle),
                page, size, EmployeeSortField.fromParam(sort), parseDirection(direction));
    }

    @GetMapping("/filter-options")
    public FilterOptions filterOptions() {
        return service.filterOptions();
    }

    @GetMapping("/{id}")
    public EmployeeResponse get(@PathVariable long id) {
        return service.get(id);
    }

    @PostMapping
    public ResponseEntity<EmployeeResponse> create(@Valid @RequestBody EmployeeRequest request) {
        EmployeeResponse created = service.create(request);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}").buildAndExpand(created.id()).toUri();
        return ResponseEntity.created(location).body(created);
    }

    @PutMapping("/{id}")
    public EmployeeResponse update(@PathVariable long id, @Valid @RequestBody EmployeeRequest request) {
        return service.update(id, request);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }

    private static Sort.Direction parseDirection(String direction) {
        return Sort.Direction.fromOptionalString(direction)
                .orElseThrow(() -> new BadRequestException("direction must be 'asc' or 'desc'."));
    }
}
