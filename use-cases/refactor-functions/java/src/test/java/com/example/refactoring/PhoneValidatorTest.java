package com.example.refactoring;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import static org.junit.jupiter.api.Assertions.*;

import java.util.*;

/**
 * Tests for the PhoneValidator class
 */
@DisplayName("Phone Validator Tests")
public class PhoneValidatorTest {
    private PhoneValidator phoneValidator;

    @BeforeEach
    public void setUp() {
        phoneValidator = new PhoneValidator();
    }

    @Test
    @DisplayName("Should validate correct phone format")
    public void testValidPhoneFormat() {
        Map<String, Object> record = new HashMap<>();
        record.put("phone", "555-123-4567");

        PhoneValidationResult result = phoneValidator.validate(
            record,
            new HashSet<>(),
            new ArrayList<>()
        );

        assertTrue(result.isValid());
        assertFalse(result.isDuplicate());
    }

    @Test
    @DisplayName("Should accept phone with international format")
    public void testValidInternationalPhoneFormat() {
        Map<String, Object> record = new HashMap<>();
        record.put("phone", "+1 555-123-4567");

        PhoneValidationResult result = phoneValidator.validate(
            record,
            new HashSet<>(),
            new ArrayList<>()
        );

        assertTrue(result.isValid());
    }

    @Test
    @DisplayName("Should reject invalid phone format")
    public void testInvalidPhoneFormat() {
        Map<String, Object> record = new HashMap<>();
        record.put("phone", "123"); // Too short

        PhoneValidationResult result = phoneValidator.validate(
            record,
            new HashSet<>(),
            new ArrayList<>()
        );

        assertFalse(result.isValid());
        assertTrue(result.getErrors().get(0).contains("Invalid phone number format"));
    }

    @Test
    @DisplayName("Should allow missing phone (optional field)")
    public void testMissingPhoneIsOptional() {
        Map<String, Object> record = new HashMap<>();

        PhoneValidationResult result = phoneValidator.validate(
            record,
            new HashSet<>(),
            new ArrayList<>()
        );

        assertTrue(result.isValid());
        assertTrue(result.getErrors().isEmpty());
    }

    @Test
    @DisplayName("Should detect duplicate phone in database")
    public void testDuplicatePhoneInDatabase() {
        Map<String, Object> record = new HashMap<>();
        record.put("phone", "5551234567");

        Set<String> existingPhones = new HashSet<>();
        existingPhones.add("5551234567");

        PhoneValidationResult result = phoneValidator.validate(
            record,
            existingPhones,
            new ArrayList<>()
        );

        assertTrue(result.isValid());
        assertTrue(result.isDuplicate());
    }

    @Test
    @DisplayName("Should normalize phone by removing non-digit characters")
    public void testPhoneNormalization() {
        String phone = "555-123-4567";
        String normalized = PhoneValidator.normalize(phone);
        assertEquals("5551234567", normalized);
    }

    @Test
    @DisplayName("Should preserve plus sign in normalization")
    public void testPhoneNormalizationWithPlus() {
        String phone = "+1-555-123-4567";
        String normalized = PhoneValidator.normalize(phone);
        assertEquals("+15551234567", normalized);
    }
}

