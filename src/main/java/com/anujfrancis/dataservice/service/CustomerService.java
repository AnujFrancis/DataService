package com.anujfrancis.dataservice.service;

import com.anujfrancis.dataservice.model.Customer;
import com.anujfrancis.dataservice.repository.CustomerRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class CustomerService {
    private final CustomerRepository customerRepository;

    public CustomerService(CustomerRepository customerRepository) {
        this.customerRepository = customerRepository;
    }

    public List<Customer> getAllCustomers() {
        return customerRepository.findAll();
    }

    public Optional<Customer> getCustomerById(String id) {
        return customerRepository.findById(id);
    }

    public Optional<Customer> getCustomerByAlias(String alias) {
        return customerRepository.findByAlias(alias);
    }

    public Optional<Customer> getCustomerByName(String name) {
        return customerRepository.findByName(name);
    }

    public Customer createCustomer(Customer customer) {
        return customerRepository.save(customer);
    }

    public Optional<Customer> updateCustomer(String id, Customer customerDetails) {
        Optional<Customer> customer = customerRepository.findById(id);
        if (customer.isPresent()) {
            Customer existingCustomer = customer.get();
            
            if (customerDetails.getName() != null) {
                existingCustomer.setName(customerDetails.getName());
            }
            
            if (customerDetails.getAlias() != null) {
                existingCustomer.setAlias(customerDetails.getAlias());
            }
            
            if (customerDetails.getDob() != null) {
                existingCustomer.setDob(customerDetails.getDob());
            }
            
            return Optional.of(customerRepository.save(existingCustomer));
        }
        return Optional.empty();
    }

    public boolean deleteCustomer(String id) {
        return customerRepository.deleteById(id);
    }
}
