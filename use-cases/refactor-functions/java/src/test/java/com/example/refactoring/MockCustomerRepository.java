package com.example.refactoring;

import java.util.*;

/**
 * In-memory mock implementation of CustomerRepository for testing
 */
public class MockCustomerRepository implements CustomerRepository {
    private final List<Customer> customers = new ArrayList<>();

    @Override
    public List<Customer> findAll() {
        return new ArrayList<>(customers);
    }

    @Override
    public void save(Customer customer) {
        if (customer.getId() == null) {
            customer.setId(UUID.randomUUID().toString());
        }
        customers.add(customer);
    }

    @Override
    public void saveAll(List<Customer> customersToSave) {
        for (Customer customer : customersToSave) {
            save(customer);
        }
    }

    @Override
    public Customer findByEmail(String email) {
        return customers.stream()
            .filter(c -> c.getEmail().equals(email))
            .findFirst()
            .orElse(null);
    }

    @Override
    public boolean existsByEmail(String email) {
        return customers.stream()
            .anyMatch(c -> c.getEmail().equals(email));
    }

    @Override
    public boolean existsByPhoneNumber(String phoneNumber) {
        return customers.stream()
            .anyMatch(c -> c.getPhoneNumber().equals(phoneNumber));
    }

    public void clear() {
        customers.clear();
    }

    public int size() {
        return customers.size();
    }
}

