package com.example.refactoring;

import java.util.List;

/**
 * Repository interface for Customer data access
 */
public interface CustomerRepository {
    /**
     * Find all customers
     */
    List<Customer> findAll();

    /**
     * Save a single customer
     */
    void save(Customer customer);

    /**
     * Save multiple customers
     */
    void saveAll(List<Customer> customers);

    /**
     * Find customer by email
     */
    Customer findByEmail(String email);

    /**
     * Check if customer with email exists
     */
    boolean existsByEmail(String email);

    /**
     * Check if customer with phone exists
     */
    boolean existsByPhoneNumber(String phoneNumber);
}

