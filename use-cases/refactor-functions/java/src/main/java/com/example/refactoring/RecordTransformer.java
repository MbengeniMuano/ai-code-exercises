package com.example.refactoring;

import java.util.Map;

/**
 * Responsible for source-specific record transformation and normalization
 */
public class RecordTransformer {

    /**
     * Applies source-specific transformations to a record
     */
    public Map<String, Object> transform(Map<String, Object> record, String source) {
        switch (source.toLowerCase()) {
            case "csv":
                return transformCsvRecord(record);
            case "api":
                return transformApiRecord(record);
            case "manual":
                return transformManualRecord(record);
            default:
                return record;
        }
    }

    /**
     * Transforms CSV-specific record format
     */
    private Map<String, Object> transformCsvRecord(Map<String, Object> record) {
        // CSV-specific transformations
        // - Trim whitespace
        // - Handle quoted fields
        // - Convert empty strings to null
        Map<String, Object> transformed = new java.util.HashMap<>(record);

        transformed.forEach((key, value) -> {
            if (value instanceof String) {
                String strValue = (String) value;
                transformed.put(key, strValue.trim().isEmpty() ? null : strValue.trim());
            }
        });

        return transformed;
    }

    /**
     * Transforms API-specific record format
     */
    private Map<String, Object> transformApiRecord(Map<String, Object> record) {
        // API-specific transformations
        // - Handle nested objects
        // - Convert data types as needed
        return record;
    }

    /**
     * Transforms manually entered record format
     */
    private Map<String, Object> transformManualRecord(Map<String, Object> record) {
        // Manual entry transformations
        // - Capitalize names
        // - Standardize phone format
        Map<String, Object> transformed = new java.util.HashMap<>(record);

        // Capitalize first letter of names
        if (transformed.containsKey("firstName")) {
            transformed.put("firstName", capitalizeFirstLetter(transformed.get("firstName")));
        }
        if (transformed.containsKey("lastName")) {
            transformed.put("lastName", capitalizeFirstLetter(transformed.get("lastName")));
        }

        return transformed;
    }

    /**
     * Capitalizes the first letter of a string
     */
    private Object capitalizeFirstLetter(Object value) {
        if (value == null) {
            return value;
        }

        String str = value.toString().trim();
        if (str.isEmpty()) {
            return str;
        }

        return str.substring(0, 1).toUpperCase() +
               (str.length() > 1 ? str.substring(1) : "");
    }
}

