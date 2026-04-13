package com.example.refactoring;

import java.util.*;

/**
 * Responsible for validating email addresses and checking for duplicates
 */
public class EmailValidator {
    private static final String EMAIL_REGEX = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,6}$";

    /**
     * Validates email in a record
     */
    public EmailValidationResult validate(Map<String, Object> record,
                                         Set<String> existingEmails,
                                         List<Map<String, Object>> previousValidRecords) {
        List<String> errors = new ArrayList<>();
        boolean isDuplicate = false;

        // Check if email exists
        if (!record.containsKey("email") || isEmpty(record.get("email"))) {
            errors.add("Missing required field: email");
            return new EmailValidationResult(false, false, errors);
        }

        String email = record.get("email").toString().trim().toLowerCase();
        record.put("email", email);

        // Validate email format
        if (!isValidFormat(email)) {
            errors.add("Invalid email format: " + email);
            return new EmailValidationResult(false, false, errors);
        }

        // Check for duplicates
        if (isDuplicateEmail(email, existingEmails, previousValidRecords)) {
            isDuplicate = true;
        }

        boolean isValid = errors.isEmpty();
        return new EmailValidationResult(isValid, isDuplicate, errors);
    }

    /**
     * Validates email format using regex
     */
    private boolean isValidFormat(String email) {
        return email.matches(EMAIL_REGEX);
    }

    /**
     * Checks if email is a duplicate
     */
    private boolean isDuplicateEmail(String email,
                                    Set<String> existingEmails,
                                    List<Map<String, Object>> previousValidRecords) {
        // Check against already processed valid records
        for (Map<String, Object> record : previousValidRecords) {
            if (email.equals(record.get("email").toString().toLowerCase())) {
                return true;
            }
        }

        // Check against existing database records
        return existingEmails.contains(email);
    }

    /**
     * Helper to check if value is empty
     */
    private boolean isEmpty(Object value) {
        return value == null || value.toString().trim().isEmpty();
    }
}

