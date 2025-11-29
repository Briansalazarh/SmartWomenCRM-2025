package com.smartwomen.repository;

import com.azure.spring.data.cosmos.repository.CosmosRepository;
import com.smartwomen.models.Customer;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CustomerRepository extends CosmosRepository<Customer, String> {
    Optional<Customer> findByCustomerId(String customerId);
    Optional<Customer> findByEmail(String email);
    long countByCountry(String country);
}