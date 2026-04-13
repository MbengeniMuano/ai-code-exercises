package com.example.refactoring;

import java.util.*;

/**
 * Encapsulates the overall result of processing all records
 */
public class ProcessingResult {
    private List<Map<String, Object>> validRecords = new ArrayList<>();
    private List<Map<String, Object>> invalidRecords = new ArrayList<>();
    private List<Map<String, Object>> duplicateRecords = new ArrayList<>();
    private Map<String, Integer> errorCounts = new HashMap<>();
    private int totalProcessed = 0;
    private int totalSkipped = 0;

    public void addValidRecord(Map<String, Object> record) {
        validRecords.add(record);
    }

    public void addInvalidRecord(Map<String, Object> record, List<String> errors) {
        record.put("errors", errors);
        invalidRecords.add(record);

        // Track error types
        for (String error : errors) {
            String errorType = error.split(":")[0].trim();
            errorCounts.put(errorType, errorCounts.getOrDefault(errorType, 0) + 1);
        }
    }

    public void addDuplicateRecord(Map<String, Object> record) {
        duplicateRecords.add(record);
    }

    public List<Map<String, Object>> getValidRecords() {
        return validRecords;
    }

    public List<Map<String, Object>> getInvalidRecords() {
        return invalidRecords;
    }

    public List<Map<String, Object>> getDuplicateRecords() {
        return duplicateRecords;
    }

    public Map<String, Integer> getErrorCounts() {
        return errorCounts;
    }

    public int getTotalSuccess() {
        return validRecords.size();
    }

    public int getTotalErrors() {
        return invalidRecords.size();
    }

    public int getTotalProcessed() {
        return totalProcessed;
    }

    public void setTotalProcessed(int totalProcessed) {
        this.totalProcessed = totalProcessed;
    }

    public int getTotalSkipped() {
        return totalSkipped;
    }

    public void setTotalSkipped(int totalSkipped) {
        this.totalSkipped = totalSkipped;
    }
}

