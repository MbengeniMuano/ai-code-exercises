package com.example.refactoring;

import java.util.List;

/**
 * Result of email validation for a record
 */
public class EmailValidationResult {
    private final boolean valid;
    private final boolean duplicate;
    private final List<String> errors;

    public EmailValidationResult(boolean valid, boolean duplicate, List<String> errors) {
        this.valid = valid;
        this.duplicate = duplicate;
        this.errors = errors;
    }

    public boolean isValid() {
        return valid;
    }

    public boolean isDuplicate() {
        return duplicate;
    }

    public List<String> getErrors() {
        return errors;
    }
}

