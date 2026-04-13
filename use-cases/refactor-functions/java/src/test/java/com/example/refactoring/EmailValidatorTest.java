package com.example.refactoring;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.*;

/**
 * Tests for the EmailValidator class
 * Demonstrates how decomposed functions are much easier to test
 */
@DisplayName("Email Validator Tests")
public class EmailValidatorTest {
    private EmailValidator emailValidator;

    @BeforeEach
    public void setUp() {
        emailValidator = new EmailValidator();
    }

    @Test
    @DisplayName("Should validate correct email format")
    public void testValidEmailFormat() {
        Map<String, Object> record = new HashMap<>();
        record.put("email", "user@example.com");

        EmailValidationResult result = emailValidator.validate(
            record,
            new HashSet<>(),
            new ArrayList<>()
        );

        assertTrue(result.isValid());
        assertFalse(result.isDuplicate());
        assertTrue(result.getErrors().isEmpty());
    }

    @Test
    @DisplayName("Should reject invalid email format")
    public void testInvalidEmailFormat() {
        Map<String, Object> record = new HashMap<>();
        record.put("email", "invalid-email");

        EmailValidationResult result = emailValidator.validate(
            record,
            new HashSet<>(),
            new ArrayList<>()
        );

        assertFalse(result.isValid());
        assertFalse(result.getErrors().isEmpty());
        assertTrue(result.getErrors().get(0).contains("Invalid email format"));
    }

    @Test
    @DisplayName("Should detect missing email")
    public void testMissingEmail() {
        Map<String, Object> record = new HashMap<>();

        EmailValidationResult result = emailValidator.validate(
            record,
            new HashSet<>(),
            new ArrayList<>()
        );

        assertFalse(result.isValid());
        assertTrue(result.getErrors().get(0).contains("Missing required field: email"));
    }

    @Test
    @DisplayName("Should detect duplicate email in existing emails")
    public void testDuplicateEmailInDatabase() {
        Map<String, Object> record = new HashMap<>();
        record.put("email", "user@example.com");

        Set<String> existingEmails = new HashSet<>();
        existingEmails.add("user@example.com");

        EmailValidationResult result = emailValidator.validate(
            record,
            existingEmails,
            new ArrayList<>()
        );

        assertTrue(result.isValid()); // Valid format
        assertTrue(result.isDuplicate()); // But is duplicate
    }

    @Test
    @DisplayName("Should detect duplicate email in previous records")
    public void testDuplicateEmailInPreviousRecords() {
        Map<String, Object> record = new HashMap<>();
        record.put("email", "user@example.com");

        List<Map<String, Object>> previousRecords = new ArrayList<>();
        Map<String, Object> previousRecord = new HashMap<>();
        previousRecord.put("email", "user@example.com");
        previousRecords.add(previousRecord);

        EmailValidationResult result = emailValidator.validate(
            record,
            new HashSet<>(),
            previousRecords
        );

        assertTrue(result.isValid()); // Valid format
        assertTrue(result.isDuplicate()); // But is duplicate
    }

    @Test
    @DisplayName("Should normalize email to lowercase")
    public void testEmailNormalization() {
        Map<String, Object> record = new HashMap<>();
        record.put("email", "User@Example.COM");

        EmailValidationResult result = emailValidator.validate(
            record,
            new HashSet<>(),
            new ArrayList<>()
        );

        assertTrue(result.isValid());
        assertEquals("user@example.com", record.get("email"));
    }

    @Test
    @DisplayName("Should trim whitespace from email")
    public void testEmailTrimming() {
        Map<String, Object> record = new HashMap<>();
        record.put("email", "  user@example.com  ");

        EmailValidationResult result = emailValidator.validate(
            record,
            new HashSet<>(),
            new ArrayList<>()
        );

        assertTrue(result.isValid());
        assertEquals("user@example.com", record.get("email"));
    }
}

