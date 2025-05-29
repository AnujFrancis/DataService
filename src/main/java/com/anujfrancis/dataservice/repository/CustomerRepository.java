package com.anujfrancis.dataservice.repository;

import com.anujfrancis.dataservice.model.Customer;
import com.anujfrancis.dataservice.model.CustomerData;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Repository;

import javax.annotation.PostConstruct;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Repository
public class CustomerRepository {
    private static final String DATA_FILE = "data/customers.json";
    private final ObjectMapper objectMapper;
    private CustomerData customerData;

    public CustomerRepository(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @PostConstruct
    public void init() {
        try {
            Path dataDirectory = Paths.get("data");
            if (!Files.exists(dataDirectory)) {
                Files.createDirectories(dataDirectory);
            }

            File file = new File(DATA_FILE);
            if (file.exists()) {
                customerData = objectMapper.readValue(file, CustomerData.class);
            } else {
                customerData = new CustomerData();
                saveData();
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to initialize customer repository", e);
        }
    }

    public List<Customer> findAll() {
        return customerData.getCustomers();
    }

    public Optional<Customer> findById(String id) {
        return customerData.getCustomers().stream()
                .filter(customer -> customer.getId().equals(id))
                .findFirst();
    }

    public Optional<Customer> findByAlias(String alias) {
        return customerData.getCustomers().stream()
                .filter(customer -> customer.getAlias().equalsIgnoreCase(alias))
                .findFirst();
    }

    public Optional<Customer> findByName(String name) {
        return customerData.getCustomers().stream()
                .filter(customer -> customer.getName().equalsIgnoreCase(name))
                .findFirst();
    }

    public Customer save(Customer customer) {
        if (customer.getId() == null) {
            customer.setId(String.valueOf(System.currentTimeMillis()));
            customer.setCreatedAt(LocalDateTime.now());
            customerData.getCustomers().add(customer);
        } else {
            Optional<Customer> existingCustomer = findById(customer.getId());
            if (existingCustomer.isPresent()) {
                Customer existing = existingCustomer.get();
                existing.setName(customer.getName());
                existing.setAlias(customer.getAlias());
                existing.setDob(customer.getDob());
                existing.setUpdatedAt(LocalDateTime.now());
            } else {
                customer.setCreatedAt(LocalDateTime.now());
                customerData.getCustomers().add(customer);
            }
        }
        saveData();
        return customer;
    }

    public boolean deleteById(String id) {
        boolean removed = customerData.getCustomers().removeIf(customer -> customer.getId().equals(id));
        if (removed) {
            saveData();
        }
        return removed;
    }

    private void saveData() {
        try {
            objectMapper.writeValue(new File(DATA_FILE), customerData);
        } catch (IOException e) {
            throw new RuntimeException("Failed to save customer data", e);
        }
    }
}
