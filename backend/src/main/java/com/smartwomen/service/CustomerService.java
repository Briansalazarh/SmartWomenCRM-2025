package com.smartwomen.service;

import com.smartwomen.models.Customer;
import com.smartwomen.repository.CustomerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Service
public class CustomerService {

    @Autowired
    private CustomerRepository customerRepository;

    public Customer saveCustomer(Customer customer) {
        if (customer.getCustomerId() == null || customer.getCustomerId().isBlank()) {
            customer.setCustomerId(UUID.randomUUID().toString());
        }
        return customerRepository.save(customer);
    }

    public Optional<Customer> getCustomerById(String customerId) {
        return customerRepository.findByCustomerId(customerId);
    }

    public List<Customer> getAllCustomers() {
        return StreamSupport.stream(customerRepository.findAll().spliterator(), false)
                           .collect(Collectors.toList());
    }

    public Optional<Customer> getCustomerByEmail(String email) {
        return customerRepository.findByEmail(email);
    }

    public Customer updateCustomerInteraction(String customerId, String message, Map<String, Object> agentResults) {
        return customerRepository.findByCustomerId(customerId)
                .map(customer -> {
                    customer.setLastMessage(message);
                    customer.setLastAgentResults(agentResults);
                    customer.setLastInteraction(LocalDateTime.now());
                    return customerRepository.save(customer);
                })
                .orElse(null);
    }

    public long countCustomers() {
        return customerRepository.count();
    }

    public long countCustomersByCountry(String country) {
        return customerRepository.countByCountry(country);
    }
}