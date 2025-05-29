package com.anujfrancis.dataservice.controller;

import com.anujfrancis.dataservice.model.Customer;
import com.anujfrancis.dataservice.service.CustomerService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@CrossOrigin
public class CustomerController {
    private final CustomerService customerService;

    public CustomerController(CustomerService customerService) {
        this.customerService = customerService;
    }

    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> healthCheck() {
        Map<String, String> response = new HashMap<>();
        response.put("status", "healthy");
        return ResponseEntity.ok(response);
    }

    @PostMapping("/customers")
    public ResponseEntity<?> createCustomer(@RequestBody Customer customer) {
        // Check if required fields are present
        if (customer.getName() == null || customer.getAlias() == null || customer.getDob() == null) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "Name, alias, and date of birth are required");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        }

        // Check if customer with alias already exists
        Optional<Customer> existingCustomer = customerService.getCustomerByAlias(customer.getAlias());
        if (existingCustomer.isPresent()) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "Customer with this alias already exists");
            return ResponseEntity.status(HttpStatus.CONFLICT).body(errorResponse);
        }

        Customer newCustomer = customerService.createCustomer(customer);
        return ResponseEntity.status(HttpStatus.CREATED).body(newCustomer);
    }

    @GetMapping("/customers")
    public ResponseEntity<List<Customer>> getAllCustomers() {
        List<Customer> customers = customerService.getAllCustomers();
        return ResponseEntity.ok(customers);
    }

    @GetMapping("/customers/search")
    public ResponseEntity<?> searchCustomer(
            @RequestParam(required = false) String id,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String alias) {

        System.out.println("Search request received with parameters: id=" + id + ", name=" + name + ", alias=" + alias);

        if (id == null && name == null && alias == null) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "At least one search parameter (id, name, or alias) is required");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        }

        Optional<Customer> customer = Optional.empty();

        if (id != null) {
            System.out.println("Searching by ID: " + id);
            customer = customerService.getCustomerById(id);
        } else if (alias != null) {
            System.out.println("Searching by alias: " + alias);
            customer = customerService.getCustomerByAlias(alias);
        } else if (name != null) {
            System.out.println("Searching by name: " + name);
            customer = customerService.getCustomerByName(name);
        }

        if (customer.isPresent()) {
            System.out.println("Customer found: " + customer.get().getId());
            return ResponseEntity.ok(customer.get());
        } else {
            System.out.println("Customer not found");
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "Customer not found");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
        }
    }

    @PutMapping("/customers/{id}")
    public ResponseEntity<?> updateCustomer(@PathVariable String id, @RequestBody Customer customerDetails) {
        if (customerDetails.getName() == null && customerDetails.getAlias() == null && customerDetails.getDob() == null) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "At least one field to update is required");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        }

        // Check if alias is being updated and is already taken
        if (customerDetails.getAlias() != null) {
            Optional<Customer> existingCustomer = customerService.getCustomerByAlias(customerDetails.getAlias());
            if (existingCustomer.isPresent() && !existingCustomer.get().getId().equals(id)) {
                Map<String, String> errorResponse = new HashMap<>();
                errorResponse.put("error", "Customer with this alias already exists");
                return ResponseEntity.status(HttpStatus.CONFLICT).body(errorResponse);
            }
        }

        Optional<Customer> updatedCustomer = customerService.updateCustomer(id, customerDetails);
        if (updatedCustomer.isPresent()) {
            return ResponseEntity.ok(updatedCustomer.get());
        } else {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "Customer not found");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
        }
    }

    @DeleteMapping("/customers/{id}")
    public ResponseEntity<?> deleteCustomer(@PathVariable String id) {
        boolean deleted = customerService.deleteCustomer(id);
        if (deleted) {
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Customer deleted successfully");
            return ResponseEntity.ok(response);
        } else {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "Customer not found");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
        }
    }
}
