package com.smartwomen.api;

import com.smartwomen.models.Customer;
import com.smartwomen.service.CustomerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/customers")
@CrossOrigin(origins = "http://localhost:3000")
public class CustomerController {
    
    @Autowired
    private CustomerService customerService;
    
    @GetMapping
    public ResponseEntity<List<Customer>> getAllCustomers() {
        List<Customer> customers = customerService.getAllCustomers();
        return ResponseEntity.ok(customers);
    }
    
    @GetMapping("/{customerId}")
    public ResponseEntity<Customer> getCustomerById(@PathVariable String customerId) {
        return customerService.getCustomerById(customerId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    
    @PostMapping
    public ResponseEntity<Customer> createCustomer(@RequestBody Customer customer) {
        Customer savedCustomer = customerService.saveCustomer(customer);
        return ResponseEntity.status(201).body(savedCustomer);
    }
    
    @PutMapping("/{customerId}")
    public ResponseEntity<Customer> updateCustomer(@PathVariable String customerId, @RequestBody Customer customer) {
        customer.setCustomerId(customerId);
        Customer updatedCustomer = customerService.saveCustomer(customer);
        return ResponseEntity.ok(updatedCustomer);
    }
    
    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getStats() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalCustomers", customerService.countCustomers());
        stats.put("customersMexico", customerService.countCustomersByCountry("Mexico"));
        stats.put("customersColombia", customerService.countCustomersByCountry("Colombia"));
        stats.put("customersArgentina", customerService.countCustomersByCountry("Argentina"));
        return ResponseEntity.ok(stats);
    }
}
