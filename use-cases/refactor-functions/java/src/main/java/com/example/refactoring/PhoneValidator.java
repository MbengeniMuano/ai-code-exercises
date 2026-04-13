package com.example.refactoring;

import java.util.*;

/**
 * Responsible for validating phone numbers and checking for duplicates
 */
public class PhoneValidator {
    private static final String PHONE_REGEX = "^\\+?[0-9]{10,15}$";

    /**
     * Validates phone number in a record
     */
    public PhoneValidationResult validate(Map<String, Object> record,
                                         Set<String> existingPhones,
                                         List<Map<String, Object>> previousValidRecords) {
        List<String> errors = new ArrayList<>();
        boolean isDuplicate = false;

        // Phone is optional, only validate if present
        if (!record.containsKey("phone") || isEmpty(record.get("phone"))) {
            return new PhoneValidationResult(true, false, errors);
        }

        String phone = record.get("phone").toString().trim();
        String normalizedPhone = normalize(phone);

        // Validate phone format
        if (!isValidFormat(normalizedPhone)) {
            errors.add("Invalid phone number format: " + phone);
            return new PhoneValidationResult(false, false, errors);
        }

        // Format phone consistently
        record.put("phone", format(normalizedPhone));

        // Check for duplicates
        if (isDuplicatePhone(normalizedPhone, existingPhones, previousValidRecords)) {
            isDuplicate = true;
        }

        boolean isValid = errors.isEmpty();
        return new PhoneValidationResult(isValid, isDuplicate, errors);
    }

    /**
     * Normalizes phone number by removing non-digit characters
     */
    public static String normalize(String phone) {
        return phone.replaceAll("[^0-9+]", "");
    }

    /**
     * Validates phone format using regex
     */
    private boolean isValidFormat(String phone) {
        return phone.matches(PHONE_REGEX);
    }

    /**
     * Formats phone number consistently
     */
    private String format(String normalizedPhone) {
        // Simple formatting - can be customized based on requirements
        return normalizedPhone;
    }

    /**
     * Checks if phone is a duplicate
     */
    private boolean isDuplicatePhone(String normalizedPhone,
                                     Set<String> existingPhones,
                                     List<Map<String, Object>> previousValidRecords) {
        // Check against already processed valid records
        for (Map<String, Object> record : previousValidRecords) {
            if (record.containsKey("phone")) {
                String recordPhone = normalize(record.get("phone").toString());
                if (normalizedPhone.equals(recordPhone)) {
                    return true;
                }
            }
        }

        // Check against existing database records
        return existingPhones.contains(normalizedPhone);
    }

    /**
     * Helper to check if value is empty
     */
    private boolean isEmpty(Object value) {
        return value == null || value.toString().trim().isEmpty();
    }
}

