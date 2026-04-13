package com.example.refactoring;

import java.util.List;

/**
 * Result of validating a single record
 */
public class RecordValidationResult {
    private final boolean valid;
    private final boolean duplicate;
    private final List<String> errors;

    public RecordValidationResult(boolean valid, boolean duplicate, List<String> errors) {
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

